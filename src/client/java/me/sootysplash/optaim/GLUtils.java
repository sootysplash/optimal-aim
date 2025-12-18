package me.sootysplash.optaim;

import java.io.IOException;
import java.io.InputStream;

import static org.lwjgl.opengl.GL32.*;

public interface GLUtils {
    static int makeCubeObject() {
        final float minimumValue = 0;
        final float maximumValue = 1;
        float minx = minimumValue;
        float miny = minimumValue;
        float minz = minimumValue;
        float maxx = maximumValue;
        float maxy = maximumValue;
        float maxz = maximumValue;
        float vertices[] = {
                // back face
                minx, miny, minz,  // bottom-left
                maxx, miny, minz,  // bottom-right
                maxx, maxy, minz,  // top-right
                maxx, maxy, minz,  // top-right
                minx, maxy, minz,  // top-left
                minx, miny, minz,  // bottom-left
                // front face
                minx, miny, maxz, // bottom-left
                maxx, maxy, maxz, // top-right
                maxx, miny, maxz, // bottom-right
                maxx, maxy, maxz, // top-right
                minx, miny, maxz, // bottom-left
                minx, maxy, maxz, // top-left
                // left face
                minx, maxy, maxz,  // top-right
                minx, miny, minz,  // bottom-left
                minx, maxy, minz,  // top-left
                minx, miny, minz,  // bottom-left
                minx, maxy, maxz,  // top-right
                minx, miny, maxz,  // bottom-right
                // right face
                maxx, maxy, maxz, // top-left
                maxx, maxy, minz, // top-right
                maxx, miny, minz, // bottom-right
                maxx, miny, minz, // bottom-right
                maxx, miny, maxz, // bottom-left
                maxx, maxy, maxz, // top-left
                // bottom face
                minx, miny, minz,  // top-right
                maxx, miny, maxz,  // bottom-left
                maxx, miny, minz,  // top-left
                maxx, miny, maxz,  // bottom-left
                minx, miny, minz,  // top-right
                minx, miny, maxz,  // bottom-right
                // top face
                minx, maxy, minz, // top-left
                maxx, maxy, minz, // top-right
                maxx, maxy, maxz, // bottom-right
                maxx, maxy, maxz, // bottom-right
                minx, maxy, maxz, // bottom-left
                minx, maxy, minz, // top-left
        };


        int VAO = glGenVertexArrays();
        glBindVertexArray(VAO);

        int VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int totalSize = 3 * 4;

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(
                0,
                3,
                GL_FLOAT,
                false,
                totalSize,
                (0 * 4L));

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return VAO;
    }

    static int makeShaderProgram(int vertexShader, int fragmentShader) {
        int shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        int success[] = new int[1];
        glGetProgramiv(shaderProgram, GL_LINK_STATUS, success);
        if(success[0] == 0) {
            String infoLog = glGetProgramInfoLog(shaderProgram, 512);
            System.out.println("ERROR::SHADER::LINK_FAILED\n" + infoLog);
        }
        return shaderProgram;
    }

    static String readAsset(String path) {
        try (InputStream is = Main.class.getClassLoader().getResourceAsStream(path)) {
            assert is != null;
            return new String(is.readAllBytes());
        } catch (IOException | NullPointerException ignored) {
        }
        return null;
    }

    static int makeShader(String pathToCode, int shaderType) {
        int shader = glCreateShader(shaderType);
        String srcCode = readAsset(pathToCode);
        if (srcCode == null) {
            System.out.println("unable to read asset: " + pathToCode);
            return shader;
        }
        glShaderSource(shader, srcCode);
        glCompileShader(shader);

        int success[] = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, success);
        if(success[0] == 0) {
            String infoLog = glGetShaderInfoLog(shader, 512);
            System.out.println("ERROR::SHADER::VERTEX/FRAGMENT::COMPILATION_FAILED\n\nShader:\n");
            System.out.println(srcCode + "\n\nErrors:\n" + infoLog);
        }
        return shader;
    }
}
