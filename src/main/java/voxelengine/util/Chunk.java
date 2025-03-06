package voxelengine.util;

import voxelengine.util.voxel.Color;

public class Chunk {
    public int xOffset = 0;
    public int yOffset = 0;
    public int zOffset = 0;
    public int xSize;
    public int ySize;
    public int zSize;
    public Color[][][] data;

    public Chunk() {
        if (Constants.LOAD_WORLD_NBT) {
            this.xSize = Constants.NBT_CHUNK_SIZE;
            this.ySize = Constants.NBT_CHUNK_SIZE;
            this.zSize = Constants.NBT_CHUNK_SIZE;
            this.data = new Color[Constants.NBT_CHUNK_SIZE][Constants.NBT_CHUNK_SIZE][Constants.NBT_CHUNK_SIZE];
        } else {
            this.xSize = Constants.CHUNK_SIZE;
            this.ySize = Constants.CHUNK_MAX_Y;
            this.zSize = Constants.CHUNK_SIZE;
            this.data = new Color[Constants.CHUNK_SIZE][Constants.CHUNK_MAX_Y][Constants.CHUNK_SIZE];
        }
    }
}
