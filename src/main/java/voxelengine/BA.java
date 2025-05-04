package voxelengine;

import java.util.ArrayList;
import java.util.List;

public class BA {
    public void generateBaseVoxelVertices() {
        List<Float> vertices = new ArrayList<>();
        BaseVoxel baseVoxel = new BaseVoxel();
        for (VoxelFace face : baseVoxel.faces) {
            for (Vector3 position : face.positions) {
                vertices.add(position.x);
                vertices.add(position.y);
                vertices.add(position.z);

                vertices.add(1.0f);
                vertices.add(0.0f);
                vertices.add(0.0f);

                vertices.add(face.normal.x);
                vertices.add(face.normal.y);
                vertices.add(face.normal.z);
            }
        }
    }

    public void generateWorldVertices() {
        World world = new World(new Color[][][]{});
        List<Float> vertices = new ArrayList<>();
        BaseVoxel baseVoxel = new BaseVoxel();
        for (int x = 0; x < world.voxelPositions.length; x++) {
            for (int y = 0; y < world.voxelPositions[x].length; y++) {
                for (int z = 0; z < world.voxelPositions[x][y].length; z++) {
                    Color color = world.voxelPositions[x][y][z];
                    if (color == null) {
                        continue;
                    }
                    for (VoxelFace face : baseVoxel.faces) {
                        int xCheck = x + (int) face.normal.x;
                        int yCheck = y + (int) face.normal.y;
                        int zCheck = z + (int) face.normal.z;
                        Color checkColor = world.voxelPositions[xCheck][yCheck][zCheck];
                        if (checkColor != null) {
                            continue;
                        }
                        for (Vector3 position : face.positions) {
                            vertices.add(position.x + x);
                            vertices.add(position.y + y);
                            vertices.add(position.z + z);

                            vertices.add(color.r);
                            vertices.add(color.g);
                            vertices.add(color.b);

                            vertices.add(face.normal.x);
                            vertices.add(face.normal.y);
                            vertices.add(face.normal.z);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {

    }

    private static class World {
        Color[][][] voxelPositions;

        public World(Color[][][] voxelPositions) {
            this.voxelPositions = voxelPositions;
        }
    }

    private static class Color {
        public final float r;
        public final float g;
        public final float b;

        public Color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    private static class BaseVoxel {
        public final VoxelFace[] faces;

        public BaseVoxel() {
            // Normale für jede Seite
            Vector3 frontNormal = new Vector3(0, 0, 1);    // Vorne (Positive Z-Achse)
            Vector3 backNormal = new Vector3(0, 0, -1);    // Hinten (Negative Z-Achse)
            Vector3 leftNormal = new Vector3(-1, 0, 0);    // Links (Negative X-Achse)
            Vector3 rightNormal = new Vector3(1, 0, 0);    // Rechts (Positive X-Achse)
            Vector3 topNormal = new Vector3(0, 1, 0);      // Oben (Positive Y-Achse)
            Vector3 bottomNormal = new Vector3(0, -1, 0);  // Unten (Negative Y-Achse)

            // Positionen für jede Seite
            // Vorne (Positive Z-Achse)
            Vector3[] frontPositions = {
                    new Vector3(-0.5f, -0.5f, 0.5f),  // Unten links
                    new Vector3(0.5f, -0.5f, 0.5f),   // Unten rechts
                    new Vector3(-0.5f, 0.5f, 0.5f),   // Oben links
                    new Vector3(0.5f, -0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, 0.5f),    // Oben rechts
                    new Vector3(-0.5f, 0.5f, 0.5f)    // Oben links
            };

            // Hinten (Negative Z-Achse)
            Vector3[] backPositions = {
                    new Vector3(-0.5f, -0.5f, -0.5f), // Unten links
                    new Vector3(-0.5f, 0.5f, -0.5f),  // Oben links
                    new Vector3(0.5f, -0.5f, -0.5f),  // Unten rechts
                    new Vector3(-0.5f, 0.5f, -0.5f),  // Oben links
                    new Vector3(0.5f, 0.5f, -0.5f),   // Oben rechts
                    new Vector3(0.5f, -0.5f, -0.5f)   // Unten rechts
            };

            // Links (Negative X-Achse)
            Vector3[] leftPositions = {
                    new Vector3(-0.5f, -0.5f, -0.5f), // Unten links
                    new Vector3(-0.5f, -0.5f, 0.5f),  // Unten rechts
                    new Vector3(-0.5f, 0.5f, -0.5f),  // Oben links
                    new Vector3(-0.5f, -0.5f, 0.5f),  // Unten rechts
                    new Vector3(-0.5f, 0.5f, 0.5f),   // Oben rechts
                    new Vector3(-0.5f, 0.5f, -0.5f)   // Oben links
            };

            // Rechts (Positive X-Achse)
            Vector3[] rightPositions = {
                    new Vector3(0.5f, -0.5f, -0.5f),  // Unten links
                    new Vector3(0.5f, 0.5f, -0.5f),   // Oben links
                    new Vector3(0.5f, -0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, -0.5f),   // Oben links
                    new Vector3(0.5f, -0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, 0.5f)     // Oben rechts
            };

            // Oben (Positive Y-Achse)
            Vector3[] topPositions = {
                    new Vector3(-0.5f, 0.5f, -0.5f),  // Unten links
                    new Vector3(-0.5f, 0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, -0.5f),   // Oben links
                    new Vector3(-0.5f, 0.5f, 0.5f),   // Unten rechts
                    new Vector3(0.5f, 0.5f, 0.5f),    // Oben rechts
                    new Vector3(0.5f, 0.5f, -0.5f)    // Oben links
            };

            // Unten (Negative Y-Achse)
            Vector3[] bottomPositions = {
                    new Vector3(-0.5f, -0.5f, -0.5f), // Unten links
                    new Vector3(0.5f, -0.5f, -0.5f),  // Unten rechts
                    new Vector3(-0.5f, -0.5f, 0.5f),  // Oben links
                    new Vector3(0.5f, -0.5f, -0.5f),  // Unten rechts
                    new Vector3(0.5f, -0.5f, 0.5f),   // Oben rechts
                    new Vector3(-0.5f, -0.5f, 0.5f)   // Oben links
            };

            this.faces = new VoxelFace[]{
                    new VoxelFace(frontNormal, frontPositions),
                    new VoxelFace(backNormal, backPositions),
                    new VoxelFace(leftNormal, leftPositions),
                    new VoxelFace(rightNormal, rightPositions),
                    new VoxelFace(topNormal, topPositions),
                    new VoxelFace(bottomNormal, bottomPositions)
            };
        }
    }

    private static class VoxelFace {
        public final Vector3 normal;
        public final Vector3[] positions;

        public VoxelFace(Vector3 normal, Vector3[] positions) {
            this.normal = normal;
            this.positions = positions;
        }
    }

    private static class Vector3 {
        public final float x;
        public final float y;
        public final float z;

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
