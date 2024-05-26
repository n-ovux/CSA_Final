package project;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.*;

public class UI {
  private long window;
  private int width = 1400;
  private int height = 900;

  public void run() {
    init();
    loop();

    glfwTerminate();
  }

  private void init() {
    GLFWErrorCallback.createPrint(System.err).set();

    if (!glfwInit())
      Logger.error("failed to initialize glfw");

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

    window = glfwCreateWindow(width, height, "OpenGL Testing", NULL, NULL);
    if (window == NULL)
      Logger.error("failed to create window");

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);

    GL.createCapabilities();

    // Callbacks
    glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
        glfwSetWindowShouldClose(window, true);
      } else if (key == GLFW_KEY_W && action == GLFW_PRESS) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
      } else if (key == GLFW_KEY_W && action == GLFW_RELEASE) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
      }
    });

    glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
      glViewport(0, 0, width, height);
      this.width = width;
      this.height = height;
    });
  }

  private void loop() {
    glViewport(0, 0, width, height);

    Shader defaultShader = new Shader("default.vert", "default.frag");

    float[] vertices = new float[] {
        // Vertices Color Texture Coords
        0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,
        -0.5f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
        0.5f, -0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f,
        -0.5f, -0.5f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f
    };
    FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
    verticesBuffer.put(vertices).flip();

    int[] indices = new int[] {
        0, 1, 2,
        1, 2, 3
    };
    IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indices.length);
    indicesBuffer.put(indices).flip();

    Texture container = new Texture("container.png");

    int vao = glGenVertexArrays();
    glBindVertexArray(vao);

    int vbo = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
    memFree(verticesBuffer);

    int ebo = glGenBuffers();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
    memFree(indicesBuffer);

    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, false, (2 + 3 + 2) * 4, 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 3, GL_FLOAT, false, (2 + 3 + 2) * 4, 2 * 4);
    glEnableVertexAttribArray(2);
    glVertexAttribPointer(2, 2, GL_FLOAT, false, (2 + 3 + 2) * 4, (2 + 3) * 4);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);

    float quadVerts[] = new float[] {
        -1.0f, 1.0f, 0.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 0.0f,
        1.0f, -1.0f, 1.0f, 0.0f,

        -1.0f, 1.0f, 0.0f, 1.0f,
        1.0f, -1.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 1.0f, 1.0f
    };
    FloatBuffer quadVertsBuffer = MemoryUtil.memAllocFloat(quadVerts.length);
    quadVertsBuffer.put(quadVerts).flip();

    int quadvao = glGenVertexArrays();
    glBindVertexArray(quadvao);

    int quadvbo = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, quadvbo);
    glBufferData(GL_ARRAY_BUFFER, quadVertsBuffer, GL_STATIC_DRAW);
    memFree(quadVertsBuffer);

    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 2, GL_FLOAT, false, (2 + 2) * 4, 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, false, (2 + 2) * 4, 2 * 4);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);

    Shader quad = new Shader("quad.vert", "quad.frag");

    Texture buffer = new Texture(width, height, GL_RGB, GL_RGB, GL_UNSIGNED_BYTE);

    int fbo = glGenFramebuffers();
    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, buffer.getId(), 0);

    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
      Logger.error("Failed to make complete framebuffer");
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    int TEXTURE_WIDTH = 512, TEXTURE_HEIGHT = 512;
    Texture compTexture = new Texture(TEXTURE_WIDTH, TEXTURE_HEIGHT, GL_RGBA32F, GL_RGBA, GL_FLOAT);
    glBindImageTexture(0, compTexture.getId(), 0, false, 0, GL_READ_ONLY, GL_RGBA32F);

    Shader compShader = new Shader("test.comp");
    compShader.bind();
    glDispatchCompute(TEXTURE_WIDTH, TEXTURE_HEIGHT, 1);
    glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);

    double lastTime = glfwGetTime();
    int newFrames = 0;
    while (!glfwWindowShouldClose(window)) {
      double currentTime = glfwGetTime();
      newFrames++;
      double difference = currentTime - lastTime;
      if (difference <= 1.0) {
        System.out.printf("\r%.4f fps | %.4f mspf", newFrames/difference, (difference*1000.0)/newFrames);
        newFrames = 0;
        lastTime += difference;
      }
      // glBindFramebuffer(GL_FRAMEBUFFER, fbo);
      // glClearColor(0.157f, 0.173f, 0.204f, 1.0f);
      // glClear(GL_COLOR_BUFFER_BIT);
      // defaultShader.bind();
      // container.bind();
      // glBindVertexArray(vao);
      // glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
      // glBindVertexArray(0);
      // container.unbind();
      // defaultShader.unbind();

      glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
      glClear(GL_COLOR_BUFFER_BIT);
      quad.bind();
      compTexture.bind();
      glBindVertexArray(quadvao);
      glDrawArrays(GL_TRIANGLES, 0, 6);
      compTexture.unbind();
      quad.unbind();

      glfwSwapBuffers(window);
      glfwPollEvents();
    }
  }
}
