package voxelengine.core;

import org.joml.Vector3d;
import voxelengine.util.Constants;
import voxelengine.window.Window;

public class Camera {
    private final Vector3d position = new Vector3d(0, 200, 0);
    private final Vector3d front = new Vector3d(0, 0, -1);
    private final Vector3d up = new Vector3d(0, 1, 0);

    private Renderer renderer;
    private Window window;

    private double yaw = 0;
    private double pitch = 0;

    public void init(Renderer renderer, Window window) {
        this.renderer = renderer;
        this.window = window;
    }

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
        double deltaTime = this.renderer.getDeltaTime();

        if (this.window.getKeyboard().isWPressed()) {
            Vector3d intermediate = new Vector3d();
            this.front.mul(Constants.CAMERA_SPEED * deltaTime, intermediate);
            this.position.add(intermediate);
        }

        if (this.window.getKeyboard().isSPressed()) {
            Vector3d intermediate = new Vector3d();
            this.front.mul(Constants.CAMERA_SPEED * deltaTime, intermediate);
            this.position.sub(intermediate);
        }

        if (this.window.getKeyboard().isDPressed()) {
            Vector3d right = new Vector3d();
            this.front.cross(this.up, right).normalize();
            right.mul(Constants.CAMERA_SPEED * deltaTime, right);
            this.position.add(right);
        }

        if (this.window.getKeyboard().isAPressed()) {
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

    public int getChunkX() {
        return Math.floorDiv((int) this.position.x, Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;
    }

    public int getChunkZ() {
        return Math.floorDiv((int) this.position.z, Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;
    }

    public Vector3d getPosition() {
        return position;
    }

    public Vector3d getFront() {
        return front;
    }

    public Vector3d getUp() {
        return up;
    }
}
