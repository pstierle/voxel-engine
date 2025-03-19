package voxelengine.util;

import org.joml.Vector3d;
import voxelengine.examples.World;
import voxelengine.util.voxel.Color;
import voxelengine.util.voxel.Voxel;
import voxelengine.util.voxel.VoxelFace;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

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
    public static final int CHUNK_SIZE = Constants.WORLD_TYPE == WorldType.NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_SIZE;
    private static int nextId = 0;
    private int id;
    private final Voxel baseVoxel = new Voxel();
    private int vboId;
    private int vaoId;
    private int eboId;
    private int xOffset;
    private int yOffset;
    private int zOffset;

    private int numVoxels;
    private int indicesCount;
    private FloatBuffer verticesBuffer;
    private IntBuffer indicesBuffer;
    private Integer[][][] data;
    private boolean needsBufferLoad = false;
    private boolean needsAttributeLoad = true;
    private Map<Direction, Integer> neighborChunkIds;

    public int getId() {
        return id;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getZOffset() {
        return zOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public void setZOffset(int zOffset) {
        this.zOffset = zOffset;
    }

    public void setNeedsBufferLoad(boolean needsBufferLoad) {
        this.needsBufferLoad = needsBufferLoad;
    }

    public boolean needsBufferLoad() {
        return needsBufferLoad;
    }

    public void setData(Integer[][][] data) {
        this.data = data;
    }

    public Integer[][][] getData() {
        return data;
    }

    public void setNeighborChunkIds(Map<Direction, Integer> neighborChunkIds) {
        this.neighborChunkIds = neighborChunkIds;
    }

    public Chunk(int chunkOffsetX, int chunkOffsetY, int chunkOffsetZ) {
        this.id = nextId++;
        this.xOffset = chunkOffsetX;
        this.yOffset = chunkOffsetY;
        this.zOffset = chunkOffsetZ;
    }

    public void loadData() {
        this.setupBuffers();
        int verticesIndex = 0;
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (this.data[x][y][z] == null) {
                        continue;
                    }
                    verticesIndex = this.loadFaces(verticesIndex, this.data[x][y][z], x, y, z);
                }
            }
        }
    }

    public void loadBuffers(int programId) {
        if (this.verticesBuffer == null) {
            return;
        }
        if (Constants.OPTIMIZATION_INSTANCE_RENDERING && this.indicesBuffer == null) {
            return;
        }

        if (this.needsAttributeLoad) {
            this.vboId = glGenBuffers();
            this.vaoId = glGenVertexArrays();
            this.eboId = glGenBuffers();
        }

        glUseProgram(programId);

        glBindVertexArray(this.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);

        if (this.needsAttributeLoad) {
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
            this.needsAttributeLoad = false;
        }

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer.flip(), GL_STATIC_DRAW);

        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, this.indicesBuffer.flip(), GL_STATIC_DRAW);
        }

        this.verticesBuffer = null;
        this.indicesBuffer = null;
        this.needsBufferLoad = false;
    }

    public void render() {
        glBindVertexArray(this.vaoId);
        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.eboId);
            glDrawElements(GL_TRIANGLES, this.indicesCount, GL_UNSIGNED_INT, 0);
        } else {
            glDrawArrays(GL_TRIANGLES, 0, Constants.VOXEL_FACES_COUNT
                    * Constants.VOXEL_FACE_VERTICES_COUNT * this.numVoxels);
        }
    }

    private int countVertices() {
        int voxelFaceVerticesCount = Constants.VOXEL_FACE_VERTICES_COUNT;
        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            voxelFaceVerticesCount = Constants.VOXEL_FACE_VERTICES_COUNT_INSTANCED;
        }
        return this.numVoxels * Constants.VOXEL_FACES_COUNT
                * voxelFaceVerticesCount
                * this.getVoxelFloatPerVertex();
    }

    private int loadFaces(int voxelVerticesStart, int colorIndex, int x, int y, int z) {
        int verticesIndex = voxelVerticesStart;
        for (VoxelFace face : this.baseVoxel.getFaces()) {
            if (Constants.OPTIMIZATION_FILTER_FACES) {
                if (this.skipFace(face.getDirection(), x, y, z)) {
                    continue;
                }
            }
            int faceVerticesOffset = (verticesIndex - voxelVerticesStart) / this.getVoxelFloatPerVertex();
            for (Vector3d vertex : face.getVertices()) {
                verticesIndex = this.addFaceVertices(
                        verticesIndex,
                        (float) vertex.x + this.xOffset + x,
                        (float) vertex.y + this.yOffset + y,
                        (float) vertex.z + this.zOffset + z,
                        colorIndex,
                        face
                );
            }
            if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
                int baseOffset = voxelVerticesStart / this.getVoxelFloatPerVertex();
                for (Integer index : face.getIndices()) {
                    int localIndex = index
                            % face.getVertices().size();
                    this.indicesBuffer.put(baseOffset + faceVerticesOffset + localIndex);
                    this.indicesCount++;
                }
            }
        }
        return verticesIndex;
    }


    private boolean skipFace(Direction direction, int x, int y, int z) {
        switch (direction) {
            case FRONT:
                if (z + 1 < CHUNK_SIZE) {
                    return this.data[x][y][z + 1] != null;
                } else {
                    Integer neighborId = this.neighborChunkIds.get(Direction.FRONT);
                    if (neighborId == null) {
                        return false;
                    }
                    Chunk neighborChunk = findChunkById(neighborId);
                    if (neighborChunk == null) {
                        return false;
                    }
                    Integer[][][] checkData = neighborChunk.getData();
                    return checkData != null && checkData[x][y][0] != null;
                }
            case BACK:
                if (z - 1 >= 0) {
                    return this.data[x][y][z - 1] != null;
                } else {
                    Integer neighborId = this.neighborChunkIds.get(Direction.BACK);
                    if (neighborId == null) {
                        return false;
                    }
                    Chunk neighborChunk = findChunkById(neighborId);
                    if (neighborChunk == null) {
                        return false;
                    }
                    Integer[][][] checkData = neighborChunk.getData();
                    return checkData != null && checkData[x][y][CHUNK_SIZE - 1] != null;
                }
            case LEFT:
                if (x - 1 >= 0) {
                    return this.data[x - 1][y][z] != null;
                } else {
                    Integer neighborId = this.neighborChunkIds.get(Direction.LEFT);
                    if (neighborId == null) {
                        return false;
                    }
                    Chunk neighborChunk = findChunkById(neighborId);
                    if (neighborChunk == null) {
                        return false;
                    }
                    Integer[][][] checkData = neighborChunk.getData();
                    return checkData != null && checkData[CHUNK_SIZE - 1][y][z] != null;
                }
            case RIGHT:
                if (x + 1 < CHUNK_SIZE) {
                    return this.data[x + 1][y][z] != null;
                } else {
                    Integer neighborId = this.neighborChunkIds.get(Direction.RIGHT);
                    if (neighborId == null) {
                        return false;
                    }
                    Chunk neighborChunk = findChunkById(neighborId);
                    if (neighborChunk == null) {
                        return false;
                    }
                    Integer[][][] checkData = neighborChunk.getData();
                    return checkData != null && checkData[0][y][z] != null;
                }
            case TOP:
                if (y + 1 < CHUNK_SIZE) {
                    return this.data[x][y + 1][z] != null;
                } else {
                    Integer neighborId = this.neighborChunkIds.get(Direction.TOP);
                    if (neighborId == null) {
                        return false;
                    }
                    Chunk neighborChunk = findChunkById(neighborId);
                    if (neighborChunk == null) {
                        return false;
                    }
                    Integer[][][] checkData = neighborChunk.getData();
                    return checkData != null && checkData[x][0][z] != null;
                }
            case BOTTOM:
                if (y - 1 >= 0) {
                    return this.data[x][y - 1] != null;
                } else {
                    Integer neighborId = this.neighborChunkIds.get(Direction.BOTTOM);
                    if (neighborId == null) {
                        return false;
                    }
                    Chunk neighborChunk = findChunkById(neighborId);
                    if (neighborChunk == null) {
                        return false;
                    }
                    Integer[][][] checkData = neighborChunk.getData();
                    return checkData != null && checkData[x][CHUNK_SIZE - 1][z] != null;
                }
        }
        return false;
    }

    private Chunk findChunkById(int id) {
        for (int i = 0; i < World.chunks.size(); i++) {
            if (World.chunks.get(i).getId() == id) {
                return World.chunks.get(i);
            }
        }
        return null;
    }

    private int calculateVoxelCount() {
        int voxelCount = 0;

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (this.data[x][y][z] != null) {
                        voxelCount++;
                    }
                }
            }
        }

        return voxelCount;
    }


    private void setupBuffers() {
        this.indicesCount = 0;
        this.numVoxels = this.calculateVoxelCount();
        this.verticesBuffer = ByteBuffer.allocateDirect(this.countVertices() * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            this.indicesBuffer = ByteBuffer.allocateDirect(this.numVoxels * Constants.VOXEL_FACES_COUNT
                            * Constants.VOXEL_FACE_INDICES_COUNT * Integer.BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer();
        }
    }

    private int addFaceVertices(int index, float x, float y, float z, int colorIndex, VoxelFace face) {
        this.verticesBuffer.put(x);
        this.verticesBuffer.put(y);
        this.verticesBuffer.put(z);

        if (Constants.OPTIMIZATION_SHADER_MEMORY) {
            this.verticesBuffer.put((float) colorIndex);
            this.verticesBuffer.put((float) face.getDirection().getIndex());
        } else {
            Color color;
            if (Constants.WORLD_TYPE == WorldType.NBT) {
                color = ColorUtil.nbtColors.get(colorIndex);
            } else {
                color = ColorUtil.noiseColors.get(colorIndex);
            }

            this.verticesBuffer.put(color.getR());
            this.verticesBuffer.put(color.getG());
            this.verticesBuffer.put(color.getB());

            this.verticesBuffer.put((float) face.getDirection().getNormal().x);
            this.verticesBuffer.put((float) face.getDirection().getNormal().y);
            this.verticesBuffer.put((float) face.getDirection().getNormal().z);
        }
        return index + this.getVoxelFloatPerVertex();
    }

    private int getVoxelFloatPerVertex() {
        return Constants.OPTIMIZATION_SHADER_MEMORY ? Constants.VOXEL_FLOAT_PER_VERTEX_OPTIMIZATION_SHADER_MEMORY : Constants.VOXEL_FLOAT_PER_VERTEX;
    }
}
