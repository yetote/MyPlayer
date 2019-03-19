package com.example.myplayer.player.gl.utils;

import android.opengl.GLES20;
import android.util.Log;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;


public class ShaderHelper {
    private static final String TAG = "ShaderHelper";

    public static int compileVertexShader(String shader) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shader);
    }

    public static int compileFragmentShader(String shader) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shader);
    }

    private static int compileShader(int type, String shader) {
        final int shaderObjectId = glCreateShader(type);
        if (shaderObjectId == 0) {
            Log.e(TAG, "compileShader: 创建shader容器失败");
            return 0;
        }
        glShaderSource(shaderObjectId, shader);
        glCompileShader(shaderObjectId);
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);
        Log.e(TAG, "compileShader: " + shader + "\n" + glGetShaderInfoLog(shaderObjectId));
        if (compileStatus[0] == 0) {
            glDeleteShader(shaderObjectId);
            Log.e(TAG, "compileShader: " + "已删除代码");
            return 0;
        }
        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderCode, int fragmentShaderCode) {
        final int program = glCreateProgram();
        if (program == 0) {
            Log.e(TAG, "linkProgram: " + "绘制程序单创建失败");
            return 0;
        }
        glAttachShader(program, vertexShaderCode);
        glAttachShader(program, fragmentShaderCode);
        glLinkProgram(program);
        final int[] programStatus = new int[1];
        glGetProgramiv(program, GL_LINK_STATUS, programStatus, 0);
        Log.e(TAG, "linkProgram: " + glGetShaderInfoLog(program));
        if (programStatus[0] == 0) {
            glDeleteProgram(program);
            return 0;
        }
        return program;
    }

    public static boolean validateProgram(int program) {
        glValidateProgram(program);
        final int[] validateStatus = new int[1];
        glGetProgramiv(program, GL_VALIDATE_STATUS, validateStatus, 0);
        if (validateStatus[0] == 0) {
            Log.e(TAG, "validateProgram: " + validateStatus[0] + "\n" + glGetProgramInfoLog(program));
        }
        return validateStatus[0] != 0;
    }

    public static int buildProgram(String vertexShaderSource, String fragmentShaderSource) {
        int program;
        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);
        program = linkProgram(vertexShader, fragmentShader);
        validateProgram(program);
        return program;
    }
}
