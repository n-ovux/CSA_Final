package project;

import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Texture {
  private int Id;
  private int width;
  private int height;

  public Texture(String path, int filter) {
    Id = glGenTextures();

    try (MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer w = stack.callocInt(1);
      IntBuffer h = stack.callocInt(1);
      IntBuffer channels = stack.mallocInt(1);

      ByteBuffer imageBuffer = stbi_load(path, w, h, channels, 4);
      if (imageBuffer == null)
        Logger.error("Failed to load image: " + path + ": " + stbi_failure_reason());

      width = w.get();
      height = h.get();

      glBindTexture(GL_TEXTURE_2D, Id);

      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);

      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
      glGenerateMipmap(GL_TEXTURE_2D);
      stbi_image_free(imageBuffer);
    }
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  public Texture(int width, int height, int internalFormat, int format, int type, int filter) {
    this.width = width;
    this.height = height;

    Id = glGenTextures();
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, Id);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);

    glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, NULL);

    glBindTexture(GL_TEXTURE_2D, 0);
  }

  public Texture(int width, int height, int format, int type, float[] pixels, int filter) {
    this.width = width;
    this.height = height;

    Id = glGenTextures();
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, Id);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filter);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filter);

    glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, type, pixels);

    glBindTexture(GL_TEXTURE_2D, 0);
  }

  public void bind() {
    glBindTexture(GL_TEXTURE_2D, Id);
  }

  public void unbind() {
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  public int getId() {
    return Id;
  }
}
