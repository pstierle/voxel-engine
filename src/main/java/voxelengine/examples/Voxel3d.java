package voxelengine.examples;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;
import voxelengine.util.voxel.Voxel;
import voxelengine.util.voxel.VoxelFace;
import voxelengine.util.voxel.VoxelFaceVertex;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL46.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL46.GL_FLOAT;
import static org.lwjgl.opengl.GL46.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL46.GL_TRIANGLES;
import static org.lwjgl.opengl.GL46.glBindBuffer;
import static org.lwjgl.opengl.GL46.glBindVertexArray;
import static org.lwjgl.opengl.GL46.glBufferData;
import static org.lwjgl.opengl.GL46.glDrawArrays;
import static org.lwjgl.opengl.GL46.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL46.glGenBuffers;
import static org.lwjgl.opengl.GL46.glGenVertexArrays;
import static org.lwjgl.opengl.GL46.glUseProgram;
import static org.lwjgl.opengl.GL46.glVertexAttribPointer;

public class Voxel3d implements BaseExample {
    @Override
    public void init(Renderer renderer, Camera camera) {
        int vboId;
        int vaoId;

        Voxel voxel = new Voxel();
        float[] vertices = new float[6 * 6 * 9];
        int verticesIndex = 0;

        for (VoxelFace face : voxel.getFaces()) {
            for (VoxelFaceVertex vertex : face.getVertices()) {
                vertices[verticesIndex++] = (float) vertex.getPosition().x;
                vertices[verticesIndex++] = (float) vertex.getPosition().y;
                vertices[verticesIndex++] = (float) vertex.getPosition().z;

                vertices[verticesIndex++] = 1.0f;
                vertices[verticesIndex++] = 0.0f;
                vertices[verticesIndex++] = 0.0f;

                vertices[verticesIndex++] = (float) vertex.getNormal().x;
                vertices[verticesIndex++] = (float) vertex.getNormal().y;
                vertices[verticesIndex++] = (float) vertex.getNormal().z;
            }
        }

        glUseProgram(renderer.getProgramId());
        vboId = glGenBuffers();
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        FloatBuffer verticesBuffer = ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                .order(java.nio.ByteOrder.nativeOrder())
                .asFloatBuffer();
        verticesBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 3, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    @Override
    public void update() {
        //
    }

    @Override
    public void render() {
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }

    @Override
    public void destroy() {
        //
    }
}
