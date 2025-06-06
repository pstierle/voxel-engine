package voxelengine;

import voxelengine.worldgen.NbtUtil;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static voxelengine.VoxelEngineUtil.FRONT_NORMAL;
import static voxelengine.VoxelEngineUtil.RIGHT_NORMAL;
import static voxelengine.VoxelEngineUtil.TOP_NORMAL;
import static voxelengine.VoxelEngineUtil.Vector3;

public class VoxelEngineGreedyMeshing extends VoxelEngineBase {
    VoxelEngineGreedyMeshing() {
        super(new NbtUtil().loadNbtWorld(false).chunks);
    }

    @Override
    public void initVertices() {
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
                VoxelEngineUtil.Color[] colorMask = new VoxelEngineUtil.Color[48 * 48];

                q[axis] = 1;

                // Sweep through each slice along the axis
                for (x[axis] = -1; x[axis] < 48; ) {
                    int n = 0;

                    // Generate mask for this slice
                    for (x[v] = 0; x[v] < 48; x[v]++) {
                        for (x[u] = 0; x[u] < 48; x[u]++) {
                            VoxelEngineUtil.Color voxel1 = getVoxel(chunk, x[0], x[1], x[2]);
                            VoxelEngineUtil.Color voxel2 = getVoxel(chunk, x[0] + q[0], x[1] + q[1], x[2] + q[2]);

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
                                VoxelEngineUtil.Color color = colorMask[n];

                                // Calculate width of the quad
                                int w;
                                for (w = 1; i + w < 48 && mask[n + w] && colorEquals(colorMask[n + w], color); w++) {

                                }

                                // Calculate height of the quad
                                boolean done = false;
                                int h;
                                for (h = 1; j + h < 48; h++) {
                                    // Check if the entire row can be merged
                                    for (int k = 0; k < w; k++) {
                                        if (!mask[n + k + h * 48] || !colorEquals(colorMask[n + k + h * 48], color)) {
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

                                addQuad(chunk, x, du, dv, axis, color);

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
        final int vertexSizeInBytes = 9 * Float.BYTES;

        glVertexAttribPointer(0, 3, GL_FLOAT, false, vertexSizeInBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, vertexSizeInBytes, (long) 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 3, GL_FLOAT, false, vertexSizeInBytes, (long) 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    private VoxelEngineUtil.Color getVoxel(VoxelEngineUtil.Chunk chunk, int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 ||
                x >= chunk.data.length ||
                y >= chunk.data[0].length ||
                z >= chunk.data[0][0].length) {
            return null;
        }
        return (VoxelEngineUtil.Color) chunk.data[x][y][z];
    }

    private boolean colorEquals(VoxelEngineUtil.Color c1, VoxelEngineUtil.Color c2) {
        if (c1 == null && c2 == null) return true;
        if (c1 == null || c2 == null) return false;
        return c1.r == c2.r && c1.g == c2.g && c1.b == c2.b;
    }

    private void addQuad(VoxelEngineUtil.Chunk chunk, int[] x, int[] du, int[] dv,
                         int axis, VoxelEngineUtil.Color color) {

        Vector3 normal = getNormalForAxis(axis);

        // Calculate the four corners of the quad
        Vector3 v1 = new Vector3(x[0] + chunk.xOffset, x[1] + chunk.yOffset, x[2] + chunk.zOffset);
        Vector3 v2 = new Vector3(x[0] + du[0] + chunk.xOffset, x[1] + du[1] + chunk.yOffset, x[2] + du[2] + chunk.zOffset);
        Vector3 v3 = new Vector3(x[0] + du[0] + dv[0] + chunk.xOffset, x[1] + du[1] + dv[1] + chunk.yOffset, x[2] + du[2] + dv[2] + chunk.zOffset);
        Vector3 v4 = new Vector3(x[0] + dv[0] + chunk.xOffset, x[1] + dv[1] + chunk.yOffset, x[2] + dv[2] + chunk.zOffset);

        // First triangle
        addVertex(v1, color, normal);
        addVertex(v2, color, normal);
        addVertex(v3, color, normal);

        // Second triangle
        addVertex(v1, color, normal);
        addVertex(v3, color, normal);
        addVertex(v4, color, normal);
    }

    private Vector3 getNormalForAxis(int axis) {
        switch (axis) {
            case 0:
                return RIGHT_NORMAL;  // X axis
            case 1:
                return TOP_NORMAL;    // Y axis
            case 2:
                return FRONT_NORMAL;  // Z axis
            default:
                return new Vector3(0, 0, 1);
        }
    }

    private void addVertex(Vector3 position, VoxelEngineUtil.Color color, Vector3 normal) {
        verticesBuffer.putFloat(position.x);
        verticesBuffer.putFloat(position.y);
        verticesBuffer.putFloat(position.z);

        verticesBuffer.putFloat(color.r);
        verticesBuffer.putFloat(color.g);
        verticesBuffer.putFloat(color.b);

        verticesBuffer.putFloat(normal.x);
        verticesBuffer.putFloat(normal.y);
        verticesBuffer.putFloat(normal.z);
    }
}
