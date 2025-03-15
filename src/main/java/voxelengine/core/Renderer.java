package voxelengine.core;

import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import voxelengine.examples.ExampleType;
import voxelengine.util.Constants;
import voxelengine.util.Log;
import voxelengine.window.Window;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL46.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL46.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL46.GL_FILL;
import static org.lwjgl.opengl.GL46.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL46.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL46.GL_LINE;
import static org.lwjgl.opengl.GL46.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL46.glClear;
import static org.lwjgl.opengl.GL46.glClearColor;
import static org.lwjgl.opengl.GL46.glCreateProgram;
import static org.lwjgl.opengl.GL46.glGetUniformLocation;
import static org.lwjgl.opengl.GL46.glPolygonMode;
import static org.lwjgl.opengl.GL46.glUniform3fv;
import static org.lwjgl.opengl.GL46.glUniformMatrix4fv;

public class Renderer {
    private final Vector3d lightPosition = new Vector3d(0, 200, 0);
    private boolean wireframeEnabled = false;
    private double deltaTime = 0;
    private double lastFrameTime;
    private double fpsTimer = 0;
    private int programId;
    private int frameCount;
    private int viewLocation;
    private int projectionLocation;
    private int lightPositionLocation;
    private int cameraPositionLocation;

    private Camera camera;
    private Window window;

    public int getProgramId() {
        return programId;
    }

    public double getDeltaTime() {
        return deltaTime;
    }

    public void toggleWireframe() {
        this.wireframeEnabled = !this.wireframeEnabled;
    }

    public void init(Camera camera, Window window) {
        this.camera = camera;
        this.window = window;
        this.lastFrameTime = glfwGetTime();
        this.programId = glCreateProgram();

        if (Constants.WORLD_EXAMPLE == ExampleType.TRIANGLE_2D || Constants.WORLD_EXAMPLE == ExampleType.VOXEL_2D) {
            Shader.loadShader(this.programId, "shaders/basic.fs", GL_FRAGMENT_SHADER);
            Shader.loadShader(this.programId, "shaders/basic.vs", GL_VERTEX_SHADER);
        } else {
            Shader.loadShader(this.programId, "shaders/world.fs", GL_FRAGMENT_SHADER);
            Shader.loadShader(this.programId, "shaders/world.vs", GL_VERTEX_SHADER);
            this.viewLocation = glGetUniformLocation(this.programId, "view");
            this.projectionLocation = glGetUniformLocation(this.programId, "projection");
            this.lightPositionLocation = glGetUniformLocation(this.programId, "light_position");
            this.cameraPositionLocation = glGetUniformLocation(this.programId, "camera_position");
        }
    }

    public void update() {
        this.displayStats();
        this.prepare();
        this.updateUniforms();
    }

    private void displayStats() {
        this.frameCount++;
        double currentFrameTime = glfwGetTime();

        this.deltaTime = currentFrameTime - this.lastFrameTime;
        this.lastFrameTime = currentFrameTime;

        double lastFpsTime = currentFrameTime - this.fpsTimer;

        if (lastFpsTime >= 1) {
            Log.info("FPS: " + this.frameCount);
            this.fpsTimer = currentFrameTime;
            this.frameCount = 0;
        }
    }

    private void prepare() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (this.wireframeEnabled) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    private void updateUniforms() {
        Matrix4d view = new Matrix4d();
        Matrix4d projection = new Matrix4d();

        Vector3d center = new Vector3d();
        center.add(this.camera.getPosition()).add(this.camera.getFront());

        view.lookAt(this.camera.getPosition(), center, this.camera.getUp());

        double aspectRatio = (double) this.window.getWidth() / this.window.getHeight();
        projection.perspective(Math.toRadians(Constants.CAMERA_FOV), aspectRatio, 0.1, 10000);

        FloatBuffer viewDest = BufferUtils.createFloatBuffer(16);
        view.get(viewDest);
        glUniformMatrix4fv(this.viewLocation, false, viewDest);

        FloatBuffer projectionDest = BufferUtils.createFloatBuffer(16);
        projection.get(projectionDest);
        glUniformMatrix4fv(this.projectionLocation, false, projectionDest);

        FloatBuffer cameraDest = BufferUtils.createFloatBuffer(3);
        this.camera.getPosition().get(cameraDest);
        glUniform3fv(this.cameraPositionLocation, cameraDest);

        FloatBuffer lightPositionDest = BufferUtils.createFloatBuffer(3);
        this.lightPosition.get(lightPositionDest);
        glUniform3fv(this.lightPositionLocation, lightPositionDest);
    }
}
