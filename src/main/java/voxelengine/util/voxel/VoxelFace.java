package voxelengine.util.voxel;

import java.util.ArrayList;
import java.util.List;

public class VoxelFace {
    private final List<VoxelFaceVertex> vertices;
    private final FaceDirection direction;
    private List<Integer> indices;

    public List<VoxelFaceVertex> getVertices() {
        return vertices;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public FaceDirection getDirection() {
        return direction;
    }

    public void setIndices(List<Integer> indices) {
        this.indices = indices;
    }

    public VoxelFace(FaceDirection direction, int verticesCount) {
        this.direction = direction;
        this.vertices = new ArrayList<>(verticesCount);
    }

    public void addVertex(VoxelFaceVertex vertex) {
        vertices.add(vertex);
    }
}
