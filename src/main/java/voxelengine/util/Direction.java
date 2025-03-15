package voxelengine.util;

import org.joml.Vector3d;

public enum Direction {
    FRONT,
    BACK,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    public int getIndex() {
        return this.ordinal();
    }

    private static final Vector3d FRONT_NORMAL = new Vector3d(0, 0, 1);
    private static final Vector3d BACK_NORMAL = new Vector3d(0, 0, -1);
    private static final Vector3d LEFT_NORMAL = new Vector3d(-1, 0, 0);
    private static final Vector3d RIGHT_NORMAL = new Vector3d(1, 0, 0);
    private static final Vector3d TOP_NORMAL = new Vector3d(0, 1, 0);
    private static final Vector3d BOTTOM_NORMAL = new Vector3d(0, -1, 0);

    public Vector3d getNormal() {
        return switch (this) {
            case FRONT -> FRONT_NORMAL;
            case BACK -> BACK_NORMAL;
            case LEFT -> LEFT_NORMAL;
            case RIGHT -> RIGHT_NORMAL;
            case TOP -> TOP_NORMAL;
            case BOTTOM -> BOTTOM_NORMAL;
        };
    }
}
