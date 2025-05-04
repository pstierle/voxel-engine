package voxelengine.util;

import org.joml.Vector3d;
import voxelengine.core.State;
import voxelengine.examples.ExampleType;
import voxelengine.examples.World;
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
    public static final int CHUNK_SIZE = Constants.WORLD_EXAMPLE == ExampleType.WORLD_NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_SIZE;
    private final Voxel baseVoxel = new Voxel();
    private int vboId;
    private int vaoId;
    private int eboId;
    private final int xOffset;
    private final int yOffset;
    private final int zOffset;
    private int numVoxels;
    private int indicesCount;
    private FloatBuffer verticesBuffer;
    private IntBuffer indicesBuffer;
    private Integer[][][] data;
    private boolean needsBufferLoad = false;
    private boolean needsAttributeLoad = true;
    private final Map<Direction, Vector3Key> neighborChunkKeys = new EnumMap<>(Direction.class);
    private final Map<Direction, Integer[][][]> neighborChunksData = new EnumMap<>(Direction.class);
    private final Vector3Key worldKey;
    private int totalVoxelFaces = 0;

    public Vector3Key getWorldKey() {
        return worldKey;
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

    public void setData(Integer[][][] data) {
        this.data = data;
    }

    public Integer[][][] getData() {
        return data;
    }

    public Chunk(int chunkOffsetX, int chunkOffsetY, int chunkOffsetZ) {
        this.xOffset = chunkOffsetX;
        this.yOffset = chunkOffsetY;
        this.zOffset = chunkOffsetZ;
        this.worldKey = new Vector3Key(chunkOffsetX, chunkOffsetY, chunkOffsetZ);
    }

    public void loadData() {
        this.setupBuffers();
        if (Constants.OPTIMIZATION_GREEDY_MESHING) {
            this.loadDataGreedyMeshing();
        } else {
            this.loadDataNormal();
        }
    }

    private void loadDataNormal() {
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

        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            // For instanced rendering, we add 4 vertices and use indices
            int baseOffset = verticesIndex / this.getVoxelFloatPerVertex();

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

            // Add indices for the 2 triangles that make up this face
            this.indicesBuffer.put(baseOffset);
            this.indicesBuffer.put(baseOffset + 1);
            this.indicesBuffer.put(baseOffset + 2);

            this.indicesBuffer.put(baseOffset + 2);
            this.indicesBuffer.put(baseOffset + 3);
            this.indicesBuffer.put(baseOffset);
            this.indicesCount += 6;
        } else {
            // For non-instanced rendering with GL_TRIANGLES, we directly add 6 vertices for 2 triangles
            // First triangle: 0-1-2
            verticesIndex = this.addFaceVertices(
                    verticesIndex,
                    (float) vertices[0].x + this.xOffset,
                    (float) vertices[0].y + this.yOffset,
                    (float) vertices[0].z + this.zOffset,
                    colorIndex,
                    face
            );

            verticesIndex = this.addFaceVertices(
                    verticesIndex,
                    (float) vertices[1].x + this.xOffset,
                    (float) vertices[1].y + this.yOffset,
                    (float) vertices[1].z + this.zOffset,
                    colorIndex,
                    face
            );

            verticesIndex = this.addFaceVertices(
                    verticesIndex,
                    (float) vertices[2].x + this.xOffset,
                    (float) vertices[2].y + this.yOffset,
                    (float) vertices[2].z + this.zOffset,
                    colorIndex,
                    face
            );

            // Second triangle: 2-3-0
            verticesIndex = this.addFaceVertices(
                    verticesIndex,
                    (float) vertices[2].x + this.xOffset,
                    (float) vertices[2].y + this.yOffset,
                    (float) vertices[2].z + this.zOffset,
                    colorIndex,
                    face
            );

            verticesIndex = this.addFaceVertices(
                    verticesIndex,
                    (float) vertices[3].x + this.xOffset,
                    (float) vertices[3].y + this.yOffset,
                    (float) vertices[3].z + this.zOffset,
                    colorIndex,
                    face
            );

            verticesIndex = this.addFaceVertices(
                    verticesIndex,
                    (float) vertices[0].x + this.xOffset,
                    (float) vertices[0].y + this.yOffset,
                    (float) vertices[0].z + this.zOffset,
                    colorIndex,
                    face
            );
        }

        // Increment the face counter
        totalVoxelFaces++;
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

    public void loadNeighbors() {
        this.neighborChunkKeys.put(Direction.FRONT, null);
        this.neighborChunkKeys.put(Direction.BACK, null);
        this.neighborChunkKeys.put(Direction.LEFT, null);
        this.neighborChunkKeys.put(Direction.RIGHT, null);
        this.neighborChunkKeys.put(Direction.TOP, null);
        this.neighborChunkKeys.put(Direction.BOTTOM, null);

        Vector3Key rightKey = new Vector3Key(this.xOffset + CHUNK_SIZE, this.yOffset, this.zOffset); // RIGHT
        Vector3Key leftKey = new Vector3Key(this.xOffset - CHUNK_SIZE, this.yOffset, this.zOffset); // LEFT
        Vector3Key frontKey = new Vector3Key(this.xOffset, this.yOffset, this.zOffset + CHUNK_SIZE); // FRONT
        Vector3Key backKey = new Vector3Key(this.xOffset, this.yOffset, this.zOffset - CHUNK_SIZE); // BACK
        Vector3Key topKey = new Vector3Key(this.xOffset, this.yOffset + CHUNK_SIZE, this.zOffset); // TOP
        Vector3Key bottomKey = new Vector3Key(this.xOffset, this.yOffset - CHUNK_SIZE, this.zOffset); // BOTTOM

        if (World.chunks.get(rightKey) != null) {
            this.neighborChunkKeys.put(Direction.RIGHT, rightKey);
        }
        if (World.chunks.get(leftKey) != null) {
            this.neighborChunkKeys.put(Direction.LEFT, leftKey);
        }
        if (World.chunks.get(frontKey) != null) {
            this.neighborChunkKeys.put(Direction.FRONT, frontKey);
        }
        if (World.chunks.get(backKey) != null) {
            this.neighborChunkKeys.put(Direction.BACK, backKey);
        }
        if (World.chunks.get(topKey) != null) {
            this.neighborChunkKeys.put(Direction.TOP, topKey);
        }
        if (World.chunks.get(bottomKey) != null) {
            this.neighborChunkKeys.put(Direction.BOTTOM, bottomKey);
        }

        if (Constants.WORLD_EXAMPLE == ExampleType.WORLD_NOISE) {
            this.neighborChunksData.put(Direction.FRONT, null);
            this.neighborChunksData.put(Direction.BACK, null);
            this.neighborChunksData.put(Direction.LEFT, null);
            this.neighborChunksData.put(Direction.RIGHT, null);
            this.neighborChunksData.put(Direction.TOP, null);
            this.neighborChunksData.put(Direction.BOTTOM, null);

            this.neighborChunkKeys.forEach(((direction, key) -> {
                if (key == null && direction != Direction.TOP && direction != Direction.BOTTOM) {
                    int dx = this.xOffset;
                    int dz = this.zOffset;
                    switch (direction) {
                        case FRONT:
                            dz += Constants.NOISE_CHUNK_SIZE;
                            break;
                        case BACK:
                            dz -= Constants.NOISE_CHUNK_SIZE;
                            break;
                        case LEFT:
                            dx -= Constants.NOISE_CHUNK_SIZE;
                            break;
                        case RIGHT:
                            dx += Constants.NOISE_CHUNK_SIZE;
                            break;
                    }
                    neighborChunksData.put(direction, State.noiseUtil.heightMapSlice(State.noiseUtil.generateHeightMap(dx, dz), this.yOffset));
                }
            }));
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
            int[] indices = new int[]{
                    0, 1, 2, 1, 2, 3
            };
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
            // For non-instanced rendering, we need to accurately track vertices
            // Each face has 6 vertices (2 triangles) when rendered as GL_TRIANGLES
            int vertexCount;

            if (Constants.OPTIMIZATION_GREEDY_MESHING) {
                // In greedy meshing, each face creates exactly 6 vertices (2 triangles)
                vertexCount = totalVoxelFaces * 6;
            } else {
                // In normal meshing, we use the standard constant
                vertexCount = Constants.VOXEL_FACE_VERTICES_COUNT * totalVoxelFaces;
            }

            glDrawArrays(GL_TRIANGLES, 0, vertexCount);
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
            totalVoxelFaces++;
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
        if (Constants.OPTIMIZATION_FACE_CULLING == false) {
            return false;
        }
        switch (direction) {
            case FRONT:
                if (z + 1 < CHUNK_SIZE) {
                    return this.data[x][y][z + 1] != null;
                } else {
                    Integer[][][] checkData = getNeighborChunksData(Direction.FRONT);
                    return checkData != null && checkData[x][y][0] != null;
                }
            case BACK:
                if (z - 1 >= 0) {
                    return this.data[x][y][z - 1] != null;
                } else {
                    Integer[][][] checkData = getNeighborChunksData(Direction.BACK);
                    return checkData != null && checkData[x][y][CHUNK_SIZE - 1] != null;
                }
            case LEFT:
                if (x - 1 >= 0) {
                    return this.data[x - 1][y][z] != null;
                } else {
                    Integer[][][] checkData = getNeighborChunksData(Direction.LEFT);
                    return checkData != null && checkData[CHUNK_SIZE - 1][y][z] != null;
                }
            case RIGHT:
                if (x + 1 < CHUNK_SIZE) {
                    return this.data[x + 1][y][z] != null;
                } else {
                    Integer[][][] checkData = getNeighborChunksData(Direction.RIGHT);
                    return checkData != null && checkData[0][y][z] != null;
                }
            case TOP:
                if (y + 1 < CHUNK_SIZE) {
                    return this.data[x][y + 1][z] != null;
                } else {
                    Integer[][][] checkData = getNeighborChunksData(Direction.TOP);
                    return checkData != null && checkData[x][0][z] != null;
                }
            case BOTTOM:
                if (Constants.WORLD_EXAMPLE == ExampleType.WORLD_NOISE) {
                    return true;
                }
                if (y - 1 >= 0) {
                    return this.data[x][y - 1] != null;
                } else {
                    Integer[][][] checkData = getNeighborChunksData(Direction.BOTTOM);
                    return checkData != null && checkData[x][CHUNK_SIZE - 1][z] != null;
                }
        }
        return false;
    }

    private Integer[][][] getNeighborChunksData(Direction direction) {
        Integer[][][] checkData = null;
        Vector3Key neighborKey = this.neighborChunkKeys.get(direction);
        if (neighborKey != null) {
            Chunk neighborChunk = World.chunks.get(neighborKey);
            if (neighborChunk != null) {
                checkData = neighborChunk.getData();
            }
        }
        if (checkData == null && this.neighborChunksData != null) {
            checkData = this.neighborChunksData.get(direction);
        }
        return checkData;
    }

    public int calculateVoxelCount() {
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
            if (Constants.WORLD_EXAMPLE == ExampleType.WORLD_NBT) {
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
        State.loadedVertices++;
        return index + this.getVoxelFloatPerVertex();
    }

    private int getVoxelFloatPerVertex() {
        return Constants.OPTIMIZATION_SHADER_MEMORY ? Constants.VOXEL_FLOAT_PER_VERTEX_OPTIMIZATION_SHADER_MEMORY : Constants.VOXEL_FLOAT_PER_VERTEX;
    }
}
