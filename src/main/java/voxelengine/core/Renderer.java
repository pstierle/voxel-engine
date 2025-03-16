package voxelengine.core;

import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import voxelengine.examples.ExampleType;
import voxelengine.util.ColorUtil;
import voxelengine.util.Constants;
import voxelengine.util.Direction;
import voxelengine.util.Log;
import voxelengine.util.WorldType;
import voxelengine.util.voxel.Color;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;
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

    public int getProgramId() {
        return programId;
    }

    public int getViewLocation() {
        return viewLocation;
    }

    public int getProjectionLocation() {
        return projectionLocation;
    }

    public int getCameraPositionLocation() {
        return cameraPositionLocation;
    }

    public double getDeltaTime() {
        return deltaTime;
    }

    public void toggleWireframe() {
        this.wireframeEnabled = !this.wireframeEnabled;
    }

    public void init() {
        this.lastFrameTime = glfwGetTime();
        this.programId = glCreateProgram();

        if (Constants.WORLD_EXAMPLE == ExampleType.TRIANGLE_2D || Constants.WORLD_EXAMPLE == ExampleType.VOXEL_2D) {
            Shader.loadShader(this.programId, "shaders/basic.fs", GL_FRAGMENT_SHADER);
            Shader.loadShader(this.programId, "shaders/basic.vs", GL_VERTEX_SHADER);
        } else {
            if (Constants.OPTIMIZATION_SHADER_MEMORY) {
                Shader.loadShader(this.programId, "shaders/world_optimized.fs", GL_FRAGMENT_SHADER);
                Shader.loadShader(this.programId, "shaders/world_optimized.vs", GL_VERTEX_SHADER);
            } else {
                Shader.loadShader(this.programId, "shaders/world.fs", GL_FRAGMENT_SHADER);
                Shader.loadShader(this.programId, "shaders/world.vs", GL_VERTEX_SHADER);
            }
            this.viewLocation = glGetUniformLocation(this.programId, "view");
            this.projectionLocation = glGetUniformLocation(this.programId, "projection");
            this.lightPositionLocation = glGetUniformLocation(this.programId, "light_position");
            this.cameraPositionLocation = glGetUniformLocation(this.programId, "camera_position");

            if (Constants.OPTIMIZATION_SHADER_MEMORY) {
                int normalBlockIndex = glGetUniformBlockIndex(this.programId, "normalPalette");
                glUniformBlockBinding(this.programId, normalBlockIndex, 0);

                List<Direction> normalPalette = List.of(
                        Direction.FRONT,
                        Direction.BACK,
                        Direction.LEFT,
                        Direction.RIGHT,
                        Direction.TOP,
                        Direction.BOTTOM
                );

                FloatBuffer normalBuffer = ByteBuffer.allocateDirect(normalPalette.size() * 4 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
                for (Direction direction : normalPalette) {
                    normalBuffer.put((float) direction.getNormal().x).put((float) direction.getNormal().y).put((float) direction.getNormal().z).put(1.0f);
                }
                normalBuffer.flip();

                int normalUbo = glGenBuffers();
                glBindBuffer(GL_UNIFORM_BUFFER, normalUbo);
                glBufferData(GL_UNIFORM_BUFFER, normalBuffer, GL_STATIC_DRAW);
                glBindBufferBase(GL_UNIFORM_BUFFER, 0, normalUbo);
            }
        }
    }

    public void setColorUBO() {
        List<Color> colorSource;
        if (Constants.WORLD_TYPE == WorldType.NBT) {
            colorSource = ColorUtil.nbtColors;
        } else {
            colorSource = ColorUtil.noiseColors;
        }

        Color[] colors = new Color[250];
        int colorIndex = 0;
        for (int i = 0; i < 250; i++) {
            if (i < colorSource.size()) {
                colors[colorIndex] = colorSource.get(i);
            } else {
                colors[colorIndex] = new Color(0.0f, 0.0f, 0.0f);
            }
            colorIndex++;
        }

        int colorBlockIndex = glGetUniformBlockIndex(this.programId, "colorPalette");
        glUniformBlockBinding(this.programId, colorBlockIndex, 1);

        FloatBuffer colorBuffer = ByteBuffer.allocateDirect(colors.length * 4 * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();

        for (int i = 0; i < 250; i++) {
            colorBuffer.put(colors[i].getR()).put(colors[i].getG()).put(colors[i].getB()).put(1.0f);
        }

        colorBuffer.flip();

        int colorUbo = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, colorUbo);
        glBufferData(GL_UNIFORM_BUFFER, colorBuffer, GL_STATIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, 1, colorUbo);
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
        FloatBuffer lightPositionDest = BufferUtils.createFloatBuffer(3);
        this.lightPosition.get(lightPositionDest);
        glUniform3fv(this.lightPositionLocation, lightPositionDest);
    }
}
