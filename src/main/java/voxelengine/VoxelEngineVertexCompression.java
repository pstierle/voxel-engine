package voxelengine;

import org.lwjgl.opengl.GL;
import voxelengine.util.Log;
import voxelengine.worldgen.NbtUtil;

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
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
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
import static org.lwjgl.system.MemoryUtil.NULL;
import static voxelengine.VoxelEngineUtil.BACK_NORMAL;
import static voxelengine.VoxelEngineUtil.BOTTOM_NORMAL;
import static voxelengine.VoxelEngineUtil.FRONT_NORMAL;
import static voxelengine.VoxelEngineUtil.LEFT_NORMAL;
import static voxelengine.VoxelEngineUtil.RIGHT_NORMAL;
import static voxelengine.VoxelEngineUtil.TOP_NORMAL;
import static voxelengine.VoxelEngineUtil.Vector2;
import static voxelengine.VoxelEngineUtil.Vector3;

public class VoxelEngineVertexCompression {
    public static void main(String[] args) {
        VoxelEngineVertexCompression voxelEngine = new VoxelEngineVertexCompression();

        voxelEngine.initGLFW();
        voxelEngine.initOpenGL();
        voxelEngine.initShaders();
        voxelEngine.initVertices();

        while (!glfwWindowShouldClose(voxelEngine.getWindowHandle())) {
            voxelEngine.handleInput();
            voxelEngine.update();
            voxelEngine.render();
        }

        voxelEngine.cleanUp();
    }

    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_SPEED = 10;
    private long windowHandle;
    private int windowWidth = 1600;
    private int windowHeight = 900;
    private final Vector2 mousePosition = new Vector2((float) this.windowWidth / 2, (float) this.windowHeight / 2);
    private boolean firstMouseMoveHandled = false;
    private float cameraYaw = 0;
    private float cameraPitch = 0;
    private final Vector3 cameraPosition = new Vector3(355.0f, 62.0f, 100.0f);
    private final Vector3 cameraFront = new Vector3(0, 0, -1);
    private final Vector3 cameraUp = new Vector3(0, 1, 0);
    private int viewLocation;
    private int projectionLocation;
    private int verticesCount = 0;
    private float deltaTime = 0;
    private float lastFrameTime = 0;
    private final World world;

    VoxelEngineVertexCompression() {
        final NbtUtil.WorldData worldData = new NbtUtil().loadNbtWorld(false);
        this.world = new World(worldData.chunks);
    }

    public long getWindowHandle() {
        return this.windowHandle;
    }

