package voxelengine.core;

import voxelengine.examples.World;
import voxelengine.util.Constants;
import voxelengine.util.WorldType;
import voxelengine.worldgen.NbtUtil;
import voxelengine.worldgen.NoiseUtil;

public class State {
    private State() {
    }

    public static final int PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
    public static final int CHUNK_SIZE = Constants.WORLD_TYPE == WorldType.NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_SIZE;
    
    public static final Camera camera = new Camera();
    public static final Renderer renderer = new Renderer();
    public static final Window window = new Window();
    public static final World world = new World();
    public static final NoiseUtil noiseUtil = new NoiseUtil();
    public static final NbtUtil nbtUtil = new NbtUtil();
    public static final Physics physics = new Physics();
    public static final ImGUI imGui = new ImGUI();
}