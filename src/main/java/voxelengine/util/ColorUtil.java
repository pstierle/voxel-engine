package voxelengine.util;

import voxelengine.util.voxel.Color;

import java.util.ArrayList;
import java.util.List;

public class ColorUtil {
    private ColorUtil() {
    }

    public static final List<Color> nbtColors = new ArrayList<>();

    private static final Color WATER_COLOR = new Color(0.1f, 0.3f, 0.8f);
    private static final Color SAND_COLOR = new Color(0.95f, 0.87f, 0.7f);
    private static final Color MOUNTAIN_COLOR = new Color(0.55f, 0.55f, 0.55f);
    private static final Color SNOW_COLOR = new Color(1.0f, 1.0f, 1.0f);
    private static final Color DEEP_OCEAN_COLOR = new Color(0.05f, 0.15f, 0.4f);
    private static final Color GRASS_GREEN_COLOR = new Color(0.2f, 0.6f, 0.2f);
    private static final Color DARK_GRASS_COLOR = new Color(0.1f, 0.4f, 0.1f);
    private static final Color ROCKY_BROWN_COLOR = new Color(0.6f, 0.5f, 0.4f);
    private static final Color GRAY_ROCK_COLOR = new Color(0.4f, 0.4f, 0.4f);
    private static final Color SNOW_MOUNTAIN_COLOR = new Color(0.9f, 0.9f, 0.95f);
    public static final int WATER_COLOR_INDEX = 0;
    public static final int SAND_COLOR_INDEX = 1;
    public static final int MOUNTAIN_COLOR_INDEX = 2;
    public static final int SNOW_COLOR_INDEX = 3;
    public static final int DEEP_OCEAN_COLOR_INDEX = 4;
    public static final int GRASS_GREEN_COLOR_INDEX = 5;
    public static final int DARK_GRASS_COLOR_INDEX = 6;
    public static final int ROCKY_BROWN_COLOR_INDEX = 7;
    public static final int GRAY_ROCK_COLOR_INDEX = 8;
    public static final int SNOW_MOUNTAIN_COLOR_INDEX = 9;
    public static final List<Color> noiseColors = List.of(
            WATER_COLOR,
            SAND_COLOR,
            MOUNTAIN_COLOR,
            SNOW_COLOR,
            DEEP_OCEAN_COLOR,
            GRASS_GREEN_COLOR,
            DARK_GRASS_COLOR,
            ROCKY_BROWN_COLOR,
            GRAY_ROCK_COLOR,
            SNOW_MOUNTAIN_COLOR
    );
}