    public void initVertices() {
        final int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        final int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        int voxelCount = 0;

        for (int i = 0; i < world.chunks.size(); i++) {
            VoxelEngineUtil.Chunk chunk = world.chunks.get(i);
            for (int x = 0; x < chunk.data.length; x++) {
                for (int y = 0; y < chunk.data[x].length; y++) {
                    for (int z = 0; z < chunk.data[x][y].length; z++) {
                        if (chunk.data[x][y][z] == null) {
                            continue;
                        }
                        voxelCount++;
                    }
                }
            }
        }

        Log.info(String.format("Voxel count: %d", voxelCount));

        BaseVoxel baseVoxel = new BaseVoxel();

        int vertexCount = voxelCount * 36 * 9 /* estimate or calculate actual number of vertices */;
        final float[] vertices = new float[vertexCount];
        int verticesIndex = 0;

        for (int k = 0; k < world.chunks.size(); k++) {
            VoxelEngineUtil.Chunk chunk = world.chunks.get(k);
            for (int x = 0; x < chunk.data.length; x++) {
                for (int y = 0; y < chunk.data[x].length; y++) {
                    for (int z = 0; z < chunk.data[x][y].length; z++) {
                        Object voxel = chunk.data[x][y][z];
                        if (voxel == null) {
                            continue;
                        }
                        for (int i = 0; i < baseVoxel.faces.length; i++) {
                            VoxelFace face = baseVoxel.faces[i];
                            for (int j = 0; j < face.vertexPositions.length; j++) {
                                verticesCount++;

                                Vector3 vertexPosition = face.vertexPositions[j];

                                vertices[verticesIndex++] = vertexPosition.x + x + chunk.xOffset;
                                vertices[verticesIndex++] = vertexPosition.y + y + chunk.yOffset;
                                vertices[verticesIndex++] = vertexPosition.z + z + chunk.zOffset;

                                vertices[verticesIndex++] = ((VoxelEngineUtil.Color) chunk.data[x][y][z]).r;
                                vertices[verticesIndex++] = ((VoxelEngineUtil.Color) chunk.data[x][y][z]).g;
                                vertices[verticesIndex++] = ((VoxelEngineUtil.Color) chunk.data[x][y][z]).b;

                                vertices[verticesIndex++] = face.normal.x;
                                vertices[verticesIndex++] = face.normal.y;
                                vertices[verticesIndex++] = face.normal.z;
                            }
                        }
                    }
                }
            }
        }

        Log.info(String.format("Vertex count: %d", verticesIndex / 9));

        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        final int vertexSizeInBytes = 9 * Float.BYTES;

        glVertexAttribPointer(0, 3, GL_FLOAT, false, vertexSizeInBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, vertexSizeInBytes, (long) 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 3, GL_FLOAT, false, vertexSizeInBytes, (long) 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    private void render() {
        glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glDrawArrays(GL_TRIANGLES, 0, this.verticesCount);

        glfwSwapBuffers(this.windowHandle);
        glfwPollEvents();
    }


    private void update() {
        float currentFrameTime = (float) glfwGetTime();

        deltaTime = currentFrameTime - this.lastFrameTime;
        this.lastFrameTime = currentFrameTime;

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

        float[] viewMatrixData = new float[]{
                xAxisX,      // m00
                yAxisX,      // m10
                zAxisX,      // m20
                0.0f,        // m30

                xAxisY,      // m01
                yAxisY,      // m11
                zAxisY,      // m21
                0.0f,        // m31

                xAxisZ,      // m02
                yAxisZ,      // m12
                zAxisZ,      // m22
                0.0f,        // m32

                tX,          // m03
                tY,          // m13
                tZ,          // m23
                1.0f         // m33
        };

        glUniformMatrix4fv(this.viewLocation, false, viewMatrixData);
    }

    private void handleInput() {
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

    public void initShaders() {
        final int programId = glCreateProgram();

        int vertexShaderId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShaderId, """
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
                """);
        glCompileShader(vertexShaderId);
        glAttachShader(programId, vertexShaderId);
        glLinkProgram(programId);
        glDeleteShader(vertexShaderId);

        int fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderId, """
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
                """);
        glCompileShader(fragmentShaderId);
        glAttachShader(programId, fragmentShaderId);
        glLinkProgram(programId);
        glDeleteShader(fragmentShaderId);

        this.viewLocation = glGetUniformLocation(programId, "view");
        this.projectionLocation = glGetUniformLocation(programId, "projection");

        glUseProgram(programId);
        this.setProjectionMatrix();


    }

    private void setProjectionMatrix() {
        float fov = 90.0f;
        float aspectRatio = (float) this.windowWidth / this.windowHeight;
        float nearPlane = 0.1f;
        float farPlane = 10000.0f;

        float test = (float) 1 / (float) Math.tan((double) fov / 2);

        final float[] projectionMatrixData = new float[]{
                test / aspectRatio,                // m00
                0.0f,                           // m10
                0.0f,                           // m20
                0.0f,                           // m30

                0.0f,                           // m01
                test,                              // m11
                0.0f,                           // m21
                0.0f,                           // m31

                0.0f,                           // m02
                0.0f,                           // m12
                farPlane / (nearPlane - farPlane),    // m22
                -1.0f,                          // m32

                0.0f,                           // m03
                0.0f,                           // m13
                (nearPlane * farPlane) / (nearPlane - farPlane),// m23
                0.0f                            // m33
        };

        glUniformMatrix4fv(projectionLocation, false, projectionMatrixData);
    }

    private void cleanUp() {
        glfwFreeCallbacks(this.windowHandle);
        glfwDestroyWindow(this.windowHandle);
        glfwTerminate();
    }

    private static class BaseVoxel {
        public final VoxelFace[] faces;

        public BaseVoxel() {
            Vector3[] frontPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f)
            };

            Vector3[] backPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f)
            };

            Vector3[] leftPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f)
            };

            Vector3[] rightPositions = new Vector3[]{
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f)
            };

            Vector3[] topPositions = new Vector3[]{
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f)
            };

            Vector3[] bottomPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f)
            };
            this.faces = new VoxelFace[]{
                    new VoxelFace(FRONT_NORMAL, frontPositions),
                    new VoxelFace(BACK_NORMAL, backPositions),
                    new VoxelFace(LEFT_NORMAL, leftPositions),
                    new VoxelFace(RIGHT_NORMAL, rightPositions),
                    new VoxelFace(TOP_NORMAL, topPositions),
                    new VoxelFace(BOTTOM_NORMAL, bottomPositions)
            };
        }
    }

    private static class VoxelFace {
        public final Vector3 normal;
        public final Vector3[] vertexPositions;

        public VoxelFace(Vector3 normal, Vector3[] vertexPositions) {
            this.normal = normal;
            this.vertexPositions = vertexPositions;
        }
    }

    private static class World {
        public List<VoxelEngineUtil.Chunk> chunks;

        public World(List<VoxelEngineUtil.Chunk> chunks) {
            this.chunks = chunks;
        }
    }
}
