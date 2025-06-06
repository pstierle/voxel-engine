package voxelengine.worldgen;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.api.Tag;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import dev.dewy.nbt.tags.primitive.StringTag;
import voxelengine.VoxelEngineUtil;
import voxelengine.VoxelEngineUtil.Color;
import voxelengine.util.Constants;

import java.io.File;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NbtUtil {
    public void loadWorld() {
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

    private List<Color> loadColors() {
        List<Color> colors = new ArrayList<>();
        try {
            URL colorsUrl = NbtUtil.class.getClassLoader().getResource("world/colors.json");
            File colorsFile = new File(colorsUrl.toURI());
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> colorsMap = objectMapper.readValue(colorsFile, Map.class);
            URL folderUrl = NbtUtil.class.getClassLoader().getResource(Constants.NBT_WORLD.folderName());
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
                        colors.add(color);
                    }
                }
            }
        } catch (Exception e) {
        }
        return colors;
    }

    public class WorldData {
        public List<VoxelEngineUtil.Chunk> chunks;
        public List<Color> colors;

        public WorldData(List<VoxelEngineUtil.Chunk> chunks, List<Color> colors) {
            this.chunks = chunks;
            this.colors = colors;
        }
    }

    public WorldData loadNbtWorld(boolean indexed) {
        List<VoxelEngineUtil.Chunk> chunks = Collections.synchronizedList(new ArrayList<>());
        List<Color> colors = loadColors();

        try {
            URL folderUrl = NbtUtil.class.getClassLoader().getResource(Constants.NBT_WORLD.folderName());
            Path folderPath = Paths.get(folderUrl.toURI());

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
                ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                List<Future<?>> futures = new ArrayList<>();

                for (Path entry : stream) {
                    futures.add(executor.submit(() -> {
                        try {
                            Object[][][] chunkData;
                            if (indexed) {
                                chunkData = new Integer[48][48][48];
                            } else {
                                chunkData = new Color[48][48][48];
                            }
                            Nbt nbt = new Nbt();
                            CompoundTag compoundTag = nbt.fromFile(entry.toFile());
                            ListTag<Tag> blocksTag = compoundTag.getList("blocks");

                            int[] chunkOffset = parseChunkOffset(entry.getFileName().toString());

                            for (Tag tag : blocksTag) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> tagMap = (Map<String, Object>) tag.getValue();
                                IntTag colorIndex = (IntTag) tagMap.get("state");

                                @SuppressWarnings("unchecked")
                                ListTag<Tag> positions = (ListTag<Tag>) tagMap.get("pos");
                                int xPos = ((IntTag) positions.get(0)).getValue();
                                int yPos = ((IntTag) positions.get(1)).getValue();
                                int zPos = ((IntTag) positions.get(2)).getValue();

                                if (indexed) {
                                    chunkData[xPos][yPos][zPos] = colorIndex.getValue();

                                } else {
                                    Color color = colors.get(colorIndex.getValue());
                                    chunkData[xPos][yPos][zPos] = color;
                                }
                            }

                            chunks.add(new VoxelEngineUtil.Chunk(chunkOffset[0], chunkOffset[1], chunkOffset[2], chunkData));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }));
                }

                // Wait for all tasks to complete
                for (Future<?> future : futures) {
                    future.get(); // this rethrows exceptions if any occurred
                }

                executor.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new WorldData(chunks, colors);
    }
}
