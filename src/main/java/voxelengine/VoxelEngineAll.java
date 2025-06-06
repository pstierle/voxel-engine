package voxelengine;

import voxelengine.worldgen.NbtUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Collections;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_HALF_FLOAT;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;
import static voxelengine.VoxelEngineUtil.NORMALS;
import static voxelengine.VoxelEngineUtil.Vector3;

public class VoxelEngineAll extends VoxelEngineBase {
    final NbtUtil.WorldData worldData = new NbtUtil().loadNbtWorld(true);
    private int indicesCount = 0;
    private int verticesOffset = 0;

    private IntBuffer indicesBuffer;

    VoxelEngineAll() {
        super(Collections.emptyList());
        chunks = worldData.chunks;
    }

    @Override
    public int vertexByteSize() {
        // 5 floats values per vertex, each value is a half float (2 bytes)
        return 10;
    }

    @Override
    public int verticesPerVoxel() {
        // 6 voxel sides, 4 vertices per side
        return 24;
    }

    @Override
    public void initVertices() {
        updateVerticesBuffer();

        indicesBuffer = ByteBuffer.allocateDirect(voxelCount * 6 * 6 * Integer.BYTES)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();

        final int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        final int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        for (VoxelEngineUtil.Chunk chunk : this.chunks) {
            for (int axis = 0; axis < 3; axis++) {
                int u = (axis + 1) % 3;
                int v = (axis + 2) % 3;

                int[] x = new int[3];
                int[] q = new int[3];

                boolean[] mask = new boolean[48 * 48];
                Integer[] colorMask = new Integer[48 * 48];

                q[axis] = 1;

                // Sweep through each slice along the axis
                for (x[axis] = -1; x[axis] < 48; ) {
                    int n = 0;

                    // Generate mask for this slice
                    for (x[v] = 0; x[v] < 48; x[v]++) {
                        for (x[u] = 0; x[u] < 48; x[u]++) {
                            Integer voxel1 = getVoxel(chunk, x[0], x[1], x[2]);
                            Integer voxel2 = getVoxel(chunk, x[0] + q[0], x[1] + q[1], x[2] + q[2]);

                            boolean face1Solid = voxel1 != null;
                            boolean face2Solid = voxel2 != null;

                            // Check if we need a face between these two voxels
                            if (face1Solid != face2Solid) {
                                if (face1Solid) {
                                    mask[n] = true;
                                    colorMask[n] = voxel1;
                                } else {
                                    mask[n] = false;
                                    colorMask[n] = null;
                                }
                            } else {
                                mask[n] = false;
                                colorMask[n] = null;
                            }
                            n++;
                        }
                    }

                    x[axis]++;
                    n = 0;

                    // Generate mesh from mask using greedy meshing
                    for (int j = 0; j < 48; j++) {
                        for (int i = 0; i < 48; ) {
                            if (mask[n]) {
                                Integer colorIndex = colorMask[n];

                                // Calculate width of the quad
                                int w;
                                for (w = 1; i + w < 48 && mask[n + w] && colorEquals(colorMask[n + w], colorIndex); w++) {

                                }

                                // Calculate height of the quad
                                boolean done = false;
                                int h;
                                for (h = 1; j + h < 48; h++) {
                                    // Check if the entire row can be merged
                                    for (int k = 0; k < w; k++) {
                                        if (!mask[n + k + h * 48] || !colorEquals(colorMask[n + k + h * 48], colorIndex)) {
                                            done = true;
                                            break;
                                        }
                                    }
                                    if (done) break;
                                }

                                // Add quad to mesh
                                x[u] = i;
                                x[v] = j;

                                int[] du = new int[3];
                                int[] dv = new int[3];
                                du[u] = w;
                                dv[v] = h;

                                addQuad(chunk, x, du, dv, axis, colorIndex);

                                // Clear the mask for the processed area
                                for (int l = 0; l < h; l++) {
                                    for (int k = 0; k < w; k++) {
                                        mask[n + k + l * 48] = false;
                                    }
                                }

                                i += w;
                                n += w;
                            } else {
                                i++;
                                n++;
                            }
                        }
                    }
                }
            }
        }

        readStatsFromVerticesBuffer();

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        final int eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer.flip(), GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_HALF_FLOAT, false, vertexByteSize(), 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 1, GL_HALF_FLOAT, false, vertexByteSize(), 3 * 2);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 1, GL_HALF_FLOAT, false, vertexByteSize(), 4 * 2);
        glEnableVertexAttribArray(2);
    }

    private Integer getVoxel(VoxelEngineUtil.Chunk chunk, int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 ||
                x >= chunk.data.length ||
                y >= chunk.data[0].length ||
                z >= chunk.data[0][0].length) {
            return null;
        }
        return (Integer) chunk.data[x][y][z];
    }

    private boolean colorEquals(Integer c1, Integer c2) {
        if (c1 == null && c2 == null) return true;
        if (c1 == null || c2 == null) return false;
        return c1 == c2;
    }

    private void addQuad(VoxelEngineUtil.Chunk chunk, int[] x, int[] du, int[] dv,
                         int axis, Integer colorIndex) {
        int normalIndex = getNormalForAxis(axis);

        // Calculate the four corners of the quad
        Vector3 v1 = new Vector3(x[0] + chunk.xOffset, x[1] + chunk.yOffset, x[2] + chunk.zOffset);
        Vector3 v2 = new Vector3(x[0] + du[0] + chunk.xOffset, x[1] + du[1] + chunk.yOffset, x[2] + du[2] + chunk.zOffset);
        Vector3 v3 = new Vector3(x[0] + du[0] + dv[0] + chunk.xOffset, x[1] + du[1] + dv[1] + chunk.yOffset, x[2] + du[2] + dv[2] + chunk.zOffset);
        Vector3 v4 = new Vector3(x[0] + dv[0] + chunk.xOffset, x[1] + dv[1] + chunk.yOffset, x[2] + dv[2] + chunk.zOffset);

        int baseIndex = verticesOffset;

        addVertex(v1, colorIndex, normalIndex);
        addVertex(v2, colorIndex, normalIndex);
        addVertex(v3, colorIndex, normalIndex);
        addVertex(v4, colorIndex, normalIndex);

        indicesBuffer.put(baseIndex);
        indicesBuffer.put(baseIndex + 1);
        indicesBuffer.put(baseIndex + 2);

        indicesBuffer.put(baseIndex);
        indicesBuffer.put(baseIndex + 2);
        indicesBuffer.put(baseIndex + 3);

        indicesCount += 6;
    }

    private int getNormalForAxis(int axis) {
        switch (axis) {
            case 0:
                return 3;  // X axis
            case 1:
                return 4;    // Y axis
            case 2:
                return 0;  // Z axis
            default:
                return 0;
        }
    }

    private void addVertex(Vector3 position, Integer colorIndex, int normalIndex) {
        verticesBuffer.putShort(toHalfFloat(position.x));
        verticesBuffer.putShort(toHalfFloat(position.y));
        verticesBuffer.putShort(toHalfFloat(position.z));
        verticesBuffer.putShort(toHalfFloat((float) colorIndex));
        verticesBuffer.putShort(toHalfFloat((float) normalIndex));

        verticesOffset++;
    }

    private static short toHalfFloat(float fval) {
        int fbits = Float.floatToIntBits(fval);
        int sign = (fbits >>> 16) & 0x8000;
        int val = (fbits & 0x7fffffff) + 0x1000;

        if (val >= 0x47800000) { // might be or become NaN/Inf
            if ((fbits & 0x7fffffff) >= 0x47800000) {
                if (val < 0x7f800000) return (short) (sign | 0x7c00); // Inf
                return (short) (sign | 0x7c00 | ((fbits & 0x007fffff) >>> 13)); // NaN
            }
            return (short) (sign | 0x7bff); // max half-float
        }
        if (val >= 0x38800000) return (short) (sign | ((val - 0x38000000) >>> 13)); // normal

        if (val < 0x33000000) return (short) sign; // too small -> zero
        val = (fbits & 0x7fffffff) >>> 23;
        return (short) (sign | ((((fbits & 0x7fffff) | 0x800000) + (0x800000 >>> (val - 102))) >>> (126 - val)));
    }

    @Override
    public void render() {
        glDrawElements(GL_TRIANGLES, this.indicesCount, GL_UNSIGNED_INT, 0);
    }

    @Override
    public String vertexShaderSource() {
        return """
                                #version 330 core
                
                layout(location = 0) in vec3 pos;
                layout(location = 1) in float colorIndex;
                layout(location = 2) in float normalIndex;
                
                uniform mat4 view;
                uniform mat4 projection;
                
                out float fragmentColorIndex;
                out float fragmentNormalIndex;
                
                void main() {
                    gl_Position = projection * view * vec4(pos, 1.0);
                    fragmentColorIndex = colorIndex;
                    fragmentNormalIndex = normalIndex;
                }
                """;
    }

    @Override
    public String fragmentShaderSource() {
        return """
                               #version 330 core
                
                in float fragmentColorIndex;
                in float fragmentNormalIndex;
                
                out vec4 FragColor;
                
                layout(std140) uniform colorPalette {
                    vec3 colors[250];
                };
                
                layout(std140) uniform normalPalette {
                    vec3 normals[6];
                };
                
                void main() {
                    vec3 color = colors[int(fragmentColorIndex)];
                    vec3 normal = normals[int(fragmentNormalIndex)];
                
                    float brightness = 1.0;
                
                    if (normal.y > 0.5) {
                        brightness = 1.0;
                    } else if (normal.y < -0.5) {
                        brightness = 0.5;
                    } else {
                        brightness = 0.8;
                    }
                
                    vec3 litColor = color * brightness;
                    FragColor = vec4(litColor, 1.0);
                }
                """;
    }

    @Override
    public void initShaders() {
        super.initShaders();

        float[] normals = new float[24];
        int normalIndex = 0;

        for (int i = 0; i < NORMALS.length; i++) {
            normals[normalIndex++] = NORMALS[i].x;
            normals[normalIndex++] = NORMALS[i].y;
            normals[normalIndex++] = NORMALS[i].z;

            normals[normalIndex++] = 1.0f;
        }

        createUBO(programId, 0, "normalPalette", normals);

        float[] colors = new float[this.worldData.colors.size() * 4];
        int colorIndex = 0;

        for (int i = 0; i < this.worldData.colors.size(); i++) {
            colors[colorIndex++] = this.worldData.colors.get(i).r;
            colors[colorIndex++] = this.worldData.colors.get(i).g;
            colors[colorIndex++] = this.worldData.colors.get(i).b;

            colors[colorIndex++] = 1.0f;
        }

        createUBO(programId, 1, "colorPalette", colors);
    }

    private void createUBO(int programId, int position, String name, float[] data) {
        int uniformIndex = glGetUniformBlockIndex(programId, name);
        glUniformBlockBinding(programId, uniformIndex, position);

        int uboId = glGenBuffers();
        glBindBuffer(GL_UNIFORM_BUFFER, uboId);
        glBufferData(GL_UNIFORM_BUFFER, data, GL_STATIC_DRAW);
        glBindBufferBase(GL_UNIFORM_BUFFER, 1, uboId);
    }
}
