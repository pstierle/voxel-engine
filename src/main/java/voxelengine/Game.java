package voxelengine;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

public class Game {
    public static void main(String[] args) {
        VoxelEngineBase voxelEngine = new VoxelEngine();

        voxelEngine.initGLFW();
        voxelEngine.initOpenGL();
        voxelEngine.initShaders();
        voxelEngine.initVertices();
        voxelEngine.initImGUI();

        while (!glfwWindowShouldClose(voxelEngine.getWindowHandle())) {
            voxelEngine.handleInput();
            voxelEngine.update();

            voxelEngine.renderPrepare();
            voxelEngine.render();
            voxelEngine.renderStats();
            voxelEngine.renderCleanup();
        }

        voxelEngine.cleanUp();
    }
}
