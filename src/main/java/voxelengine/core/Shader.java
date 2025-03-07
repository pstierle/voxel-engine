package voxelengine.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.lwjgl.opengl.GL46.*;

public class Shader {
    public static void checkShaderStatus(int shaderId) {
        int status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        if (status != GL_TRUE) {
            int logLength = glGetShaderi(shaderId, GL_INFO_LOG_LENGTH);
            String infoLog = glGetShaderInfoLog(shaderId, logLength);
            System.err.println("Shader compile error: " + infoLog);
            System.exit(1);
        }
    }

    public static String readShaderFile(String path) {
        try (InputStream inputStream = Shader.class.getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                System.err.println("Error: Shader file not found: " + path);
                System.exit(1);
            }

            try (
                    Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A")) {
                return scanner.hasNext() ? scanner.next() : "";
            }
        } catch (IOException e) {
            System.out.print(e);
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
