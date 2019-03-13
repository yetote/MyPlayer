package com.example.myplayer.player;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.player
 * @class describe
 * @time 2019/3/13 17:36
 * @change
 * @chang time
 * @class describe
 */
public class MyPlayer {
    static {
        System.loadLibrary("native-lib");
    }
    public native void prepare(String path);
}
