package voxelengine.util.voxel;

public class Color {
    public float r, g, b;

    public Color() {
        this.r = 0.0f;
        this.g = 0.0f;
        this.b = 0.0f;
    }

    public static Color fromString(String colorString) {
        String[] parts = colorString.split(",");
        if (parts.length == 3) {
            Color color = new Color();
            color.r = Float.parseFloat(parts[0]);
            color.g = Float.parseFloat(parts[1]);
            color.b = Float.parseFloat(parts[2]);
            return color;
        } else {
            throw new IllegalArgumentException("Invalid color string: " + colorString);
        }
    }
}
