package com.example.myplayer.player.listener;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.player.listener
 * @class describe
 * @time 2019/3/19 10:39
 * @change
 * @chang time
 * @class describe
 */
public interface OnDecodeVideoDataCallback {
    /**
     * 传递yuv数组回调
     * @param y y
     * @param u u
     * @param v v
     */
    void videoData(byte[] y, byte[] u, byte[] v);
}
