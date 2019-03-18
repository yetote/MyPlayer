package com.example.myplayer.player;

import android.util.Log;

import com.example.myplayer.player.listener.OnPreparedCallBack;

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

    private static final String TAG = "MyPlayer";
    OnPreparedCallBack preparedCallBack;

    public native void prepare(String path);

    void onPrepared(boolean isSuccess, int error) {
        if (preparedCallBack != null) {
            preparedCallBack.onPrepared(isSuccess, error);
        } else {
            Log.e(TAG, "onPrepared: " + "无法回调接口");
        }
    }

    public void setPreparedCallBack(OnPreparedCallBack preparedCallBack) {
        this.preparedCallBack = preparedCallBack;
    }
}
