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
        if (Constants.INSTANCE_RENDERING) {
            verticesCount = Constants.VOXEL_FACE_VERTICES_COUNT_INSTANCED;
        } else {
            verticesCount = Constants.VOXEL_FACE_VERTICES_COUNT;
        }
        VoxelFace face = new VoxelFace(direction, verticesCount);

        if (Constants.INSTANCE_RENDERING) {
            switch (direction) {
                case FRONT:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, 0.5),
                                    new Vector3d(0.0, 0.0, 1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, 0.5),
                                    new Vector3d(0.0, 0.0, 1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, 0.5),
                                    new Vector3d(0.0, 0.0, 1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, 0.5),
                                    new Vector3d(0.0, 0.0, 1.0)));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case BACK:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case LEFT:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, -0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, 0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, 0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, -0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case RIGHT:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, 0.5),
                                    new Vector3d(1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, -0.5),
                                    new Vector3d(1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, -0.5),
                                    new Vector3d(1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, 0.5),
                                    new Vector3d(1.0, 0.0, 0.0)));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case TOP:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, 0.5),
                                    new Vector3d(0.0, 1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, 0.5),
                                    new Vector3d(0.0, 1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 1.0, 0.0)));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
                case BOTTOM:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, -0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, -0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, 0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, 0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    face.setIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3)));
                    break;
            }
        } else {
            switch (direction) {
                case FRONT:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, 0.5),
                                    new Vector3d(0.0, 0.0, 1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, 0.5),
                                    new Vector3d(0.0, 0.0, 1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, 0.5),
                                    new Vector3d(0.0, 0.0, 1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, 0.5),
                                    new Vector3d(0.0, 0.0, 1.0)));
                    face.addVertex(new VoxelFaceVertex(new Vector3d(0.5, 0.5, 0.5),
                            new Vector3d(0.0, 0.0, 1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, 0.5),
                                    new Vector3d(0.0, 0.0, 1.0)));
                    break;
                case BACK:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, -0.5),
                                    new Vector3d(0.0, 0.0, -1.0)));
                    break;
                case LEFT:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, -0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, 0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, -0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, -0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, 0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, 0.5),
                                    new Vector3d(-1.0, 0.0, 0.0)));
                    break;
                case RIGHT:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, -0.5),
                                    new Vector3d(1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, -0.5),
                                    new Vector3d(1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, 0.5),
                                    new Vector3d(1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, -0.5),
                                    new Vector3d(1.0, 0.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, 0.5),
                                    new Vector3d(1.0, 0.0, 0.0)));
                    face.addVertex(new VoxelFaceVertex(new Vector3d(0.5, 0.5, 0.5),
                            new Vector3d(1.0, 0.0, 0.0)));
                    break;
                case TOP:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, 0.5),
                                    new Vector3d(0.0, 1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, 0.5, -0.5),
                                    new Vector3d(0.0, 1.0, 0.0)));
                    face.addVertex(new VoxelFaceVertex(new Vector3d(0.5, 0.5, 0.5),
                            new Vector3d(0.0, 1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, 0.5, 0.5),
                                    new Vector3d(0.0, 1.0, 0.0)));
                    break;
                case BOTTOM:
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, -0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, 0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, -0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(-0.5, -0.5, 0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, 0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    face.addVertex(
                            new VoxelFaceVertex(new Vector3d(0.5, -0.5, -0.5),
                                    new Vector3d(0.0, -1.0, 0.0)));
                    break;
            }
        }

        return face;
    }
}
