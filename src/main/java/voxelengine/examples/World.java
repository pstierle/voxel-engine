package voxelengine.examples;

import org.joml.Vector2d;
import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.util.Chunk;
import voxelengine.util.Constants;
import voxelengine.util.NbtUtil;
import voxelengine.util.NoiseUtil;
import voxelengine.util.voxel.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicLong;

public class World implements BaseExample {
    public Renderer renderer;
    public NbtUtil nbtUtil;
    public NoiseUtil noiseUtil;
    public List<Chunk> chunks;
    public Camera camera;
    private AtomicLong lastUpdateTime = new AtomicLong(0);
    private static final long UPDATE_INTERVAL = 200_000_000;
    private ExecutorService threadPool;
    private ReentrantLock chunksLock = new ReentrantLock();
    private boolean isUpdating = false;

    @Override
    public void init() {
        // Create a thread pool for chunk updates
        int processorCount = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(Math.max(1, processorCount - 1));

        if (Constants.LOAD_WORLD_NBT) {
            this.chunks = this.nbtUtil.loadWorld();
        } else {
            this.chunks = new CopyOnWriteArrayList<>(this.noiseUtil.loadWorld());
        }
    }

    @Override
    public void update() {
        if (Constants.LOAD_WORLD_NBT) {
            return;
        }

        long currentTime = System.nanoTime();
        if (currentTime - lastUpdateTime.get() < UPDATE_INTERVAL || isUpdating) {
            return;
        }

        isUpdating = true;
        lastUpdateTime.set(currentTime);

        threadPool.submit(() -> {
            try {
                updateChunks();
            } finally {
                isUpdating = false;
            }
        });
    }

    private void updateChunks() {
        int playerX = (int) this.camera.position.x;
        int playerZ = (int) this.camera.position.z;
        int playerChunkX = Math.floorDiv(playerX, Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;
        int playerChunkZ = Math.floorDiv(playerZ, Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;

        Set<Vector2d> visibleChunkPositions = new HashSet<>();
        int radius = Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE;

        for (int dx = playerChunkX - radius; dx <= playerChunkX + radius; dx += Constants.NOISE_CHUNK_SIZE) {
            for (int dz = playerChunkZ - radius; dz <= playerChunkZ + radius; dz += Constants.NOISE_CHUNK_SIZE) {
                visibleChunkPositions.add(new Vector2d(dx, dz));
            }
        }

        List<Chunk> chunksToUpdate = new ArrayList<>();
        List<Chunk> visibleChunks = new ArrayList<>();

        chunksLock.lock();
        try {
            for (Chunk chunk : this.chunks) {
                Vector2d pos = new Vector2d(chunk.xOffset, chunk.zOffset);
                if (!visibleChunkPositions.contains(pos)) {
                    chunksToUpdate.add(chunk);
                } else {
                    visibleChunks.add(chunk);
                }
            }
        } finally {
            chunksLock.unlock();
        }

        List<Vector2d> positionsToAdd = new ArrayList<>();
        for (Vector2d pos : visibleChunkPositions) {
            boolean chunkExists = false;
            for (Chunk chunk : visibleChunks) {
                if (chunk.xOffset == (int) pos.x && chunk.zOffset == (int) pos.y) {
                    chunkExists = true;
                    break;
                }
            }

            if (!chunkExists) {
                positionsToAdd.add(pos);
            }
        }

        positionsToAdd.sort((a, b) -> {
            double distA = Math.sqrt(Math.pow(a.x - playerChunkX, 2) + Math.pow(a.y - playerChunkZ, 2));
            double distB = Math.sqrt(Math.pow(b.x - playerChunkX, 2) + Math.pow(b.y - playerChunkZ, 2));
            return Double.compare(distA, distB);
        });

        int updateCount = Math.min(positionsToAdd.size(), chunksToUpdate.size());
        for (int i = 0; i < updateCount; i++) {
            final int index = i;
            Chunk chunk = chunksToUpdate.get(index);
            Vector2d position = positionsToAdd.get(index);

            chunk.xOffset = (int) position.x;
            chunk.zOffset = (int) position.y;

            Color[][][] chunkData = this.noiseUtil.generateChunkData(chunk.xOffset, chunk.zOffset);
            chunk.loadData(chunkData);
            chunk.needsBufferUpdate = true;
        }
    }

    @Override
    public void render() {
        int uploadedCount = 0;
        for (Chunk chunk : this.chunks) {
            if (chunk.needsBufferUpdate && uploadedCount <= 2) {
                chunk.uploadBuffers(this.renderer.programId);
                chunk.render();
                uploadedCount++;
            } else {
                chunk.render();
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