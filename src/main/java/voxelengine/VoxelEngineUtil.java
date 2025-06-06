package voxelengine;

import java.util.List;

public class VoxelEngineUtil {
    private VoxelEngineUtil() {
    }

    public static final Vector3 FRONT_NORMAL = new Vector3(0, 0, 1);
    public static final Vector3 BACK_NORMAL = new Vector3(0, 0, -1);
    public static final Vector3 LEFT_NORMAL = new Vector3(-1, 0, 0);
    public static final Vector3 RIGHT_NORMAL = new Vector3(1, 0, 0);
    public static final Vector3 TOP_NORMAL = new Vector3(0, 1, 0);
    public static final Vector3 BOTTOM_NORMAL = new Vector3(0, -1, 0);

    public static final Vector3[] NORMALS = new Vector3[]{
            FRONT_NORMAL,
            BACK_NORMAL,
            LEFT_NORMAL,
            RIGHT_NORMAL,
            TOP_NORMAL,
            BOTTOM_NORMAL
    };

    public static final Color DEBUG_RED = new Color(1.0f, 0.0f, 0.0f);
    public static final Color DEBUG_BLUE = new Color(0.0f, 0.0f, 1.0f);

    public static final Color[] DEBUG_COLORS = new Color[]{
            DEBUG_RED,
            DEBUG_BLUE
    };

    public static final Color[][][] DEBUG_WORLD = new Color[][][]{
            // x=0
            {
                    // y=0
                    {
                            DEBUG_RED,  // (x=0, y=0, z=0)
                            DEBUG_RED   // (x=0, y=0, z=1)
                    },
                    // y=1
                    {
                            DEBUG_BLUE,                                  // (x=0, y=1, z=0)
                            null   // (x=0, y=1, z=1)
                    }
            },
            // x=1
            {
                    // y=0
                    {
                            DEBUG_RED,  // (x=1, y=0, z=0)
                            DEBUG_RED   // (x=1, y=0, z=1)
                    },
                    // y=1
                    {
                            DEBUG_BLUE,                                  // (x=1, y=1, z=0)
                            null   // (x=1, y=1, z=1)
                    }
            }
    };

    public static final Integer[][][] DEBUG_WORLD_INDEXED = new Integer[][][]{
            // x=0
            {
                    // y=0
                    {
                            0,  // (x=0, y=0, z=0)
                            0   // (x=0, y=0, z=1)
                    },
                    // y=1
                    {
                            1,                                  // (x=0, y=1, z=0)
                            null   // (x=0, y=1, z=1)
                    }
            },
            // x=1
            {
                    // y=0
                    {
                            0,  // (x=1, y=0, z=0)
                            0   // (x=1, y=0, z=1)
                    },
                    // y=1
                    {
                            1,                                  // (x=1, y=1, z=0)
                            null   // (x=1, y=1, z=1)
                    }
            }
    };

    public static class Vector3 {
        public float x;
        public float y;
        public float z;

        public Vector3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public static class Vector2 {
        public float x;
        public float y;

        public Vector2(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Chunk {
        public final int xOffset;
        public final int yOffset;
        public final int zOffset;
        public final Object[][][] data;

        public Chunk(int xOffset, int yOffset, int zOffset, Object[][][] data) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.data = data;
        }
    }

    public static class Color {
        public final float r;
        public final float g;
        public final float b;

        public Color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public static Color fromString(String colorString) {
            String[] parts = colorString.split(",");
            if (parts.length == 3) {
                return new Color(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
            } else {
                throw new IllegalArgumentException("Invalid color string: " + colorString);
            }
        }
    }

    public static class BaseVoxelIndexed {
        public final VoxelFaceIndexed[] faces;

        public BaseVoxelIndexed() {
            Vector3[] frontPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f)
            };

            Vector3[] backPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f)
            };

