package voxelengine.util.voxel;

import org.joml.Vector3d;
import voxelengine.util.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VoxelFace {
    private final List<Vector3d> vertices;
    private final Direction direction;
    private List<Integer> indices;

    public List<Vector3d> getVertices() {
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

    public void setVertices(Vector3d[] vertices) {
        this.vertices.clear();
        this.vertices.addAll(Arrays.asList(vertices));
    }

    public void addVertex(Vector3d vertex) {
        vertices.add(vertex);
    }
}
