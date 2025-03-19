package voxelengine;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.core.Statistic;
import voxelengine.examples.BaseExample;
import voxelengine.examples.Triangle2d;
import voxelengine.examples.Voxel2d;
import voxelengine.examples.Voxel3d;
import voxelengine.examples.World;
import voxelengine.util.Constants;
import voxelengine.window.Window;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL46.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL46.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL46.glClear;

public class Game {
    private Camera camera;
    private Renderer renderer;
    private Window window;
    private BaseExample example;
    private Statistic statistic;

    public void init() {
        this.statistic = new Statistic();
        this.camera = new Camera();
        this.renderer = new Renderer(statistic);
        this.window = new Window();

        this.window.init(camera, renderer);
        this.camera.init(renderer, window, statistic);
        this.window.init();
        this.renderer.init();

        switch (Constants.WORLD_EXAMPLE) {
            case TRIANGLE_2D:
                this.example = new Triangle2d();
                break;
            case VOXEL_2D:
                this.example = new Voxel2d();
                break;
            case VOXEL_3D:
                this.example = new Voxel3d();
                break;
            case WORLD:
                this.example = new World(statistic);
                break;
        }

        this.example.init(renderer, camera);
        this.statistic.startPrint();
    }

    public void loop() {
        while (!glfwWindowShouldClose(this.window.getHandle())) {
            this.window.input();

            this.camera.update();
            this.renderer.update();
            this.example.update();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            this.example.render();

            glfwSwapBuffers(this.window.getHandle());
            glfwPollEvents();
        }
    }

    public void destroy() {
        this.window.destroy();
        this.example.destroy();
    }

    public static void main(String[] args) {
        Game game = new Game();
        game.init();
        game.loop();
        game.destroy();
    }
}
