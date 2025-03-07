package voxelengine.window.input;

import org.joml.Vector2d;

public class Mouse {
    private final Vector2d position;
    private boolean firstMouseMoveHandled = false;

    public Vector2d getPosition() {
        return position;
    }

    public void setPosition(double x, double y) {
        this.position.set(x, y);
    }

    public boolean isFirstMouseMoveHandled() {
        return firstMouseMoveHandled;
    }

    public void setFirstMouseMoveHandled(boolean firstMouseMoveHandled) {
        this.firstMouseMoveHandled = firstMouseMoveHandled;
    }

    public Mouse(double initialX, double initialY) {
        position = new Vector2d(initialX, initialY);
    }
}
