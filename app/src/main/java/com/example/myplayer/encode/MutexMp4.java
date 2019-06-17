package com.example.myplayer.encode;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.encode
 * @class describe
 * @time 2019/5/29 10:14
 * @change
 * @chang time
 * @class describe
 */
public class MutexMp4 {
    private Context context;
    private MediaMuxer mediaMuxer;
    private String path;
    private int videoTrackIndex, audioTrackIndex;
    public static final int TRACK_VIDEO = 1;
    public static final int TRACK_AUDIO = 2;
    private static final String TAG = "MutexMp4";
    private long audioPresentationTimeUsLast = 0;
    private boolean audioClose, videoClose;

    public MutexMp4(Context context, String path) {
        this.context = context;
        this.path = path;
    }


    public void initMutex(MediaFormat audioFmt, MediaFormat videoFmt) {
        try {
            mediaMuxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            audioTrackIndex = mediaMuxer.addTrack(audioFmt);
            videoTrackIndex = mediaMuxer.addTrack(videoFmt);
            Log.e(TAG, "initMutex: 轨道索引" + audioTrackIndex + videoTrackIndex);
            mediaMuxer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMutex(int trackType, ByteBuffer data, MediaCodec.BufferInfo bufferInfo) {
        switch (trackType) {
            case TRACK_AUDIO:
//                bufferInfo.presentationTimeUs = audioPresentationTimeUsLast;
//                audioPresentationTimeUsLast++;
                data.position(bufferInfo.offset);
                data.limit(bufferInfo.offset + bufferInfo.size);
                mediaMuxer.writeSampleData(audioTrackIndex, data, bufferInfo);
                break;
            case TRACK_VIDEO:
                mediaMuxer.writeSampleData(videoTrackIndex, data, bufferInfo);
                break;
            default:
                Log.e(TAG, "startMutex: 传入的轨道类型不对");
                break;
        }
    }

    public void requestStop(int trackType) {
        if (trackType == audioTrackIndex) {
            audioClose = true;
        }
        if (trackType == videoTrackIndex) {
            videoClose = true;
        }
        if (videoClose && audioClose) {
            stopMutex();
        }
    }


    private void stopMutex() {
        if (mediaMuxer != null) {
            Log.e(TAG, "stopMutex: 停止合成器");
            mediaMuxer.stop();
            mediaMuxer.release();
        }
    }
}
