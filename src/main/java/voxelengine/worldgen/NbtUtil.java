package voxelengine.worldgen;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.api.Tag;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import dev.dewy.nbt.tags.primitive.StringTag;
import voxelengine.examples.World;
import voxelengine.util.Chunk;
import voxelengine.util.ColorUtil;
import voxelengine.util.Constants;
import voxelengine.util.Log;
import voxelengine.util.Vector3Key;
import voxelengine.util.voxel.Color;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NbtUtil {
    public void loadWorld() {
        if (Constants.NBT_DEBUG) {
            this.loadDebugWorld();
        } else {
            this.loadNbtWorld();
        }
    }

    private int[] parseChunkOffset(String input) {
        String regex = "ots_x(-?\\d+)_y(-?\\d+)_z(-?\\d+)\\.nbt";

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);

        if (matcher.matches()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            return new int[]{x, y, z};
        } else {
            throw new IllegalArgumentException("Input string does not match the expected pattern.");
        }
    }

    private void loadColors() throws URISyntaxException, IOException {
        URL colorsUrl = NbtUtil.class.getClassLoader().getResource("world/colors.json");
        File colorsFile = new File(colorsUrl.toURI());
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> colorsMap = objectMapper.readValue(colorsFile, Map.class);
        URL folderUrl = NbtUtil.class.getClassLoader().getResource(Constants.NBT_FOLDER_PATH);
        Path folderPath = Paths.get(folderUrl.toURI());
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
            for (Path entry : stream) {
                Nbt nbt = new Nbt();
                CompoundTag compoundTag = nbt.fromFile(entry.toFile());
                ListTag<Tag> paletteTag = compoundTag.getList("palette");
                for (Tag tag : paletteTag) {
                    Map<String, Object> tagMap = (Map<String, Object>) tag.getValue();
                    StringTag colorName = (StringTag) tagMap.get("Name");
                    String colorString = colorsMap.get(colorName.getValue());
                    Color color = Color.fromString(colorString);
                    ColorUtil.nbtColors.add(color);
                }
                return;
            }
        }
    }

    private void loadNbtWorld() {
        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            loadColors();
            URL folderUrl = NbtUtil.class.getClassLoader().getResource(Constants.NBT_FOLDER_PATH);
            Path folderPath = Paths.get(folderUrl.toURI());
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (Path entry : stream) {
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> processNbtFile(entry), threadPool);
                    futures.add(future);
                }
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
        } catch (IOException | URISyntaxException | RuntimeException e) {
            Log.error(e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    private void processNbtFile(Path entry) {
        try {
            Nbt nbt = new Nbt();
            CompoundTag compoundTag = nbt.fromFile(entry.toFile());
            int[] chunkOffset = parseChunkOffset(entry.getFileName().toString());

            Chunk chunk = new Chunk(chunkOffset[0], chunkOffset[1], chunkOffset[2]);
            Integer[][][] data = new Integer[Constants.NBT_CHUNK_SIZE][Constants.NBT_CHUNK_SIZE][Constants.NBT_CHUNK_SIZE];

            ListTag<Tag> blocksTag = compoundTag.getList("blocks");
            int voxelCount = 0;
            for (Tag tag : blocksTag) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tagMap = (Map<String, Object>) tag.getValue();
                IntTag colorIndex = (IntTag) tagMap.get("state");

                @SuppressWarnings("unchecked")
                ListTag<Tag> positions = (ListTag<Tag>) tagMap.get("pos");
                Integer xPos = ((IntTag) positions.get(0)).getValue();
                Integer yPos = ((IntTag) positions.get(1)).getValue();
                Integer zPos = ((IntTag) positions.get(2)).getValue();

                data[xPos][yPos][zPos] = colorIndex.getValue();
                voxelCount++;
            }

            if (voxelCount > 0) {
                chunk.setData(data);
                synchronized (World.chunks) {
                    World.chunks.put(new Vector3Key(chunk.getXOffset(), chunk.getYOffset(), chunk.getZOffset()), chunk);
                }
            }
        } catch (IOException | RuntimeException e) {
            Log.error("Error processing file " + entry.getFileName() + ": " + e.getMessage());
        }
    }

    private void loadDebugWorld() {
        ColorUtil.nbtColors.add(new Color(1.0f, 0.0f, 0.0f));

        for (int dx = 0; dx < 4; dx++) {
            for (int dz = 0; dz < 4; dz++) {
                Chunk chunk = new Chunk(dx * Constants.NBT_CHUNK_SIZE, 0, dz * Constants.NBT_CHUNK_SIZE);
                Integer[][][] data = new Integer[Constants.NBT_CHUNK_SIZE][Constants.NBT_CHUNK_SIZE][Constants.NBT_CHUNK_SIZE];
                for (int x = 0; x < Constants.NBT_CHUNK_SIZE; x++) {
                    for (int y = 0; y < Constants.NBT_CHUNK_SIZE; y++) {
                        for (int z = 0; z < Constants.NBT_CHUNK_SIZE; z++) {
                            data[x][y][z] = 0;
                        }
                    }
                }
                chunk.setData(data);
                World.chunks.put(new Vector3Key(chunk.getXOffset(), chunk.getYOffset(), chunk.getZOffset()), chunk);
            }
        }
    }
}
