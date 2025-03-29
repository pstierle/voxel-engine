package voxelengine;

import voxelengine.core.State;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL46.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL46.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL46.glClear;

public class Game {
    public void init() {
        State.window.init();
        State.renderer.init();
        State.world.init();
        State.imGui.init();
    }

    public void loop() {
        while (!glfwWindowShouldClose(State.window.getHandle())) {
            State.window.input();

            State.physics.update();
            State.camera.update();
            State.renderer.update();
            State.world.update();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            State.world.render();
            State.imGui.render();

            glfwSwapBuffers(State.window.getHandle());
            glfwPollEvents();
        }
    }

    public void destroy() {
        State.window.destroy();
        State.world.destroy();
        State.imGui.destroy();
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.init();
        game.loop();
        game.destroy();
    }
}
