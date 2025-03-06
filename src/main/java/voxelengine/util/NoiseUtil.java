package voxelengine.util;

import java.util.ArrayList;
import java.util.List;

import voxelengine.core.Renderer;
import voxelengine.util.voxel.Color;

public class NoiseUtil {
    public Renderer renderer;

    public List<Chunk> loadWorld() {
        List<Chunk> chunks = new ArrayList<>();
        int voxelCount = 0;
        for (int dx = -1; dx < 1; dx++) {
            for (int dy = -1; dy < 1; dy++) {
                Chunk chunk = new Chunk();
                chunk.xOffset = dx * Constants.CHUNK_SIZE;
                chunk.yOffset = dy * Constants.CHUNK_SIZE;
                for (int x = 0; x < chunk.xSize; x++) {
                    for (int y = 0; y < chunk.ySize; y++) {
                        for (int z = 0; z < chunk.zSize; z++) {
                            Color color = new Color();
                            color.r = 1.0f;
                            chunk.data[x][y][z] = color;
                            voxelCount++;
                        }
                    }
                }
                chunks.add(chunk);
            }
        }
        this.renderer.numVoxels = voxelCount;
        return chunks;
    }
}
