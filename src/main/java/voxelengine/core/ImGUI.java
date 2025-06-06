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

    private static final int STATS_WINDOW_WIDTH = 330;
    private static final int STATS_WINDOW_HEIGHT = 168;

    public void init(long windowHandle) {
        ImGui.createContext();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 150");
    }

    public void render(String className, double cameraX, double cameraY, double cameraZ, int fps, int voxelCount,
            int verticesCount,
            int verticesByteSize, long jvmMemory, int gpuMemoryUsedKB) {
        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        ImGui.setNextWindowPos(10, 10);
        ImGui.setNextWindowSize(STATS_WINDOW_WIDTH, STATS_WINDOW_HEIGHT);

        ImGui.begin("Statistics", ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.AlwaysAutoResize |
                ImGuiWindowFlags.NoCollapse);

        ImGui.text("Active Class: " + className);
        ImGui.text("FPS: " + fps);
        ImGui.text(String.format("Position: (x=%d, y=%d, z=%d)",
                (int) cameraX,
                (int) cameraY,
                (int) cameraZ));
        ImGui.text(String.format("Voxels: %d", voxelCount));
        ImGui.text(String.format("Vertices: %d", verticesCount));
        ImGui.text(String.format("Vertices Size (mb): %.2f", verticesByteSize / (1024.0 * 1024.0)));
        ImGui.text(String.format("JVM Memory (mb): %.2f", jvmMemory / (1024.0 * 1024.0)));
        ImGui.text(String.format("GPU Memory (mb): %.2f", gpuMemoryUsedKB / 1024.0));

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