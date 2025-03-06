package voxelengine.examples;

import static org.lwjgl.opengl.GL46.*;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryStack;

import voxelengine.core.Renderer;
import voxelengine.core.Shader;

public class Voxel2d implements BaseExample {
    public Renderer renderer;

    @Override
    public void init() {
        Shader.loadShader(this.renderer.programId, "shaders/basic.fs", GL_FRAGMENT_SHADER);
        Shader.loadShader(this.renderer.programId, "shaders/basic.vs", GL_VERTEX_SHADER);

        float vertices[] = {
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

        glUseProgram(this.renderer.programId);
        this.renderer.vboId = glGenBuffers();
        this.renderer.vaoId = glGenVertexArrays();

        glBindVertexArray(this.renderer.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.renderer.vboId);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer verticesBuffer = stack.mallocFloat(vertices.length);
            verticesBuffer.put(vertices).flip();
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        }

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
    }

    @Override
    public void update() {
    }

    @Override
    public void render() {
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }
}
