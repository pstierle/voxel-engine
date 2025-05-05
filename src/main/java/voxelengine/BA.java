package voxelengine;

import org.joml.Matrix4d;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import voxelengine.core.State;
import voxelengine.util.Constants;
import voxelengine.util.Direction;
import voxelengine.util.Shader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_U;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_LOCK_KEY_MODS;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPolygonMode;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;
import static org.lwjgl.system.MemoryUtil.NULL;

public class BA {
    private static final double CAMERA_SPEED = 10;

    private FloatBuffer verticesBuffer;
    private IntBuffer indicesBuffer;
    private int vboId;
    private int vaoId;
    private int eboId;

    private long windowHandle;
    private int windowWidth = 1600;
    private int windowHeight = 900;
    private final Vector2d mousePosition = new Vector2d((double) this.windowWidth / 2, (double) this.windowHeight / 2);
    private boolean firstMouseMoveHandled = false;
    public boolean wPressed = false;
    public boolean aPressed = false;
    public boolean sPressed = false;
    public boolean dPressed = false;

    private double yaw = 0;
    private double pitch = 0;
    private final Vector3d position = new Vector3d(0, 0, 0);
    private final Vector3d front = new Vector3d(0, 0, -1);
    private final Vector3d up = new Vector3d(0, 1, 0);
    private Matrix4d viewMatrix = new Matrix4d();
    private Matrix4d projectionMatrix = new Matrix4d();

    private int programId;
    private double lastFrameTime;
    private boolean wireframeEnabled = false;
    private double deltaTime = 0;
    private int frameCount;
    private int fps = 0;
    private int viewLocation;
    private int projectionLocation;
    private double fpsTimer = 0;

    private World world;
    private int totalVoxelFaces = 0;

    public void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        this.windowHandle = glfwCreateWindow(this.windowWidth, this.windowHeight, "Voxel Engine", NULL, NULL);

        if (this.windowHandle == NULL) {
            throw new IllegalStateException("Unable to create the GLFW window");
        }

        glfwMakeContextCurrent(this.windowHandle);
        GL.createCapabilities();
        glfwSetInputMode(this.windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetInputMode(this.windowHandle, GLFW_LOCK_KEY_MODS, GLFW_TRUE);

        glfwSetCursorPosCallback(this.windowHandle, (long window, double xpos, double ypos) -> {
            if (!this.firstMouseMoveHandled) {
                this.mousePosition.set(xpos, ypos);
                this.firstMouseMoveHandled = true;
            }

            this.handleMouseMove(this.mousePosition.x, xpos, this.mousePosition.y, ypos);
            this.mousePosition.set(xpos, ypos);
        });

        glfwSetKeyCallback(this.windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
            if (key == GLFW_KEY_U && action == GLFW_RELEASE) {
                State.renderer.toggleWireframe();
            }
            if (key == GLFW_KEY_O && action == GLFW_RELEASE) {
                State.physics.togglePhysics();
            }
        });

        glfwSetFramebufferSizeCallback(this.windowHandle, (long handle, int newWidth, int newHeight) -> {
            this.windowWidth = newWidth;
            this.windowHeight = newHeight;
            glViewport(0, 0, this.windowWidth, this.windowHeight);
        });

        glfwSwapInterval(0);
        glEnable(GL_DEPTH_TEST);

        this.lastFrameTime = glfwGetTime();
        this.programId = glCreateProgram();
        if (Constants.OPTIMIZATION_SHADER_MEMORY) {
            Shader.loadShader(this.programId, "shaders/3D_optimized.fs", GL_FRAGMENT_SHADER);
            Shader.loadShader(this.programId, "shaders/3D_optimized.vs", GL_VERTEX_SHADER);
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
            int normalUboId = glGenBuffers();
            glBindBuffer(GL_UNIFORM_BUFFER, normalUboId);
            glBufferData(GL_UNIFORM_BUFFER, normalBuffer, GL_STATIC_DRAW);
            glBindBufferBase(GL_UNIFORM_BUFFER, 0, normalUboId);
        } else {
            Shader.loadShader(this.programId, "shaders/3D.fs", GL_FRAGMENT_SHADER);
            Shader.loadShader(this.programId, "shaders/3D.vs", GL_VERTEX_SHADER);
        }
        this.viewLocation = glGetUniformLocation(this.programId, "view");
        this.projectionLocation = glGetUniformLocation(this.programId, "projection");

