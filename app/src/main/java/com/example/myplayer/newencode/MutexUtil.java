package com.example.myplayer.newencode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.newencode
 * @class describe
 * @time 2019/6/26 11:17
 * @change
 * @chang time
 * @class describe
 */
public class MutexUtil {
    private MediaMuxer mediaMuxer;
    private int audioTrack = -1, videoTrack = -1;
    private static final String TAG = "MutexUtil";
    private boolean isAudioStop, isVideoStop;
    private boolean isStart;

    public MutexUtil(String path) {
        try {
            mediaMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addTrack(MediaFormat mediaFormat, boolean isAudio) {
        if (isAudio) {
            audioTrack = mediaMuxer.addTrack(mediaFormat);
            Log.e(TAG, "addTrack: 添加音轨");
        } else {
            videoTrack = mediaMuxer.addTrack(mediaFormat);
            Log.e(TAG, "addTrack: 添加视频轨");
        }
        if (audioTrack != -1 && videoTrack != -1) {
            Log.e(TAG, "addTrack: 开启封包器");
            mediaMuxer.start();
            isStart = true;
        }
    }

    public synchronized void writeData(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo, boolean isAudio) {
        if (isStart) {
            if (isAudio) {
                if (audioTrack != -1) {
                    Log.e(TAG, "writeData: audiobuffer" + buffer.limit() + "position" + buffer.position());
                    mediaMuxer.writeSampleData(audioTrack, buffer, bufferInfo);
                } else {
                    Log.e(TAG, "writeData: 未添加音轨，无法写入");
                }
            } else {
                if (videoTrack != -1) {
                    Log.e(TAG, "writeData: videobuffer" + buffer.limit() + "position" + buffer.position());

                    mediaMuxer.writeSampleData(videoTrack, buffer, bufferInfo);
                } else {
                    Log.e(TAG, "writeData: 未添加视频轨，无法写入");
                }
            }
        }
    }

    public synchronized void stop(boolean isAudio) {
        if (isAudio) {
            isAudioStop = true;
            Log.e(TAG, "stop: 音频停止");
        } else {
            isVideoStop = true;
            Log.e(TAG, "stop: 视频停止");
        }
        if (isAudioStop && isVideoStop) {
            mediaMuxer.stop();
            Log.e(TAG, "stop: 停止封包器");
        }
    }

    public boolean isStart() {
        return isStart;
    }
}
