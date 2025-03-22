package voxelengine.examples;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.core.Statistic;
import voxelengine.util.Chunk;
import voxelengine.util.Constants;
import voxelengine.util.NbtUtil;
import voxelengine.util.NoiseUtil;
import voxelengine.util.Vector3Key;
import voxelengine.util.WorldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class World implements BaseExample {
    private static final int CHUNK_SIZE = Constants.WORLD_TYPE == WorldType.NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_SIZE;
    private static final long UPDATE_INTERVAL = 100_000_000;
    private final AtomicLong lastUpdateTime = new AtomicLong(0);
    private boolean isUpdating = false;
    private Renderer renderer;
    private NoiseUtil noiseUtil;
    public static final Map<Vector3Key, Chunk> chunks = new ConcurrentHashMap<>();
    private Camera camera;
    private final Statistic statistic;
    private ExecutorService threadPool;
    private int lastPlayerChunkX = 0;
    private int lastPlayerChunkZ = 0;

    public World(Statistic statistic) {
        this.statistic = statistic;
    }

    @Override
    public void init(Renderer renderer, Camera camera) {
        this.renderer = renderer;
        this.camera = camera;
        this.noiseUtil = new NoiseUtil(renderer, camera);

        int processorCount = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(Math.max(1, processorCount - 1));

        if (Constants.WORLD_TYPE == WorldType.NBT) {
            new NbtUtil(renderer).loadWorld();
        } else {
            this.noiseUtil.loadWorld();
            this.lastPlayerChunkX = this.camera.getChunkX();
            this.lastPlayerChunkZ = this.camera.getChunkZ();
        }

        if (Constants.OPTIMIZATION_SHADER_MEMORY) {
            this.renderer.setColorUBO();
        }
    }

    @Override
    public void update() {
        long currentTime = System.nanoTime();
        if (currentTime - lastUpdateTime.get() < UPDATE_INTERVAL || isUpdating) {
            return;
        }

        isUpdating = true;
        lastUpdateTime.set(currentTime);

        threadPool.submit(() -> {
            try {
                if (Constants.WORLD_TYPE == WorldType.NOISE) {
                    updateNoiseChunks();
                }
            } finally {
                isUpdating = false;
            }
        });
    }

    private void updateNoiseChunks() {
        if (Constants.WORLD_TYPE == WorldType.NBT) {
            return;
        }

        int playerChunkX = this.camera.getChunkX();
        int playerChunkZ = this.camera.getChunkZ();

        boolean playerMoved = playerChunkX != lastPlayerChunkX || playerChunkZ != lastPlayerChunkZ;
        if (!playerMoved) {
            return;
        }

        lastPlayerChunkX = playerChunkX;
        lastPlayerChunkZ = playerChunkZ;

        List<Vector3Key> neededKeys = new ArrayList<>();

        for (int dx = playerChunkX - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx <= playerChunkX + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx += Constants.NOISE_CHUNK_SIZE) {
            for (int dz = playerChunkZ - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz <= playerChunkZ + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz += Constants.NOISE_CHUNK_SIZE) {
                int[][] heightMap = noiseUtil.generateHeightMap(dx, dz);
                int maxHeight = noiseUtil.heightMapMaxHeight(heightMap);
                for (int dy = 0; dy <= maxHeight; dy += Constants.NOISE_CHUNK_SIZE) {
                    neededKeys.add(new Vector3Key(dx, dy, dz));
                }
            }
        }

        for (Vector3Key chunkKey : chunks.keySet()) {
            if (!neededKeys.contains(chunkKey)) {
                World.chunks.remove(chunkKey);
            }
        }

        List<Chunk> newChunks = new ArrayList<>();

        for (Vector3Key chunkKey : neededKeys) {
            if (World.chunks.get(chunkKey) == null) {
                Chunk chunk = new Chunk(chunkKey.getX(), chunkKey.getY(), chunkKey.getZ());
                int[][] heightMap = this.noiseUtil.generateHeightMap(chunkKey.getX(), chunkKey.getZ());
                chunk.setData(this.noiseUtil.heightMapSlice(heightMap, chunkKey.getY()));
                newChunks.add(chunk);
                World.chunks.put(new Vector3Key(chunk.getXOffset(), chunk.getYOffset(), chunk.getZOffset()), chunk);
            }
        }

        for (Chunk chunk : newChunks) {
            noiseUtil.updateChunkNeighbours(chunk);
            chunk.loadData();
            chunk.setNeedsBufferLoad(true);
        }
    }

    @Override
    public void render() {
        int renderCount = 0;

        for (Chunk chunk : World.chunks.values()) {
            try {
                if (chunk.needsBufferLoad()) {
                    chunk.loadBuffers(this.renderer.getProgramId());
                }
                if (Constants.OPTIMIZATION_FRUSTUM_CULLING) {
                    boolean isOnFrustum = this.camera.isOnFrustum(
                            chunk.getXOffset(),
                            chunk.getYOffset(),
                            chunk.getZOffset());
                    if (isOnFrustum) {
                        chunk.render();
                        renderCount++;
                    }
                } else {
                    chunk.render();
                    renderCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.statistic.setRenderedChunkCount(renderCount);
    }

    @Override
    public void destroy() {

    }
}
