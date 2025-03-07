package voxelengine.core;

import org.joml.Vector3d;
import voxelengine.util.Constants;
import voxelengine.window.Window;

public class Camera {
    public Vector3d position = new Vector3d(160, 200, 433);
    public Vector3d front = new Vector3d(0, 0, -1);
    public Vector3d up = new Vector3d(0, 1, 0);
    public double fov = 90;
    public double yaw = 0;
    public double pitch = 0;
    public Renderer renderer;
    public Window window;

    public void handleMouseMove(double oldX, double newX, double oldY, double newY) {
        double xOffset = newX - oldX;
        double yOffset = oldY - newY;

        xOffset *= Constants.MOUSE_SENSITIVITY;
        yOffset *= Constants.MOUSE_SENSITIVITY;

        this.yaw += xOffset;
        this.pitch += yOffset;

        if (this.pitch > 89.0f)
            this.pitch = 89.0f;
        if (this.pitch < -89.0f)
            this.pitch = -89.0f;
    }

    public void update() {
        double deltaTime = this.renderer.deltaTime;

        if (this.window.keyboard.wPressed) {
            Vector3d intermediate = new Vector3d();
            this.front.mul(Constants.CAMERA_SPEED * deltaTime, intermediate);
            this.position.add(intermediate);
        }

        if (this.window.keyboard.sPressed) {
            Vector3d intermediate = new Vector3d();
            this.front.mul(Constants.CAMERA_SPEED * deltaTime, intermediate);
            this.position.sub(intermediate);
        }

        if (this.window.keyboard.dPressed) {
            Vector3d right = new Vector3d();
            this.front.cross(this.up, right).normalize();
            right.mul(Constants.CAMERA_SPEED * deltaTime, right);
            this.position.add(right);
        }

        if (this.window.keyboard.aPressed) {
            Vector3d left = new Vector3d();
            this.front.cross(this.up, left).normalize();
            left.mul(Constants.CAMERA_SPEED * deltaTime, left);
            this.position.sub(left);
        }

        double dirX = Math.cos(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch));
        double dirY = Math.sin(Math.toRadians(this.pitch));
        double dirZ = Math.sin(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch));

        Vector3d direction = new Vector3d(dirX, dirY, dirZ);
        direction.normalize();

        this.front.set(direction);
    }
}
