package voxelengine.core;

import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImFloat;
import org.lwjgl.glfw.GLFW;

public class ImGUI {
    protected ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    protected ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    private final ImFloat playerHeight = new ImFloat((float) State.physics.PLAYER_HEIGHT);
    private final ImFloat playerWidth = new ImFloat((float) State.physics.PLAYER_WIDTH);
    private final ImFloat jumpStrength = new ImFloat((float) State.physics.JUMP_STRENGTH);
    private final ImFloat gravity = new ImFloat((float) State.physics.GRAVITY);
    private final ImFloat terminalVelocity = new ImFloat((float) State.physics.TERMINAL_VELOCITY);
    private final ImFloat maxPlayerSpeed = new ImFloat((float) State.physics.MAX_PLAYER_SPEED);
    private final ImFloat acceleration = new ImFloat((float) State.physics.ACCELERATION);
    private final ImFloat deceleration = new ImFloat((float) State.physics.DECELERATION);
    private final ImFloat airControlFactor = new ImFloat((float) State.physics.AIR_CONTROL_FACTOR);

    private static final int STATS_WINDOW_WIDTH = 230;
    private static final int STATS_WINDOW_HEIGHT = 90;
    private static final int PARAMS_WINDOW_WIDTH = 420;
    private static final int PARAMS_WINDOW_HEIGHT = 300;

    public void init() {
        ImGui.createContext();
        imGuiGlfw.init(State.window.getHandle(), true);
        imGuiGl3.init("#version 150");
    }

    public void render() {
        imGuiGl3.newFrame();
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        renderStatsWindow();
        renderPhysicsControlWindow();

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());

        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupCurrentContext = GLFW.glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            GLFW.glfwMakeContextCurrent(backupCurrentContext);
        }
    }

    private void renderStatsWindow() {
        int windowWidth = State.window.getWidth();
        float posX = windowWidth - STATS_WINDOW_WIDTH - (float) 10;
        float posY = 10;

        ImGui.setNextWindowPos(posX, posY);
        ImGui.setNextWindowSize(STATS_WINDOW_WIDTH, STATS_WINDOW_HEIGHT);

        int windowFlags = ImGuiWindowFlags.NoResize |
                ImGuiWindowFlags.AlwaysAutoResize |
                ImGuiWindowFlags.NoCollapse;

        ImGui.begin("Statistics", windowFlags);

        ImGui.text("FPS: " + State.renderer.getFPS());
        ImGui.text("Rendered Chunks: " + State.world.getRenderedChunks());
        ImGui.text(String.format("Position: (x=%d, y=%d, z=%d)",
                (int) State.camera.getPosition().x,
                (int) State.camera.getPosition().y,
                (int) State.camera.getPosition().z));

        ImGui.end();
    }

    private void renderPhysicsControlWindow() {
        ImGui.setNextWindowCollapsed(State.window.isCursorDisabled());
        ImGui.setNextWindowPos(10, 10);
        ImGui.setNextWindowSize(PARAMS_WINDOW_WIDTH, PARAMS_WINDOW_HEIGHT);

        ImGui.begin("Game Parameters");

        playerHeight.set((float) State.physics.PLAYER_HEIGHT);
        playerWidth.set((float) State.physics.PLAYER_WIDTH);
        jumpStrength.set((float) State.physics.JUMP_STRENGTH);
        gravity.set((float) State.physics.GRAVITY);
        terminalVelocity.set((float) State.physics.TERMINAL_VELOCITY);
        maxPlayerSpeed.set((float) State.physics.MAX_PLAYER_SPEED);
        acceleration.set((float) State.physics.ACCELERATION);
        deceleration.set((float) State.physics.DECELERATION);
        airControlFactor.set((float) State.physics.AIR_CONTROL_FACTOR);

        // Player dimensions
        ImGui.text("Player Dimensions");
        if (ImGui.dragFloat("Player Height", playerHeight.getData(), 0.1f, 0.5f, 20.0f, "%.1f")) {
            State.physics.PLAYER_HEIGHT = playerHeight.get();
        }

        if (ImGui.dragFloat("Player Width", playerWidth.getData(), 0.1f, 0.5f, 10.0f, "%.1f")) {
            State.physics.PLAYER_WIDTH = playerWidth.get();
        }

        ImGui.separator();

        ImGui.text("Movement Parameters");
        if (ImGui.dragFloat("Max Player Speed", maxPlayerSpeed.getData(), 0.5f, 1.0f, 50.0f, "%.1f")) {
            State.physics.MAX_PLAYER_SPEED = maxPlayerSpeed.get();
        }

        if (ImGui.dragFloat("Acceleration", acceleration.getData(), 1.0f, 5.0f, 300.0f, "%.1f")) {
            State.physics.ACCELERATION = acceleration.get();
        }

        if (ImGui.dragFloat("Deceleration", deceleration.getData(), 1.0f, 1.0f, 100.0f, "%.1f")) {
            State.physics.DECELERATION = deceleration.get();
        }

        if (ImGui.dragFloat("Air Control Factor", airControlFactor.getData(), 0.01f, 0.0f, 1.0f, "%.2f")) {
            State.physics.AIR_CONTROL_FACTOR = airControlFactor.get();
        }

        ImGui.separator();

        ImGui.text("Physics Parameters");
        if (ImGui.dragFloat("Jump Strength", jumpStrength.getData(), 0.5f, 1.0f, 40.0f, "%.1f")) {
            State.physics.JUMP_STRENGTH = jumpStrength.get();
        }

        if (ImGui.dragFloat("Gravity", gravity.getData(), 0.5f, -50.0f, 0.0f, "%.1f")) {
            State.physics.GRAVITY = gravity.get();
        }

        if (ImGui.dragFloat("Terminal Velocity", terminalVelocity.getData(), 1.0f, -300.0f, -10.0f, "%.1f")) {
            State.physics.TERMINAL_VELOCITY = terminalVelocity.get();
        }
        ImGui.end();
    }

    public void destroy() {
        imGuiGl3.shutdown();
        imGuiGlfw.shutdown();
        ImGui.destroyContext();
    }
}