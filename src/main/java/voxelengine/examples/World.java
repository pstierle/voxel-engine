package voxelengine.examples;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.util.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class World implements BaseExample {
    private static final long UPDATE_INTERVAL = 500_000_000;
    private final AtomicLong lastUpdateTime = new AtomicLong(0);
    private boolean isUpdating = false;
    private final ReentrantLock chunksLock = new ReentrantLock();

    private Renderer renderer;
    private NoiseUtil noiseUtil;
    private List<Chunk> chunks;
    private Camera camera;
    private ExecutorService threadPool;

    @Override
    public void init(Renderer renderer, Camera camera) {
        this.renderer = renderer;
        this.camera = camera;
        this.noiseUtil = new NoiseUtil(renderer, camera);

        int processorCount = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(Math.max(1, processorCount - 1));

        if (Constants.WORLD_TYPE == WorldType.NBT) {
            this.chunks = new NbtUtil(renderer).loadWorld();
        } else {
            this.chunks = new CopyOnWriteArrayList<>(this.noiseUtil.loadWorld());
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
        int playerChunkX = this.camera.getChunkX();
        int playerChunkZ = this.camera.getChunkZ();
        int radius = Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE;
        int chunkSize = Constants.NOISE_CHUNK_SIZE;

        Set<ChunkPosition> visibleChunkPositions = new HashSet<>();
        for (int dx = playerChunkX - radius; dx <= playerChunkX + radius; dx += chunkSize) {
            for (int dz = playerChunkZ - radius; dz <= playerChunkZ + radius; dz += chunkSize) {
                visibleChunkPositions.add(new ChunkPosition(dx, dz));
            }
        }

        Map<ChunkPosition, Chunk> visibleChunksMap = new HashMap<>();
        List<Chunk> chunksToUpdate = new ArrayList<>();

        chunksLock.lock();
        try {
            for (Chunk chunk : this.chunks) {
                ChunkPosition pos = new ChunkPosition(chunk.getXOffset(), chunk.getZOffset());
                if (visibleChunkPositions.contains(pos)) {
                    visibleChunksMap.put(pos, chunk);
                } else {
                    chunksToUpdate.add(chunk);
                }
            }
        } finally {
            chunksLock.unlock();
        }

        List<ChunkPosition> positionsToAdd = new ArrayList<>();
        for (ChunkPosition pos : visibleChunkPositions) {
            if (!visibleChunksMap.containsKey(pos)) {
                positionsToAdd.add(pos);
            }
        }

        positionsToAdd.sort(Comparator.comparingDouble(pos ->
                Math.sqrt(Math.pow(pos.x - playerChunkX, 2) + Math.pow(pos.z - playerChunkZ, 2))));

        int updateCount = Math.min(positionsToAdd.size(), chunksToUpdate.size());
        for (int i = 0; i < updateCount; i++) {
            Chunk chunk = chunksToUpdate.get(i);
            ChunkPosition position = positionsToAdd.get(i);

            chunk.setXOffset(position.x);
            chunk.setZOffset(position.z);

            int[][] heightMap = this.noiseUtil.generateHeightMap(position.x, position.z);

            chunk.setHeightMapData(heightMap);
            this.noiseUtil.updateChunkNeighbourHeightMap(chunks, chunk);
            chunk.loadDataHeightMap();
            chunk.setNeedsBufferLoad(true);
        }
    }

    private static class ChunkPosition {
        final int x;
        final int z;

        ChunkPosition(int x, int z) {
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkPosition that = (ChunkPosition) o;
            return x == that.x && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }
    }

    @Override
    public void render() {
        for (int i = 0; i < this.chunks.size(); i++) {
            if (this.chunks.get(i).needsBufferLoad()) {
                this.chunks.get(i).loadBuffers(this.renderer.getProgramId());
            }
            if (Constants.OPTIMIZATION_FRUSTUM_CULLING) {
                int chunkSize = Constants.WORLD_TYPE == WorldType.NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_SIZE;
                int height = Constants.WORLD_TYPE == WorldType.NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_MAX_Y;
                boolean isOnFrustum = this.camera.isOnFrustum(
                        chunks.get(i).getXOffset(),
                        chunks.get(i).getZOffset(),
                        chunkSize,
                        height);
                if (isOnFrustum) {
                    this.chunks.get(i).render();
                }
            } else {
                this.chunks.get(i).render();
            }
        }
    }

    @Override
    public void destroy() {
        if (threadPool != null) {
            threadPool.shutdown();
        }
    }
}
