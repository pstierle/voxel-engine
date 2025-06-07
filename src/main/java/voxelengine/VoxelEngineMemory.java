package voxelengine;

import voxelengine.worldgen.NbtUtil;

import java.util.Collections;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;
import static voxelengine.VoxelEngineUtil.NORMALS;
import static voxelengine.VoxelEngineUtil.Vector3;

public class VoxelEngineMemory extends VoxelEngineBase {
    final NbtUtil.WorldData worldData = new NbtUtil().loadNbtWorld(true);

    VoxelEngineMemory() {
        super(Collections.emptyList());
        chunks = worldData.chunks;
    }

    @Override
    public int vertexByteSize() {
        // 5 floats values per vertex, each value is a float (4 bytes)
        return 20;
    }

    @Override
    public void initVertices() {
        updateVerticesBuffer();

        final int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        final int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        VoxelEngineUtil.BaseVoxelIndexed baseVoxel = new VoxelEngineUtil.BaseVoxelIndexed();

        for (int k = 0; k < chunks.size(); k++) {
            VoxelEngineUtil.Chunk chunk = chunks.get(k);
            for (int x = 0; x < chunk.data.length; x++) {
                for (int y = 0; y < chunk.data[x].length; y++) {
                    for (int z = 0; z < chunk.data[x][y].length; z++) {
                        Object voxel = chunk.data[x][y][z];
                        if (voxel == null) {
                            continue;
                        }
                        for (int i = 0; i < baseVoxel.faces.length; i++) {
                            VoxelEngineUtil.VoxelFaceIndexed face = baseVoxel.faces[i];
                            for (int j = 0; j < face.vertexPositions.length; j++) {
                                Vector3 vertexPosition = face.vertexPositions[j];

                                verticesBuffer.putFloat(vertexPosition.x + x + chunk.xOffset);
                                verticesBuffer.putFloat(vertexPosition.y + y + chunk.yOffset);
                                verticesBuffer.putFloat(vertexPosition.z + z + chunk.zOffset);

                                verticesBuffer.putFloat((float) ((int) chunk.data[x][y][z]));
                                verticesBuffer.putFloat((float) face.normalIndex);
                            }
                        }
                    }
                }
            }
        }

        readStatsFromVerticesBuffer();

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, vertexByteSize(), 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 1, GL_FLOAT, false, vertexByteSize(), (long) 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 1, GL_FLOAT, false, vertexByteSize(), (long) 4 * Float.BYTES);
        glEnableVertexAttribArray(2);
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
