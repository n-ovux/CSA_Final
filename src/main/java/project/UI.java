package project;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import org.joml.*;

import java.nio.*;
import java.lang.Math;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.*;

public class UI {
  private long window;
  private int width = 1400;
  private int height = 900;
  private double deltaTime;
  private float lastX;
  private float lastY;
  private float yaw;
  private float pitch;
  private boolean firstMouse = true;
  private Vector3f cameraPosition;
  private Vector3f cameraFront;
  private Vector3f cameraUp;

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

    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);

    GL.createCapabilities();

    // Callbacks
    glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
      glViewport(0, 0, width, height);
      this.width = width;
      this.height = height;
    });

    glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
      if (firstMouse) {
        lastX = (float) xpos;
        lastY = (float) ypos;
        firstMouse = false;
      }

      float xoffset = (float) xpos - lastX;
      float yoffset = lastY - (float) ypos;
      lastX = (float) xpos;
      lastY = (float) ypos;

      float sensitivity = 0.09f;
      xoffset *= sensitivity;
      yoffset *= sensitivity;

      yaw += xoffset;
      pitch += yoffset;

      if (pitch > 89.0f)
        pitch = 89.0f;
      if (pitch < -89.0f)
        pitch = -89.0f;

      Vector3f direction = new Vector3f();
      direction.setComponent(0, (float) (Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))));
      direction.setComponent(1, (float) (Math.sin(Math.toRadians(pitch))));
      direction.setComponent(2, (float) ((Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))));
      cameraFront = direction.normalize();
    });
  }

  private void processInput() {
    float cameraSpeed = 5.0f * (float) deltaTime;
    if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS)
      cameraSpeed *= 0.5;
    if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
      glfwSetWindowShouldClose(window, true);
    }
    if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) {
      glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
    } else if (glfwGetKey(window, GLFW_KEY_E) == GLFW_RELEASE) {
      glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }
    if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
      cameraPosition.add(new Vector3f(cameraFront).mul(cameraSpeed));
    }
    if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
      cameraPosition.sub(new Vector3f(cameraFront).mul(cameraSpeed));
    }
    if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
      cameraPosition.sub(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
    }
    if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
      cameraPosition.add(new Vector3f(cameraFront).cross(cameraUp).normalize().mul(cameraSpeed));
    }
  }

  private void loop() {
    glViewport(0, 0, width, height);
    glEnable(GL_DEPTH_TEST);

    float quadVerts[] = new float[] {
        -1.0f, -1.0f, -5.0f, 0.0f, 0.0f,
        -1.0f, 1.0f, -3.0f, 0.0f, 1.0f,
        1.0f, -1.0f, -5.0f, 1.0f, 0.0f,
        1.0f, 1.0f, -3.0f, 1.0f, 1.0f
    };
    FloatBuffer quadVertsBuffer = MemoryUtil.memAllocFloat(quadVerts.length);
    quadVertsBuffer.put(quadVerts).flip();

    int quadInds[] = new int[] {
        0, 1, 2,
        1, 2, 3
    };
    IntBuffer quadIndsBuffer = MemoryUtil.memAllocInt(quadInds.length);
    quadIndsBuffer.put(quadInds).flip();

    int quadvao = glGenVertexArrays();
    glBindVertexArray(quadvao);

    int quadvbo = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, quadvbo);
    glBufferData(GL_ARRAY_BUFFER, quadVertsBuffer, GL_STATIC_DRAW);
    memFree(quadVertsBuffer);

    int quadebo = glGenBuffers();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, quadebo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, quadIndsBuffer, GL_STATIC_DRAW);
    memFree(quadIndsBuffer);

    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, (3 + 2) * 4, 0);
    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 2, GL_FLOAT, false, (3 + 2) * 4, 3 * 4);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);

    Shader quadShader = new Shader("quad.vert", "quad.frag");
    Texture container = new Texture("container.png");
    float cubeVerts[] = new float[] {
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 0.0f,

        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 1.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,

        -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        -0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,

        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, -0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, -0.5f, 0.5f, 1.0f, 0.0f,
        -0.5f, -0.5f, 0.5f, 0.0f, 0.0f,
        -0.5f, -0.5f, -0.5f, 0.0f, 1.0f,

        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f,
        0.5f, 0.5f, -0.5f, 1.0f, 1.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        0.5f, 0.5f, 0.5f, 1.0f, 0.0f,
        -0.5f, 0.5f, 0.5f, 0.0f, 0.0f,
        -0.5f, 0.5f, -0.5f, 0.0f, 1.0f
    };
    FloatBuffer cubeVertsBuffer = MemoryUtil.memAllocFloat(cubeVerts.length);
    cubeVertsBuffer.put(cubeVerts).flip();

    VertexArray cubevao = new VertexArray();
    cubevao.bind();

    int cubevbo = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, cubevbo);
    glBufferData(GL_ARRAY_BUFFER, cubeVertsBuffer, GL_STATIC_DRAW);

    cubevao.push(3);
    cubevao.push(2);
    cubevao.enable();
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    cubevao.unbind();

    cameraPosition = new Vector3f(0.0f, 0.0f, 3.0f);
    cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
    cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

    double lastTime = glfwGetTime();
    int newFrames = 0;
    while (!glfwWindowShouldClose(window)) {
      double currentTime = glfwGetTime();
      newFrames++;
      deltaTime = currentTime - lastTime;
      if (deltaTime <= 1.0) {
        System.out.printf("\r%.4f fps | %.4f mspf", newFrames / deltaTime, (deltaTime * 1000.0) / newFrames);
        newFrames = 0;
        lastTime = currentTime;
      }

      processInput();

      glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      quadShader.bind();

      Matrix4f model = new Matrix4f();
      Matrix4f view = new Matrix4f();
      view.lookAt(cameraPosition, new Vector3f(cameraPosition).add(cameraFront), cameraUp);
      Matrix4f projection = new Matrix4f();
      projection.setPerspective((float) Math.toRadians(60.0), (float) width / height, 0.01f, 100.0f);

      quadShader.setMatrix4("projection", projection);
      quadShader.setMatrix4("model", model);
      quadShader.setMatrix4("view", view);

      container.bind();
      cubevao.bind();
      glDrawArrays(GL_TRIANGLES, 0, 36);
      container.unbind();
      quadShader.unbind();

      glfwSwapBuffers(window);
      glfwPollEvents();
    }
  }
}
