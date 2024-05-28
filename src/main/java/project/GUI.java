package project;

import org.lwjgl.stb.*;
import org.lwjgl.nuklear.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryStack.*;

public class GUI {
  private long window;
  private final ByteBuffer ttf;
  private NkContext ctx = NkContext.create();
  private NkUserFont default_font = NkUserFont.create();
  private NkBuffer cmds = NkBuffer.create();
  private NkDrawNullTexture null_texture = NkDrawNullTexture.create();
  private static final int BUFFER_INITIAL_SIZE = 4 * 1024;

  private static final int MAX_VERTEX_BUFFER = 512 * 1024;
  private static final int MAX_ELEMENT_BUFFER = 128 * 1024;

  private static final NkAllocator ALLOCATOR;

  private static final NkDrawVertexLayoutElement.Buffer VERTEX_LAYOUT;
  private int vbo, vao, ebo;
  private int prog;
  private int vert_shdr;
  private int frag_shdr;
  private int uniform_tex;
  private int uniform_proj;

  static {
    ALLOCATOR = NkAllocator.create()
        .alloc((handle, old, size) -> nmemAllocChecked(size))
        .mfree((handle, ptr) -> nmemFree(ptr));

    VERTEX_LAYOUT = NkDrawVertexLayoutElement.create(4)
        .position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
        .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
        .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
        .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
        .flip();
  }

  public GUI(long window) {
    this.window = window;
    setupWindow();

    int BITMAP_W = 1024;
    int BITMAP_H = 1024;

    int FONT_HEIGHT = 18;
    int fontTexID = glGenTextures();

    STBTTFontinfo fontInfo = STBTTFontinfo.create();
    STBTTPackedchar.Buffer cdata = STBTTPackedchar.create(95);

    float scale;
    float descent;

    try (MemoryStack stack = MemoryStack.stackPush()) {
      stbtt_InitFont(fontInfo, ttf);
      scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);

      IntBuffer d = stack.mallocInt(1);
      stbtt_GetFontVMetrics(fontInfo, null, d, null);
      descent = d.get(0) * scale;

      ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);

      STBTTPackContext pc = STBTTPackContext.mallocStack(stack);
      stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
      stbtt_PackSetOversampling(pc, 4, 4);
      stbtt_PackFontRange(pc, ttf, 0, FONT_HEIGHT, 32, cdata);
      stbtt_PackEnd(pc);

      // Convert R8 to RGBA8
      ByteBuffer texture = memAlloc(BITMAP_W * BITMAP_H * 4);
      for (int i = 0; i < bitmap.capacity(); i++) {
        texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
      }
      texture.flip();

      glBindTexture(GL_TEXTURE_2D, fontTexID);
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

