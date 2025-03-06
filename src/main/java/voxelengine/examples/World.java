package voxelengine.examples;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL46.*;

import voxelengine.core.Renderer;
import voxelengine.core.Shader;
import voxelengine.util.Chunk;
import voxelengine.util.Constants;
import voxelengine.util.NbtUtil;
import voxelengine.util.NoiseUtil;
import voxelengine.util.voxel.Color;
import voxelengine.util.voxel.Voxel;
import voxelengine.util.voxel.VoxelFace;
import voxelengine.util.voxel.VoxelFaceVertex;

public class World implements BaseExample {
    public Renderer renderer;
    public NbtUtil nbtUtil;
    public NoiseUtil noiseUtil;
    public int indicesCount;

    private int countVertices() {
        int voxelFaceVerticesCount = Constants.VOXEL_FACE_VERTICES_COUNT;
        if (Constants.INSTANCE_RENDERING) {
            voxelFaceVerticesCount = Constants.VOXEL_FACE_VERTICES_COUNT_INSTANCED;
        }
        return this.renderer.numVoxels * Constants.VOXEL_FACES_COUNT
                * voxelFaceVerticesCount
                * Constants.FLOAT_PER_VERTEX;
    }

    private boolean isAirVoxel(Chunk chunk, int x, int y, int z) {
        if (x >= chunk.xSize || y >= chunk.ySize || z >= chunk.zSize || x < 0 || y < 0 || z < 0) {
            return true;
        }

        return chunk.data[x][y][z] == null;
    }

    private boolean skipFace(Chunk chunk, VoxelFace face, int x, int y, int z) {
        switch (face.direction) {
            case FRONT:
                if (!isAirVoxel(chunk, x, y, z + 1)) {
                    return true;
                }
                break;
            case BACK:
                if (!isAirVoxel(chunk, x, y, z - 1)) {
                    return true;
                }
                break;
            case LEFT:
                if (!isAirVoxel(chunk, x - 1, y, z)) {
                    return true;
                }
                break;
            case RIGHT:
                if (!isAirVoxel(chunk, x + 1, y, z)) {
                    return true;
                }
                break;
            case TOP:
                if (!isAirVoxel(chunk, x, y + 1, z)) {
                    return true;
                }
                break;
            case BOTTOM:
                if (!isAirVoxel(chunk, x, y - 1, z)) {
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public void init() {
        Shader.loadShader(this.renderer.programId, "shaders/world.fs", GL_FRAGMENT_SHADER);
        Shader.loadShader(this.renderer.programId, "shaders/world.vs", GL_VERTEX_SHADER);
        List<Chunk> chunks;
        if (Constants.LOAD_WORLD_NBT) {
            chunks = this.nbtUtil.loadWorld();
        } else {
            chunks = this.noiseUtil.loadWorld();
        }
        float[] vertices = new float[this.countVertices()];
        int verticesIndex = 0;
        int[] indices = new int[0];
        if (Constants.INSTANCE_RENDERING) {
            indices = new int[this.renderer.numVoxels * Constants.VOXEL_FACES_COUNT
                    * Constants.VOXEL_FACE_INDICES_COUNT];
        }
        int indicesIndex = 0;
        Voxel voxel = new Voxel();
        for (Chunk chunk : chunks) {
            for (int x = 0; x < chunk.xSize; x++) {
                for (int y = 0; y < chunk.ySize; y++) {
                    for (int z = 0; z < chunk.zSize; z++) {
                        Color color = chunk.data[x][y][z];
                        if (color == null) {
                            continue;
                        }
                        int voxelVerticesStart = verticesIndex;
                        for (VoxelFace face : voxel.faces) {
                            if (Constants.FILTER_FACES && skipFace(chunk, face, x, y, z)) {
                                continue;
                            }
                            int faceVerticesOffset = (verticesIndex - voxelVerticesStart) / 9;
                            for (VoxelFaceVertex vertex : face.vertices) {
                                vertices[verticesIndex++] = (float) vertex.position.x + chunk.xOffset + x;
                                vertices[verticesIndex++] = (float) vertex.position.y + chunk.yOffset + y;
                                vertices[verticesIndex++] = (float) vertex.position.z + chunk.zOffset + z;

                                vertices[verticesIndex++] = color.r;
                                vertices[verticesIndex++] = color.g;
                                vertices[verticesIndex++] = color.b;

                                vertices[verticesIndex++] = (float) vertex.normal.x;
                                vertices[verticesIndex++] = (float) vertex.normal.y;
                                vertices[verticesIndex++] = (float) vertex.normal.z;
                            }
                            if (Constants.INSTANCE_RENDERING) {
                                int baseOffset = voxelVerticesStart / 9;
                                for (Integer index : face.indices) {
                                    int localIndex = index
                                            % face.vertices.size();
                                    indices[indicesIndex++] = baseOffset + faceVerticesOffset + localIndex;
                                }
                            }
                        }
                    }
                }
            }
        }

        this.renderer.vboId = glGenBuffers();
        this.renderer.vaoId = glGenVertexArrays();
        if (Constants.INSTANCE_RENDERING) {
            this.renderer.eboId = glGenBuffers();
        }
        this.renderer.viewLocation = glGetUniformLocation(this.renderer.programId, "view");
        this.renderer.projectionLocation = glGetUniformLocation(this.renderer.programId, "projection");
        this.renderer.lightPositionLocation = glGetUniformLocation(this.renderer.programId, "light_position");
        this.renderer.cameraPositionLocation = glGetUniformLocation(this.renderer.programId, "camera_position");

        glUseProgram(this.renderer.programId);

        glBindVertexArray(this.renderer.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.renderer.vboId);

        FloatBuffer verticesBuffer = ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                .order(java.nio.ByteOrder.nativeOrder())
                .asFloatBuffer();
        verticesBuffer.put(vertices).flip();

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 3, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);

        if (Constants.INSTANCE_RENDERING) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.renderer.eboId);
            IntBuffer indicesBuffer = ByteBuffer.allocateDirect(indices.length * Integer.BYTES)
                    .order(java.nio.ByteOrder.nativeOrder())
                    .asIntBuffer();
            indicesBuffer.put(indices).flip();
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
            this.indicesCount = indicesIndex;
            System.out.println(String.format("Loaded '%s' indices", indicesIndex));
        }
        System.out.println(String.format("Loaded '%s' vertices", verticesIndex));
    }

    @Override
    public void update() {
    }

    @Override
    public void render() {
        if (Constants.INSTANCE_RENDERING) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.renderer.eboId);
            glDrawElements(GL_TRIANGLES, this.indicesCount, GL_UNSIGNED_INT, 0);
        } else {
            glDrawArrays(GL_TRIANGLES, 0, Constants.VOXEL_FACES_COUNT
                    * Constants.VOXEL_FACE_VERTICES_COUNT * this.renderer.numVoxels);
        }
    }
}
