package voxelengine.core;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;

import voxelengine.window.Window;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL46.*;

import java.nio.FloatBuffer;

public class Renderer {
    public boolean wireframeEnabled = false;
    public double deltaTime = 0;
    public double lastFrameTime;
    public double fpsTimer = 0;
    public int programId;
    public int vboId;
    public int vaoId;
    public int eboId;
    public int frameCount;
    public int viewLocation;
    public int projectionLocation;
    public int lightPositionLocation;
    public int cameraPositionLocation;
    public Camera camera;
    public Window window;
    public Vector3d lightPosition;
    public int numVoxels;

    public void init() {
        this.lastFrameTime = glfwGetTime();
        this.programId = glCreateProgram();
        this.lightPosition = new Vector3d(0, 50, 0);
    }

    public void udpate() {
        this.frameCount++;
        double currentFrameTime = glfwGetTime();

        this.deltaTime = currentFrameTime - this.lastFrameTime;
        this.lastFrameTime = currentFrameTime;

        double lastFpsTime = currentFrameTime - this.fpsTimer;

        if (lastFpsTime >= 1) {
            System.out.println("FPS: " + this.frameCount);
            this.fpsTimer = currentFrameTime;
            this.frameCount = 0;
        }

        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (this.wireframeEnabled == true) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        Matrix4d view = new Matrix4d();
        Matrix4d projection = new Matrix4d();

        Vector3d center = new Vector3d();
        center.add(this.camera.position).add(this.camera.front);

        view.lookAt(this.camera.position, center, this.camera.up);

        double aspectRatio = (double) this.window.width / this.window.height;
        projection.perspective(Math.toRadians(this.camera.fov), aspectRatio, 0.1, 1000);

        FloatBuffer viewDest = BufferUtils.createFloatBuffer(16);
        view.get(viewDest);
        glUniformMatrix4fv(this.viewLocation, false, viewDest);

        FloatBuffer projectionDest = BufferUtils.createFloatBuffer(16);
        projection.get(projectionDest);
        glUniformMatrix4fv(this.projectionLocation, false, projectionDest);

        FloatBuffer cameraDest = BufferUtils.createFloatBuffer(3);
        this.camera.position.get(cameraDest);
        glUniform3fv(this.cameraPositionLocation, cameraDest);

        FloatBuffer lightPositionDest = BufferUtils.createFloatBuffer(3);
        this.lightPosition.get(lightPositionDest);
        glUniform3fv(this.lightPositionLocation, lightPositionDest);
    }
}
