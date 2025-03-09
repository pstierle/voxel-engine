package voxelengine.util.voxel;

import voxelengine.util.Direction;

import java.util.ArrayList;
import java.util.List;

public class VoxelFace {
    private final List<VoxelFaceVertex> vertices;
    private final Direction direction;
    private List<Integer> indices;

    public List<VoxelFaceVertex> getVertices() {
        return vertices;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setIndices(List<Integer> indices) {
        this.indices = indices;
    }

    public VoxelFace(Direction direction, int verticesCount) {
        this.direction = direction;
        this.vertices = new ArrayList<>(verticesCount);
    }

    public void addVertex(VoxelFaceVertex vertex) {
        vertices.add(vertex);
    }
}
