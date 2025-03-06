package voxelengine.util;

import voxelengine.examples.ExampleType;

public class Constants {
    public static double MOUSE_SENSITIVITY = 0.2;
    public static double CAMERA_SPEED = 200.0;
    public static ExampleType EXAMPLE = ExampleType.WORLD;
    public static String NBT_FOLDER_PATH = "world/colluseum";
    public static int VOXEL_FACES_COUNT = 6;
    public static int VOXEL_FACE_VERTICES_COUNT = 6;
    public static int VOXEL_FACE_VERTICES_COUNT_INSTANCED = 4;
    public static int VOXEL_FACE_INDICES_COUNT = 6;
    public static boolean INSTANCE_RENDERING = true;
    public static boolean FILTER_FACES = true;
    public static boolean LOAD_WORLD_NBT = false;
    public static int FLOAT_PER_VERTEX = 9;
    public static int NBT_CHUNK_SIZE = 48;
    public static int NOISE_CHUNK_SIZE = 48;
    public static int NOISE_CHUNK_MAX_Y = 128;
    public static int WORLD_SEED = 1337;
}
