package voxelengine.examples;

import voxelengine.core.State;

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

public class Triangle2d implements BaseExample {
    @Override
    public void init() {
        int vboId;
        int vaoId;

        float[] vertices = {
                // Triangle
                -0.5f, -0.5f, // Bottom left
                0.5f, -0.5f, // Bottom Right
                0.0f, 0.5f, // Top
        };

        glUseProgram(State.renderer.getProgramId());
        vboId = glGenBuffers();
        vaoId = glGenVertexArrays();

        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        FloatBuffer verticesBuffer = ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                .order(java.nio.ByteOrder.nativeOrder())
                .asFloatBuffer();
        verticesBuffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 2 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
    }

    @Override
    public void update() {
        //
    }

    @Override
    public void render() {
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }

    @Override
    public void destroy() {
        //
    }
}
