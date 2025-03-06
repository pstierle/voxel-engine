package voxelengine.examples;

import static org.lwjgl.opengl.GL46.*;

import java.nio.FloatBuffer;

import org.lwjgl.system.MemoryStack;

import voxelengine.core.Renderer;
import voxelengine.core.Shader;

public class Triangle2d implements BaseExample {
    public Renderer renderer;

    @Override
    public void init() {
        Shader.loadShader(this.renderer.programId, "shaders/basic.fs", GL_FRAGMENT_SHADER);
        Shader.loadShader(this.renderer.programId, "shaders/basic.vs", GL_VERTEX_SHADER);

        float vertices[] = {
                // Triangle
                -0.5f, -0.5f, 0.0f, // Bottom left
                0.5f, -0.5f, 0.0f, // Bottom Right
                0.0f, 0.5f, 0.0f // Top
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
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}
