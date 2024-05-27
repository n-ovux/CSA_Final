package project;

import org.lwjgl.system.*;

import java.io.*;
import java.nio.*;

import org.joml.*;

import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Shader {
  private int Id;

  // This one is for a pipeline shader
  public Shader(String vertexSource, String fragmentSource) {
    String vertexShaderSource = getSourceString("shaders/" + vertexSource);
    String fragmentShaderSource = getSourceString("shaders/" + fragmentSource);

    Id = glCreateProgram();
    if (Id == 0)
      Logger.error("failed to create shader program");

    int vertexShaderId = createShader(vertexShaderSource, GL_VERTEX_SHADER);
    int fragmentShaderId = createShader(fragmentShaderSource, GL_FRAGMENT_SHADER);
    glAttachShader(Id, vertexShaderId);
    glAttachShader(Id, fragmentShaderId);
    
    linkProgram();
  }
  
  // This one is for compute shaders
  public Shader(String computeSource) {
    String computeShaderSource = getSourceString("shaders/" + computeSource);

    Id = glCreateProgram();
    if (Id == 0)
      Logger.error("failed to create sahder program");

    int computeShaderId = createShader(computeShaderSource, GL_COMPUTE_SHADER);
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
    FloatBuffer matrixBuffer = MemoryUtil.memAllocFloat(4*4);
    matrix.get(matrixBuffer);
    glUniformMatrix4fv(getUniformLocation(name), false, matrixBuffer);
    memFree(matrixBuffer);
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
