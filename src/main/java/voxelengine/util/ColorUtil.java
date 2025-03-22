package voxelengine.util;

import voxelengine.util.voxel.Color;

import java.util.ArrayList;
import java.util.List;

public class ColorUtil {
    private ColorUtil() {
    }

    public static final List<Color> nbtColors = new ArrayList<>();
    public static final Color WATER_COLOR = new Color(0.1f, 0.3f, 0.8f);
    public static final Color SAND_COLOR = new Color(0.95f, 0.87f, 0.7f);
    public static final Color MOUNTAIN_COLOR = new Color(0.55f, 0.55f, 0.55f);
    public static final Color SNOW_COLOR = new Color(1.0f, 1.0f, 1.0f);
    public static final int WATER_COLOR_INDEX = 0;
    public static final int SAND_COLOR_INDEX = 1;
    public static final int MOUNTAIN_COLOR_INDEX = 2;
    public static final int SNOW_COLOR_INDEX = 3;
    public static final List<Color> noiseColors = List.of(
            WATER_COLOR,
            SAND_COLOR,
            MOUNTAIN_COLOR,
            SNOW_COLOR
    );
}
