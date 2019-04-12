package com.example.myplayer.player;

import android.util.Log;
import android.view.Surface;

import com.example.myplayer.player.listener.FFmpegCallBack;

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

    FFmpegCallBack ffmpegCallBack;

    public void setFFmpegCallBack(FFmpegCallBack fFmpegCallBack) {
        this.ffmpegCallBack = fFmpegCallBack;
    }

    /**
     * 初始化完成后，ffmpeg会回调此接口
     * @param isSuccess  初始化是否成功
     * @param error  错误码
     */
    void onPrepared(boolean isSuccess, int error) {
        if (ffmpegCallBack != null) {
            ffmpegCallBack.onPrepared(isSuccess, error);
        } else {
            Log.e(TAG, "onPrepared: " + "无法该回调接口");
        }
    }

    /**
     * ffmpeg播放结束后会回调此方法
     */
    void onFinish() {
        if (ffmpegCallBack != null) {
            ffmpegCallBack.onFinish();
        } else {
            Log.e(TAG, "onPrepared: " + "无法该回调接口");
        }
    }

    /**
     * ffmpeg暂停后回调此接口
     */
    void onPause() {
        if (ffmpegCallBack != null) {
            ffmpegCallBack.onPause();
        } else {
            Log.e(TAG, "onStop: " + "无法该回调接口");
        }
    }

    /**
     * ffmpeg以及egl,gles,oboe的初始化
     *
     * @param path       视频路径
     * @param vertexCode 顶点着色器代码
     * @param fragCode   片元着色器代码
     * @param surface    用于显示的画面
     */
    public native void prepare(String path, String vertexCode, String fragCode, Surface surface);

    /**
     * 播放
     *
     * @param w 用于播放的画面的宽
     * @param h 用于播放的画面的高
     */
    public native void play(int w, int h);

    /**
     * 播放暂停
     */
    public native void pause();

    /**
     * 暂停后继续播放
     */
    public native void recover();
}
