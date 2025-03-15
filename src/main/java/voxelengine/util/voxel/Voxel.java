package voxelengine.util.voxel;

import org.joml.Vector3d;
import voxelengine.util.Constants;
import voxelengine.util.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Voxel {
    private final List<VoxelFace> faces;

    public List<VoxelFace> getFaces() {
        return faces;
    }

    public Voxel() {
        faces = new ArrayList<>();
        this.faces.add(createVoxelFace(Direction.FRONT));
        this.faces.add(createVoxelFace(Direction.BACK));
        this.faces.add(createVoxelFace(Direction.LEFT));
        this.faces.add(createVoxelFace(Direction.RIGHT));
        this.faces.add(createVoxelFace(Direction.TOP));
        this.faces.add(createVoxelFace(Direction.BOTTOM));
    }

    private VoxelFace createVoxelFace(Direction direction) {
        int verticesCount;
        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            verticesCount = Constants.VOXEL_FACE_VERTICES_COUNT_INSTANCED;
        } else {
            verticesCount = Constants.VOXEL_FACE_VERTICES_COUNT;
        }
        VoxelFace face = new VoxelFace(direction, verticesCount);

        if (Constants.OPTIMIZATION_INSTANCE_RENDERING) {
            switch (direction) {
                case FRONT:
                    face.addVertex(new Vector3d(-0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, 0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, 0.5));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case BACK:
                    face.addVertex(new Vector3d(0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, -0.5));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case LEFT:
                    face.addVertex(new Vector3d(-0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, 0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, -0.5));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case RIGHT:
                    face.addVertex(new Vector3d(0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, 0.5));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case TOP:
                    face.addVertex(new Vector3d(-0.5, 0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, -0.5));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case BOTTOM:
                    face.addVertex(new Vector3d(-0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(-0.5, -0.5, 0.5));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
            }
        } else {
            switch (direction) {
                case FRONT:
                    face.addVertex(new Vector3d(-0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, 0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, 0.5));
                    break;
                case BACK:
                    face.addVertex(new Vector3d(-0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, -0.5));
                    break;
                case LEFT:
                    face.addVertex(new Vector3d(-0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, 0.5));
                    break;
                case RIGHT:
                    face.addVertex(new Vector3d(0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, 0.5));
                    break;
                case TOP:
                    face.addVertex(new Vector3d(-0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, -0.5));
                    face.addVertex(new Vector3d(0.5, 0.5, 0.5));
                    face.addVertex(new Vector3d(-0.5, 0.5, 0.5));
                    break;
                case BOTTOM:
                    face.addVertex(new Vector3d(-0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, -0.5));
                    face.addVertex(new Vector3d(-0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, 0.5));
                    face.addVertex(new Vector3d(0.5, -0.5, -0.5));
                    break;
            }
        }
        return face;
    }
}
