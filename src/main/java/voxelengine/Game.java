package voxelengine;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;
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

    public void init() {
        this.camera = new Camera();
        this.renderer = new Renderer();
        this.window = new Window();


        this.window.renderer = this.renderer;
        this.window.camera = this.camera;

        this.camera.init(renderer, window);
        this.window.init();
        this.renderer.init(camera, window);

        switch (Constants.EXAMPLE) {
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
                this.example = new World();
                break;
        }

        this.example.init(renderer, camera);
    }

    public void loop() {
        while (!glfwWindowShouldClose(this.window.handle)) {
            this.window.input();

            this.camera.update();
            this.renderer.update();
            this.example.update();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            this.example.render();

            glfwSwapBuffers(this.window.handle);
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
