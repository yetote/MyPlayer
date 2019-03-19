package com.example.myplayer.player.gl.programs;

import android.content.Context;

import com.example.myplayer.R;

import java.nio.Buffer;

import static android.opengl.GLES20.GL_LUMINANCE;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE1;
import static android.opengl.GLES20.GL_TEXTURE2;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glTexImage2D;
import static android.opengl.GLES20.glUniform1i;

/**
 * @author yetote QQ:503779938
 * @name ScrollFFmpegDemo
 * @class name：com.example.scrollffmpegdemo.programs
 * @class describe
 * @time 2019/3/1 10:18
 * @change
 * @chang time
 * @class describe
 */
public class PlayerProgram extends ShaderProgram {
    public final int aPositionLocation;
    public final int aTexCoordLocation;
    public static  int uTexYLocation;
    public static int uTexULocation;
    public static int uTexVLocation;
    private static final String TAG = "RectProgram";
    private  int[] yuvLocation;
    private static final int[] YUV_TEXTURE_ID = {GL_TEXTURE0, GL_TEXTURE1, GL_TEXTURE2};

    public PlayerProgram(Context context) {
        super(context, R.raw.yuv_vertex_shader, R.raw.yuv_frag_shader);

        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTexCoordLocation = glGetAttribLocation(program, A_TEXTURE_COORDINATES);

        uTexYLocation = glGetUniformLocation(program, U_TEXTURE_Y);
        uTexULocation = glGetUniformLocation(program, U_TEXTURE_U);
        uTexVLocation = glGetUniformLocation(program, U_TEXTURE_V);
        yuvLocation = new int[]{uTexYLocation, uTexULocation, uTexVLocation};
    }

    public int getAttrPositionLocation() {
        return aPositionLocation;
    }

    public int getAttrTexCoordLocation() {
        return aTexCoordLocation;
    }

    public void setUniform(int textureId, Buffer buffer, int type) {
        buffer.position(0);
        glActiveTexture(GL_TEXTURE0 + type);
        glBindTexture(GL_TEXTURE_2D, textureId);
        if (type == 0) {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 1280, 720, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, buffer);
        } else {
            glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, 1280 / 2, 720 / 2, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE, buffer);
        }
        glUniform1i(yuvLocation[type], type);

    }
}
