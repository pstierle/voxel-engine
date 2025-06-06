package voxelengine;

import voxelengine.worldgen.NbtUtil;

public class VoxelEngine extends VoxelEngineBase {
    VoxelEngine() {
        super(new NbtUtil().loadNbtWorld(false).chunks);
    }
}
