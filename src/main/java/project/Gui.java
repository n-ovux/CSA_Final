package project;

import java.util.Hashtable;

import imgui.*;
import imgui.flag.ImGuiSliderFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.*;
import imgui.glfw.ImGuiImplGlfw;
import imgui.gl3.ImGuiImplGl3;

import static org.lwjgl.opengl.GL43.*;

public class Gui {
  private ImGuiImplGlfw imguiGlfw;
  private ImGuiImplGl3 imguiGl3;
  private ImGuiIO imguiIO;

  private Hashtable values;

  public Gui(long window) {
    imguiGlfw = new ImGuiImplGlfw();
    imguiGl3 = new ImGuiImplGl3();

    ImGui.createContext();
    imguiIO = ImGui.getIO();
    imguiIO.setIniFilename(null);
    imguiIO.setLogFilename(null);
    imguiGlfw.init(window, true);
    imguiGl3.init();

    this.values = new Hashtable();

    values.put("subdivisions", new float[] { glGetFloat(GL_MAX_TESS_GEN_LEVEL) });
    values.put("frequency", new float[] { 0.02f });
    values.put("wireframe", new ImBoolean(false));
    values.put("fps", Integer.valueOf(0));
    values.put("mspf", Float.valueOf(0.0f));
    values.put("perlinNoise", Integer.valueOf(0));
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

    int flags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove
        | ImGuiWindowFlags.NoResize;

    ImGui.setNextWindowPos(1.0f, 1.0f);
    ImGui.setNextWindowSize(300.0f, 250.0f);
    ImGui.begin("Information", flags);
    ImGui.text("fps: " + values.get("fps"));
    ImGui.text("mspf: " + values.get("mspf"));
    ImGui.sliderFloat("subdivisions", (float[]) values.get("subdivisions"), 0.0f, glGetFloat(GL_MAX_TESS_GEN_LEVEL));
    ImGui.sliderFloat("frequency", (float[]) values.get("frequency"), 0.0f, 1.0f, "%.3f", ImGuiSliderFlags.Logarithmic);
    ImGui.checkbox("Wireframe", (ImBoolean) values.get("wireframe"));
    ImGui.image(((Integer) values.get("perlinNoise")), 128.0f, 128.0f);
    ImGui.end();
  }

  public <T> T getValue(String valueName) {
    return (T) this.values.get(valueName);
  }

  public <T> void setValue(String valueName, T object) {
    this.values.put(valueName, object);
  }

  public void drawWindow() {
    ImGui.render();
    imguiGl3.renderDrawData(ImGui.getDrawData());
  }
}
