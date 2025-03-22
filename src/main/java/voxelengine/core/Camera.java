package voxelengine.core;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector4d;
import org.lwjgl.BufferUtils;
import voxelengine.util.Constants;
import voxelengine.util.WorldType;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.glUniform3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

public class Camera {
    private static final float FRUSTUM_SHRINK_FACTOR = 0.0f;
    public final Vector3d position = new Vector3d(0, 100, 0);
    private final Vector3d front = new Vector3d(0, 0, -1);
    private final Vector3d up = new Vector3d(0, 1, 0);
    private Matrix4d viewMatrix = new Matrix4d();
    private Matrix4d projectionMatrix = new Matrix4d();
    private Vector4d[] frustumPlanes = new Vector4d[6];
    private double yaw = 0;
    private double pitch = 0;

    public Camera() {
        for (int i = 0; i < 6; i++) {
            frustumPlanes[i] = new Vector4d();
        }
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

    public boolean isOnFrustum(int positionX, int positionY, int positionZ) {
        int height = State.CHUNK_SIZE;
        if (Constants.WORLD_TYPE == WorldType.NOISE) {
            height *= 2;
        }
        Vector3d[] corners = new Vector3d[8];
        // Bottom corners
        corners[0] = new Vector3d(positionX, positionY, positionZ);
        corners[1] = new Vector3d(positionX + State.CHUNK_SIZE, positionY, positionZ);
        corners[2] = new Vector3d(positionX + State.CHUNK_SIZE, positionY, positionZ + State.CHUNK_SIZE);
        corners[3] = new Vector3d(positionX, positionY, positionZ + State.CHUNK_SIZE);
        // Top corners
        corners[4] = new Vector3d(positionX, positionY + height, positionZ);
        corners[5] = new Vector3d(positionX + State.CHUNK_SIZE, positionY + height, positionZ);
        corners[6] = new Vector3d(positionX + State.CHUNK_SIZE, positionY + height, positionZ + State.CHUNK_SIZE);
        corners[7] = new Vector3d(positionY, positionY + height, positionZ + State.CHUNK_SIZE);

        for (int i = 0; i < 6; i++) {
            boolean allCornersOutside = true;

            for (Vector3d corner : corners) {
                if (distanceToPlane(frustumPlanes[i], corner) >= (0 + FRUSTUM_SHRINK_FACTOR)) {
                    allCornersOutside = false;
                    break;
                }
            }

            if (allCornersOutside) {
                return false;
            }
        }

        return true;
    }

    private double distanceToPlane(Vector4d plane, Vector3d point) {
        return plane.x * point.x + plane.y * point.y + plane.z * point.z + plane.w;
    }

    public void update() {
        double deltaTime = State.renderer.getDeltaTime();

        if (State.window.wPressed) {
            Vector3d intermediate = new Vector3d();
            this.front.mul(State.CAMERA_SPEED * deltaTime, intermediate);
            this.position.add(intermediate);
        }

        if (State.window.sPressed) {
            Vector3d intermediate = new Vector3d();
            this.front.mul(State.CAMERA_SPEED * deltaTime, intermediate);
            this.position.sub(intermediate);
        }

        if (State.window.dPressed) {
            Vector3d right = new Vector3d();
            this.front.cross(this.up, right).normalize();
            right.mul(State.CAMERA_SPEED * deltaTime, right);
            this.position.add(right);
        }

        if (State.window.aPressed) {
            Vector3d left = new Vector3d();
            this.front.cross(this.up, left).normalize();
            left.mul(State.CAMERA_SPEED * deltaTime, left);
            this.position.sub(left);
        }

        double dirX = Math.cos(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch));
        double dirY = Math.sin(Math.toRadians(this.pitch));
        double dirZ = Math.sin(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch));

        Vector3d direction = new Vector3d(dirX, dirY, dirZ);
        direction.normalize();

        this.front.set(direction);

        this.projectionMatrix = new Matrix4d();
        this.viewMatrix = new Matrix4d();

        Vector3d center = new Vector3d();
        center.add(this.position).add(this.front);

        this.viewMatrix.lookAt(this.position, center, this.up);

        double aspectRatio = (double) State.window.getWidth() / State.window.getHeight();
        this.projectionMatrix.perspective(Math.toRadians(Constants.CAMERA_FOV), aspectRatio, 0.1, 10000);

        FloatBuffer viewDest = BufferUtils.createFloatBuffer(16);
        this.viewMatrix.get(viewDest);
        glUniformMatrix4fv(State.renderer.getViewLocation(), false, viewDest);

        FloatBuffer projectionDest = BufferUtils.createFloatBuffer(16);
        this.projectionMatrix.get(projectionDest);
        glUniformMatrix4fv(State.renderer.getProjectionLocation(), false, projectionDest);

        FloatBuffer cameraDest = BufferUtils.createFloatBuffer(3);
        this.position.get(cameraDest);
        glUniform3fv(State.renderer.getCameraPositionLocation(), cameraDest);

        Matrix4d viewProjection = new Matrix4d(projectionMatrix).mul(viewMatrix);

        // Left plane
        frustumPlanes[0].set(
                viewProjection.m03() + viewProjection.m00(),
                viewProjection.m13() + viewProjection.m10(),
                viewProjection.m23() + viewProjection.m20(),
                viewProjection.m33() + viewProjection.m30());

        // Right plane
        frustumPlanes[1].set(
                viewProjection.m03() - viewProjection.m00(),
                viewProjection.m13() - viewProjection.m10(),
                viewProjection.m23() - viewProjection.m20(),
                viewProjection.m33() - viewProjection.m30());

        // Bottom plane
        frustumPlanes[2].set(
                viewProjection.m03() + viewProjection.m01(),
                viewProjection.m13() + viewProjection.m11(),
                viewProjection.m23() + viewProjection.m21(),
                viewProjection.m33() + viewProjection.m31());

        // Top plane
        frustumPlanes[3].set(
                viewProjection.m03() - viewProjection.m01(),
                viewProjection.m13() - viewProjection.m11(),
                viewProjection.m23() - viewProjection.m21(),
                viewProjection.m33() - viewProjection.m31());

        // Near plane
        frustumPlanes[4].set(
                viewProjection.m03() + viewProjection.m02(),
                viewProjection.m13() + viewProjection.m12(),
                viewProjection.m23() + viewProjection.m22(),
                viewProjection.m33() + viewProjection.m32());

        // Far plane
        frustumPlanes[5].set(
                viewProjection.m03() - viewProjection.m02(),
                viewProjection.m13() - viewProjection.m12(),
                viewProjection.m23() - viewProjection.m22(),
                viewProjection.m33() - viewProjection.m32());

        normalizePlanes();
    }


    public int getChunkX() {
        return Math.floorDiv((int) this.position.x, Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;
    }

    public int getChunkZ() {
        return Math.floorDiv((int) this.position.z, Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;
    }

    private void normalizePlanes() {
        for (int i = 0; i < 6; i++) {
            double length = Math.sqrt(frustumPlanes[i].x * frustumPlanes[i].x + frustumPlanes[i].y * frustumPlanes[i].y + frustumPlanes[i].z * frustumPlanes[i].z);
            frustumPlanes[i].x /= length;
            frustumPlanes[i].y /= length;
            frustumPlanes[i].z /= length;
            frustumPlanes[i].w /= length;
        }
    }
}
