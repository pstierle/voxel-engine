package voxelengine.examples;

import org.joml.Vector2d;
import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.util.Chunk;
import voxelengine.util.Constants;
import voxelengine.util.NbtUtil;
import voxelengine.util.NoiseUtil;
import voxelengine.util.WorldType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
                if (Constants.OPTIMIZATION_FRUSTUM_CULLING) {
                    checkChunkFrustum();
                }
                if (Constants.WORLD_TYPE == WorldType.NOISE) {
                    updateNoiseChunks();
                }
            } finally {
                isUpdating = false;
            }
        });
    }

    private void checkChunkFrustum() {
        chunksLock.lock();
        try {
            for (int i = 0; i < this.chunks.size(); i++) {
                int chunkSize = Constants.WORLD_TYPE == WorldType.NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_SIZE;
                int height = Constants.WORLD_TYPE == WorldType.NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_MAX_Y;
                boolean isOnFrustum = this.camera.isOnFrustum(
                        chunks.get(i).getXOffset(),
                        chunks.get(i).getZOffset(),
                        chunkSize,
                        height);
                chunks.get(i).setIsOnFrustum(isOnFrustum);
            }
        } finally {
            chunksLock.unlock();
        }
    }

    private void updateNoiseChunks() {
        int playerChunkX = this.camera.getChunkX();
        int playerChunkZ = this.camera.getChunkZ();

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
            for (int i = 0; i < this.chunks.size(); i++) {
                Vector2d pos = new Vector2d(chunks.get(i).getXOffset(), chunks.get(i).getZOffset());
                if (!visibleChunkPositions.contains(pos)) {
                    chunksToUpdate.add(chunks.get(i));
                } else {
                    visibleChunks.add(chunks.get(i));
                }
            }
        } finally {
            chunksLock.unlock();
        }

        List<Vector2d> positionsToAdd = new ArrayList<>();

        for (Vector2d pos : visibleChunkPositions) {
            boolean chunkExists = false;
            for (Chunk chunk : visibleChunks) {
                if (chunk.getXOffset() == (int) pos.x && chunk.getZOffset() == (int) pos.y) {
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
            Chunk chunk = chunksToUpdate.get(i);
            Vector2d position = positionsToAdd.get(i);

            chunk.setXOffset((int) position.x);
            chunk.setZOffset((int) position.y);

            if (Constants.OPTIMIZATION_FRUSTUM_CULLING) {
                boolean isOnFrustum = this.camera.isOnFrustum(
                        chunk.getXOffset(),
                        chunk.getZOffset(),
                        Constants.NOISE_CHUNK_SIZE,
                        Constants.NOISE_CHUNK_MAX_Y);
                chunk.setIsOnFrustum(isOnFrustum);
            }

            float[][] heightMap = this.noiseUtil.generateHeightMap(chunk.getXOffset(), chunk.getZOffset());

            chunk.setHeightMapData(heightMap);
            this.noiseUtil.updateChunkNeighbourHeightMap(chunks, chunk);
            chunk.loadDataHeightMap();
            chunk.setNeedsBufferLoad(true);
        }
    }

    @Override
    public void render() {
        int uploadedCount = 0;
        for (int i = 0; i < this.chunks.size(); i++) {
            if (this.chunks.get(i).needsBufferLoad() && uploadedCount < Constants.NOISE_CHUNK_BUFFER_UPLOADS_PER_FRAME) {
                this.chunks.get(i).loadBuffers(this.renderer.getProgramId());
                uploadedCount++;
            }
            if (Constants.OPTIMIZATION_FRUSTUM_CULLING) {
                if (chunks.get(i).isOnFrustum()) {
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