      memFree(texture);
      memFree(bitmap);
    }

    default_font
        .width((handle, h, text, len) -> {
          float text_width = 0;
          try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer unicode = stack.mallocInt(1);

            int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
            int text_len = glyph_len;

            if (glyph_len == 0) {
              return 0;
            }

            IntBuffer advance = stack.mallocInt(1);
            while (text_len <= len && glyph_len != 0) {
              if (unicode.get(0) == NK_UTF_INVALID) {
                break;
              }

              /* query currently drawn glyph information */
              stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
              text_width += advance.get(0) * scale;

              /* offset next glyph */
              glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
              text_len += glyph_len;
            }
          }
          return text_width;
        })
        .height(FONT_HEIGHT)
        .query((handle, font_height, glyph, codepoint, next_codepoint) -> {
          try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);

            STBTTAlignedQuad q = STBTTAlignedQuad.mallocStack(stack);
            IntBuffer advance = stack.mallocInt(1);

            stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
            stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);

            NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);

            ufg.width(q.x1() - q.x0());
            ufg.height(q.y1() - q.y0());
            ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
            ufg.xadvance(advance.get(0) * scale);
            ufg.uv(0).set(q.s0(), q.t0());
            ufg.uv(1).set(q.s1(), q.t1());
          }
        })
        .texture(it -> it
            .id(fontTexID));

    nk_style_set_font(ctx, default_font);
  }

  public void scrollCallback(long window, float xoffset, float yoffset) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      NkVec2 scroll = NkVec2.mallocStack(stack)
          .x((float) xoffset)
          .y((float) yoffset);
      nk_input_scroll(ctx, scroll);
    }
  };

  public void setCharCallback(long window, int codepoint) {
    nk_input_unicode(ctx, codepoint);
  };

  public void setKeyCallback(long window, int key, int scancode, int action, int mods) {
    boolean press = action == GLFW_PRESS;
    switch (key) {
      case GLFW_KEY_ESCAPE:
        glfwSetWindowShouldClose(window, true);
        break;
      case GLFW_KEY_DELETE:
        nk_input_key(ctx, NK_KEY_DEL, press);
        break;
      case GLFW_KEY_ENTER:
        nk_input_key(ctx, NK_KEY_ENTER, press);
        break;
      case GLFW_KEY_TAB:
        nk_input_key(ctx, NK_KEY_TAB, press);
        break;
      case GLFW_KEY_BACKSPACE:
        nk_input_key(ctx, NK_KEY_BACKSPACE, press);
        break;
      case GLFW_KEY_UP:
        nk_input_key(ctx, NK_KEY_UP, press);
        break;
      case GLFW_KEY_DOWN:
        nk_input_key(ctx, NK_KEY_DOWN, press);
        break;
      case GLFW_KEY_HOME:
        nk_input_key(ctx, NK_KEY_TEXT_START, press);
        nk_input_key(ctx, NK_KEY_SCROLL_START, press);
        break;
      case GLFW_KEY_END:
        nk_input_key(ctx, NK_KEY_TEXT_END, press);
        nk_input_key(ctx, NK_KEY_SCROLL_END, press);
        break;
      case GLFW_KEY_PAGE_DOWN:
        nk_input_key(ctx, NK_KEY_SCROLL_DOWN, press);
        break;
      case GLFW_KEY_PAGE_UP:
        nk_input_key(ctx, NK_KEY_SCROLL_UP, press);
        break;
      case GLFW_KEY_LEFT_SHIFT:
      case GLFW_KEY_RIGHT_SHIFT:
        nk_input_key(ctx, NK_KEY_SHIFT, press);
        break;
      case GLFW_KEY_LEFT_CONTROL:
      case GLFW_KEY_RIGHT_CONTROL:
        if (press) {
          nk_input_key(ctx, NK_KEY_COPY, glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_PASTE, glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_CUT, glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_TEXT_UNDO, glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_TEXT_REDO, glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_TEXT_WORD_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_TEXT_WORD_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_TEXT_LINE_START, glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_TEXT_LINE_END, glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS);
        } else {
          nk_input_key(ctx, NK_KEY_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
          nk_input_key(ctx, NK_KEY_COPY, false);
          nk_input_key(ctx, NK_KEY_PASTE, false);
          nk_input_key(ctx, NK_KEY_CUT, false);
          nk_input_key(ctx, NK_KEY_SHIFT, false);
        }
        break;
    }
  }

  public void setCursorPosCallback(long window, float xpos, float ypos) {
    nk_input_motion(ctx, (int) xpos, (int) ypos);
  }

  public void setMouseButtonCallback(long window, int button, int action, int mods) {
    try (MemoryStack stack = MemoryStack.stackPush()) {
      DoubleBuffer cx = stack.mallocDouble(1);
      DoubleBuffer cy = stack.mallocDouble(1);

      glfwGetCursorPos(window, cx, cy);

      int x = (int) cx.get(0);
      int y = (int) cy.get(0);

      int nkButton;
      switch (button) {
        case GLFW_MOUSE_BUTTON_RIGHT:
          nkButton = NK_BUTTON_RIGHT;
          break;
        case GLFW_MOUSE_BUTTON_MIDDLE:
          nkButton = NK_BUTTON_MIDDLE;
          break;
        default:
          nkButton = NK_BUTTON_LEFT;
      }
      nk_input_button(ctx, nkButton, x, y, action == GLFW_PRESS);
    }
  }

  private NkContext setupWindow() {
        nk_init(ctx, ALLOCATOR, null);
        ctx.clip()
            .copy((handle, text, len) -> {
                if (len == 0) {
                    return;
                }

                try (MemoryStack stack = stackPush()) {
                    ByteBuffer str = stack.malloc(len + 1);
                    memCopy(text, memAddress(str), len);
                    str.put(len, (byte)0);

                    glfwSetClipboardString(win, str);
                }
            })
            .paste((handle, edit) -> {
                long text = nglfwGetClipboardString(win);
                if (text != NULL) {
                    nnk_textedit_paste(edit, text, nnk_strlen(text));
                }
            });

        setupContext();
  }

  private void setupContext() {
    nk_buffer_init(cmds, ALLOCATOR, BUFFER_INITIAL_SIZE);
    Shader gui = new Shader("nuklear.vert", "nuklear.frag");

    uniform_tex = glGetUniformLocation(gui.getId(), "Texture");
    uniform_proj = glGetUniformLocation(gui.getId(), "ProjMtx");
    int attrib_pos = glGetAttribLocation(gui.getId(), "Position");
    int attrib_uv = glGetAttribLocation(gui.getId(), "TexCoord");
    int attrib_col = glGetAttribLocation(gui.getId(), "Color");

    {
      // buffer setup
      vbo = glGenBuffers();
      ebo = glGenBuffers();
      vao = glGenVertexArrays();

      glBindVertexArray(vao);
      glBindBuffer(GL_ARRAY_BUFFER, vbo);
      glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);

      glEnableVertexAttribArray(attrib_pos);
      glEnableVertexAttribArray(attrib_uv);
      glEnableVertexAttribArray(attrib_col);

      glVertexAttribPointer(attrib_pos, 2, GL_FLOAT, false, 20, 0);
      glVertexAttribPointer(attrib_uv, 2, GL_FLOAT, false, 20, 8);
      glVertexAttribPointer(attrib_col, 4, GL_UNSIGNED_BYTE, true, 20, 16);
    }

    {
      // null texture setup
      int nullTexID = glGenTextures();

      null_texture.texture().id(nullTexID);
      null_texture.uv().set(0.5f, 0.5f);

      glBindTexture(GL_TEXTURE_2D, nullTexID);
      try (MemoryStack stack = MemoryStack.stackPush()) {
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1, 1, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, stack.ints(0xFFFFFFFF));
      }
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    }

    glBindTexture(GL_TEXTURE_2D, 0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
  }
}
