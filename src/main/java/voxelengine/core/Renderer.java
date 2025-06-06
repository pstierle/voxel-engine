package voxelengine.core;

import voxelengine.VoxelEngineUtil.Color;
import voxelengine.examples.ExampleType;
import voxelengine.util.ColorUtil;
import voxelengine.util.Constants;
import voxelengine.util.Direction;
import voxelengine.util.Shader;

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

public class Renderer {
    private boolean wireframeEnabled = false;
    private double deltaTime = 0;
    private double lastFrameTime;
    private double fpsTimer = 0;
    private int programId;
    private int frameCount;
    private int fps = 0;
    private int viewLocation;
    private int projectionLocation;

    public int getProgramId() {
        return programId;
    }

    public int getViewLocation() {
        return viewLocation;
    }

    public int getFPS() {
        return fps;
    }

    public int getProjectionLocation() {
        return projectionLocation;
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

        if (Constants.WORLD_EXAMPLE == ExampleType.TRIANGLE_2D) {
            Shader.loadShader(this.programId, "shaders/2D.fs", GL_FRAGMENT_SHADER);
            Shader.loadShader(this.programId, "shaders/2D.vs", GL_VERTEX_SHADER);
        } else if (Constants.WORLD_EXAMPLE == ExampleType.QUAD_2D) {
            Shader.loadShader(this.programId, "shaders/2D_color.fs", GL_FRAGMENT_SHADER);
            Shader.loadShader(this.programId, "shaders/2D_color.vs", GL_VERTEX_SHADER);
        } else {
            if (Constants.OPTIMIZATION_SHADER_MEMORY) {
                Shader.loadShader(this.programId, "shaders/3D_optimized.fs", GL_FRAGMENT_SHADER);
                Shader.loadShader(this.programId, "shaders/3D_optimized.vs", GL_VERTEX_SHADER);
            } else {
                Shader.loadShader(this.programId, "shaders/3D.fs", GL_FRAGMENT_SHADER);
                Shader.loadShader(this.programId, "shaders/3D.vs", GL_VERTEX_SHADER);
            }
            this.viewLocation = glGetUniformLocation(this.programId, "view");
            this.projectionLocation = glGetUniformLocation(this.programId, "projection");

            if (Constants.OPTIMIZATION_SHADER_MEMORY) {
                int normalPaletteId = glGetUniformBlockIndex(this.programId, "normalPalette");
                glUniformBlockBinding(this.programId, normalPaletteId, 0);

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

                float[] normals = new float[]{
                        0.0f, 0.0f, 1.0f, 1.0f,
                        0.0f, 0.0f, -1.0f, 1.0f,
                        -1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        0.0f, 1.0f, 0.0f, 1.0f,
                        0.0f, -1.0f, 0.0f, 1.0f
                };
                int normalUboId = glGenBuffers();
                glBindBuffer(GL_UNIFORM_BUFFER, normalUboId);
                glBufferData(GL_UNIFORM_BUFFER, normalBuffer, GL_STATIC_DRAW);
                glBindBufferBase(GL_UNIFORM_BUFFER, 0, normalUboId);
            }
        }
    }

    public void setColorUBO() {
        List<Color> colorSource;
        if (Constants.WORLD_EXAMPLE == ExampleType.WORLD_NBT) {
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
            colorBuffer.put(colors[i].r).put(colors[i].g).put(colors[i].b).put(1.0f);
        }

        colorBuffer.flip();

        int colorUbo = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, colorUbo);
        glBufferData(GL_UNIFORM_BUFFER, colorBuffer, GL_STATIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, 1, colorUbo);
    }

    public void update() {
        this.updateStats();
        this.prepare();
    }

    private void updateStats() {
        this.frameCount++;
        double currentFrameTime = glfwGetTime();

        this.deltaTime = currentFrameTime - this.lastFrameTime;
        this.lastFrameTime = currentFrameTime;

        double lastFpsTime = currentFrameTime - this.fpsTimer;

        if (lastFpsTime >= 1) {
            this.fpsTimer = currentFrameTime;
            this.fps = frameCount;
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
}
