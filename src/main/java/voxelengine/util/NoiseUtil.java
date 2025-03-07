package voxelengine.util;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.util.voxel.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NoiseUtil {
    private final SimplexNoise simplexNoise;
    private final Renderer renderer;
    private final Camera camera;

    public NoiseUtil(Renderer renderer, Camera camera) {
        this.simplexNoise = new SimplexNoise(Constants.WORLD_SEED);
        this.renderer = renderer;
        this.camera = camera;
    }

    public List<Chunk> loadWorld() {
        int playerX = (int) this.camera.getPosition().x;
        int playerZ = (int) this.camera.getPosition().z;

        int playerChunkX = (playerX / Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;
        int playerChunkZ = (playerZ / Constants.NOISE_CHUNK_SIZE) * Constants.NOISE_CHUNK_SIZE;

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

    public Color[][][] generateChunkData(int chunkOffsetX, int chunkOffsetZ) {
        Color[][][] data = new Color[Constants.NOISE_CHUNK_SIZE][Constants.NOISE_CHUNK_MAX_Y][Constants.NOISE_CHUNK_SIZE];
        for (int x = 0; x < Constants.NOISE_CHUNK_SIZE; x++) {
            for (int z = 0; z < Constants.NOISE_CHUNK_SIZE; z++) {
                float worldX = (chunkOffsetX + x) * 0.01f;
                float worldZ = (chunkOffsetZ + z) * 0.01f;
                double heightNoise = (simplexNoise.noise(worldX, worldZ) + 1) * 0.5;
                int terrainHeight = (int) (heightNoise * Constants.NOISE_CHUNK_MAX_Y * 0.8f);

                for (int y = 0; y < Constants.NOISE_CHUNK_MAX_Y; y++) {
                    if (y < terrainHeight) {
                        Color color;

                        if (y >= terrainHeight - 1) {
                            color = new Color(0.2f, 0.8f, 0.2f);
                        } else if (y >= terrainHeight - 4) {
                            color = new Color(0.6f, 0.4f, 0.2f);
                        } else {
                            float depth = 1.0f - ((float) (y) / terrainHeight) * 0.5f;
                            color = new Color(0.5f * depth, 0.5f * depth, 0.5f * depth);
                        }
                        double caveNoise = simplexNoise.noise(
                                worldX * 3,
                                y * 0.1f,
                                worldZ * 3);

                        if (caveNoise > 0.75) {
                            data[x][y][z] = null;
                        } else {
                            data[x][y][z] = color;
                        }
                    }
                }
            }
        }
        return data;
    }

    private static class SimplexNoise {
        private final int[] p;

        public SimplexNoise(long seed) {
            Random random = new Random(seed);
            int[] permutation = new int[256];
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
