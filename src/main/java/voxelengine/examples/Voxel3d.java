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

public class Voxel3d implements BaseExample {
    @Override
    public void init() {
        int vboId;
        int vaoId;

        float[] vertices = {
                // Vorne (Positive Z-Achse)
                -0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, // Unten links
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, // Unten rechts
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,// Oben links
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,// Unten rechts
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,// Oben rechts
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f,// Oben links

                // Hinten (Negative Z-Achse)
                -0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,// Unten links
                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,// Oben links
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,// Unten rechts
                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,// Oben links
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,// Oben rechts
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f,// Unten rechts

                // Links (Negative X-Achse)
                -0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,// Unten links
                -0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,// Unten rechts
                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,// Oben links
                -0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,// Unten rechts
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,// Oben rechts
                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,// Oben links

                // Rechts (Positive X-Achse)
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,// Unten links
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,// Oben links
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,// Unten rechts
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,// Oben links
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,// Unten rechts
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,// Oben rechts

                // Oben (Positive Y-Achse)
                -0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,// Unten links
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,// Unten rechts
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,// Oben links
                -0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,// Unten rechts
                0.5f, 0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,// Oben rechts
                0.5f, 0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f,// Oben links

                // Unten (Negative Y-Achse)
                -0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,// Unten links
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,// Unten rechts
                -0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,// Oben links
                0.5f, -0.5f, -0.5f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,// Unten rechts
                0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,// Oben rechts
                -0.5f, -0.5f, 0.5f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f,// Oben links
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

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, (long) 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 3, GL_FLOAT, false, 9 * Float.BYTES, (long) 6 * Float.BYTES);
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