        this.vboId = glGenBuffers();
        this.vaoId = glGenVertexArrays();
        this.eboId = glGenBuffers();

        glUseProgram(programId);

        glBindVertexArray(this.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);

        if (Constants.OPTIMIZATION_SHADER_MEMORY) {
            glVertexAttribPointer(0, 3, GL_FLOAT, false, this.getVoxelFloatPerVertex() * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, 1, GL_FLOAT, false, this.getVoxelFloatPerVertex() * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);

            glVertexAttribPointer(2, 1, GL_FLOAT, false, this.getVoxelFloatPerVertex() * Float.BYTES, 4 * Float.BYTES);
            glEnableVertexAttribArray(2);
        } else {
            glVertexAttribPointer(0, 3, GL_FLOAT, false, this.getVoxelFloatPerVertex() * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, 3, GL_FLOAT, false, this.getVoxelFloatPerVertex() * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);

            glVertexAttribPointer(2, 3, GL_FLOAT, false, this.getVoxelFloatPerVertex() * Float.BYTES, 6 * Float.BYTES);
            glEnableVertexAttribArray(2);
        }

        this.world = new World(DEBUG_WORLD);

        BaseVoxel baseVoxel = new BaseVoxel();
        int verticesIndex = 0;
        this.verticesBuffer = ByteBuffer.allocateDirect(this.countVertices() * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int x = 0; x < world.voxelPositions.length; x++) {
            for (int y = 0; y < world.voxelPositions[x].length; y++) {
                for (int z = 0; z < world.voxelPositions[x][y].length; z++) {
                    Color color = world.voxelPositions[x][y][z];
                    if (color == null) {
                        continue;
                    }
                    for (VoxelFace face : baseVoxel.faces) {
                        int xCheck = x + (int) face.normal.x;
                        int yCheck = y + (int) face.normal.y;
                        int zCheck = z + (int) face.normal.z;

                        Color checkColor = world.voxelPositions[xCheck][yCheck][zCheck];
                        if (checkColor != null) {
                            continue;
                        }
                        totalVoxelFaces++;
                        for (Vector3 position : face.positions) {
                            this.verticesBuffer.put(position.x + x);
                            this.verticesBuffer.put(position.y + y);
                            this.verticesBuffer.put(position.z + z);

                            this.verticesBuffer.put(color.r);
                            this.verticesBuffer.put(color.g);
                            this.verticesBuffer.put(color.b);

                            this.verticesBuffer.put(face.normal.x);
                            this.verticesBuffer.put(face.normal.y);
                            this.verticesBuffer.put(face.normal.z);
                        }
                    }
                }
            }
        }

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer.flip(), GL_STATIC_DRAW);
        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.indicesBuffer.flip(), GL_STATIC_DRAW);
        }
        this.verticesBuffer = null;
        this.indicesBuffer = null;

        //glBindVertexArray(this.vaoId);
    }

    public void run() {
        while (!glfwWindowShouldClose(windowHandle)) {
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

            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (this.wireframeEnabled) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            } else {
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            }

            glfwPollEvents();

            if (glfwGetKey(this.windowHandle, GLFW_KEY_W) == GLFW_PRESS) {
                Vector3d intermediate = new Vector3d();
                this.front.mul(CAMERA_SPEED * deltaTime, intermediate);
                this.position.add(intermediate);
            }
            if (glfwGetKey(this.windowHandle, GLFW_KEY_S) == GLFW_PRESS) {
                Vector3d intermediate = new Vector3d();
                this.front.mul(CAMERA_SPEED * deltaTime, intermediate);
                this.position.sub(intermediate);
            }
            if (glfwGetKey(this.windowHandle, GLFW_KEY_A) == GLFW_PRESS) {
                Vector3d left = new Vector3d();
                this.front.cross(this.up, left).normalize();
                left.mul(CAMERA_SPEED * deltaTime, left);
                this.position.sub(left);
            }
            if (glfwGetKey(this.windowHandle, GLFW_KEY_D) == GLFW_PRESS) {
                Vector3d right = new Vector3d();
                this.front.cross(this.up, right).normalize();
                right.mul(CAMERA_SPEED * deltaTime, right);
                this.position.add(right);
            }

            if (glfwGetKey(this.windowHandle, GLFW_KEY_W) == GLFW_RELEASE) {
                this.wPressed = false;
            }
            if (glfwGetKey(this.windowHandle, GLFW_KEY_S) == GLFW_RELEASE) {
                this.sPressed = false;
            }
            if (glfwGetKey(this.windowHandle, GLFW_KEY_A) == GLFW_RELEASE) {
                this.aPressed = false;
            }
            if (glfwGetKey(this.windowHandle, GLFW_KEY_D) == GLFW_RELEASE) {
                this.dPressed = false;
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

            double aspectRatio = (double) windowWidth / windowHeight;
            this.projectionMatrix.perspective(Math.toRadians(Constants.CAMERA_FOV), aspectRatio, 0.1, 10000);

            FloatBuffer viewDest = BufferUtils.createFloatBuffer(16);
            this.viewMatrix.get(viewDest);
            glUniformMatrix4fv(viewLocation, false, viewDest);

            FloatBuffer projectionDest = BufferUtils.createFloatBuffer(16);
            this.projectionMatrix.get(projectionDest);
            glUniformMatrix4fv(projectionLocation, false, projectionDest);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            int vertexCount;

            if (Constants.OPTIMIZATION_GREEDY_MESHING) {
                // In greedy meshing, each face creates exactly 6 vertices (2 triangles)
                vertexCount = totalVoxelFaces * 6;
            } else {
                // In normal meshing, we use the standard constant
                vertexCount = Constants.VOXEL_FACE_VERTICES_COUNT * totalVoxelFaces;
            }

            glDrawArrays(GL_TRIANGLES, 0, vertexCount);

            glfwSwapBuffers(windowHandle);
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new BA().run();
    }

    private static class World {
        Color[][][] voxelPositions;
        public final int numVoxels;

        public World(Color[][][] voxelPositions) {
            this.voxelPositions = voxelPositions;
            int voxelCount = 0;
            for (int x = 0; x < voxelPositions.length; x++) {
                for (int y = 0; y < voxelPositions[x].length; y++) {
                    for (int z = 0; z < voxelPositions[x][y].length; z++) {
                        if (voxelPositions[x][y][z] != null) {
                            voxelCount++;
                        }
                    }
                }
            }
            this.numVoxels = voxelCount;
        }
    }

    private static class Color {
        public final float r;
        public final float g;
        public final float b;

        public Color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    private static class BaseVoxel {
        public final VoxelFace[] faces;

        public BaseVoxel() {
            // Normale für jede Seite
            Vector3 frontNormal = new Vector3(0, 0, 1);    // Vorne (Positive Z-Achse)
            Vector3 backNormal = new Vector3(0, 0, -1);    // Hinten (Negative Z-Achse)
            Vector3 leftNormal = new Vector3(-1, 0, 0);    // Links (Negative X-Achse)
            Vector3 rightNormal = new Vector3(1, 0, 0);    // Rechts (Positive X-Achse)
            Vector3 topNormal = new Vector3(0, 1, 0);      // Oben (Positive Y-Achse)
            Vector3 bottomNormal = new Vector3(0, -1, 0);  // Unten (Negative Y-Achse)

            // Positionen für jede Seite
            // Vorne (Positive Z-Achse)
            Vector3[] frontPositions = {
                    new Vector3(-0.5f, -0.5f, 0.5f),  // Unten links
                    new Vector3(0.5f, -0.5f, 0.5f),   // Unten rechts
                    new Vector3(-0.5f, 0.5f, 0.5f),   // Oben links
                    new Vector3(0.5f, -0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, 0.5f),    // Oben rechts
                    new Vector3(-0.5f, 0.5f, 0.5f)    // Oben links
            };

            // Hinten (Negative Z-Achse)
            Vector3[] backPositions = {
                    new Vector3(-0.5f, -0.5f, -0.5f), // Unten links
                    new Vector3(-0.5f, 0.5f, -0.5f),  // Oben links
                    new Vector3(0.5f, -0.5f, -0.5f),  // Unten rechts
                    new Vector3(-0.5f, 0.5f, -0.5f),  // Oben links
                    new Vector3(0.5f, 0.5f, -0.5f),   // Oben rechts
                    new Vector3(0.5f, -0.5f, -0.5f)   // Unten rechts
            };

            // Links (Negative X-Achse)
            Vector3[] leftPositions = {
                    new Vector3(-0.5f, -0.5f, -0.5f), // Unten links
                    new Vector3(-0.5f, -0.5f, 0.5f),  // Unten rechts
                    new Vector3(-0.5f, 0.5f, -0.5f),  // Oben links
                    new Vector3(-0.5f, -0.5f, 0.5f),  // Unten rechts
                    new Vector3(-0.5f, 0.5f, 0.5f),   // Oben rechts
                    new Vector3(-0.5f, 0.5f, -0.5f)   // Oben links
            };

            // Rechts (Positive X-Achse)
            Vector3[] rightPositions = {
                    new Vector3(0.5f, -0.5f, -0.5f),  // Unten links
                    new Vector3(0.5f, 0.5f, -0.5f),   // Oben links
                    new Vector3(0.5f, -0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, -0.5f),   // Oben links
                    new Vector3(0.5f, -0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, 0.5f)     // Oben rechts
            };

            // Oben (Positive Y-Achse)
            Vector3[] topPositions = {
                    new Vector3(-0.5f, 0.5f, -0.5f),  // Unten links
                    new Vector3(-0.5f, 0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, -0.5f),   // Oben links
                    new Vector3(-0.5f, 0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, 0.5f),    // Oben rechts
                    new Vector3(0.5f, 0.5f, -0.5f)    // Oben links
            };

            // Unten (Negative Y-Achse)
            Vector3[] bottomPositions = {
                    new Vector3(-0.5f, -0.5f, -0.5f), // Unten links
                    new Vector3(0.5f, -0.5f, -0.5f),  // Unten rechts
                    new Vector3(-0.5f, -0.5f, 0.5f),  // Oben links
                    new Vector3(0.5f, -0.5f, -0.5f),  // Unten rechts
                    new Vector3(0.5f, -0.5f, 0.5f),   // Oben rechts
                    new Vector3(-0.5f, -0.5f, 0.5f)   // Oben links
            };

            this.faces = new VoxelFace[]{
                    new VoxelFace(frontNormal, frontPositions),
                    new VoxelFace(backNormal, backPositions),
                    new VoxelFace(leftNormal, leftPositions),
                    new VoxelFace(rightNormal, rightPositions),
                    new VoxelFace(topNormal, topPositions),
                    new VoxelFace(bottomNormal, bottomPositions)
            };
        }
    }

    private static class VoxelFace {
        public final Vector3 normal;
        public final Vector3[] positions;

        public VoxelFace(Vector3 normal, Vector3[] positions) {
            this.normal = normal;
            this.positions = positions;
        }
    }

    private static class Vector3 {
        public final float x;
        public final float y;
        public final float z;

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private void initWindow() {

    }

    private void handleMouseMove(double oldX, double newX, double oldY, double newY) {
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

    private int countVertices() {
        int voxelFaceVerticesCount = Constants.VOXEL_FACE_VERTICES_COUNT;
        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            voxelFaceVerticesCount = Constants.VOXEL_FACE_VERTICES_COUNT_INSTANCED;
        }
        return this.world.numVoxels * Constants.VOXEL_FACES_COUNT
                * voxelFaceVerticesCount
                * this.getVoxelFloatPerVertex();
    }

    private int getVoxelFloatPerVertex() {
        return Constants.OPTIMIZATION_SHADER_MEMORY ? Constants.VOXEL_FLOAT_PER_VERTEX_OPTIMIZATION_SHADER_MEMORY : Constants.VOXEL_FLOAT_PER_VERTEX;
    }

    private static final Color[][][] DEBUG_WORLD = new Color[][][]{
            // x=0
            {
                    // y=0
                    {
                            new Color(1.0f, 0.0f, 0.0f),  // (x=0, y=0, z=0)
                            new Color(1.0f, 0.0f, 0.0f)   // (x=0, y=0, z=1)
                    },
                    // y=1
                    {
                            new Color(0.0f, 0.0f, 1.0f),                                  // (x=0, y=1, z=0)
                            null   // (x=0, y=1, z=1)
                    }
            },
            // x=1
            {
                    // y=0
                    {
                            new Color(1.0f, 0.0f, 0.0f),  // (x=1, y=0, z=0)
                            new Color(1.0f, 0.0f, 0.0f)   // (x=1, y=0, z=1)
                    },
                    // y=1
                    {
                            new Color(0.0f, 0.0f, 1.0f),                                  // (x=1, y=1, z=0)
                            null   // (x=1, y=1, z=1)
                    }
            }
    };
}
