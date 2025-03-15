package voxelengine.util;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NoiseUtil {
    private final FastNoiseLite baseNoise;
    private final FastNoiseLite detailNoise;
    private final FastNoiseLite largeFeatureNoise;
    private final Renderer renderer;
    private final Camera camera;

    public NoiseUtil(Renderer renderer, Camera camera) {
        this.baseNoise = new FastNoiseLite(Constants.NOISE_WORLD_SEED);
        this.baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.baseNoise.SetFrequency(0.01f);

        this.detailNoise = new FastNoiseLite(Constants.NOISE_WORLD_SEED);
        this.detailNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.detailNoise.SetFrequency(0.02f);

        this.largeFeatureNoise = new FastNoiseLite(Constants.NOISE_WORLD_SEED);
        this.largeFeatureNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.largeFeatureNoise.SetFrequency(0.005f);

        this.renderer = renderer;
        this.camera = camera;
    }

    public float[][] generateHeightMap(int chunkOffsetX, int chunkOffsetZ) {
        float[][] heightMap = new float[Constants.NOISE_CHUNK_SIZE][Constants.NOISE_CHUNK_SIZE];

        float mountainScale = Constants.NOISE_CHUNK_MAX_Y * 0.9f;
        float valleyScale = Constants.NOISE_CHUNK_MAX_Y * 0.1f;

        float baseFrequency = 0.015f;
        float mountainFrequency = 0.05f;
        float detailFrequency = 0.1f;

        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                float worldX = (chunkOffsetX + x);
                float worldZ = (chunkOffsetZ + z);

                float continentNoise = largeFeatureNoise.GetNoise(worldX * baseFrequency, worldZ * baseFrequency);
                float exaggeratedNoise = (float) Math.pow(Math.abs(continentNoise), 0.5) * Math.signum(continentNoise);
                float mountainNoise = baseNoise.GetNoise(worldX * mountainFrequency, worldZ * mountainFrequency) * 0.8f;
                float detailValue = detailNoise.GetNoise(worldX * detailFrequency, worldZ * detailFrequency) * 0.2f;
                float microDetail = detailNoise.GetNoise(worldX * detailFrequency * 2, worldZ * detailFrequency * 2) * 0.1f;
                float combinedNoise = exaggeratedNoise + mountainNoise + detailValue + microDetail;

                combinedNoise = (combinedNoise + 1.6f) * 0.31f;
                combinedNoise = Math.max(0.0f, Math.min(1.0f, combinedNoise)); // Clamp to 0-1
                float heightValue = (float) Math.pow(combinedNoise, 2.5);
                float terrainHeight;
                if (heightValue > 0.5f) {
                    float mountainFactor = (float) Math.pow((heightValue - 0.5f) * 2, 1.5);
                    terrainHeight = (mountainScale * mountainFactor) + (Constants.NOISE_CHUNK_MAX_Y * 0.25f);
                } else {
                    terrainHeight = valleyScale + heightValue * (Constants.NOISE_CHUNK_MAX_Y * 0.3f);
                }

                if (heightValue > 0.6f) {
                    float ridgeNoise = Math.abs(detailNoise.GetNoise(worldX * 0.08f + 100, worldZ * 0.08f + 100));
                    if (ridgeNoise > 0.5f) {
                        terrainHeight += (ridgeNoise - 0.5f) * Constants.NOISE_CHUNK_MAX_Y * 0.4f;
                    }
                }

                if (heightValue > 0.4f && heightValue < 0.6f) {
                    float cliffNoise = Math.abs(baseNoise.GetNoise(worldX * 0.03f + 200, worldZ * 0.03f + 200));
                    if (cliffNoise > 0.7f) {
                        terrainHeight += (cliffNoise - 0.7f) * Constants.NOISE_CHUNK_MAX_Y * 0.5f;
                    }
                }

                terrainHeight = Math.min(terrainHeight, Constants.NOISE_CHUNK_MAX_Y);
                heightMap[x][z] = terrainHeight;
            }
        }
        return heightMap;
    }

    public List<Chunk> loadWorld() {
        int playerChunkX = this.camera.getChunkX();
        int playerChunkZ = this.camera.getChunkZ();

        List<Chunk> chunks = new ArrayList<>();

        for (int dx = playerChunkX - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx <= playerChunkX + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx += Constants.NOISE_CHUNK_SIZE) {
            for (int dz = playerChunkZ - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz <= playerChunkZ + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz += Constants.NOISE_CHUNK_SIZE) {
                Chunk chunk = new Chunk(dx, 0, dz, Constants.NOISE_CHUNK_SIZE, Constants.NOISE_CHUNK_MAX_Y, Constants.NOISE_CHUNK_SIZE);
                float[][] heightMap = generateHeightMap(chunk.getXOffset(), chunk.getZOffset());
                chunk.setHeightMapData(heightMap);
                chunks.add(chunk);
            }
        }

        for (Chunk chunk : chunks) {
            updateChunkNeighbourHeightMap(chunks, chunk);
        }

        for (int i = 0; i < chunks.size(); i++) {
            Log.info(String.format("Loaded chunk %d/%d", i + 1, chunks.size()));
            chunks.get(i).loadDataHeightMap();
            chunks.get(i).loadBuffers(this.renderer.getProgramId());
        }

        return chunks;
    }

    public void updateChunkNeighbourHeightMap(List<Chunk> chunks, Chunk chunk) {
        Map<Direction, float[][]> neighborHeightMap = new EnumMap<>(Direction.class);

        // ignore top/bottom for noise
        neighborHeightMap.put(Direction.FRONT, null);
        neighborHeightMap.put(Direction.BACK, null);
        neighborHeightMap.put(Direction.LEFT, null);
        neighborHeightMap.put(Direction.RIGHT, null);

        for (Chunk neighbourChunk : chunks) {
            Direction neighborDirection = null;
            if (neighbourChunk.getZOffset() == chunk.getZOffset() + chunk.getZSize() && neighbourChunk.getXOffset() == chunk.getXOffset()) {
                neighborDirection = Direction.FRONT;
            } else if (neighbourChunk.getZOffset() == chunk.getZOffset() - chunk.getZSize() && neighbourChunk.getXOffset() == chunk.getXOffset()) {
                neighborDirection = Direction.BACK;
            } else if (neighbourChunk.getXOffset() == chunk.getXOffset() + chunk.getXSize() && neighbourChunk.getZOffset() == chunk.getZOffset()) {
                neighborDirection = Direction.RIGHT;
            } else if (neighbourChunk.getXOffset() == chunk.getXOffset() - chunk.getXSize() && neighbourChunk.getZOffset() == chunk.getZOffset()) {
                neighborDirection = Direction.LEFT;
            }
            if (neighborDirection != null) {
                neighborHeightMap.put(neighborDirection, neighbourChunk.getHeightMapData());
            }
        }
        // fill neighbours that are not yet generated
        neighborHeightMap.forEach((direction, heightMap) -> {
            if (heightMap == null && direction != Direction.TOP && direction != Direction.BOTTOM) {
                int dx = chunk.getXOffset();
                int dz = chunk.getZOffset();
                switch (direction) {
                    case FRONT:
                        dz += Constants.NOISE_CHUNK_SIZE;
                        break;
                    case BACK:
                        dz -= Constants.NOISE_CHUNK_SIZE;
                        break;
                    case LEFT:
                        dx -= Constants.NOISE_CHUNK_SIZE;
                        break;
                    case RIGHT:
                        dx += Constants.NOISE_CHUNK_SIZE;
                        break;
                }
                neighborHeightMap.put(direction, generateHeightMap(dx, dz));
            }
        });
        chunk.setNeighborHeightMap(neighborHeightMap);
    }
}
