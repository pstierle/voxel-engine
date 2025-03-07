package voxelengine.core;

import voxelengine.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static org.lwjgl.opengl.GL46.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL46.GL_INFO_LOG_LENGTH;
import static org.lwjgl.opengl.GL46.GL_TRUE;
import static org.lwjgl.opengl.GL46.glAttachShader;
import static org.lwjgl.opengl.GL46.glCompileShader;
import static org.lwjgl.opengl.GL46.glCreateShader;
import static org.lwjgl.opengl.GL46.glDeleteShader;
import static org.lwjgl.opengl.GL46.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL46.glGetShaderi;
import static org.lwjgl.opengl.GL46.glLinkProgram;
import static org.lwjgl.opengl.GL46.glShaderSource;


public class Shader {
    private Shader() {
    }

    public static void checkShaderStatus(int shaderId) {
        int status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        if (status != GL_TRUE) {
            int logLength = glGetShaderi(shaderId, GL_INFO_LOG_LENGTH);
            String infoLog = glGetShaderInfoLog(shaderId, logLength);
            Log.error(String.format("Shader %s failed to compile", infoLog));
            System.exit(1);
        }
    }

    public static String readShaderFile(String path) {
        try (InputStream inputStream = Shader.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                Log.error(String.format("Failed to read shader %s", path));
                System.exit(1);
            }

            try (Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A")) {
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (IOException e) {
            Log.error(String.format("Failed to read shader %s", path));
            System.exit(1);
        }
        return null;
    }

    public static void loadShader(int programId, String filePath, int type) {
        int shaderId = glCreateShader(type);
        String shaderSource = readShaderFile(filePath);
        glShaderSource(shaderId, shaderSource);
        glCompileShader(shaderId);
        checkShaderStatus(shaderId);
        glAttachShader(programId, shaderId);
        glLinkProgram(programId);
        glDeleteShader(shaderId);
    }
}
