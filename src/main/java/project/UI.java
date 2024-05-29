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
  private Camera camera;
  private Gui gui;
  private boolean mouseFree = true;

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

    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);

    GL.createCapabilities();

    gui = new Gui(window);

    // Callbacks
    glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
      glViewport(0, 0, width, height);
      this.width = width;
      this.height = height;
    });

    glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
      if (!gui.captureMouse() && !mouseFree) {
        camera.updateCamera((float) xpos, (float) ypos);
      }
    });

    glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
      if (!gui.captureMouse()) {
        if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
          glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
          mouseFree = false;
        }
      }
    });
  }

  private void processInput() {
    if (!gui.captureKeyboard()) {
      if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        mouseFree = true;
        camera.setFirstMouse(true);
      }
      if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
        camera.move(Camera.Direction.FORWARD, (float) deltaTime);
      }
      if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
        camera.move(Camera.Direction.BACKWARD, (float) deltaTime);
      }
      if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
        camera.move(Camera.Direction.LEFT, (float) deltaTime);
      }
      if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
        camera.move(Camera.Direction.RIGHT, (float) deltaTime);
      }
    }
  }

  private void loop() {
    glViewport(0, 0, width, height);

    camera = new Camera(0.0f, 0.0f, 3.0f, 5.0f);

    float quadVerts[] = new float[] {
        -1.0f, -1.0f, 0.0f,
        1.0f, -1.0f, 0.0f,
        1.0f, 1.0f, 0.0f,
        -1.0f, 1.0f, 0.0f,
    };
    FloatBuffer quadVertsBuffer = MemoryUtil.memAllocFloat(quadVerts.length);
    quadVertsBuffer.put(quadVerts).flip();

    VertexArray quadvao = new VertexArray();
    quadvao.bind();

    int quadvbo = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, quadvbo);
    glBufferData(GL_ARRAY_BUFFER, quadVertsBuffer, GL_STATIC_DRAW);
    memFree(quadVertsBuffer);

    quadvao.push(3, GL_FLOAT);
    quadvao.enable();
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    quadvao.unbind();

    Shader defaultShader = new Shader("default.vert", "default.frag");

    Shader textureShader = new Shader("texture.vert", "texture.frag");
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

    cubevao.push(3, GL_FLOAT);
    cubevao.push(2, GL_FLOAT);
    cubevao.enable();
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    cubevao.unbind();

    Shader tess = new Shader("default.vert", "default.frag", "tessellation.tesc", "tessellation.tese");

    double lastTime = glfwGetTime();
    int newFrames = 0;
    float fps = 0;
    float mspf = 0;
    while (!glfwWindowShouldClose(window)) {
      double currentTime = glfwGetTime();
      newFrames++;
      deltaTime = currentTime - lastTime;
      if (deltaTime <= 1.0) {
        fps = (float) (newFrames / deltaTime);
        mspf = (float) (deltaTime * 1000.0) / newFrames;
        newFrames = 0;
        lastTime = currentTime;
      }

      processInput();

      glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      if(gui.getWireframe()) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
      } else {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
      }

      gui.setStatusVariables(fps, mspf);
      gui.render();

      Matrix4f model = new Matrix4f();
      Matrix4f view = new Matrix4f();
      view.mul(camera.getLook());
      Matrix4f projection = new Matrix4f();
      projection.setPerspective((float) Math.toRadians(60.0), (float) width / height, 0.01f, 100.0f);

      glEnable(GL_DEPTH_TEST);
      textureShader.bind();
      textureShader.setMatrix4("projection", projection);
      textureShader.setMatrix4("model", model);
      textureShader.setMatrix4("view", view);
      container.bind();
      cubevao.bind();
      glDrawArrays(GL_TRIANGLES, 0, 36);
      container.unbind();
      textureShader.unbind();
      cubevao.unbind();

      model.scale(5.0f, 1.0f, 5.0f);
      model.rotate((float) Math.toRadians(90), 1.0f, 0.0f, 0.0f);
      model.translate(0.0f, 0.0f, 0.5f);
      tess.bind();
      tess.setFloat("subdivisions", gui.getSubdivisions());
      tess.setMatrix4("projection", projection);
      tess.setMatrix4("model", model);
      tess.setMatrix4("view", view);
      quadvao.bind();
      glPatchParameteri(GL_PATCH_VERTICES, 4);
      glDrawArrays(GL_PATCHES, 0, 4);
      quadvao.unbind();
      tess.unbind();

      glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
      gui.drawWindow();

      glfwSwapBuffers(window);
      glfwPollEvents();
    }
  }
}
