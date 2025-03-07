package voxelengine.util.voxel;

public class Color {
    private float r, g, b;

    public Color(float r, float g, float b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public static Color fromString(String colorString) {
        String[] parts = colorString.split(",");
        if (parts.length == 3) {
            return new Color(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
        } else {
            throw new IllegalArgumentException("Invalid color string: " + colorString);
        }
    }
}
