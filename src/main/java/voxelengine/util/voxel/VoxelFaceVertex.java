package voxelengine.util.voxel;

import org.joml.Vector3d;

public class VoxelFaceVertex {
    private final Vector3d position;
    private final Vector3d normal;

    public VoxelFaceVertex(Vector3d position, Vector3d normal) {
        this.position = position;
        this.normal = normal;
    }

    public Vector3d getPosition() {
        return position;
    }

    public Vector3d getNormal() {
        return normal;
    }
}
