package voxelengine.examples;

import voxelengine.core.Camera;
import voxelengine.core.Renderer;

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

public class Voxel2d implements BaseExample {
    @Override
    public void init(Renderer renderer, Camera camera) {
        int vboId;
        int vaoId;

        float[] vertices = {
                // Front face (Positive Z-axis)
                -0.5f, -0.5f, 0.5f, // Bottom left
                0.5f, -0.5f, 0.5f, // Bottom right
                -0.5f, 0.5f, 0.5f, // Top left
                0.5f, -0.5f, 0.5f, // Bottom right
                0.5f, 0.5f, 0.5f, // Top right
                -0.5f, 0.5f, 0.5f, // Top left

                // Back face (Negative Z-axis)
                -0.5f, -0.5f, -0.5f, // Bottom left
                -0.5f, 0.5f, -0.5f, // Top left
                0.5f, -0.5f, -0.5f, // Bottom right
                -0.5f, 0.5f, -0.5f, // Top left
                0.5f, 0.5f, -0.5f, // Top right
                0.5f, -0.5f, -0.5f, // Bottom right

                // Left face (Negative X-axis)
                -0.5f, -0.5f, -0.5f, // Bottom left
                -0.5f, -0.5f, 0.5f, // Bottom right
                -0.5f, 0.5f, -0.5f, // Top left
                -0.5f, -0.5f, 0.5f, // Bottom right
                -0.5f, 0.5f, 0.5f, // Top right
                -0.5f, 0.5f, -0.5f, // Top left

                // Right face (Positive X-axis)
                0.5f, -0.5f, -0.5f, // Bottom left
                0.5f, 0.5f, -0.5f, // Top left
                0.5f, -0.5f, 0.5f, // Bottom right
                0.5f, 0.5f, -0.5f, // Top left
                0.5f, -0.5f, 0.5f, // Bottom right
                0.5f, 0.5f, 0.5f, // Top right

                // Top face (Positive Y-axis)
                -0.5f, 0.5f, -0.5f, // Bottom left
                -0.5f, 0.5f, 0.5f, // Bottom right
                0.5f, 0.5f, -0.5f, // Top left
                -0.5f, 0.5f, 0.5f, // Bottom right
                0.5f, 0.5f, 0.5f, // Top right
                0.5f, 0.5f, -0.5f, // Top left

                // Bottom face (Negative Y-axis)
                -0.5f, -0.5f, -0.5f, // Bottom left
                0.5f, -0.5f, -0.5f, // Bottom right
                -0.5f, -0.5f, 0.5f, // Top left
                0.5f, -0.5f, -0.5f, // Bottom right
                0.5f, -0.5f, 0.5f, // Top right
                -0.5f, -0.5f, 0.5f // Top left
        };

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

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
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
