package com.example.myplayer.player.gl.programs;

import android.content.Context;

import com.example.myplayer.player.gl.utils.ShaderHelper;
import com.example.myplayer.player.gl.utils.TextRecourseReader;

import static android.opengl.GLES20.glUseProgram;

public class ShaderProgram {
    public static final String A_POSITION="a_Position";
    public static final String A_TEXTURE_COORDINATES="a_TextureCoordinates";
    public static final String A_RADIUS="a_Radius";
    public static final String U_TEXTURE_Y="u_TextureY";
    public static final String U_TEXTURE_U="u_TextureU";
    public static final String U_TEXTURE_V="u_TextureV";
    public static final String U_SCALE="u_Scale";
    public static final String U_MATRIX="u_Matrix";


    public final int program;

    public ShaderProgram(Context context, int vertexShaderRecourseId, int fragmentShaderRecourseId) {
        program = ShaderHelper.buildProgram(TextRecourseReader.readTextFileFromResource(context, vertexShaderRecourseId),
                TextRecourseReader.readTextFileFromResource(context, fragmentShaderRecourseId));

    }

    public void useProgram() {
        glUseProgram(program);
    }
}
