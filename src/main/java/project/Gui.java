package project;

import imgui.*;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.*;
import imgui.glfw.ImGuiImplGlfw;
import imgui.gl3.ImGuiImplGl3;

import static org.lwjgl.opengl.GL43.*;

public class Gui {
  private ImGuiImplGlfw imguiGlfw;
  private ImGuiImplGl3 imguiGl3;
  private ImGuiIO imguiIO;

  // Status Variables
  private float fps;
  private float mspf;
  private float[] subdivisions = {1.0f};
  private float[] frequency = {1.0f};

  // Config Variables
  private ImBoolean wireframe;

  public Gui(long window) {
    imguiGlfw = new ImGuiImplGlfw();
    imguiGl3 = new ImGuiImplGl3();

    ImGui.createContext();
    imguiIO = ImGui.getIO();
    imguiIO.setIniFilename(null);
    imguiIO.setLogFilename(null);
    imguiGlfw.init(window, true);
    imguiGl3.init();

    wireframe = new ImBoolean(false);
  }

  public boolean captureMouse() {
    return imguiIO.getWantCaptureMouse();
  }

  public boolean captureKeyboard() {
    return imguiIO.getWantCaptureKeyboard();
  }

  public void render() {
    imguiGlfw.newFrame();
    ImGui.newFrame();

    // ImGui.showDemoWindow();
  
    int flags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize;
    
    ImGui.setNextWindowPos(1.0f, 1.0f);
    ImGui.setNextWindowSize(300.0f, 200.0f);
    ImGui.begin("Information", flags);
    ImGui.text("fps: " + (int) fps);
    ImGui.text("mspf: " + mspf);
    ImGui.sliderFloat("subdivisions", subdivisions, 0.0f, glGetFloat(GL_MAX_TESS_GEN_LEVEL));
    ImGui.sliderFloat("frequency", frequency, 0.0f, 1.0f);
    ImGui.checkbox("Wireframe", wireframe);
    ImGui.end();
  }

  public void setStatusVariables(float fps, float mspf) {
    this.fps = fps;
    this.mspf = mspf;
  }

  public float getSubdivisions() {
    return this.subdivisions[0];
  }

  public float getFrequency() {
    return this.frequency[0];
  }
  
  public boolean getWireframe() {
    return wireframe.get();
  }

  public void drawWindow() {
    ImGui.render();
    imguiGl3.renderDrawData(ImGui.getDrawData());
  }
}
