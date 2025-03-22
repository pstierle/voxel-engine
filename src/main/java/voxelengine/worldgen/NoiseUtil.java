package voxelengine.worldgen;

import voxelengine.core.State;
import voxelengine.examples.World;
import voxelengine.util.Chunk;
import voxelengine.util.ColorUtil;
import voxelengine.util.Constants;
import voxelengine.util.Vector3Key;
import voxelengine.util.VectorXZKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NoiseUtil {
    private static final float SAND_LEVEL = Constants.NOISE_CHUNK_MAX_Y * 0.25f;
    private static final float MOUNTAIN_LEVEL = Constants.NOISE_CHUNK_MAX_Y * 0.65f;

    private final FastNoiseLite baseNoise;
    private final FastNoiseLite detailNoise;
    private final FastNoiseLite largeFeatureNoise;
    private final FastNoiseLite biomeNoise;
    private final FastNoiseLite erosionNoise;

    private final Map<VectorXZKey, int[][]> heightMapCache = new ConcurrentHashMap<>();
    private static final int HEIGHT_MAP_CACHE_SIZE = 64;

    public NoiseUtil() {
        this.baseNoise = new FastNoiseLite(Constants.NOISE_WORLD_SEED);
        this.baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.baseNoise.SetFrequency(0.01f);

        this.detailNoise = new FastNoiseLite(Constants.NOISE_WORLD_SEED + 1);
        this.detailNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.detailNoise.SetFrequency(0.02f);

        this.largeFeatureNoise = new FastNoiseLite(Constants.NOISE_WORLD_SEED + 2);
        this.largeFeatureNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.largeFeatureNoise.SetFrequency(0.005f);

        this.biomeNoise = new FastNoiseLite(Constants.NOISE_WORLD_SEED + 3);
        this.biomeNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.biomeNoise.SetFrequency(0.002f);
        this.biomeNoise.SetFractalType(FastNoiseLite.FractalType.FBm);
        this.biomeNoise.SetFractalOctaves(4);

        this.erosionNoise = new FastNoiseLite(Constants.NOISE_WORLD_SEED + 4);
        this.erosionNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.erosionNoise.SetFrequency(0.04f);
        this.erosionNoise.SetFractalType(FastNoiseLite.FractalType.Ridged);
        this.erosionNoise.SetFractalOctaves(3);
    }

    public void loadWorld() {
        int playerChunkX = State.camera.getChunkX();
        int playerChunkZ = State.camera.getChunkZ();

        for (int dx = playerChunkX - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx <= playerChunkX + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx += Constants.NOISE_CHUNK_SIZE) {
            for (int dz = playerChunkZ - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz <= playerChunkZ + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz += Constants.NOISE_CHUNK_SIZE) {
                int[][] heightMap = generateHeightMap(dx, dz);
                int maxHeight = heightMapMaxHeight(heightMap);
                for (int dy = 0; dy <= maxHeight; dy += Constants.NOISE_CHUNK_SIZE) {
                    Chunk chunk = new Chunk(dx, dy, dz);
                    chunk.setData(heightMapSlice(heightMap, dy));
                    World.chunks.put(new Vector3Key(dx, dy, dz), chunk);
                }
            }
        }
    }

    public int[][] generateHeightMap(int chunkOffsetX, int chunkOffsetZ) {
        VectorXZKey cacheKey = new VectorXZKey(chunkOffsetX, chunkOffsetZ);
        if (heightMapCache.containsKey(cacheKey)) {
            return heightMapCache.get(cacheKey);
        }

        int[][] heightMap = new int[Constants.NOISE_CHUNK_SIZE][Constants.NOISE_CHUNK_SIZE];

        float mountainScale = Constants.NOISE_CHUNK_MAX_Y * 0.9f;
        float valleyScale = Constants.NOISE_CHUNK_MAX_Y * 0.1f;
        float oceanDepth = Constants.NOISE_CHUNK_MAX_Y * 0.15f;
        float plateauHeight = Constants.NOISE_CHUNK_MAX_Y * 0.5f;

        float baseFrequency = 0.015f;
        float mountainFrequency = 0.05f;
        float detailFrequency = 0.1f;

        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                float worldX = (chunkOffsetX + x);
                float worldZ = (chunkOffsetZ + z);

                float biomeValue = biomeNoise.GetNoise(worldX * 0.001f, worldZ * 0.001f);

                float continentNoise = largeFeatureNoise.GetNoise(worldX * baseFrequency, worldZ * baseFrequency);
                float exaggeratedNoise = (float) Math.pow(Math.abs(continentNoise), 0.5) * Math.signum(continentNoise);

                float mountainNoise = baseNoise.GetNoise(worldX * mountainFrequency, worldZ * mountainFrequency) * 0.8f;
                float detailValue = detailNoise.GetNoise(worldX * detailFrequency, worldZ * detailFrequency) * 0.2f;
                float microDetail = detailNoise.GetNoise(worldX * detailFrequency * 2, worldZ * detailFrequency * 2) * 0.1f;

                float erosionValue = erosionNoise.GetNoise(worldX * 0.03f, worldZ * 0.03f) * 0.3f;

                float combinedNoise = exaggeratedNoise + mountainNoise + detailValue + microDetail;

                if (biomeValue > 0.3f) {
                    combinedNoise = combinedNoise * 1.2f + 0.3f;
                } else if (biomeValue < -0.3f) {
                    combinedNoise = combinedNoise * 0.8f - 0.3f;
                } else {
                    float plateauNoise = baseNoise.GetNoise(worldX * 0.02f + 500, worldZ * 0.02f + 500);
                    if (plateauNoise > 0.6f) {
                        combinedNoise = Math.max(combinedNoise, 0.4f);
                    }
                }

                combinedNoise -= Math.abs(erosionValue) * 0.5f;

                combinedNoise = (combinedNoise + 1.6f) * 0.31f;
                combinedNoise = Math.max(0.0f, Math.min(1.0f, combinedNoise)); // Clamp to 0-1

                float heightValue = (float) Math.pow(combinedNoise, 2.2f);

                float terrainHeight;
                if (heightValue > 0.5f) {
                    float mountainFactor = (float) Math.pow((heightValue - 0.5f) * 2, 1.8f);
                    terrainHeight = (mountainScale * mountainFactor) + (Constants.NOISE_CHUNK_MAX_Y * 0.25f);
                } else if (heightValue < 0.2f) {
                    float oceanFactor = 1.0f - (heightValue / 0.2f);
                    terrainHeight = Math.max(0, valleyScale - oceanDepth * oceanFactor);
                } else {
                    terrainHeight = valleyScale + heightValue * (Constants.NOISE_CHUNK_MAX_Y * 0.4f);
                }

                if (heightValue > 0.6f) {
                    float ridgeNoise = Math.abs(detailNoise.GetNoise(worldX * 0.08f + 100, worldZ * 0.08f + 100));
                    if (ridgeNoise > 0.5f) {
                        terrainHeight += (ridgeNoise - 0.5f) * Constants.NOISE_CHUNK_MAX_Y * 0.45f;
                    }
                }

                if (heightValue > 0.35f && heightValue < 0.6f) {
                    float cliffNoise = Math.abs(baseNoise.GetNoise(worldX * 0.03f + 200, worldZ * 0.03f + 200));
                    if (cliffNoise > 0.7f) {
                        terrainHeight += (cliffNoise - 0.7f) * Constants.NOISE_CHUNK_MAX_Y * 0.6f;
                    }
                }

                float plateauDetection = baseNoise.GetNoise(worldX * 0.025f + 300, worldZ * 0.025f + 300);
                if (plateauDetection > 0.7f && heightValue > 0.4f && heightValue < 0.65f) {
                    terrainHeight = Math.max(terrainHeight, plateauHeight +
                            (plateauDetection - 0.7f) * Constants.NOISE_CHUNK_MAX_Y * 0.2f);
                }

                float riverNoise = Math.abs(baseNoise.GetNoise(worldX * 0.01f + 400, worldZ * 0.01f + 400));
                if (riverNoise < 0.05f && heightValue > 0.2f && heightValue < 0.7f) {
                    float riverDepth = (0.05f - riverNoise) * 20 * Constants.NOISE_CHUNK_MAX_Y * 0.2f;
                    if (heightValue > 0.4f) {
                        riverDepth *= 1.5f;
                    }
                    terrainHeight -= riverDepth;
                    terrainHeight = Math.max(terrainHeight, valleyScale);
                }

                terrainHeight = Math.min(terrainHeight, Constants.NOISE_CHUNK_MAX_Y);
                heightMap[x][z] = (int) Math.ceil(terrainHeight);
            }
        }

        smoothHeightMap(heightMap);

        if (heightMapCache.size() > HEIGHT_MAP_CACHE_SIZE) {
            heightMapCache.remove(heightMapCache.keySet().iterator().next());
        }
        heightMapCache.put(cacheKey, heightMap);

        return heightMap;
    }

    private void smoothHeightMap(int[][] heightMap) {
        int[][] smoothed = new int[Constants.NOISE_CHUNK_SIZE][Constants.NOISE_CHUNK_SIZE];

        for (int i = 0; i < Constants.NOISE_CHUNK_SIZE; i++) {
            smoothed[0][i] = heightMap[0][i];
            smoothed[Constants.NOISE_CHUNK_SIZE - 1][i] = heightMap[Constants.NOISE_CHUNK_SIZE - 1][i];
            smoothed[i][0] = heightMap[i][0];
            smoothed[i][Constants.NOISE_CHUNK_SIZE - 1] = heightMap[i][Constants.NOISE_CHUNK_SIZE - 1];
        }

        for (int x = 1; x < Constants.NOISE_CHUNK_SIZE - 1; x++) {
            for (int z = 1; z < Constants.NOISE_CHUNK_SIZE - 1; z++) {
                int maxDiff = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        maxDiff = Math.max(maxDiff,
                                Math.abs(heightMap[x][z] - heightMap[x + dx][z + dz]));
                    }
                }

                if (maxDiff > Constants.NOISE_CHUNK_MAX_Y * 0.15f) {
                    smoothed[x][z] = heightMap[x][z];
                    continue;
                }

                float sum = 0;
                int count = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int weight = (dx == 0 && dz == 0) ? 4 : 1;
                        sum += heightMap[x + dx][z + dz] * weight;
                        count += weight;
                    }
                }
                smoothed[x][z] = (int) Math.ceil(sum / count);
            }
        }

        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                heightMap[x][z] = smoothed[x][z];
            }
        }
    }

    public int heightMapMaxHeight(int[][] heightMap) {
        int maxHeight = 0;
        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                maxHeight = Math.max(maxHeight, heightMap[x][z]);
            }
        }
        return maxHeight;
    }

    public Integer[][][] heightMapSlice(int[][] heightMap, int dy) {
        Integer[][][] data = new Integer[Constants.NOISE_CHUNK_SIZE][Constants.NOISE_CHUNK_SIZE][Constants.NOISE_CHUNK_SIZE];

        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int y = 0; y < Constants.NOISE_CHUNK_SIZE; y++) {
                for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                    int worldY = y + dy;
                    if (worldY <= heightMap[x][z]) {
                        int colorIndex;
                        if (worldY <= 10) {
                            colorIndex = ColorUtil.WATER_COLOR_INDEX;
                        } else if (worldY <= SAND_LEVEL) {
                            colorIndex = ColorUtil.SAND_COLOR_INDEX;
                        } else if (worldY <= MOUNTAIN_LEVEL) {
                            colorIndex = ColorUtil.MOUNTAIN_COLOR_INDEX;
                        } else {
                            colorIndex = ColorUtil.SNOW_COLOR_INDEX;
                        }
                        data[x][y][z] = colorIndex;
                    } else {
                        if (worldY <= 10) {
                            data[x][y][z] = ColorUtil.WATER_COLOR_INDEX;

                        }
                    }
                }
            }
        }
        return data;
    }
}
