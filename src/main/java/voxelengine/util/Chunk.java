package voxelengine.util;

import voxelengine.util.voxel.Color;
import voxelengine.util.voxel.Voxel;
import voxelengine.util.voxel.VoxelFace;
import voxelengine.util.voxel.VoxelFaceVertex;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Chunk {
    private static int nextId = 0;
    public int id;
    public int xOffset;
    public int yOffset;
    public int zOffset;
    public int xSize;
    public int ySize;
    public int zSize;
    public int vboId;
    public int vaoId;
    public int eboId;
    public int numVoxels;
    public int indicesCount;
    public Color[][][] data;
    public float[] vertices;
    public int[] indices;
    public boolean needsBufferUpdate = false;

    public Chunk(int chunkOffsetX, int chunkOffsetY, int chunkOffsetZ, int xSize, int ySize, int zSize) {
        this.id = nextId++;
        this.xOffset = chunkOffsetX;
        this.yOffset = chunkOffsetY;
        this.zOffset = chunkOffsetZ;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.vboId = glGenBuffers();
        this.vaoId = glGenVertexArrays();
        if (Constants.INSTANCE_RENDERING) {
            this.eboId = glGenBuffers();
        }
    }

    public void loadData(Color[][][] data) {
        this.data = data;
        this.numVoxels = this.calculateVoxelCount();
        this.vertices = new float[this.countVertices()];
        int verticesIndex = 0;
        if (Constants.INSTANCE_RENDERING) {
            this.indices = new int[this.numVoxels * Constants.VOXEL_FACES_COUNT
                    * Constants.VOXEL_FACE_INDICES_COUNT];
        }
        int indicesIndex = 0;
        Voxel voxel = new Voxel();
        for (int x = 0; x < this.xSize; x++) {
            for (int y = 0; y < this.ySize; y++) {
                for (int z = 0; z < this.zSize; z++) {
                    Color color = this.data[x][y][z];
                    if (color == null) {
                        continue;
                    }
                    int voxelVerticesStart = verticesIndex;
                    for (VoxelFace face : voxel.faces) {
                        if (Constants.FILTER_FACES && this.skipFace(face, x, y, z)) {
                            continue;
                        }
                        int faceVerticesOffset = (verticesIndex - voxelVerticesStart) / Constants.FLOAT_PER_VERTEX;
                        for (VoxelFaceVertex vertex : face.vertices) {
                            this.vertices[verticesIndex++] = (float) vertex.position.x + this.xOffset + x;
                            this.vertices[verticesIndex++] = (float) vertex.position.y + this.yOffset + y;
                            this.vertices[verticesIndex++] = (float) vertex.position.z + this.zOffset + z;

                            this.vertices[verticesIndex++] = color.r;
                            this.vertices[verticesIndex++] = color.g;
                            this.vertices[verticesIndex++] = color.b;

                            this.vertices[verticesIndex++] = (float) vertex.normal.x;
                            this.vertices[verticesIndex++] = (float) vertex.normal.y;
                            this.vertices[verticesIndex++] = (float) vertex.normal.z;
                        }
                        if (Constants.INSTANCE_RENDERING) {
                            int baseOffset = voxelVerticesStart / Constants.FLOAT_PER_VERTEX;
                            for (Integer index : face.indices) {
                                int localIndex = index
                                        % face.vertices.size();
                                this.indices[indicesIndex++] = baseOffset + faceVerticesOffset + localIndex;
                            }
                        }
                    }
                }
            }
        }

        if (Constants.INSTANCE_RENDERING) {
            this.indicesCount = indicesIndex;
        }
    }

    public void uploadBuffers(int programId) {
        if (this.vertices == null) {
            return;
        }
        if (Constants.INSTANCE_RENDERING) {
            if (this.indices == null) {
                return;
            }
        }

        glUseProgram(programId);

        glBindVertexArray(this.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);

        FloatBuffer verticesBuffer = ByteBuffer.allocateDirect(this.vertices.length * Float.BYTES)
                .order(java.nio.ByteOrder.nativeOrder())
                .asFloatBuffer();
        verticesBuffer.put(this.vertices).flip();

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 3, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        if (Constants.INSTANCE_RENDERING) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboId);
            IntBuffer indicesBuffer = ByteBuffer.allocateDirect(this.indices.length * Integer.BYTES)
                    .order(java.nio.ByteOrder.nativeOrder())
                    .asIntBuffer();
            indicesBuffer.put(this.indices).flip();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        }

        this.data = null;
        vertices = null;
        indices = null;
        this.needsBufferUpdate = false;
    }

    public void render() {
        glBindVertexArray(this.vaoId);
        if (Constants.INSTANCE_RENDERING) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboId);
            glDrawElements(GL_TRIANGLES, this.indicesCount, GL_UNSIGNED_INT, 0);
        } else {
            glDrawArrays(GL_TRIANGLES, 0, Constants.VOXEL_FACES_COUNT
                    * Constants.VOXEL_FACE_VERTICES_COUNT * this.numVoxels);
        }
    }

    private int countVertices() {
        int voxelFaceVerticesCount = Constants.VOXEL_FACE_VERTICES_COUNT;
        if (Constants.INSTANCE_RENDERING) {
            voxelFaceVerticesCount = Constants.VOXEL_FACE_VERTICES_COUNT_INSTANCED;
        }
        return this.numVoxels * Constants.VOXEL_FACES_COUNT
                * voxelFaceVerticesCount
                * Constants.FLOAT_PER_VERTEX;
    }

    private boolean isAirVoxel(int x, int y, int z) {
        if (x >= this.xSize || y >= this.ySize || z >= this.zSize || x < 0 || y < 0 || z < 0) {
            return true;
        }

        return this.data[x][y][z] == null;
    }

    private boolean skipFace(VoxelFace face, int x, int y, int z) {
        switch (face.direction) {
            case FRONT:
                if (!isAirVoxel(x, y, z + 1)) {
                    return true;
                }
                break;
            case BACK:
                if (!isAirVoxel(x, y, z - 1)) {
                    return true;
                }
                break;
            case LEFT:
                if (!isAirVoxel(x - 1, y, z)) {
                    return true;
                }
                break;
            case RIGHT:
                if (!isAirVoxel(x + 1, y, z)) {
                    return true;
                }
                break;
            case TOP:
                if (!isAirVoxel(x, y + 1, z)) {
                    return true;
                }
                break;
            case BOTTOM:
                if (!isAirVoxel(x, y - 1, z)) {
                    return true;
                }
                break;
        }
        return false;
    }

    private int calculateVoxelCount() {
        int voxelCount = 0;

        for (int x = 0; x < this.xSize; x++) {
            for (int y = 0; y < this.ySize; y++) {
                for (int z = 0; z < this.zSize; z++) {
                    if (this.data[x][y][z] != null) {
                        voxelCount++;
                    }
                }
            }
        }

        return voxelCount;
    }

}
