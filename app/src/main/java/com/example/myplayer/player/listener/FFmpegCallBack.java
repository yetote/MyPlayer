package com.example.myplayer.player.listener;

/**
 * @author ether QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.player.listener
 * @class describe
 * @time 2019/4/11 17:03
 * @change
 * @chang time
 * @class describe
 */
public interface FFmpegCallBack {
    /**
     * 播放结束回调接口
     */
    void onFinish();

    /**
     * 停止播放回调
     */
    void onPause();

    /**
     * ffmpeg准备完成回调
     *
     * @param isSuccess 是否准备成功
     * @param errorCode 错误代码
     */
    void onPrepared(boolean isSuccess, int errorCode);
}
