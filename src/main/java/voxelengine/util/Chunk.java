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
    private Map<Direction, Integer[][][]> neighborChunksData;

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

    public void setNeedsBufferLoad(boolean needsBufferLoad) {
        this.needsBufferLoad = needsBufferLoad;
    }

    public boolean needsBufferLoad() {
        return needsBufferLoad;
    }

    public void setNeighborChunksData(Map<Direction, Integer[][][]> neighborChunksData) {
        this.neighborChunksData = neighborChunksData;
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
        if (Constants.OPTIMIZATION_GREEDY_MESHING) {
            this.loadDataGreedyMeshing();
        } else {
            this.loadDataNormal();
        }
    }

    private void loadDataNormal() {
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

    private void loadDataGreedyMeshing() {
        this.setupBuffers();
        int verticesIndex = 0;
        for (Direction direction : Direction.values()) {
            verticesIndex = processDirectionGreedy(direction, verticesIndex);
        }
    }

    private int processDirectionGreedy(Direction direction, int verticesIndex) {
        boolean[][] processed = new boolean[CHUNK_SIZE][CHUNK_SIZE];
        for (int i3 = 0; i3 < CHUNK_SIZE; i3++) {
            for (int i = 0; i < CHUNK_SIZE; i++) {
                for (int j = 0; j < CHUNK_SIZE; j++) {
                    processed[i][j] = false;
                }
            }
            for (int i1 = 0; i1 < CHUNK_SIZE; i1++) {
                for (int i2 = 0; i2 < CHUNK_SIZE; i2++) {
                    if (processed[i1][i2]) continue;
                    int[] voxelCoords = getVoxelCoords(direction, i1, i2, i3);
                    int x = voxelCoords[0];
                    int y = voxelCoords[1];
                    int z = voxelCoords[2];
                    if (this.data[x][y][z] == null || skipFace(direction, x, y, z)) {
                        continue;
                    }
                    Integer colorIndex = this.data[x][y][z];
                    int width = 1;
                    while (i1 + width < CHUNK_SIZE &&
                            canMerge(direction, i1 + width, i2, i3, colorIndex, processed)) {
                        width++;
                    }
                    int height = 1;
                    boolean canExtendHeight = true;
                    while (i2 + height < CHUNK_SIZE && canExtendHeight) {
                        for (int w = 0; w < width; w++) {
                            if (!canMerge(direction, i1 + w, i2 + height, i3, colorIndex, processed)) {
                                canExtendHeight = false;
                                break;
                            }
                        }
                        if (canExtendHeight) height++;
                    }
                    for (int w = 0; w < width; w++) {
                        for (int h = 0; h < height; h++) {
                            processed[i1 + w][i2 + h] = true;
                        }
                    }
                    verticesIndex = addGreedyFace(verticesIndex, direction, x, y, z, width, height, colorIndex);
                }
            }
        }

        return verticesIndex;
    }

    private boolean canMerge(Direction direction, int i1, int i2, int i3, Integer colorIndex, boolean[][] processed) {
        int[] voxelCoords = getVoxelCoords(direction, i1, i2, i3);
        int x = voxelCoords[0];
        int y = voxelCoords[1];
        int z = voxelCoords[2];
        if (processed[i1][i2] ||
                this.data[x][y][z] == null ||
                skipFace(direction, x, y, z) ||
                !this.data[x][y][z].equals(colorIndex)) {
            return false;
        }

        return true;
    }

    private int[] getVoxelCoords(Direction direction, int i1, int i2, int i3) {
        switch (direction) {
            case FRONT:
                return new int[]{i1, i2, i3};
            case BACK:
                return new int[]{i1, i2, CHUNK_SIZE - 1 - i3};
            case LEFT:
                return new int[]{i3, i2, i1};
            case RIGHT:
                return new int[]{CHUNK_SIZE - 1 - i3, i2, i1};
            case TOP:
                return new int[]{i1, i3, i2};
            case BOTTOM:
                return new int[]{i1, CHUNK_SIZE - 1 - i3, i2};
            default:
                return new int[]{i1, i2, i3};
        }
    }

    private int addGreedyFace(int verticesIndex, Direction direction, int x, int y, int z,
                              int width, int height, int colorIndex) {
        Vector3d[] vertices = calculateGreedyFaceVertices(direction, x, y, z, width, height);
        VoxelFace face = baseVoxel.getFaces().stream().filter(f -> f.getDirection() == direction).findFirst().orElse(null);
        for (Vector3d vertex : vertices) {
            verticesIndex = this.addFaceVertices(
                    verticesIndex,
                    (float) vertex.x + this.xOffset,
                    (float) vertex.y + this.yOffset,
                    (float) vertex.z + this.zOffset,
                    colorIndex,
                    face
            );
        }
        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            int baseOffset = verticesIndex / this.getVoxelFloatPerVertex() - vertices.length;
            this.indicesBuffer.put(baseOffset);
            this.indicesBuffer.put(baseOffset + 1);
            this.indicesBuffer.put(baseOffset + 2);

            this.indicesBuffer.put(baseOffset + 2);
            this.indicesBuffer.put(baseOffset + 3);
            this.indicesBuffer.put(baseOffset);
            this.indicesCount += 6;
        }

        return verticesIndex;
    }

    private Vector3d[] calculateGreedyFaceVertices(Direction direction, int x, int y, int z,
                                                   int width, int height) {
        Vector3d[] vertices = new Vector3d[4];
        switch (direction) {
            case FRONT:
                vertices[0] = new Vector3d(x, y, z + 1);
                vertices[1] = new Vector3d(x + width, y, z + 1);
                vertices[2] = new Vector3d(x + width, y + height, z + 1);
                vertices[3] = new Vector3d(x, y + height, z + 1);
                break;
            case BACK:
                vertices[0] = new Vector3d(x, y, z);
                vertices[1] = new Vector3d(x, y + height, z);
                vertices[2] = new Vector3d(x + width, y + height, z);
                vertices[3] = new Vector3d(x + width, y, z);
                break;
            case LEFT:
                vertices[0] = new Vector3d(x, y, z);
                vertices[1] = new Vector3d(x, y, z + width);
                vertices[2] = new Vector3d(x, y + height, z + width);
                vertices[3] = new Vector3d(x, y + height, z);
                break;
            case RIGHT:
                vertices[0] = new Vector3d(x + 1, y, z);
                vertices[1] = new Vector3d(x + 1, y + height, z);
                vertices[2] = new Vector3d(x + 1, y + height, z + width);
                vertices[3] = new Vector3d(x + 1, y, z + width);
                break;
            case TOP:
                vertices[0] = new Vector3d(x, y + 1, z);
                vertices[1] = new Vector3d(x, y + 1, z + height);
                vertices[2] = new Vector3d(x + width, y + 1, z + height);
                vertices[3] = new Vector3d(x + width, y + 1, z);
                break;
            case BOTTOM:
                vertices[0] = new Vector3d(x, y, z);
                vertices[1] = new Vector3d(x + width, y, z);
                vertices[2] = new Vector3d(x + width, y, z + height);
                vertices[3] = new Vector3d(x, y, z + height);
                break;
        }

        return vertices;
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
            if (this.skipFace(face.getDirection(), x, y, z)) {
                continue;
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
        if (Constants.OPTIMIZATION_FILTER_FACES == false) {
            return false;
        }
        switch (direction) {
            case FRONT:
                if (z + 1 < CHUNK_SIZE) {
                    return this.data[x][y][z + 1] != null;
                } else {
                    Integer[][][] checkData = null;
                    Integer neighborId = this.neighborChunkIds.get(Direction.FRONT);
                    if (neighborId != null) {
                        Chunk neighborChunk = findChunkById(neighborId);
                        if (neighborChunk != null) {
                            checkData = neighborChunk.getData();
                        }
                    }
                    if (checkData == null && this.neighborChunksData != null) {
                        checkData = this.neighborChunksData.get(Direction.FRONT);
                    }
                    return checkData != null && checkData[x][y][0] != null;
                }
            case BACK:
                if (z - 1 >= 0) {
                    return this.data[x][y][z - 1] != null;
                } else {
                    Integer[][][] checkData = null;
                    Integer neighborId = this.neighborChunkIds.get(Direction.BACK);
                    if (neighborId != null) {
                        Chunk neighborChunk = findChunkById(neighborId);
                        if (neighborChunk != null) {
                            checkData = neighborChunk.getData();
                        }
                    }
                    if (checkData == null && this.neighborChunksData != null) {
                        checkData = this.neighborChunksData.get(Direction.BACK);
                    }
                    return checkData != null && checkData[x][y][CHUNK_SIZE - 1] != null;
                }
            case LEFT:
                if (x - 1 >= 0) {
                    return this.data[x - 1][y][z] != null;
                } else {
                    Integer[][][] checkData = null;
                    Integer neighborId = this.neighborChunkIds.get(Direction.LEFT);
                    if (neighborId != null) {
                        Chunk neighborChunk = findChunkById(neighborId);
                        if (neighborChunk != null) {
                            checkData = neighborChunk.getData();
                        }
                    }
                    if (checkData == null && this.neighborChunksData != null) {
                        checkData = this.neighborChunksData.get(Direction.LEFT);
                    }
                    return checkData != null && checkData[CHUNK_SIZE - 1][y][z] != null;
                }
            case RIGHT:
                if (x + 1 < CHUNK_SIZE) {
                    return this.data[x + 1][y][z] != null;
                } else {
                    Integer[][][] checkData = null;
                    Integer neighborId = this.neighborChunkIds.get(Direction.RIGHT);
                    if (neighborId != null) {
                        Chunk neighborChunk = findChunkById(neighborId);
                        if (neighborChunk != null) {
                            checkData = neighborChunk.getData();
                        }
                    }
                    if (checkData == null && this.neighborChunksData != null) {
                        checkData = this.neighborChunksData.get(Direction.RIGHT);
                    }
                    return checkData != null && checkData[0][y][z] != null;
                }
            case TOP:
                if (y + 1 < CHUNK_SIZE) {
                    return this.data[x][y + 1][z] != null;
                } else {
                    Integer[][][] checkData = null;
                    Integer neighborId = this.neighborChunkIds.get(Direction.TOP);
                    if (neighborId != null) {
                        Chunk neighborChunk = findChunkById(neighborId);
                        if (neighborChunk != null) {
                            checkData = neighborChunk.getData();
                        }
                    }
                    if (checkData == null && this.neighborChunksData != null) {
                        checkData = this.neighborChunksData.get(Direction.TOP);
                    }
                    return checkData != null && checkData[x][0][z] != null;
                }
            case BOTTOM:
                if (y == 0 && Constants.WORLD_TYPE == WorldType.NOISE && Constants.OPTIMIZATION_FILTER_FACES) {
                    return true;
                }
                if (y - 1 >= 0) {
                    return this.data[x][y - 1] != null;
                } else {
                    Integer[][][] checkData = null;
                    Integer neighborId = this.neighborChunkIds.get(Direction.BOTTOM);
                    if (neighborId != null) {
                        Chunk neighborChunk = findChunkById(neighborId);
                        if (neighborChunk != null) {
                            checkData = neighborChunk.getData();
                        }
                    }
                    if (checkData == null && this.neighborChunksData != null) {
                        checkData = this.neighborChunksData.get(Direction.BOTTOM);
                    }
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
