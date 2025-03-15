package voxelengine.util;

import voxelengine.util.voxel.Color;
import voxelengine.util.voxel.Voxel;
import voxelengine.util.voxel.VoxelFace;
import voxelengine.util.voxel.VoxelFaceVertex;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumMap;
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
    private final Voxel baseVoxel = new Voxel();
    private final int xSize;
    private final int ySize;
    private final int zSize;
    private final int vboId;
    private final int vaoId;
    private final int eboId;
    private int xOffset;
    private final int yOffset;
    private int zOffset;

    private int numVoxels;
    private int indicesCount;
    private FloatBuffer verticesBuffer;
    private IntBuffer indicesBuffer;
    private float[][] heightMapData;
    private Color[][][] nbtData;
    private boolean needsBufferLoad = false;
    private boolean needsAttributeLoad = true;
    private Map<Direction, float[][]> neighborHeightMap;
    private Map<Direction, Color[][][]> neighborNbtData;

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getZOffset() {
        return zOffset;
    }

    public int getZSize() {
        return zSize;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
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

    public Color[][][] getNbtData() {
        return nbtData;
    }

    public float[][] getHeightMapData() {
        return heightMapData;
    }

    public void setNeighborNbtData(Map<Direction, Color[][][]> neighborNbtData) {
        this.neighborNbtData = neighborNbtData;
    }

    public void setNeighborHeightMap(Map<Direction, float[][]> neighborHeightMap) {
        this.neighborHeightMap = neighborHeightMap;
    }

    public void setNbtData(Color[][][] nbtData) {
        this.nbtData = nbtData;
    }

    public void setHeightMapData(float[][] heightMapData) {
        this.heightMapData = heightMapData;
    }

    public Chunk(int chunkOffsetX, int chunkOffsetY, int chunkOffsetZ, int xSize, int ySize, int zSize) {
        this.xOffset = chunkOffsetX;
        this.yOffset = chunkOffsetY;
        this.zOffset = chunkOffsetZ;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.vboId = glGenBuffers();
        this.vaoId = glGenVertexArrays();
        this.eboId = glGenBuffers();
        this.neighborHeightMap = new EnumMap<>(Direction.class);
        this.neighborNbtData = new EnumMap<>(Direction.class);
    }


    public void loadDataHeightMap() {
        this.setupBuffers();
        float sandLevel = Constants.NOISE_CHUNK_MAX_Y * 0.25f;
        float mountainLevel = Constants.NOISE_CHUNK_MAX_Y * 0.65f;
        float snowLevel = Constants.NOISE_CHUNK_MAX_Y * 0.8f;
        int verticesIndex = 0;
        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                int maxHeight = (int) Math.ceil(heightMapData[x][z]);
                for (int y = 0; y <= maxHeight; y++) {
                    Color color;
                    if (y <= 0) {
                        color = new Color(0.1f, 0.3f, 0.8f);
                    } else if (y <= sandLevel) {
                        color = new Color(0.95f, 0.87f, 0.7f);
                    } else if (y <= mountainLevel) {
                        color = new Color(0.55f, 0.55f, 0.55f);
                    } else if (y <= snowLevel) {
                        color = new Color(0.6f, 0.6f, 0.6f);
                    } else {
                        color = new Color(0.95f, 0.95f, 0.95f);
                    }
                    verticesIndex = this.loadFaces(verticesIndex, color, x, y, z);
                }
            }
        }
        this.neighborHeightMap = null;
    }

    public void loadDataNbt() {
        this.setupBuffers();
        int verticesIndex = 0;
        for (int x = 0; x < this.xSize; x++) {
            for (int y = 0; y < this.ySize; y++) {
                for (int z = 0; z < this.zSize; z++) {
                    Color color = nbtData[x][y][z];
                    if (color == null) {
                        continue;
                    }
                    verticesIndex = this.loadFaces(verticesIndex, color, x, y, z);
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

        glUseProgram(programId);

        glBindVertexArray(this.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer.flip(), GL_STATIC_DRAW);

        if (this.needsAttributeLoad) {
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
            glEnableVertexAttribArray(0);

            glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);

            glVertexAttribPointer(2, 3, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
            glEnableVertexAttribArray(2);
            this.needsAttributeLoad = false;
        }

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
                * Constants.VOXEL_FLOAT_PER_VERTEX;
    }

    private int loadFaces(int voxelVerticesStart, Color color, int x, int y, int z) {
        int verticesIndex = voxelVerticesStart;
        for (VoxelFace face : this.baseVoxel.getFaces()) {
            if (Constants.OPTIMIZATION_FILTER_FACES && Constants.WORLD_NBT ? this.skipFaceNbt(face.getDirection(), x, y, z) : this.skipFaceHeightMap(face.getDirection(), x, y, z)) {
                continue;
            }
            int faceVerticesOffset = (verticesIndex - voxelVerticesStart) / Constants.VOXEL_FLOAT_PER_VERTEX;
            for (VoxelFaceVertex vertex : face.getVertices()) {
                verticesIndex = this.addFaceVertices(
                        verticesIndex,
                        (float) vertex.getPosition().x + this.xOffset + x,
                        (float) vertex.getPosition().y + this.yOffset + y,
                        (float) vertex.getPosition().z + this.zOffset + z,
                        color.getR(),
                        color.getG(),
                        color.getB(),
                        (float) vertex.getNormal().x,
                        (float) vertex.getNormal().y,
                        (float) vertex.getNormal().z
                );
            }
            if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
                int baseOffset = voxelVerticesStart / Constants.VOXEL_FLOAT_PER_VERTEX;
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

    private boolean skipFaceHeightMap(Direction direction, int x, int y, int z) {
        switch (direction) {
            case FRONT:
                if (z + 1 < Constants.NOISE_CHUNK_SIZE) {
                    return y <= Math.ceil(this.heightMapData[x][z + 1]);
                } else {
                    return y <= Math.ceil(neighborHeightMap.get(Direction.FRONT)[x][0]);
                }
            case BACK:
                if (z - 1 >= 0) {
                    return y <= Math.ceil(this.heightMapData[x][z - 1]);
                } else {
                    return y <= Math.ceil(neighborHeightMap.get(Direction.BACK)[x][Constants.NOISE_CHUNK_SIZE - 1]);
                }
            case LEFT:
                if (x - 1 >= 0) {
                    return y <= Math.ceil(this.heightMapData[x - 1][z]);
                } else {
                    return y <= Math.ceil(neighborHeightMap.get(Direction.LEFT)[Constants.NOISE_CHUNK_SIZE - 1][z]);
                }
            case RIGHT:
                if (x + 1 < Constants.NOISE_CHUNK_SIZE) {
                    return y <= Math.ceil(this.heightMapData[x + 1][z]);
                } else {
                    return y <= Math.ceil(neighborHeightMap.get(Direction.RIGHT)[0][z]);
                }
            case TOP:
                return y < Math.ceil(this.heightMapData[x][z]);
            case BOTTOM:
                return true;
        }
        return false;
    }

    private boolean skipFaceNbt(Direction direction, int x, int y, int z) {
        switch (direction) {
            case FRONT:
                if (z + 1 < this.zSize) {
                    return this.nbtData[x][y][z + 1] != null;
                } else {
                    Color[][][] checkData = neighborNbtData.get(Direction.FRONT);
                    return checkData != null && checkData[x][y][0] != null;
                }
            case BACK:
                if (z - 1 >= 0) {
                    return this.nbtData[x][y][z - 1] != null;
                } else {
                    Color[][][] checkData = neighborNbtData.get(Direction.BACK);
                    return checkData != null && checkData[x][y][this.zSize - 1] != null;
                }
            case LEFT:
                if (x - 1 >= 0) {
                    return this.nbtData[x - 1][y][z] != null;
                } else {
                    Color[][][] checkData = neighborNbtData.get(Direction.LEFT);
                    return checkData != null && checkData[this.xSize - 1][y][z] != null;
                }
            case RIGHT:
                if (x + 1 < this.xSize) {
                    return this.nbtData[x + 1][y][z] != null;
                } else {
                    Color[][][] checkData = neighborNbtData.get(Direction.RIGHT);
                    return checkData != null && checkData[0][y][z] != null;
                }
            case TOP:
                if (y + 1 < this.ySize) {
                    return this.nbtData[x][y + 1][z] != null;
                } else {
                    Color[][][] checkData = neighborNbtData.get(Direction.TOP);
                    return checkData != null && checkData[x][0][z] != null;
                }
            case BOTTOM:
                if (y - 1 >= 0) {
                    return this.nbtData[x][y - 1] != null;
                } else {
                    Color[][][] checkData = neighborNbtData.get(Direction.BOTTOM);
                    return checkData != null && checkData[x][this.ySize - 1][z] != null;
                }
        }
        return false;
    }

    private int calculateVoxelCountNbt() {
        int voxelCount = 0;

        for (int x = 0; x < this.xSize; x++) {
            for (int y = 0; y < this.ySize; y++) {
                for (int z = 0; z < this.zSize; z++) {
                    if (this.nbtData[x][y][z] != null) {
                        voxelCount++;
                    }
                }
            }
        }

        return voxelCount;
    }

    private int calculateVoxelCountHeightMap() {
        int voxelCount = 0;

        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                int maxHeight = (int) Math.ceil(this.heightMapData[x][z]);
                for (int y = 0; y <= maxHeight; y++) {
                    voxelCount++;
                }
            }
        }

        return voxelCount;
    }

    private void setupBuffers() {
        this.indicesCount = 0;
        if (Constants.WORLD_NBT) {
            this.numVoxels = this.calculateVoxelCountNbt();
        } else {
            this.numVoxels = this.calculateVoxelCountHeightMap();
        }
        this.verticesBuffer = ByteBuffer.allocateDirect(this.countVertices() * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            this.indicesBuffer = ByteBuffer.allocateDirect(this.numVoxels * Constants.VOXEL_FACES_COUNT
                            * Constants.VOXEL_FACE_INDICES_COUNT * Integer.BYTES)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer();
        }
    }

    private int addFaceVertices(int index, float x, float y, float z, float r, float g, float b, float normalX, float normalY, float normalZ) {
        this.verticesBuffer.put(x);
        this.verticesBuffer.put(y);
        this.verticesBuffer.put(z);

        this.verticesBuffer.put(r);
        this.verticesBuffer.put(g);
        this.verticesBuffer.put(b);

        this.verticesBuffer.put(normalX);
        this.verticesBuffer.put(normalY);
        this.verticesBuffer.put(normalZ);

        return index + 9;
    }
}
