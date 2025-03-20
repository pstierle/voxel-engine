package voxelengine.examples;

import org.joml.Vector3d;
import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.core.Statistic;
import voxelengine.util.Chunk;
import voxelengine.util.Constants;
import voxelengine.util.NbtUtil;
import voxelengine.util.NoiseUtil;
import voxelengine.util.WorldType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class World implements BaseExample {
    private static final int CHUNK_SIZE = Constants.WORLD_TYPE == WorldType.NBT ? Constants.NBT_CHUNK_SIZE : Constants.NOISE_CHUNK_SIZE;
    private static final long UPDATE_INTERVAL = 500_000_000;
    private final AtomicLong lastUpdateTime = new AtomicLong(0);
    private boolean isUpdating = false;
    private Renderer renderer;
    private NoiseUtil noiseUtil;
    public static final List<Chunk> chunks = new ArrayList<>();
    private Camera camera;
    private final Statistic statistic;
    private ExecutorService threadPool;

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

        List<Vector3d> radiusPositions = new ArrayList<>();

        for (int dx = playerChunkX - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx <= playerChunkX + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx += Constants.NOISE_CHUNK_SIZE) {
            for (int dz = playerChunkZ - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz <= playerChunkZ + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz += Constants.NOISE_CHUNK_SIZE) {
                radiusPositions.add(new Vector3d(dx, 0, dz));
            }
        }

        List<Vector3d> missingXZPositions = new ArrayList<>();

        for (Vector3d radiusPosition : radiusPositions) {
            int x = (int) radiusPosition.x;
            int z = (int) radiusPosition.z;
            boolean missingXZ = true;
            for (Chunk chunk : World.chunks) {
                if (chunk.getXOffset() == x && chunk.getZOffset() == z) {
                    missingXZ = false;
                    break;
                }
            }
            if (missingXZ) {
                missingXZPositions.add(radiusPosition);
            }
        }

        Iterator<Chunk> iterator = World.chunks.iterator();
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            boolean isOnRadiusPosition = false;
            for (Vector3d radiusPosition : radiusPositions) {
                int x = (int) radiusPosition.x;
                int z = (int) radiusPosition.z;
                if (chunk.getXOffset() == x && chunk.getZOffset() == z) {
                    isOnRadiusPosition = true;
                    break;
                }
            }
            if (!isOnRadiusPosition) {
                iterator.remove();
            }
        }


        List<Chunk> chunksToUpdate = new ArrayList<>();

        for (Vector3d missingXZPosition : missingXZPositions) {
            int x = (int) missingXZPosition.x;
            int z = (int) missingXZPosition.z;
            int[][] heightMap = this.noiseUtil.generateHeightMap(x, z);
            int maxHeight = this.noiseUtil.heightMapMaxHeight(heightMap);
            for (int dy = 0; dy <= maxHeight; dy += Constants.NOISE_CHUNK_SIZE) {
                Chunk chunk = new Chunk(x, dy, z);
                chunk.setData(this.noiseUtil.heightMapSlice(heightMap, dy));
                chunksToUpdate.add(chunk);
            }
        }

        World.chunks.addAll(chunksToUpdate);

        for (Chunk chunk : World.chunks) {
            noiseUtil.updateChunkNeighbours(World.chunks, chunk);
        }

        for (Chunk chunk : chunksToUpdate) {
            chunk.loadData();
            chunk.setNeedsBufferLoad(true);
        }
    }

    @Override
    public void render() {
        int renderCount = 0;

        for (int i = 0; i < World.chunks.size(); i++) {
            try {
                if (World.chunks.get(i).needsBufferLoad()) {
                    World.chunks.get(i).loadBuffers(this.renderer.getProgramId());
                }
                if (Constants.OPTIMIZATION_FRUSTUM_CULLING) {
                    boolean isOnFrustum = this.camera.isOnFrustum(
                            chunks.get(i).getXOffset(),
                            chunks.get(i).getYOffset(),
                            chunks.get(i).getZOffset(),
                            CHUNK_SIZE,
                            CHUNK_SIZE,
                            CHUNK_SIZE);
                    if (isOnFrustum) {
                        World.chunks.get(i).render();
                        renderCount++;
                    }
                } else {
                    World.chunks.get(i).render();
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
