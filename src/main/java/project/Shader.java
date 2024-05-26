package project;

import java.io.*;
import static org.lwjgl.opengl.GL43.*;

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
