package voxelengine.examples;

import org.lwjgl.system.MemoryStack;
import voxelengine.core.Renderer;
import voxelengine.core.Shader;
import voxelengine.util.voxel.Voxel;
import voxelengine.util.voxel.VoxelFace;
import voxelengine.util.voxel.VoxelFaceVertex;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL46.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL46.GL_FLOAT;
import static org.lwjgl.opengl.GL46.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL46.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL46.GL_TRIANGLES;
import static org.lwjgl.opengl.GL46.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL46.glBindBuffer;
import static org.lwjgl.opengl.GL46.glBindVertexArray;
import static org.lwjgl.opengl.GL46.glBufferData;
import static org.lwjgl.opengl.GL46.glDrawArrays;
import static org.lwjgl.opengl.GL46.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL46.glGenBuffers;
import static org.lwjgl.opengl.GL46.glGenVertexArrays;
import static org.lwjgl.opengl.GL46.glGetUniformLocation;
import static org.lwjgl.opengl.GL46.glUseProgram;
import static org.lwjgl.opengl.GL46.glVertexAttribPointer;

public class Voxel3d implements BaseExample {
    public Renderer renderer;
    public int vboId;
    public int vaoId;

    @Override
    public void init() {
        Shader.loadShader(this.renderer.programId, "shaders/world.fs", GL_FRAGMENT_SHADER);
        Shader.loadShader(this.renderer.programId, "shaders/world.vs", GL_VERTEX_SHADER);

        Voxel voxel = new Voxel();
        float[] vertices = new float[6 * 6 * 9];
        int verticesIndex = 0;

        for (VoxelFace face : voxel.faces) {
            for (VoxelFaceVertex vertex : face.vertices) {
                vertices[verticesIndex++] = (float) vertex.position.x;
                vertices[verticesIndex++] = (float) vertex.position.y;
                vertices[verticesIndex++] = (float) vertex.position.z;

                vertices[verticesIndex++] = 1.0f;
                vertices[verticesIndex++] = vertex.color.g;
                vertices[verticesIndex++] = vertex.color.b;

                vertices[verticesIndex++] = (float) vertex.normal.x;
                vertices[verticesIndex++] = (float) vertex.normal.y;
                vertices[verticesIndex++] = (float) vertex.normal.z;
            }
        }

        glUseProgram(this.renderer.programId);
        this.vboId = glGenBuffers();
        this.vaoId = glGenVertexArrays();

        this.renderer.viewLocation = glGetUniformLocation(this.renderer.programId, "view");
        this.renderer.projectionLocation = glGetUniformLocation(this.renderer.programId, "projection");
        this.renderer.lightPositionLocation = glGetUniformLocation(this.renderer.programId, "light_position");
        this.renderer.cameraPositionLocation = glGetUniformLocation(this.renderer.programId, "camera_position");

        glBindVertexArray(this.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer verticesBuffer = stack.mallocFloat(vertices.length);
            verticesBuffer.put(vertices).flip();
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        }

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 9 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, 9 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 3, GL_FLOAT, false, 9 * Float.BYTES, 6 * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    @Override
    public void update() {
    }

    @Override
    public void render() {
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }

    @Override
    public void destroy() {
    }
}
