package com.example.myplayer.utils;

import android.content.Context;

/**
 * @author yetote QQ:503779938
 * @name ScrollFFmpegDemo
 * @class name：com.example.scrollffmpegdemo.utils
 * @class describe
 * @time 2019/3/12 13:39
 * @change
 * @chang time
 * @class describe
 */
public class DPUtil {

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
