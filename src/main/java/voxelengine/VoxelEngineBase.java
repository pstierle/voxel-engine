package voxelengine;

import org.lwjgl.opengl.GL;

import voxelengine.core.ImGUI;
import voxelengine.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR;
import static org.lwjgl.glfw.GLFW.GLFW_CURSOR_DISABLED;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_LOCK_KEY_MODS;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
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
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import static org.lwjgl.system.MemoryUtil.NULL;

public class VoxelEngineBase {
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_SPEED = 10;
    protected long windowHandle;
    protected int programId;
    private int windowWidth = 1600;
    private int windowHeight = 900;
    private final VoxelEngineUtil.Vector2 mousePosition = new VoxelEngineUtil.Vector2((float) this.windowWidth / 2,
            (float) this.windowHeight / 2);
    private boolean firstMouseMoveHandled = false;
    private float cameraYaw = 0;
    private float cameraPitch = 0;
    private final VoxelEngineUtil.Vector3 cameraPosition = new VoxelEngineUtil.Vector3(355.0f, 62.0f, 100.0f);
    private final VoxelEngineUtil.Vector3 cameraFront = new VoxelEngineUtil.Vector3(0, 0, -1);
    private final VoxelEngineUtil.Vector3 cameraUp = new VoxelEngineUtil.Vector3(0, 1, 0);
    private int viewLocation;
    private int projectionLocation;
    private int verticesCount = 0;
    private float deltaTime = 0;
    private float lastFrameTime = 0;
    private int frames = 0;
    private int fps = 0;
    private float fpsTimeAccumulator = 0f;
    private final Runtime runtime = Runtime.getRuntime();
    private final ImGUI imGui = new ImGUI();
    protected List<VoxelEngineUtil.Chunk> chunks;
    protected int voxelCount = 0;
    protected ByteBuffer verticesBuffer;
    private int verticesByteSize = 0;

    protected VoxelEngineBase(List<VoxelEngineUtil.Chunk> chunks) {
        this.chunks = chunks;
        updateVerticesBuffer();
    }

    public void updateVerticesBuffer() {
        for (var chunk : this.chunks) {
            for (int x = 0; x < chunk.data.length; x++) {
                for (int y = 0; y < chunk.data[x].length; y++) {
                    for (int z = 0; z < chunk.data[x][y].length; z++) {
                        if (chunk.data[x][y][z] != null)
                            voxelCount++;
                    }
                }
            }
        }
        Log.info("Voxel count: " + voxelCount);
        verticesBuffer = ByteBuffer.allocateDirect(voxelCount * verticesPerVoxel() * vertexByteSize())
                .order(ByteOrder.nativeOrder());
    }

    public int vertexByteSize() {
        // 9 values per vertex, each value is a float (4 bytes)
        return 36;
    }

    public int verticesPerVoxel() {
        // 6 voxel sides, 6 vertices per side
        return 36;
    }

    public long getWindowHandle() {
        return windowHandle;
    }

