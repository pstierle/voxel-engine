package voxelengine.examples;

import org.joml.Vector2d;
import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.core.Shader;
import voxelengine.util.Chunk;
import voxelengine.util.Constants;
import voxelengine.util.NbtUtil;
import voxelengine.util.NoiseUtil;
import voxelengine.util.voxel.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL46.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL46.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL46.glGetUniformLocation;

public class World implements BaseExample {
    public Renderer renderer;
    public NbtUtil nbtUtil;
    public NoiseUtil noiseUtil;
    public List<Chunk> chunks;
    public Camera camera;
    private long lastUpdateTime = 0;
    private static final long UPDATE_INTERVAL = 200_000_000;

    @Override
    public void init() {
        Shader.loadShader(this.renderer.programId, "shaders/world.fs", GL_FRAGMENT_SHADER);
        Shader.loadShader(this.renderer.programId, "shaders/world.vs", GL_VERTEX_SHADER);

        if (Constants.LOAD_WORLD_NBT) {
            this.chunks = this.nbtUtil.loadWorld();
        } else {
            this.chunks = this.noiseUtil.loadWorld();
        }

        this.renderer.viewLocation = glGetUniformLocation(this.renderer.programId, "view");
        this.renderer.projectionLocation = glGetUniformLocation(this.renderer.programId, "projection");
        this.renderer.lightPositionLocation = glGetUniformLocation(this.renderer.programId, "light_position");
        this.renderer.cameraPositionLocation = glGetUniformLocation(this.renderer.programId, "camera_position");
    }

    @Override
    public void update() {
        if (Constants.LOAD_WORLD_NBT) {
            return;
        }

        long currentTime = System.nanoTime();
        if (currentTime - lastUpdateTime < UPDATE_INTERVAL) {
            return;
        }

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
        for (Chunk chunk : this.chunks) {
            Vector2d pos = new Vector2d(chunk.xOffset, chunk.zOffset);
            if (!visibleChunkPositions.contains(pos)) {
                chunksToUpdate.add(chunk);
            }
        }

        List<Vector2d> positionsToAdd = new ArrayList<>();
        for (Vector2d pos : visibleChunkPositions) {
            boolean chunkExists = false;
            for (Chunk chunk : this.chunks) {
                if (chunk.xOffset == (int) pos.x && chunk.zOffset == (int) pos.y) {
                    chunkExists = true;
                    break;
                }
            }

            if (!chunkExists) {
                positionsToAdd.add(pos);
            }
        }

        int updateCount = Math.min(positionsToAdd.size(), chunksToUpdate.size());
        for (int i = 0; i < updateCount; i++) {
            Chunk chunk = chunksToUpdate.get(i);
            Vector2d position = positionsToAdd.get(i);

            chunk.xOffset = (int) position.x;
            chunk.zOffset = (int) position.y;

            Color[][][] chunkData = this.noiseUtil.generateChunkData(chunk.xOffset, chunk.zOffset);
            chunk.load(this.renderer.programId, chunkData);
        }

        lastUpdateTime = currentTime;
    }

    @Override
    public void render() {
        for (Chunk chunk : this.chunks) {
            chunk.render();
        }
    }

    @Override
    public void destroy() {
    }
}
