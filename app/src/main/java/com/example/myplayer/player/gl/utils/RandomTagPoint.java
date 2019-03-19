package com.example.myplayer.player.gl.utils;

import java.math.BigDecimal;
import java.util.Random;

/**
 * @author yetote QQ:503779938
 * @name Bamboo
 * @class name：com.example.bamboo.opengl.utils
 * @class describe
 * @time 2018/11/13 14:38
 * @change
 * @chang time
 * @class describe
 */
public class RandomTagPoint {
    /**
     * 生成随机点的X坐标
     *
     * @param xArr  x坐标数组
     * @param count 要生成的个数
     */
    public static void pointX(float[] xArr, int count) {
        xArr[0] = randomNum();
        for (int i = 1; i < count; i++) {
            xArr[i] = randomNum();
            if (Math.abs(Math.abs(xArr[i]) - Math.abs(xArr[i - 1])) < 0.05f) {
                i--;
            }
        }
    }

    /**
     * 生成随机点的Y坐标
     *
     * @param yArr  y坐标数组
     * @param count 要生成的个数
     */
    public static void pointY(float[] yArr, int count) {
        yArr[0] = randomNum();
        for (int i = 1; i < count; i++) {
            yArr[i] = randomNum();
            if (Math.abs(Math.abs(yArr[i]) - Math.abs(yArr[i - 1])) < 0.05f) {
                i--;
            }
        }
    }

    private static float randomNum() {
        float temp = 0;
        while (Math.abs(temp) <= 0.02f) {
            temp = new Random(System.currentTimeMillis()).nextFloat() * 2f - 1.0f;
        }
        BigDecimal b = new BigDecimal(temp);
        float f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        return f1;
    }

    public static void randomRadius(float[] radiusArr, int count) {
        radiusArr[0] = Math.abs(randomNum()) / 2;
        for (int i = 1; i < count; i++) {
            radiusArr[i] = Math.abs(randomNum() / 2);
            if (Math.abs(radiusArr[i] - radiusArr[i - 1]) < 0.05) {
                i--;
            }
        }

    }
}
