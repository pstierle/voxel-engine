package voxelengine.util.voxel;

import java.util.ArrayList;
import java.util.List;

public class VoxelFace {
    public List<VoxelFaceVertex> vertices;
    public List<Integer> indices;
    public FaceDirection direction;

    public VoxelFace(FaceDirection direction, int verticesCount) {
        this.direction = direction;
        this.vertices = new ArrayList<>(verticesCount);
    }

    public void addVertex(VoxelFaceVertex vertex) {
        vertices.add(vertex);
    }
}
