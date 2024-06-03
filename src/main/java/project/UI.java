package project;

import java.lang.Math;

import imgui.type.*;

import org.joml.*;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43.*; import static org.lwjgl.system.MemoryUtil.*;

public class UI {
  private long window;
  private int width = 1400;
  private int height = 900;
  private double deltaTime;
  private boolean mouseFree;

  private Camera camera;
  private Gui gui;

  public void run() {
    init();
    loop();
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
    glfwWindowHint(GLFW_SAMPLES, 4);

    window = glfwCreateWindow(width, height, "OpenGL Testing", NULL, NULL);
    if (window == NULL)
      Logger.error("failed to create window");

    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);

    GL.createCapabilities();

    gui = new Gui(window);
    camera = new Camera(0.0f, 5.0f, 3.0f, 60.0f);
    mouseFree = true;
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
    if (!gui.captureMouse()) {
      if (!mouseFree) {
        double[] xpos = new double[1];
        double[] ypos = new double[1];
        glfwGetCursorPos(window, xpos, ypos);
        camera.updateCamera((float) xpos[0], (float) ypos[0]);
      }
      if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        mouseFree = false;
      }
    }
  }

  private void loop() {
    glViewport(0, 0, width, height);

    float planeVertices[] = new float[] {
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
        1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
        -1.0f, 1.0f, 0.0f, 0.0f, 1.0f
    };

    VertexArrayBuffer planeVab = new VertexArrayBuffer();
    planeVab.bind();
    planeVab.addVerticesBuffer(planeVertices);
    planeVab.addVertexAttribute(3, GL_FLOAT);
    planeVab.addVertexAttribute(2, GL_FLOAT);
    planeVab.enableVertexAttributes();

    Shader shader = new Shader("texture.vert", "texture.frag", "tessellation.tesc", "tessellation.tese");
    Perlin noise = new Perlin(256);

    double lastTime = glfwGetTime();
    float secondTime = (float) glfwGetTime();
    int newFrames = 0;
    int fps = 0;
    float mspf = 0;
    while (!glfwWindowShouldClose(window)) {
      double currentTime = glfwGetTime();
      deltaTime = currentTime - lastTime;
      lastTime = currentTime;
      secondTime += deltaTime;
      newFrames++;
      if (secondTime >= 1.0) {
        fps = (int) (newFrames / secondTime);
        mspf = (float) (secondTime * 1000.0) / newFrames;
        newFrames = 0;
        secondTime = 0;
      }
      glClearColor(0.16f, 0.17f, 0.2f, 1.0f);
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      glEnable(GL_DEPTH_TEST);
      glEnable(GL_MULTISAMPLE);

      processInput();

      if (((ImBoolean) gui.getValue("wireframe")).get())
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
      else
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

      gui.setValue("fps", fps);
      gui.setValue("mspf", mspf);
      gui.render();

      Matrix4f model = new Matrix4f();
      model.scale(20.0f, 1.0f, 20.0f);
      model.rotate((float) Math.toRadians(90), 1.0f, 0.0f, 0.0f);
      model.translate(0.0f, 0.0f, 0.5f);
      Matrix4f view = new Matrix4f();
      view.mul(camera.getLook());
      Matrix4f projection = new Matrix4f();
      projection.setPerspective((float) Math.toRadians(camera.getFov()), (float) width / height, 0.01f, 100.0f);

      shader.bind();
      noise.generateTexture(128, ((float[]) gui.getValue("frequency"))[0]).bind();
      shader.setFloat("subdivisions", ((float[]) gui.getValue("subdivisions"))[0]);
      shader.setMatrix4("model", model);
      shader.setMatrix4("view", view);
      shader.setMatrix4("projection", projection);
      planeVab.bind();
      glPatchParameteri(GL_PATCH_VERTICES, 4);
      glDrawArrays(GL_PATCHES, 0, 4);

      glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
      gui.drawWindow();

      glfwSwapBuffers(window);
      glfwPollEvents();
    }
  }
}
