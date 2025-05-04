package voxelengine.core;

import voxelengine.examples.BaseExample;
import voxelengine.examples.ExampleType;
import voxelengine.examples.Quad2d;
import voxelengine.examples.Triangle2d;
import voxelengine.examples.Voxel3d;
import voxelengine.examples.World;
import voxelengine.util.Constants;
import voxelengine.worldgen.NbtUtil;
import voxelengine.worldgen.NoiseUtil;

public class State {
    private State() {
    }

    public static int loadedVertices = 0;
    public static int RENDERED_CHUNKS = 0;
    public static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
    public static final int CHUNK_SIZE = Constants.WORLD_EXAMPLE == ExampleType.WORLD_NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_SIZE;
    public static final Camera camera = new Camera();
    public static final Renderer renderer = new Renderer();
    public static final Window window = new Window();
    public static final NoiseUtil noiseUtil = new NoiseUtil();
    public static final NbtUtil nbtUtil = new NbtUtil();
    public static final Physics physics = new Physics();
    public static final ImGUI imGui = new ImGUI();
    public static final BaseExample example = switch (Constants.WORLD_EXAMPLE) {
        case TRIANGLE_2D -> new Triangle2d();
        case QUAD_2D -> new Quad2d();
        case VOXEL_3D -> new Voxel3d();
        case WORLD_NBT, WORLD_NOISE -> new World();
    };
}