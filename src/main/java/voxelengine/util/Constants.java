package voxelengine.util;

import voxelengine.examples.ExampleType;

public final class Constants {
    private Constants() {
    }

    public static final WorldType WORLD_TYPE = WorldType.NOISE;
    public static final ExampleType WORLD_EXAMPLE = ExampleType.WORLD;

    public static final double MOUSE_SENSITIVITY = 0.2;
    public static final int CAMERA_FOV = 90;

    public static final int VOXEL_FACES_COUNT = 6;
    public static final int VOXEL_FACE_VERTICES_COUNT = 6;
    public static final int VOXEL_FACE_VERTICES_COUNT_INSTANCED = 4;
    public static final int VOXEL_FACE_INDICES_COUNT = 6;
    public static final int VOXEL_FLOAT_PER_VERTEX = 9;
    public static final int VOXEL_FLOAT_PER_VERTEX_OPTIMIZATION_SHADER_MEMORY = 5;

    public static final int NBT_CHUNK_SIZE = 48;
    public static final boolean NBT_DEBUG = false;
    public static final String NBT_FOLDER_PATH = "world/italy";

    public static final int NOISE_CHUNK_SIZE = 16;
    public static final int NOISE_CHUNK_MAX_Y = 128;
    public static final int NOISE_CHUNK_RADIUS = 15;
    public static final int NOISE_WORLD_SEED = 1337;

    public static final boolean OPTIMIZATION_INSTANCE_RENDERING = true;
    public static final boolean OPTIMIZATION_FILTER_FACES = true;
    public static final boolean OPTIMIZATION_SHADER_MEMORY = true;
    public static final boolean OPTIMIZATION_FRUSTUM_CULLING = true;
    public static final boolean OPTIMIZATION_GREEDY_MESHING = true;
}
