package voxelengine.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import voxelengine.core.Renderer;
import voxelengine.util.voxel.Color;

public class NoiseUtil {
    public Renderer renderer;
    private final SimplexNoise simplexNoise;

    public NoiseUtil() {
        this.simplexNoise = new SimplexNoise(Constants.WORLD_SEED);
    }

    public List<Chunk> loadWorld() {
        List<Chunk> chunks = new ArrayList<>();
        int voxelCount = 0;

        for (int dx = -2; dx < 2; dx++) {
            for (int dz = -2; dz < 2; dz++) {
                Chunk chunk = generateChunk(dx, dz);
                chunks.add(chunk);

                for (int x = 0; x < chunk.xSize; x++) {
                    for (int y = 0; y < chunk.ySize; y++) {
                        for (int z = 0; z < chunk.zSize; z++) {
                            if (chunk.data[x][y][z] != null) {
                                voxelCount++;
                            }
                        }
                    }
                }
            }
        }

        this.renderer.numVoxels = voxelCount;
        return chunks;
    }

    public Chunk generateChunk(int chunkX, int chunkZ) {
        Chunk chunk = new Chunk();
        chunk.xOffset = chunkX * Constants.NOISE_CHUNK_SIZE;
        chunk.yOffset = 0;
        chunk.zOffset = chunkZ * Constants.NOISE_CHUNK_SIZE;

        for (int x = 0; x < chunk.xSize; x++) {
            for (int z = 0; z < chunk.zSize; z++) {
                float worldX = (chunk.xOffset + x) * 0.01f;
                float worldZ = (chunk.zOffset + z) * 0.01f;

                double heightNoise = (simplexNoise.noise(worldX, worldZ) + 1) * 0.5;
                int terrainHeight = (int) (heightNoise * Constants.NOISE_CHUNK_MAX_Y * 0.8f);

                for (int y = 0; y < chunk.ySize; y++) {
                    int worldY = chunk.yOffset + y;

                    if (worldY < terrainHeight) {
                        Color color = new Color();

                        if (worldY >= terrainHeight - 1) {
                            color.r = 0.2f;
                            color.g = 0.8f;
                            color.b = 0.2f;
                        } else if (worldY >= terrainHeight - 4) {
                            color.r = 0.6f;
                            color.g = 0.4f;
                            color.b = 0.2f;
                        } else {
                            float depth = 1.0f - ((float) (worldY) / terrainHeight) * 0.5f;
                            color.r = 0.5f * depth;
                            color.g = 0.5f * depth;
                            color.b = 0.5f * depth;
                        }
                        double caveNoise = simplexNoise.noise(
                                worldX * 3,
                                worldY * 0.1f,
                                worldZ * 3);

                        if (caveNoise > 0.75) {
                            chunk.data[x][y][z] = null;
                        } else {
                            chunk.data[x][y][z] = color;
                        }
                    } else {
                        chunk.data[x][y][z] = null;
                    }
                }
            }
        }

        return chunk;
    }

    private class SimplexNoise {
        private final int[] permutation;
        private final int[] p;

        public SimplexNoise(long seed) {
            Random random = new Random(seed);
            permutation = new int[256];
            p = new int[512];

            for (int i = 0; i < 256; i++) {
                permutation[i] = i;
            }

            for (int i = 0; i < 256; i++) {
                int j = random.nextInt(256);
                int temp = permutation[i];
                permutation[i] = permutation[j];
                permutation[j] = temp;
            }

            for (int i = 0; i < 512; i++) {
                p[i] = permutation[i & 255];
            }
        }

        public double noise(double x, double y) {
            return perlinNoise(x, y);
        }

        public double noise(double x, double y, double z) {
            return perlinNoise(x, y) * 0.5 + perlinNoise(y, z) * 0.3 + perlinNoise(x, z) * 0.2;
        }

        private double perlinNoise(double x, double y) {
            int X = (int) Math.floor(x) & 255;
            int Y = (int) Math.floor(y) & 255;

            x -= Math.floor(x);
            y -= Math.floor(y);

            double u = fade(x);
            double v = fade(y);

            int A = p[X] + Y;
            int B = p[X + 1] + Y;

            return lerp(v,
                    lerp(u, grad(p[A], x, y), grad(p[B], x - 1, y)),
                    lerp(u, grad(p[A + 1], x, y - 1), grad(p[B + 1], x - 1, y - 1)));
        }

        private double fade(double t) {
            return t * t * t * (t * (t * 6 - 15) + 10);
        }

        private double lerp(double t, double a, double b) {
            return a + t * (b - a);
        }

        private double grad(int hash, double x, double y) {
            int h = hash & 15;
            double u = h < 8 ? x : y;
            double v = h < 4 ? y : x;
            return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
        }
    }
}