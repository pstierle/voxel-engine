package voxelengine.examples;

import voxelengine.core.State;
import voxelengine.util.Chunk;
import voxelengine.util.Constants;
import voxelengine.util.Vector3Key;
import voxelengine.util.WorldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class World {
    private static final long UPDATE_INTERVAL = 100_000_000;
    public static final Map<Vector3Key, Chunk> chunks = new ConcurrentHashMap<>();
    private final AtomicLong lastUpdateTime = new AtomicLong(0);
    private ExecutorService threadPool;
    private boolean isUpdating = false;
    private int lastPlayerChunkX = 0;
    private int lastPlayerChunkZ = 0;
    private int renderedChunks = 0;

    public int getRenderedChunks() {
        return renderedChunks;
    }

    public void init() {
        int processorCount = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(Math.max(1, processorCount - 1));

        if (Constants.WORLD_TYPE == WorldType.NBT) {
            State.nbtUtil.loadWorld();
        } else {
            State.noiseUtil.loadWorld();
            this.lastPlayerChunkX = State.camera.getChunkX();
            this.lastPlayerChunkZ = State.camera.getChunkZ();
        }

        if (Constants.OPTIMIZATION_SHADER_MEMORY) {
            State.renderer.setColorUBO();
        }
    }

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

        int playerChunkX = State.camera.getChunkX();
        int playerChunkZ = State.camera.getChunkZ();

        boolean playerMoved = playerChunkX != lastPlayerChunkX || playerChunkZ != lastPlayerChunkZ;
        if (!playerMoved) {
            return;
        }

        lastPlayerChunkX = playerChunkX;
        lastPlayerChunkZ = playerChunkZ;

        List<Vector3Key> neededKeys = new ArrayList<>();

        for (int dx = playerChunkX - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx <= playerChunkX + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx += Constants.NOISE_CHUNK_SIZE) {
            for (int dz = playerChunkZ - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz <= playerChunkZ + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz += Constants.NOISE_CHUNK_SIZE) {
                int[][] heightMap = State.noiseUtil.generateHeightMap(dx, dz);
                int maxHeight = State.noiseUtil.heightMapMaxHeight(heightMap);
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
                int[][] heightMap = State.noiseUtil.generateHeightMap(chunkKey.getX(), chunkKey.getZ());
                chunk.setData(State.noiseUtil.heightMapSlice(heightMap, chunkKey.getY()));
                newChunks.add(chunk);
                World.chunks.put(new Vector3Key(chunk.getXOffset(), chunk.getYOffset(), chunk.getZOffset()), chunk);
            }
        }

        for (Chunk chunk : newChunks) {
            State.noiseUtil.updateChunkNeighbours(chunk);
            chunk.loadData();
            chunk.setNeedsBufferLoad(true);
        }
    }

    public void render() {
        this.renderedChunks = 0;
        for (Chunk chunk : World.chunks.values()) {
            if (chunk.needsBufferLoad()) {
                chunk.loadBuffers(State.renderer.getProgramId());
            }
            if (Constants.OPTIMIZATION_FRUSTUM_CULLING) {
                boolean isOnFrustum = State.camera.isOnFrustum(
                        chunk.getXOffset(),
                        chunk.getYOffset(),
                        chunk.getZOffset());
                if (isOnFrustum) {
                    chunk.render();
                    this.renderedChunks++;
                }
            } else {
                chunk.render();
                this.renderedChunks++;
            }
        }
    }

    public void destroy() {
        if (threadPool != null) {
            threadPool.shutdownNow();
        }
    }
}
