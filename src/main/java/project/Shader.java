package project;

import org.lwjgl.system.*;
import org.lwjgl.system.MemoryUtil.*;
import org.lwjgl.system.MemoryStack.*;

import java.io.*;
import java.nio.*;

import org.joml.*;

import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.system.MemoryStack.*;

public class Shader {
  private int Id;

  public Shader(String vertexSource, String fragmentSource) {
    Id = glCreateProgram();
    if (Id == 0)
      Logger.error("failed to create shader program");

    int vertexShaderId = createShader(getSourceString("shaders/" + vertexSource), GL_VERTEX_SHADER);
    int fragmentShaderId = createShader(getSourceString("shaders/" + fragmentSource), GL_FRAGMENT_SHADER);
    glAttachShader(Id, vertexShaderId);
    glAttachShader(Id, fragmentShaderId);

    linkProgram();
  }

  public Shader(String vertexSource, String fragmentSource, String tessellationControlSource,
      String tessellationEvalSource) {
    Id = glCreateProgram();
    if (Id == 0)
      Logger.error("failed to create shader program");

    int vertexShaderId = createShader(getSourceString("shaders/" + vertexSource), GL_VERTEX_SHADER);
    int fragmentShaderId = createShader(getSourceString("shaders/" + fragmentSource), GL_FRAGMENT_SHADER);
    int tessellationControlShaderId = createShader(getSourceString("shaders/" + tessellationControlSource),
        GL_TESS_CONTROL_SHADER);
    int tessellationEvalShaderId = createShader(getSourceString("shaders/" + tessellationEvalSource),
        GL_TESS_EVALUATION_SHADER);
    glAttachShader(Id, vertexShaderId);
    glAttachShader(Id, fragmentShaderId);
    glAttachShader(Id, tessellationControlShaderId);
    glAttachShader(Id, tessellationEvalShaderId);

    linkProgram();
  }

  public Shader(String computeSource) {
    Id = glCreateProgram();
    if (Id == 0)
      Logger.error("failed to create sahder program");

    int computeShaderId = createShader(getSourceString("shaders/" + computeSource), GL_COMPUTE_SHADER);
    glAttachShader(Id, computeShaderId);
    linkProgram();
  }

  public void bind() {
    glUseProgram(Id);
  }

  public void unbind() {
    glUseProgram(0);
  }

  public int getId() {
    return Id;
  }

  public void setInt(String name, int value) {
    glUniform1i(getUniformLocation(name), value);
  }

  public void setFloat(String name, float value) {
    glUniform1f(getUniformLocation(name), value);
  }

  public void setMatrix4(String name, Matrix4f matrix) {
    try (MemoryStack stack = stackPush()) {
      FloatBuffer matrixBuffer = stack.callocFloat(4 * 4);
      matrix.get(matrixBuffer);
      glUniformMatrix4fv(getUniformLocation(name), false, matrixBuffer);
    }
  }

  private int getUniformLocation(String name) {
    int location = glGetUniformLocation(Id, name);
    if (location < 0)
      Logger.error("failed to find uniform: " + name);
    return location;
  }

  private void linkProgram() {
    glLinkProgram(Id);
    if (glGetProgrami(Id, GL_LINK_STATUS) == 0)
      Logger.error("Failed to link shader: " + glGetProgramInfoLog(Id));

    glValidateProgram(Id);
    if (glGetProgrami(Id, GL_VALIDATE_STATUS) == 0)
      Logger.error("failed to validate program: " + glGetProgramInfoLog(Id));
  }

  private int createShader(String shaderSource, int shaderType) {
    int shaderId = glCreateShader(shaderType);
    if (shaderId == 0)
      Logger.error("failed to create shader: " + shaderType);

    glShaderSource(shaderId, shaderSource);
    glCompileShader(shaderId);

    if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0)
      Logger.error("failed to compile shader: " + shaderSource + "\n" + glGetShaderInfoLog(shaderId));

    return shaderId;
  }

  private String getSourceString(String name) {
    // tbh idk how these streams and readers work
    InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(name);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

    String line;
    String text = new String();
    try {
      while ((line = reader.readLine()) != null) {
        text += line + "\n";
      }
    } catch (IOException e) {
      Logger.error("failed to read shader file");
    }

    return text;
  }
}
