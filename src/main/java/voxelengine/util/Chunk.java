package voxelengine.util;

import org.joml.Vector3d;
import voxelengine.util.voxel.Color;
import voxelengine.util.voxel.Voxel;
import voxelengine.util.voxel.VoxelFace;

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
    private int yOffset;
    private int zOffset;

    private int numVoxels;
    private int indicesCount;
    private FloatBuffer verticesBuffer;
    private IntBuffer indicesBuffer;
    private int[][] heightMapData;
    private Integer[][][] nbtData;
    private boolean needsBufferLoad = false;
    private boolean needsAttributeLoad = true;
    private boolean isOnFrustum = true;
    private Map<Direction, int[][]> neighborHeightMap;
    private Map<Direction, Integer[][][]> neighborNbtData;

    public boolean isOnFrustum() {
        return isOnFrustum;
    }

    public void setIsOnFrustum(boolean isOnFrustum) {
        this.isOnFrustum = isOnFrustum;
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

    public Integer[][][] getNbtData() {
        return nbtData;
    }

    public int[][] getHeightMapData() {
        return heightMapData;
    }

    public void setNeighborNbtData(Map<Direction, Integer[][][]> neighborNbtData) {
        this.neighborNbtData = neighborNbtData;
    }

    public void setNeighborHeightMap(Map<Direction, int[][]> neighborHeightMap) {
        this.neighborHeightMap = neighborHeightMap;
    }

    public void setNbtData(Integer[][][] nbtData) {
        this.nbtData = nbtData;
    }

    public void setHeightMapData(int[][] heightMapData) {
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
        int verticesIndex = 0;
        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                int maxHeight = heightMapData[x][z];
                for (int y = 0; y <= maxHeight; y++) {
                    int colorIndex;
                    if (y + this.yOffset <= 0) {
                        colorIndex = ColorUtil.WATER_COLOR_INDEX;
                    } else if (y + this.yOffset <= sandLevel) {
                        colorIndex = ColorUtil.SAND_COLOR_INDEX;
                    } else if (y + this.yOffset <= mountainLevel) {
                        colorIndex = ColorUtil.MOUNTAIN_COLOR_INDEX;
                    } else {
                        colorIndex = ColorUtil.SNOW_COLOR_INDEX;
                    }
                    verticesIndex = this.loadFaces(verticesIndex, colorIndex, x, y, z);
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
                    if (nbtData[x][y][z] == null) {
                        continue;
                    }
                    verticesIndex = this.loadFaces(verticesIndex, nbtData[x][y][z], x, y, z);
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
                if (Constants.WORLD_TYPE == WorldType.NBT) {
                    if (this.skipFaceNbt(face.getDirection(), x, y, z)) {
                        continue;
                    }
                } else {
                    if (this.skipFaceHeightMap(face.getDirection(), x, y, z)) {
                        continue;
                    }
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

    private boolean skipFaceHeightMap(Direction direction, int x, int y, int z) {
        switch (direction) {
            case FRONT:
                if (z + 1 < Constants.NOISE_CHUNK_SIZE) {
                    return y <= this.heightMapData[x][z + 1];
                } else {
                    return y <= neighborHeightMap.get(Direction.FRONT)[x][0];
                }
            case BACK:
                if (z - 1 >= 0) {
                    return y <= this.heightMapData[x][z - 1];
                } else {
                    return y <= neighborHeightMap.get(Direction.BACK)[x][Constants.NOISE_CHUNK_SIZE - 1];
                }
            case LEFT:
                if (x - 1 >= 0) {
                    return y <= this.heightMapData[x - 1][z];
                } else {
                    return y <= neighborHeightMap.get(Direction.LEFT)[Constants.NOISE_CHUNK_SIZE - 1][z];
                }
            case RIGHT:
                if (x + 1 < Constants.NOISE_CHUNK_SIZE) {
                    return y <= this.heightMapData[x + 1][z];
                } else {
                    return y <= neighborHeightMap.get(Direction.RIGHT)[0][z];
                }
            case TOP:
                return y < this.heightMapData[x][z];
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
                    Integer[][][] checkData = neighborNbtData.get(Direction.FRONT);
                    return checkData != null && checkData[x][y][0] != null;
                }
            case BACK:
                if (z - 1 >= 0) {
                    return this.nbtData[x][y][z - 1] != null;
                } else {
                    Integer[][][] checkData = neighborNbtData.get(Direction.BACK);
                    return checkData != null && checkData[x][y][this.zSize - 1] != null;
                }
            case LEFT:
                if (x - 1 >= 0) {
                    return this.nbtData[x - 1][y][z] != null;
                } else {
                    Integer[][][] checkData = neighborNbtData.get(Direction.LEFT);
                    return checkData != null && checkData[this.xSize - 1][y][z] != null;
                }
            case RIGHT:
                if (x + 1 < this.xSize) {
                    return this.nbtData[x + 1][y][z] != null;
                } else {
                    Integer[][][] checkData = neighborNbtData.get(Direction.RIGHT);
                    return checkData != null && checkData[0][y][z] != null;
                }
            case TOP:
                if (y + 1 < this.ySize) {
                    return this.nbtData[x][y + 1][z] != null;
                } else {
                    Integer[][][] checkData = neighborNbtData.get(Direction.TOP);
                    return checkData != null && checkData[x][0][z] != null;
                }
            case BOTTOM:
                if (y - 1 >= 0) {
                    return this.nbtData[x][y - 1] != null;
                } else {
                    Integer[][][] checkData = neighborNbtData.get(Direction.BOTTOM);
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
        if (Constants.WORLD_TYPE == WorldType.NBT) {
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
