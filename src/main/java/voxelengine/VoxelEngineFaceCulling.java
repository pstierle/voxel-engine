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
import static voxelengine.VoxelEngineUtil.Vector3;

public class VoxelEngineFaceCulling extends VoxelEngineBase {
    VoxelEngineFaceCulling() {
        super(new NbtUtil().loadNbtWorld(false).chunks);
    }

    @Override
    public void initVertices() {
        final int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        final int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);


        VoxelEngineUtil.BaseVoxel baseVoxel = new VoxelEngineUtil.BaseVoxel();

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
                            VoxelEngineUtil.VoxelFace face = baseVoxel.faces[i];
                            int xCheck = x + (int) face.normal().x;
                            int yCheck = y + (int) face.normal().y;
                            int zCheck = z + (int) face.normal().z;

                            if (xCheck >= 0 && xCheck < chunk.data.length &&
                                    yCheck >= 0 && yCheck < chunk.data[xCheck].length &&
                                    zCheck >= 0 && zCheck < chunk.data[xCheck][yCheck].length) {
                                Object voxelCheck = chunk.data[xCheck][yCheck][zCheck];
                                if (voxelCheck != null) {
                                    continue;
                                }
                            }
                            for (int j = 0; j < face.vertexPositions().length; j++) {
                                Vector3 vertexPosition = face.vertexPositions()[j];

                                verticesBuffer.putFloat(vertexPosition.x + x + chunk.xOffset);
                                verticesBuffer.putFloat(vertexPosition.y + y + chunk.yOffset);
                                verticesBuffer.putFloat(vertexPosition.z + z + chunk.zOffset);

                                verticesBuffer.putFloat(((VoxelEngineUtil.Color) chunk.data[x][y][z]).r);
                                verticesBuffer.putFloat(((VoxelEngineUtil.Color) chunk.data[x][y][z]).g);
                                verticesBuffer.putFloat(((VoxelEngineUtil.Color) chunk.data[x][y][z]).b);

                                verticesBuffer.putFloat(face.normal().x);
                                verticesBuffer.putFloat(face.normal().y);
                                verticesBuffer.putFloat(face.normal().z);
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
}
