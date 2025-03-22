package voxelengine.util;

public class VectorXZKey {
    private final int x;
    private final int z;

    public VectorXZKey(int x, int z) {
        this.x = x;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        VectorXZKey other = (VectorXZKey) obj;
        return x == other.x && z == other.z;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + x;
        result = 31 * result + z;
        return result;
    }
}
