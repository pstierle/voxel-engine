package voxelengine.core;

import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.lwjgl.glfw.GLFW;

public class ImGUI {
    protected ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    protected ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private static final int STATS_WINDOW_WIDTH = 230;
    private static final int STATS_WINDOW_HEIGHT = 85;

    public void init(long windowHandle) {
        ImGui.createContext();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 150");
    }

    public void render(double cameraX, double cameraY, double cameraZ, int fps, int verticesCount) {
        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        ImGui.setNextWindowPos(10, 10);
        ImGui.setNextWindowSize(STATS_WINDOW_WIDTH, STATS_WINDOW_HEIGHT);

        ImGui.begin("Statistics", ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.AlwaysAutoResize |
                ImGuiWindowFlags.NoCollapse);

        ImGui.text("FPS: " + fps);
        ImGui.text(String.format("Position: (x=%d, y=%d, z=%d)",
                (int) cameraX,
                (int) cameraY,
                (int) cameraZ));
        ImGui.text(String.format("Loaded vertices: %d", verticesCount));

        ImGui.end();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupCurrentContext = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupCurrentContext);
        }
    }

    public void destroy() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
    }
}