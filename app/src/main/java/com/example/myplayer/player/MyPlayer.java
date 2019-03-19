package com.example.myplayer.player;

import android.util.Log;

import com.example.myplayer.player.listener.OnDecodeVideoDataCallback;
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
    OnDecodeVideoDataCallback videoDataCallback;




    void onPrepared(boolean isSuccess, int error) {
        if (preparedCallBack != null) {
            preparedCallBack.onPrepared(isSuccess, error);
        } else {
            Log.e(TAG, "onPrepared: " + "无法该回调接口");
        }
    }

    void onDecodeVideoData(byte[] y, byte[] u, byte[] v) {
        if (videoDataCallback != null) {
            videoDataCallback.videoData(y, u, v);
        } else {
            Log.e(TAG, "onDecodeVideoData: " + "无法该回调接口");
        }
    }


    public void setPreparedCallBack(OnPreparedCallBack preparedCallBack) {
        this.preparedCallBack = preparedCallBack;
    }

    public void setVideoDataCallback(OnDecodeVideoDataCallback videoDataCallback) {
        this.videoDataCallback = videoDataCallback;
    }

    public native void prepare(String path);
    public native void play();
}
