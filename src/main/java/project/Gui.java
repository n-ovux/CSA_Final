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
    values.put("frequency", new float[] { 0.006f });
    values.put("octaves", new int[] { 5 });
    values.put("wireframe", new ImBoolean(false));
    values.put("erosion", new ImBoolean(true));
    values.put("fps", Integer.valueOf(0));
    values.put("mspf", Float.valueOf(0.0f));
    values.put("perlinNoise", Integer.valueOf(0));
    values.put("normalMap", Integer.valueOf(0));

    values.put("depositionRate", new float[] { 1.0f });
    values.put("erosionRate", new float[] { 1.0f });
    values.put("friction", new float[] { 1.0f });
    values.put("speed", new float[] { 1.0f });
    values.put("maxIterations", new int[] { 80 });
    values.put("drops", new int[] { 100 });
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
    ImGui.setNextWindowSize(360.0f, 480.0f);
    ImGui.begin("Information", flags);
    ImGui.text("fps: " + values.get("fps"));
    ImGui.text("mspf: " + values.get("mspf"));
    ImGui.sliderFloat("Subdivisions", (float[]) values.get("subdivisions"), 0.0f, glGetFloat(GL_MAX_TESS_GEN_LEVEL));
    ImGui.sliderFloat("Frequency", (float[]) values.get("frequency"), 0.0f, 1.0f, "%.3f", ImGuiSliderFlags.Logarithmic);
    ImGui.sliderInt("Octaves", (int[]) values.get("octaves"), 1, 5);
    ImGui.sliderFloat("depositionRate", (float[]) values.get("depositionRate"), 0.0f, 1.0f);
    ImGui.sliderFloat("erosionRate", (float[]) values.get("erosionRate"), 0.0f, 1.0f);
    ImGui.sliderFloat("friction", (float[]) values.get("friction"), 0.0f, 1.0f);
    ImGui.sliderFloat("speed", (float[]) values.get("speed"), 0.0f, 10.0f);
    ImGui.sliderInt("maxIterations", (int[]) values.get("maxIterations"), 1, 100);
    ImGui.sliderInt("drops", (int[]) values.get("drops"), 1, 500);
    ImGui.checkbox("Wireframe", (ImBoolean) values.get("wireframe"));
    ImGui.checkbox("Erosion", (ImBoolean) values.get("erosion"));
    ImGui.text("Perlin Noise:");
    ImGui.sameLine(140.0f);
    ImGui.text("Normal Map:");
    ImGui.image(((Integer) values.get("perlinNoise")), 128.0f, 128.0f);
    ImGui.sameLine();
    ImGui.image(((Integer) values.get("normalMap")), 128.0f, 128.0f);
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
