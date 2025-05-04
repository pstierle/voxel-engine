package voxelengine;

import voxelengine.core.State;
import voxelengine.examples.ExampleType;
import voxelengine.util.Constants;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL46.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL46.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL46.glClear;

public class Game {
    private static final boolean imGuiEnabled = Constants.WORLD_EXAMPLE == ExampleType.WORLD_NBT || Constants.WORLD_EXAMPLE == ExampleType.WORLD_NOISE;

    public void init() {
        State.window.init();
        State.renderer.init();

        if (imGuiEnabled) {
            State.imGui.init();
        }

        State.example.init();
    }

    public void loop() {
        while (!glfwWindowShouldClose(State.window.getHandle())) {
            State.window.input();

            State.physics.update();
            State.camera.update();
            State.renderer.update();
            State.example.update();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            State.example.render();

            if (imGuiEnabled) {
                State.imGui.render();
            }

            glfwSwapBuffers(State.window.getHandle());
            glfwPollEvents();
        }
    }

    public void destroy() {
        State.window.destroy();
        State.example.destroy();
        if (imGuiEnabled) {
            State.imGui.destroy();
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.init();
        game.loop();
        game.destroy();
    }
}
