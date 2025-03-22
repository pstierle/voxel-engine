package voxelengine.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.api.Tag;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.collection.ListTag;
import dev.dewy.nbt.tags.primitive.IntTag;
import dev.dewy.nbt.tags.primitive.StringTag;
import voxelengine.core.State;
import voxelengine.examples.World;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class NbtUtil {
    public void loadWorld() {
        List<Chunk> chunks = new ArrayList<>();

        if (Constants.NBT_DEBUG) {
            ColorUtil.nbtColors = List.of(new Color(1.0f, 0.0f, 0.0f));

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
                    chunks.add(chunk);
                }
            }
        } else {
            try {
                URL colorsUrl = NbtUtil.class.getClassLoader().getResource("world/colors.json");

                if (colorsUrl == null) {
                    throw new IllegalArgumentException("Colors json not found");
                }

                File colorsFile = new File(colorsUrl.toURI());
                ObjectMapper objectMapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, String> colorsMap = objectMapper.readValue(colorsFile, Map.class);

                URL folderUrl = NbtUtil.class.getClassLoader().getResource(Constants.NBT_FOLDER_PATH);

                if (folderUrl == null) {
                    throw new IllegalArgumentException(String.format("Folder %s not found", Constants.NBT_FOLDER_PATH));
                }

                Path folderPath = Paths.get(folderUrl.toURI());
                boolean colorsLoaded = false;
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderPath)) {
                    for (Path entry : stream) {
                        Nbt nbt = new Nbt();
                        CompoundTag compoundTag = nbt.fromFile(entry.toFile());
                        if (!colorsLoaded) {
                            ListTag<Tag> paletteTag = compoundTag.getList("palette");
                            List<Color> colors = new ArrayList<>();
                            for (Tag tag : paletteTag) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> tagMap = (Map<String, Object>) tag.getValue();
                                StringTag colorName = (StringTag) tagMap.get("Name");
                                String colorString = colorsMap.get(colorName.getValue());
                                Color color = Color.fromString(colorString);
                                colors.add(color);
                            }
                            ColorUtil.nbtColors = colors;
                            colorsLoaded = true;
                        }

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
                            chunks.add(chunk);
                        }
                    }
                }
            } catch (IOException | RuntimeException | URISyntaxException e) {
                Log.error(e.getMessage());
            }
        }

        for (Chunk chunk : chunks) {
            Map<Direction, Vector3Key> neighborChunkKeys = new EnumMap<>(Direction.class);

            int x = chunk.getXOffset();
            int y = chunk.getYOffset();
            int z = chunk.getZOffset();

            Vector3Key rightKey = new Vector3Key(x + Constants.NOISE_CHUNK_SIZE, y, z); // RIGHT
            Vector3Key leftKey = new Vector3Key(x - Constants.NOISE_CHUNK_SIZE, y, z); // LEFT
            Vector3Key frontKey = new Vector3Key(x, y, z + Constants.NOISE_CHUNK_SIZE); // FRONT
            Vector3Key backKey = new Vector3Key(x, y, z - Constants.NOISE_CHUNK_SIZE); // BACK
            Vector3Key topKey = new Vector3Key(x, y + Constants.NOISE_CHUNK_SIZE, z); // TOP
            Vector3Key bottomKey = new Vector3Key(x, y - Constants.NOISE_CHUNK_SIZE, z); // BOTTOM

            if (World.chunks.get(rightKey) != null) {
                neighborChunkKeys.put(Direction.RIGHT, rightKey);
            }
            if (World.chunks.get(leftKey) != null) {
                neighborChunkKeys.put(Direction.LEFT, leftKey);
            }
            if (World.chunks.get(frontKey) != null) {
                neighborChunkKeys.put(Direction.FRONT, frontKey);
            }
            if (World.chunks.get(backKey) != null) {
                neighborChunkKeys.put(Direction.BACK, backKey);
            }
            if (World.chunks.get(topKey) != null) {
                neighborChunkKeys.put(Direction.TOP, topKey);
            }
            if (World.chunks.get(bottomKey) != null) {
                neighborChunkKeys.put(Direction.BOTTOM, bottomKey);
            }
            chunk.setNeighborChunkKeys(neighborChunkKeys);
        }

        for (int i = 0; i < chunks.size(); i++) {
            Log.info(String.format("Loaded chunk %d/%d", i + 1, chunks.size()));
            chunks.get(i).loadData();
            chunks.get(i).loadBuffers(State.renderer.getProgramId());
            World.chunks.put(new Vector3Key(chunks.get(i).getXOffset(), chunks.get(i).getYOffset(), chunks.get(i).getZOffset()), chunks.get(i));
        }
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