            Vector3[] leftPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f)
            };

            Vector3[] rightPositions = new Vector3[]{
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f)
            };

            Vector3[] topPositions = new Vector3[]{
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f)
            };

            Vector3[] bottomPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f)
            };
            this.faces = new VoxelFaceIndexed[]{
                    new VoxelFaceIndexed(0, frontPositions),
                    new VoxelFaceIndexed(1, backPositions),
                    new VoxelFaceIndexed(2, leftPositions),
                    new VoxelFaceIndexed(3, rightPositions),
                    new VoxelFaceIndexed(4, topPositions),
                    new VoxelFaceIndexed(5, bottomPositions)
            };
        }
    }

    public static class VoxelFaceIndexed {
        public final int normalIndex;
        public final Vector3[] vertexPositions;

        public VoxelFaceIndexed(int normalIndex, Vector3[] vertexPositions) {
            this.normalIndex = normalIndex;
            this.vertexPositions = vertexPositions;
        }
    }

    public static class BaseVoxelInstanced {
        public final VoxelFaceInstanced[] faces;

        public BaseVoxelInstanced() {
            Vector3[] frontPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f)
            };

            Vector3[] backPositions = new Vector3[]{
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
            };

            Vector3[] leftPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
            };

            Vector3[] rightPositions = new Vector3[]{
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f),
            };

            Vector3[] topPositions = new Vector3[]{
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
            };

            Vector3[] bottomPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
            };
            this.faces = new VoxelFaceInstanced[]{
                    new VoxelFaceInstanced(FRONT_NORMAL, frontPositions),
                    new VoxelFaceInstanced(BACK_NORMAL, backPositions),
                    new VoxelFaceInstanced(LEFT_NORMAL, leftPositions),
                    new VoxelFaceInstanced(RIGHT_NORMAL, rightPositions),
                    new VoxelFaceInstanced(TOP_NORMAL, topPositions),
                    new VoxelFaceInstanced(BOTTOM_NORMAL, bottomPositions)
            };
        }
    }

    public static class VoxelFaceInstanced {
        public final Vector3 normal;
        public final Vector3[] vertexPositions;
        public final int[] indices = new int[]{0, 1, 2, 0, 2, 3};

        public VoxelFaceInstanced(Vector3 normal, Vector3[] vertexPositions) {
            this.normal = normal;
            this.vertexPositions = vertexPositions;
        }
    }

    public static class BaseVoxel {
        public final VoxelFace[] faces;

        public BaseVoxel() {
            Vector3[] frontPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f)
            };

            Vector3[] backPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f)
            };

            Vector3[] leftPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(-0.5f, 0.5f, -0.5f)
            };

            Vector3[] rightPositions = new Vector3[]{
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f)
            };

            Vector3[] topPositions = new Vector3[]{
                    new Vector3(-0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f),
                    new Vector3(-0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, 0.5f),
                    new Vector3(0.5f, 0.5f, -0.5f)
            };

            Vector3[] bottomPositions = new Vector3[]{
                    new Vector3(-0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f),
                    new Vector3(0.5f, -0.5f, -0.5f),
                    new Vector3(0.5f, -0.5f, 0.5f),
                    new Vector3(-0.5f, -0.5f, 0.5f)
            };
            this.faces = new VoxelFace[]{
                    new VoxelFace(FRONT_NORMAL, frontPositions),
                    new VoxelFace(BACK_NORMAL, backPositions),
                    new VoxelFace(LEFT_NORMAL, leftPositions),
                    new VoxelFace(RIGHT_NORMAL, rightPositions),
                    new VoxelFace(TOP_NORMAL, topPositions),
                    new VoxelFace(BOTTOM_NORMAL, bottomPositions)
            };
        }
    }

    public record VoxelFace(Vector3 normal, Vector3[] vertexPositions) {
    }

    public record World(List<Chunk> chunks) {
    }

    public record WorldIndexed(List<VoxelEngineUtil.Chunk> chunks, List<VoxelEngineUtil.Color> uboColors) {
    }
}
