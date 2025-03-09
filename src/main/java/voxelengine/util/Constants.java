package voxelengine.util;

import voxelengine.examples.ExampleType;

public final class Constants {
    private Constants() {
    }

    public static final double MOUSE_SENSITIVITY = 0.2;
    public static final double CAMERA_SPEED = 100.0;
    public static final ExampleType EXAMPLE = ExampleType.WORLD;
    public static final String NBT_FOLDER_PATH = "world/italy";
    public static final int VOXEL_FACES_COUNT = 6;
    public static final int VOXEL_FACE_VERTICES_COUNT = 6;
    public static final int VOXEL_FACE_VERTICES_COUNT_INSTANCED = 4;
    public static final int VOXEL_FACE_INDICES_COUNT = 6;
    public static final boolean INSTANCE_RENDERING = true;
    public static final boolean DEBUG_NBT = false;
    public static final boolean FILTER_FACES = true;
    public static final boolean LOAD_WORLD_NBT = false;
    public static final int FLOAT_PER_VERTEX = 9;
    public static final int NBT_CHUNK_SIZE = 48;
    public static final int NOISE_CHUNK_SIZE = 16;
    public static final int NOISE_CHUNK_MAX_Y = 128;
    public static final int NOISE_CHUNK_RADIUS = 10;
    public static final int NOISE_CHUNK_BUFFER_UPLOADS_PER_FRAME = 2;
    public static final int WORLD_SEED = 1337;
    public static final int CAMERA_FOV = 90;
}
