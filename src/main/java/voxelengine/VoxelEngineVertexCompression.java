package voxelengine;

import voxelengine.worldgen.NbtUtil;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_HALF_FLOAT;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static voxelengine.VoxelEngineUtil.Vector3;

public class VoxelEngineVertexCompression extends VoxelEngineBase {
    VoxelEngineVertexCompression() {
        super(new NbtUtil().loadNbtWorld(false).chunks);
    }

    public int vertexByteSize() {
        // 9 values per vertex, each value is a float (4 bytes)
        return 36 / 2;
    }

    @Override
    public void initVertices() {
        final int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        final int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        VoxelEngineUtil.BaseVoxel baseVoxel = new VoxelEngineUtil.BaseVoxel();

        for (int k = 0; k < this.chunks.size(); k++) {
            VoxelEngineUtil.Chunk chunk = this.chunks.get(k);
            for (int x = 0; x < chunk.data.length; x++) {
                for (int y = 0; y < chunk.data[x].length; y++) {
                    for (int z = 0; z < chunk.data[x][y].length; z++) {
                        Object voxel = chunk.data[x][y][z];
                        if (voxel == null) {
                            continue;
                        }
                        for (int i = 0; i < baseVoxel.faces.length; i++) {
                            VoxelEngineUtil.VoxelFace face = baseVoxel.faces[i];
                            for (int j = 0; j < face.vertexPositions().length; j++) {
                                Vector3 vertexPosition = face.vertexPositions()[j];

                                verticesBuffer.putShort(toHalfFloat(vertexPosition.x + x + chunk.xOffset));
                                verticesBuffer.putShort(toHalfFloat(vertexPosition.y + y + chunk.yOffset));
                                verticesBuffer.putShort(toHalfFloat(vertexPosition.z + z + chunk.zOffset));

                                verticesBuffer.putShort(toHalfFloat(((VoxelEngineUtil.Color) chunk.data[x][y][z]).r));
                                verticesBuffer.putShort(toHalfFloat(((VoxelEngineUtil.Color) chunk.data[x][y][z]).g));
                                verticesBuffer.putShort(toHalfFloat(((VoxelEngineUtil.Color) chunk.data[x][y][z]).b));

                                verticesBuffer.putShort(toHalfFloat(face.normal().x));
                                verticesBuffer.putShort(toHalfFloat(face.normal().y));
                                verticesBuffer.putShort(toHalfFloat(face.normal().z));
                            }
                        }
                    }
                }
            }
        }

        readStatsFromVerticesBuffer();

        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_HALF_FLOAT, false, vertexByteSize(), 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_HALF_FLOAT, false, vertexByteSize(), 3 * 2);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 3, GL_HALF_FLOAT, false, vertexByteSize(), 6 * 2);
        glEnableVertexAttribArray(2);
    }

    private static short toHalfFloat(float fval) {
        int fbits = Float.floatToIntBits(fval);
        int sign = (fbits >>> 16) & 0x8000;
        int val = (fbits & 0x7fffffff) + 0x1000;

        if (val >= 0x47800000) { // might be or become NaN/Inf
            if ((fbits & 0x7fffffff) >= 0x47800000) {
                if (val < 0x7f800000) return (short) (sign | 0x7c00); // Inf
                return (short) (sign | 0x7c00 | ((fbits & 0x007fffff) >>> 13)); // NaN
            }
            return (short) (sign | 0x7bff); // max half-float
        }
        if (val >= 0x38800000) return (short) (sign | ((val - 0x38000000) >>> 13)); // normal

        if (val < 0x33000000) return (short) sign; // too small -> zero
        val = (fbits & 0x7fffffff) >>> 23;
        return (short) (sign | ((((fbits & 0x7fffff) | 0x800000) + (0x800000 >>> (val - 102))) >>> (126 - val)));
    }
}
