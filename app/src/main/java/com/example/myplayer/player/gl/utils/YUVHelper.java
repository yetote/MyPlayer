package com.example.myplayer.player.gl.utils;

import android.opengl.GLES20;
import android.util.Log;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glPixelStorei;

/**
 * @author yetote QQ:503779938
 * @name OpenGLPlay
 * @class name：com.example.openglplay.utils
 * @class describe
 * @time 2018/9/18 13:25
 * @change
 * @chang time
 * @class describe
 */
public class YUVHelper {
    private static final String TAG = "YUVHelper";
    public static final int Y_TYPE = 0;
    public static final int U_TYPE = 1, V_TYPE = 2;

    public static int[] loadTexture(int[] textureObjectIds) {
        glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        glGenTextures(3, textureObjectIds, 0);
        if (textureObjectIds[0] == 0) {
            Log.e(TAG, "loadTexture: " + "opengl无法创建纹理对象");
//            return -1;
        }
//        if (bufferData == null) {
//            Log.e(TAG, "输入数据为null: " + "无法加载位图图片");
//            glDeleteTextures(1, textureObjectIds, 0);
////            return 0;
//        }
//        if (!even(w, h)) {
//            Log.e(TAG, "loadTexture: " + "yuv数据的宽高必须是偶数");
////            return 0;
//        }
        for (int i = 0; i < 3; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureObjectIds[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        }

//        bufferData.clear();
//        glBindTexture(GL_TEXTURE_2D, type);
        Log.e(TAG, "loadTexture: " + textureObjectIds[2]);
        glBindTexture(GL_TEXTURE_2D, 0);
        return textureObjectIds;
    }
}
