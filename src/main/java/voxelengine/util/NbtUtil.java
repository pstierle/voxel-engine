package voxelengine.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.api.Tag;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import dev.dewy.nbt.tags.primitive.StringTag;
import voxelengine.core.Renderer;
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

public class NbtUtil {
    public Renderer renderer;

    public List<Chunk> loadWorld() {
        List<Chunk> chunks = new ArrayList<>();
        try {
            URL colorsUrl = NbtUtil.class.getClassLoader().getResource("world/colors.json");

            if (colorsUrl == null) {
                throw new RuntimeException("Colors json not found");
            }

            File colorsFile = new File(colorsUrl.toURI());
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, String> colorsMap = objectMapper.readValue(colorsFile, Map.class);
            List<Color> colorsList = new ArrayList<>();

            URL folderUrl = NbtUtil.class.getClassLoader().getResource(Constants.NBT_FOLDER_PATH);

            if (folderUrl == null) {
                throw new RuntimeException(String.format("Folder %s not found", Constants.NBT_FOLDER_PATH));
            }

            Path folderPath = Paths.get(folderUrl.toURI());
            boolean colorsLoaded = false;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
                for (Path entry : stream) {
                    Nbt nbt = new Nbt();
                    CompoundTag compoundTag = nbt.fromFile(entry.toFile());

                    if (!colorsLoaded) {
                        ListTag<Tag> paletteTag = compoundTag.getList("palette");
                        for (Tag tag : paletteTag) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> tagMap = (Map<String, Object>) tag.getValue();
                            StringTag colorName = (StringTag) tagMap.get("Name");
                            String colorString = colorsMap.get(colorName.getValue());
                            Color color = Color.fromString(colorString);
                            colorsList.add(color);
                        }
                        colorsLoaded = true;
                    }
                    int[] chunkOffset = parseChunkOffset(entry.getFileName().toString());
                    Chunk chunk = new Chunk(chunkOffset[0], chunkOffset[1], chunkOffset[2], Constants.NBT_CHUNK_SIZE, Constants.NBT_CHUNK_SIZE, Constants.NBT_CHUNK_SIZE);
                    chunk.data = new Color[Constants.NBT_CHUNK_SIZE][Constants.NBT_CHUNK_SIZE][Constants.NBT_CHUNK_SIZE];
                    ListTag<Tag> blocksTag = compoundTag.getList("blocks");
                    int voxelCount = 0;
                    for (Tag tag : blocksTag) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tagMap = (Map<String, Object>) tag.getValue();
                        IntTag colorIndex = (IntTag) tagMap.get("state");
                        Color color = colorsList.get(colorIndex.getValue());
                        @SuppressWarnings("unchecked")
                        ListTag<Tag> positions = (ListTag<Tag>) tagMap.get("pos");
                        Integer xPos = ((IntTag) positions.get(0)).getValue();
                        Integer yPos = ((IntTag) positions.get(1)).getValue();
                        Integer zPos = ((IntTag) positions.get(2)).getValue();
                        chunk.data[xPos][yPos][zPos] = color;
                        voxelCount++;
                    }
                    if (voxelCount > 0) {
                        chunk.numVoxels = voxelCount;
                        chunk.load(this.renderer.programId, chunk.data);
                        chunks.add(chunk);
                    }
                }
            }
        } catch (IOException | RuntimeException | URISyntaxException e) {
            System.out.println(e);
        }

        return chunks;
    }

    public static int[] parseChunkOffset(String input) {
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
}
