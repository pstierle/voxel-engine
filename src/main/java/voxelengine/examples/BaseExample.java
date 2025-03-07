package voxelengine.examples;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;

public interface BaseExample {
    void init(Renderer renderer, Camera camera);

    void update();

    void render();

    void destroy();
}