    public void initVertices() {
        final int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        final int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        VoxelEngineUtil.BaseVoxel baseVoxel = new VoxelEngineUtil.BaseVoxel();

        for (int k = 0; k < this.chunks.size(); k++) {
            VoxelEngineUtil.Chunk chunk = this.chunks.get(k);
            for (int x = 0; x < chunk.data.length; x++) {
                for (int y = 0; y < chunk.data[x].length; y++) {
                    for (int z = 0; z < chunk.data[x][y].length; z++) {
                        Object voxel = chunk.data[x][y][z];
                        if (voxel == null) {
                            continue;
                        }
                        for (int i = 0; i < baseVoxel.faces.length; i++) {
                            VoxelEngineUtil.VoxelFace face = baseVoxel.faces[i];
                            for (int j = 0; j < face.vertexPositions().length; j++) {
                                VoxelEngineUtil.Vector3 vertexPosition = face.vertexPositions()[j];

                                verticesBuffer.putFloat(vertexPosition.x + x + chunk.xOffset);
                                verticesBuffer.putFloat(vertexPosition.y + y + chunk.yOffset);
                                verticesBuffer.putFloat(vertexPosition.z + z + chunk.zOffset);

                                verticesBuffer.putFloat(((VoxelEngineUtil.Color) chunk.data[x][y][z]).r);
                                verticesBuffer.putFloat(((VoxelEngineUtil.Color) chunk.data[x][y][z]).g);
                                verticesBuffer.putFloat(((VoxelEngineUtil.Color) chunk.data[x][y][z]).b);

                                verticesBuffer.putFloat(face.normal().x);
                                verticesBuffer.putFloat(face.normal().y);
                                verticesBuffer.putFloat(face.normal().z);
                            }
                        }
                    }
                }
            }
        }

        readStatsFromVerticesBuffer();

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, vertexByteSize(), 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, vertexByteSize(), (long) 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 3, GL_FLOAT, false, vertexByteSize(), (long) 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    protected void readStatsFromVerticesBuffer() {
        verticesBuffer.flip();
        verticesByteSize = verticesBuffer.remaining();
        verticesCount = verticesByteSize / vertexByteSize();
    }

    public void render() {
        glDrawArrays(GL_TRIANGLES, 0, this.verticesCount);
    }

    protected void renderPrepare() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    protected void renderCleanup() {
        glfwSwapBuffers(this.windowHandle);
        glfwPollEvents();
    }

    public void update() {
        // glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        float currentFrameTime = (float) glfwGetTime();
        deltaTime = currentFrameTime - this.lastFrameTime;
        this.lastFrameTime = currentFrameTime;

        frames++;
        fpsTimeAccumulator += deltaTime;

        if (fpsTimeAccumulator >= 1.0f) {
            fps = frames;
            fpsTimeAccumulator = 0f;
            frames = 0;
        }

        double dirX = Math.cos(Math.toRadians(this.cameraYaw)) * Math.cos(Math.toRadians(this.cameraPitch));
        double dirY = Math.sin(Math.toRadians(this.cameraPitch));
        double dirZ = Math.sin(Math.toRadians(this.cameraYaw)) * Math.cos(Math.toRadians(this.cameraPitch));

        double length = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX /= length;
        dirY /= length;
        dirZ /= length;

        this.cameraFront.x = (float) dirX;
        this.cameraFront.y = (float) dirY;
        this.cameraFront.z = (float) dirZ;

        float zAxisX = -this.cameraFront.x;
        float zAxisY = -this.cameraFront.y;
        float zAxisZ = -this.cameraFront.z;

        float xAxisX = this.cameraUp.y * zAxisZ - this.cameraUp.z * zAxisY;
        float xAxisY = this.cameraUp.z * zAxisX - this.cameraUp.x * zAxisZ;
        float xAxisZ = this.cameraUp.x * zAxisY - this.cameraUp.y * zAxisX;

        length = Math.sqrt(xAxisX * xAxisX + xAxisY * xAxisY + xAxisZ * xAxisZ);
        xAxisX /= (float) length;
        xAxisY /= (float) length;
        xAxisZ /= (float) length;

        float yAxisX = zAxisY * xAxisZ - zAxisZ * xAxisY;
        float yAxisY = zAxisZ * xAxisX - zAxisX * xAxisZ;
        float yAxisZ = zAxisX * xAxisY - zAxisY * xAxisX;

        float tX = -(xAxisX * this.cameraPosition.x + xAxisY * this.cameraPosition.y + xAxisZ * this.cameraPosition.z);
        float tY = -(yAxisX * this.cameraPosition.x + yAxisY * this.cameraPosition.y + yAxisZ * this.cameraPosition.z);
        float tZ = -(zAxisX * this.cameraPosition.x + zAxisY * this.cameraPosition.y + zAxisZ * this.cameraPosition.z);

        float[] viewMatrixData = new float[] {
                xAxisX, // m00
                yAxisX, // m10
                zAxisX, // m20
                0.0f, // m30

                xAxisY, // m01
                yAxisY, // m11
                zAxisY, // m21
                0.0f, // m31

                xAxisZ, // m02
                yAxisZ, // m12
                zAxisZ, // m22
                0.0f, // m32

                tX, // m03
                tY, // m13
                tZ, // m23
                1.0f // m33
        };

        glUniformMatrix4fv(this.viewLocation, false, viewMatrixData);
    }

    public void handleInput() {
        glfwPollEvents();

        if (glfwGetKey(this.windowHandle, GLFW_KEY_W) == GLFW_PRESS) {
            float moveX = this.cameraFront.x * CAMERA_SPEED * deltaTime;
            float moveY = this.cameraFront.y * CAMERA_SPEED * deltaTime;
            float moveZ = this.cameraFront.z * CAMERA_SPEED * deltaTime;

            this.cameraPosition.x += moveX;
            this.cameraPosition.y += moveY;
            this.cameraPosition.z += moveZ;
        }

        if (glfwGetKey(this.windowHandle, GLFW_KEY_S) == GLFW_PRESS) {
            float moveX = this.cameraFront.x * CAMERA_SPEED * deltaTime;
            float moveY = this.cameraFront.y * CAMERA_SPEED * deltaTime;
            float moveZ = this.cameraFront.z * CAMERA_SPEED * deltaTime;

            this.cameraPosition.x -= moveX;
            this.cameraPosition.y -= moveY;
            this.cameraPosition.z -= moveZ;
        }

        if (glfwGetKey(this.windowHandle, GLFW_KEY_A) == GLFW_PRESS) {
            float leftX = this.cameraFront.y * this.cameraUp.z - this.cameraFront.z * this.cameraUp.y;
            float leftY = this.cameraFront.z * this.cameraUp.x - this.cameraFront.x * this.cameraUp.z;
            float leftZ = this.cameraFront.x * this.cameraUp.y - this.cameraFront.y * this.cameraUp.x;

            double length = Math.sqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
            leftX /= (float) length;
            leftY /= (float) length;
            leftZ /= (float) length;

            float moveX = leftX * CAMERA_SPEED * deltaTime;
            float moveY = leftY * CAMERA_SPEED * deltaTime;
            float moveZ = leftZ * CAMERA_SPEED * deltaTime;

            this.cameraPosition.x -= moveX;
            this.cameraPosition.y -= moveY;
            this.cameraPosition.z -= moveZ;
        }

        if (glfwGetKey(this.windowHandle, GLFW_KEY_D) == GLFW_PRESS) {
            float rightX = this.cameraFront.y * this.cameraUp.z - this.cameraFront.z * this.cameraUp.y;
            float rightY = this.cameraFront.z * this.cameraUp.x - this.cameraFront.x * this.cameraUp.z;
            float rightZ = this.cameraFront.x * this.cameraUp.y - this.cameraFront.y * this.cameraUp.x;

            double length = Math.sqrt(rightX * rightX + rightY * rightY + rightZ * rightZ);
            rightX /= (float) length;
            rightY /= (float) length;
            rightZ /= (float) length;

            float moveX = rightX * CAMERA_SPEED * deltaTime;
            float moveY = rightY * CAMERA_SPEED * deltaTime;
            float moveZ = rightZ * CAMERA_SPEED * deltaTime;

            this.cameraPosition.x += moveX;
            this.cameraPosition.y += moveY;
            this.cameraPosition.z += moveZ;
        }
    }

    public void initGLFW() {
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        this.windowHandle = glfwCreateWindow(this.windowWidth, this.windowHeight, "Voxel Engine", NULL, NULL);

        glfwMakeContextCurrent(this.windowHandle);
        glfwSetInputMode(this.windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        glfwSetInputMode(this.windowHandle, GLFW_LOCK_KEY_MODS, GLFW_TRUE);

        glfwSetCursorPosCallback(this.windowHandle, (long window, double newX, double newY) -> {
            if (!this.firstMouseMoveHandled) {
                this.mousePosition.x = (float) newX;
                this.mousePosition.y = (float) newY;
                this.firstMouseMoveHandled = true;
            }

            float xOffset = (float) newX - this.mousePosition.x;
            float yOffset = this.mousePosition.y - (float) newY;

            xOffset *= MOUSE_SENSITIVITY;
            yOffset *= MOUSE_SENSITIVITY;

            this.cameraYaw += xOffset;
            this.cameraPitch += yOffset;

            if (this.cameraPitch > 89.0f) {
                this.cameraPitch = 89.0f;
            }

            if (this.cameraPitch < -89.0f) {
                this.cameraPitch = -89.0f;
            }

            this.mousePosition.x = (float) newX;
            this.mousePosition.y = (float) newY;
        });

        glfwSetKeyCallback(this.windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }
        });

        glfwSetFramebufferSizeCallback(this.windowHandle, (long handle, int newWidth, int newHeight) -> {
            this.windowWidth = newWidth;
            this.windowHeight = newHeight;
            glViewport(0, 0, this.windowWidth, this.windowHeight);
            this.setProjectionMatrix();
        });
    }

