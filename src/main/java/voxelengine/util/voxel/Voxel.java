package voxelengine.util.voxel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joml.Vector3d;

import voxelengine.util.Constants;

public class Voxel {
        public List<VoxelFace> faces;

        public Voxel() {
                faces = new ArrayList<>();
                this.faces.add(createVoxelFace(FaceDirection.FRONT));
                this.faces.add(createVoxelFace(FaceDirection.BACK));
                this.faces.add(createVoxelFace(FaceDirection.LEFT));
                this.faces.add(createVoxelFace(FaceDirection.RIGHT));
                this.faces.add(createVoxelFace(FaceDirection.TOP));
                this.faces.add(createVoxelFace(FaceDirection.BOTTOM));
        }

        private VoxelFace createVoxelFace(FaceDirection direction) {
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
                                        face.indices = new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3));
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
                                        face.indices = new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3));
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
                                        face.indices = new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3));
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
                                        face.indices = new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3));
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
                                        face.indices = new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3));
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
                                        face.indices = new ArrayList<>(Arrays.asList(0, 1, 2, 0, 2, 3));
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
