package voxelengine.examples;

import voxelengine.core.State;
import voxelengine.util.Chunk;
import voxelengine.util.Constants;
import voxelengine.util.Log;
import voxelengine.util.Vector3Key;
import voxelengine.util.WorldType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class World {
    private static final long UPDATE_INTERVAL = 100_000_000;
    public static final Map<Vector3Key, Chunk> chunks = new ConcurrentHashMap<>();
    private long lastUpdateTime = 0;
    private ExecutorService threadPool;
    private boolean isUpdating = false;
    private int lastPlayerChunkX = 0;
    private int lastPlayerChunkZ = 0;
    private int renderedChunks = 0;
    private boolean initialWorldLoaded = false;

    public int getRenderedChunks() {
        return renderedChunks;
    }

    public void init() {
        threadPool = Executors.newFixedThreadPool(State.PROCESSOR_COUNT);
        if (Constants.WORLD_TYPE == WorldType.NBT) {
            State.nbtUtil.loadWorld();
        } else {
            State.noiseUtil.loadWorld();
            this.lastPlayerChunkX = State.camera.getChunkX();
            this.lastPlayerChunkZ = State.camera.getChunkZ();
        }
        Log.info("World loaded");
        if (Constants.OPTIMIZATION_SHADER_MEMORY) {
            State.renderer.setColorUBO();
        }
        threadPool.submit(() -> {
            try {
                List<Chunk> sortedChunks = new ArrayList<>(chunks.values());
                sortedChunks.sort((chunk1, chunk2) -> {
                    int distance1 = calculateDistanceToPlayer(chunk1);
                    int distance2 = calculateDistanceToPlayer(chunk2);
                    return Integer.compare(distance1, distance2);
                });
                for (Chunk chunk : sortedChunks) {
                    chunk.loadNeighbors();
                    chunk.loadData();
                    chunk.setNeedsBufferLoad(true);
                }
            } finally {
                initialWorldLoaded = true;
            }
        });
    }

    private int calculateDistanceToPlayer(Chunk chunk) {
        int dx = chunk.getXOffset() - lastPlayerChunkX;
        int dz = chunk.getZOffset() - lastPlayerChunkZ;
        return dx * dx + dz * dz;
    }

    public void update() {
        if (!initialWorldLoaded) {
            return;
        }
        if (Constants.WORLD_TYPE == WorldType.NBT) {
            return;
        }
        long currentTime = System.nanoTime();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL || isUpdating) {
            return;
        }
        isUpdating = true;
        lastUpdateTime = currentTime;
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
                World.chunks.put(chunk.getWorldKey(), chunk);
            }
        }

        newChunks.sort((chunk1, chunk2) -> {
            int distance1 = calculateDistanceToPlayer(chunk1);
            int distance2 = calculateDistanceToPlayer(chunk2);
            return Integer.compare(distance1, distance2);
        });

        for (Chunk chunk : newChunks) {
            chunk.loadNeighbors();
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
