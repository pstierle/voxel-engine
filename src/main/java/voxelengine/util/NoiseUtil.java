package voxelengine.util;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.util.voxel.Color;

import java.util.ArrayList;
import java.util.List;

public class NoiseUtil {
    private final FastNoiseLite baseNoise;
    private final FastNoiseLite detailNoise;
    private final FastNoiseLite largeFeatureNoise;
    private final FastNoiseLite caveNoise;
    private final Renderer renderer;
    private final Camera camera;

    public NoiseUtil(Renderer renderer, Camera camera) {
        this.baseNoise = new FastNoiseLite(Constants.WORLD_SEED);
        this.baseNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.baseNoise.SetFrequency(0.01f);

        this.detailNoise = new FastNoiseLite(Constants.WORLD_SEED);
        this.detailNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.detailNoise.SetFrequency(0.02f);

        this.largeFeatureNoise = new FastNoiseLite(Constants.WORLD_SEED);
        this.largeFeatureNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.largeFeatureNoise.SetFrequency(0.005f);

        this.caveNoise = new FastNoiseLite(Constants.WORLD_SEED);
        this.caveNoise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        this.caveNoise.SetFrequency(0.03f);

        this.renderer = renderer;
        this.camera = camera;
    }

    public Color[][][] generateChunkData(int chunkOffsetX, int chunkOffsetZ) {
        Color[][][] data = new Color[Constants.NOISE_CHUNK_SIZE][Constants.NOISE_CHUNK_MAX_Y][Constants.NOISE_CHUNK_SIZE];
        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                float worldX = (float) chunkOffsetX + x;
                float worldZ = (float) chunkOffsetZ + z;

                double baseHeightNoise = (baseNoise.GetNoise(worldX, worldZ) + 1) * 0.5;
                double detailNoiseValue = (detailNoise.GetNoise(worldX, worldZ) + 1) * 0.25;
                double largeFeatureNoiseValue = (largeFeatureNoise.GetNoise(worldX, worldZ) + 1) * 0.25;

                double heightNoise = baseHeightNoise + detailNoiseValue + largeFeatureNoiseValue;

                int terrainHeight = (int) (heightNoise * Constants.NOISE_CHUNK_MAX_Y * 0.8f);

                for (int y = 0; y < Constants.NOISE_CHUNK_MAX_Y; y++) {
                    Color color = null;

                    if (y < terrainHeight) {
                        if (y >= terrainHeight - 1) {
                            color = new Color(0.2f, 0.8f, 0.2f);
                        } else if (y >= terrainHeight - 4) {
                            color = new Color(0.6f, 0.4f, 0.2f);
                        } else {
                            float depth = 1.0f - ((float) (y) / terrainHeight) * 0.5f;
                            color = new Color(0.5f * depth, 0.5f * depth, 0.5f * depth);
                        }

                        double caveNoiseValue = caveNoise.GetNoise(worldX * 0.3f, y * 0.1f, worldZ * 0.3f);
                        if (caveNoiseValue > 0.75 && color.getR() < 0.6f) {
                            color = null;
                        }
                    }

                    data[x][y][z] = color;
                }
            }
        }
        return data;
    }

    public List<Chunk> loadWorld() {
        int playerX = (int) this.camera.getPosition().x;
        int playerZ = (int) this.camera.getPosition().z;

        int playerChunkX = Math.floorDiv(playerX, Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;
        int playerChunkZ = Math.floorDiv(playerZ, Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;

        List<Chunk> chunks = new ArrayList<>();

        for (int dx = playerChunkX - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx <= playerChunkX + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dx += Constants.NOISE_CHUNK_SIZE) {
            for (int dz = playerChunkZ - Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz <= playerChunkZ + Constants.NOISE_CHUNK_RADIUS * Constants.NOISE_CHUNK_SIZE; dz += Constants.NOISE_CHUNK_SIZE) {
                Chunk chunk = new Chunk(dx, 0, dz, Constants.NOISE_CHUNK_SIZE, Constants.NOISE_CHUNK_MAX_Y, Constants.NOISE_CHUNK_SIZE);
                Color[][][] chunkData = generateChunkData(dx, dz);
                chunk.loadData(chunkData);
                chunk.uploadBuffers(this.renderer.getProgramId());
                chunks.add(chunk);
            }
        }
        return chunks;
    }
}