    public void initOpenGL() {
        GL.createCapabilities();
        glfwSwapInterval(0);
        glEnable(GL_DEPTH_TEST);
        glfwSetInputMode(this.windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    private void setProjectionMatrix() {
        float fov = 90.0f;
        float aspectRatio = (float) this.windowWidth / this.windowHeight;
        float nearPlane = 0.1f;
        float farPlane = 10000.0f;

        float test = 1 / (float) Math.tan((double) fov / 2);

        final float[] projectionMatrixData = new float[] {
                test / aspectRatio, // m00
                0.0f, // m10
                0.0f, // m20
                0.0f, // m30

                0.0f, // m01
                test, // m11
                0.0f, // m21
                0.0f, // m31

                0.0f, // m02
                0.0f, // m12
                farPlane / (nearPlane - farPlane), // m22
                -1.0f, // m32

                0.0f, // m03
                0.0f, // m13
                (nearPlane * farPlane) / (nearPlane - farPlane), // m23
                0.0f // m33
        };

        glUniformMatrix4fv(projectionLocation, false, projectionMatrixData);
    }

    public String vertexShaderSource() {
        return """
                    #version 330 core

                    layout(location = 0) in vec3 aPos;
                    layout(location = 1) in vec3 aColor;
                    layout(location = 2) in vec3 aNormal;

                    uniform mat4 view;
                    uniform mat4 projection;

                    out vec3 color;
                    out vec3 normal;

                    void main() {
                        gl_Position = projection * view * vec4(aPos, 1.0);
                        color = aColor;
                        normal = aNormal;
                    }
                """;
    }

    public String fragmentShaderSource() {
        return """
                    #version 330 core

                    in vec3 color;
                    in vec3 normal;

                    out vec4 fragmentColor;

                    void main() {
                        float brightness = 1.0;

                        if (normal.y > 0.5) {
                            brightness = 1.0;
                        } else if (normal.y < -0.5) {
                            brightness = 0.5;
                        } else {
                            brightness = 0.8;
                        }

                        vec3 litColor = color * brightness;
                        fragmentColor = vec4(litColor, 1.0);
                    }
                """;
    }

    public void initShaders() {
        programId = glCreateProgram();

        int vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderId, vertexShaderSource());
        glCompileShader(vertexShaderId);
        glAttachShader(programId, vertexShaderId);
        glLinkProgram(programId);
        glDeleteShader(vertexShaderId);

        int fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderId, fragmentShaderSource());
        glCompileShader(fragmentShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);
        glDeleteShader(fragmentShaderId);
        glUseProgram(programId);

        this.viewLocation = glGetUniformLocation(programId, "view");
        this.projectionLocation = glGetUniformLocation(programId, "projection");

        this.setProjectionMatrix();
    }

    public void cleanUp() {
        glfwFreeCallbacks(this.windowHandle);
        glfwDestroyWindow(this.windowHandle);
        glfwTerminate();
    }

    public void renderStats() {
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        int gpuMemoryUsedKB = 0;
        try {
            int gpuMemoryTotalKB = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
            int gpuMemoryAvailableKB = glGetInteger(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
            gpuMemoryUsedKB = gpuMemoryTotalKB - gpuMemoryAvailableKB;
        } catch (Exception e) {
            Log.error("GPU memory info not available (extension may not be supported).");
        }
        imGui.render(className, cameraPosition.x, cameraPosition.y, cameraPosition.z, fps, voxelCount, verticesCount,
                verticesByteSize, usedMemory, gpuMemoryUsedKB);
    }

    private String className = "";

    public void initImGUI(String className) {
        this.className = className;
        imGui.init(windowHandle);
    }

    private int glGetInteger(int name) {
        int[] buffer = new int[1];
        glGetIntegerv(name, buffer);
        return buffer[0];
    }
}
