package voxelengine;

import voxelengine.core.Renderer;
import voxelengine.window.Window;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;

import voxelengine.core.Camera;
import voxelengine.examples.BaseExample;
import voxelengine.examples.Triangle2d;
import voxelengine.examples.Voxel2d;
import voxelengine.examples.Voxel3d;
import voxelengine.examples.World;
import voxelengine.util.Constants;
import voxelengine.util.NbtUtil;
import voxelengine.util.NoiseUtil;

public class Game {
    private Camera camera;
    private Renderer renderer;
    private Window window;
    private BaseExample example;
    private NbtUtil nbtUtil;
    private NoiseUtil noiseUtil;

    public void init() {
        this.camera = new Camera();
        this.renderer = new Renderer();
        this.window = new Window();
        this.nbtUtil = new NbtUtil();
        this.noiseUtil = new NoiseUtil();

        renderer.camera = camera;
        renderer.window = window;

        window.renderer = renderer;
        window.camera = camera;

        camera.renderer = renderer;
        camera.window = window;

        this.nbtUtil.renderer = renderer;
        this.noiseUtil.renderer = renderer;

        this.window.init();
        this.renderer.init();

        switch (Constants.EXAMPLE) {
            case TRIANGLE_2D:
                Triangle2d triangle2d = new Triangle2d();
                triangle2d.renderer = renderer;
                this.example = triangle2d;
                break;
            case VOXEL_2D:
                Voxel2d voxel2d = new Voxel2d();
                voxel2d.renderer = renderer;
                this.example = voxel2d;
                break;
            case VOXEL_3D:
                Voxel3d voxel3d = new Voxel3d();
                voxel3d.renderer = renderer;
                this.example = voxel3d;
                break;
            case WORLD:
                World world = new World();
                world.renderer = renderer;
                world.nbtUtil = nbtUtil;
                world.noiseUtil = noiseUtil;
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

    }

    public static void main(String[] args) {
        Game game = new Game();
        game.init();
        game.loop();
        game.destroy();
    }
}
