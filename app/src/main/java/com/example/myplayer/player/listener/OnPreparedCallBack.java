package com.example.myplayer.player.listener;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.player.listener
 * @class describe
 * @time 2019/3/14 11:47
 * @change
 * @chang time
 * @class describe
 */
public interface OnPreparedCallBack {
    /**
     * ffmpeg准备完成回调借口
     *
     * @param isSuccess 是否准备成功
     * @param errorCode 错误代码
     */
    void onPrepared(boolean isSuccess, int errorCode);
}
