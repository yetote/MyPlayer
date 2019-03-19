package com.example.myplayer.player.gl.utils;

public class MatrixHelper {
    /**
     * @param m             透视投影矩阵
     * @param yFovInDegrees 视椎体角度
     * @param aspect        焦距
     * @param n             视椎体远端
     * @param f             视椎体近端
     */
    public static void perspectiveM(float[] m, float yFovInDegrees, float aspect, float n, float f) {
        //视野角度
        final float angleInRadians = (float) (yFovInDegrees * Math.PI / 180.0f);
        //相机的焦距
        final float a = (float) (1.0 / Math.tan(angleInRadians / 2.0f));
        m[0] = a / aspect;
        m[1] = 0f;
        m[2] = 0f;
        m[3] = 0f;

        m[4] = 0f;
        m[5] = a;
        m[6] = 0f;
        m[7] = 0f;

        m[8] = 0f;
        m[9] = 0f;
        m[10] = -((f + n) / (f - n));
        m[11] = -1f;

        m[12] = 0f;
        m[13] = 0f;
        m[14] = -((2f * f * n) / (f - n));
        m[15] = 0f;
    }
}
