package voxelengine.window.input;

import org.joml.Vector2d;

public class Mouse {
    public Vector2d position;
    public boolean firstMouseMoveHandled = false;

    public Mouse(double initialX, double initialY) {
        position = new Vector2d(initialX, initialY);
    }
}
