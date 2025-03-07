package voxelengine;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.examples.BaseExample;
import voxelengine.examples.Triangle2d;
import voxelengine.examples.Voxel2d;
import voxelengine.examples.Voxel3d;
import voxelengine.examples.World;
import voxelengine.util.Constants;
import voxelengine.util.NbtUtil;
import voxelengine.util.NoiseUtil;
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

        this.renderer.camera = this.camera;
        this.renderer.window = this.window;

        this.window.renderer = this.renderer;
        this.window.camera = this.camera;

        this.camera.renderer = this.renderer;
        this.camera.window = this.window;

        NbtUtil nbtUtil = new NbtUtil();
        NoiseUtil noiseUtil = new NoiseUtil();
        nbtUtil.renderer = this.renderer;
        noiseUtil.renderer = this.renderer;
        noiseUtil.camera = this.camera;

        this.window.init();
        this.renderer.init();

        switch (Constants.EXAMPLE) {
            case TRIANGLE_2D:
                Triangle2d triangle2d = new Triangle2d();
                triangle2d.renderer = this.renderer;
                this.example = triangle2d;
                break;
            case VOXEL_2D:
                Voxel2d voxel2d = new Voxel2d();
                voxel2d.renderer = this.renderer;
                this.example = voxel2d;
                break;
            case VOXEL_3D:
                Voxel3d voxel3d = new Voxel3d();
                voxel3d.renderer = this.renderer;
                this.example = voxel3d;
                break;
            case WORLD:
                World world = new World();
                world.renderer = this.renderer;
                world.nbtUtil = nbtUtil;
                world.noiseUtil = noiseUtil;
                world.camera = this.camera;
                this.example = world;
                break;
        }

        this.example.init();
    }

    public void loop() {
        while (!glfwWindowShouldClose(this.window.handle)) {
            this.window.input();

            this.camera.update();
            this.renderer.udpate();
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
