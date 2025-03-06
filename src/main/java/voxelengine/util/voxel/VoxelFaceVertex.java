package voxelengine.util.voxel;

import org.joml.Vector3d;

public class VoxelFaceVertex {
    public Vector3d position;
    public Vector3d normal;
    public Color color;

    public VoxelFaceVertex(Vector3d position, Vector3d normal) {
        this.position = position;
        this.normal = normal;
        this.color = new Color();
    }

}
