package com.example.myplayer.newencode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static android.media.MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR;
import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.newencode
 * @class describe
 * @time 2019/6/18 15:53
 * @change
 * @chang time
 * @class describe
 */
public class Encode {
    private MediaCodec audioCodec, videoCodec;
    private MediaFormat audioFormat, videoFormat;
    private BlockingQueue<byte[]> audioQueue;
    private BlockingQueue<byte[]> videoQueue;
    private HandlerThread videoHandlerThread, audioHandlerThread;
    private Handler videoHandler, audioHandler;
    private boolean isRecording;
    private static final String TAG = "Encode";

    public Encode(int recordWidth, int recordHeight, int sampleRate, int channelCount) {
        audioQueue = new LinkedBlockingQueue<>();
        videoQueue = new LinkedBlockingQueue<>();
        audioFormat = MediaFormat.createAudioFormat(MIMETYPE_AUDIO_AAC, sampleRate, channelCount);
        videoFormat = MediaFormat.createVideoFormat(MIMETYPE_VIDEO_AVC, recordWidth, recordHeight);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * channelCount);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 512 * 1024);

        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * channelCount);
        videoFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, BITRATE_MODE_VBR);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        videoHandlerThread = new HandlerThread("videoEncode");
        audioHandlerThread = new HandlerThread("audioHandler");
        videoHandlerThread.start();
        audioHandlerThread.start();
        videoHandler = new Handler(videoHandlerThread.getLooper());
        audioHandler = new Handler(audioHandlerThread.getLooper());
        try {
            videoCodec = MediaCodec.createEncoderByType(MIMETYPE_VIDEO_AVC);
            audioCodec = MediaCodec.createEncoderByType(MIMETYPE_AUDIO_AAC);
        } catch (IOException e) {
            e.printStackTrace();
        }

        videoCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                if (!isRecording && videoQueue.isEmpty()) {
                    Log.e(TAG, "onInputBufferAvailable: 视频最后一帧");
                } else {
                    Log.e(TAG, "onInputBufferAvailable: 视频送入编码区");
                }
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                Log.e(TAG, "onOutputBufferAvailable: 视频送出编码区");
                codec.releaseOutputBuffer(index, false);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

            }
        }, videoHandler);

        audioCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                if (!isRecording && audioQueue.isEmpty()) {
                    Log.e(TAG, "onInputBufferAvailable: 音频最后一帧");
                }
                Log.e(TAG, "onInputBufferAvailable: 音频送入编码区");
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                Log.e(TAG, "onOutputBufferAvailable: 音频送出编码区");
                codec.releaseOutputBuffer(index, false);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

            }
        }, audioHandler);


        audioCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        videoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public void pushData(byte[] data, boolean isAudio) {
        if (isAudio) {
            try {
                audioQueue.put(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            try {
                videoQueue.put(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        isRecording = true;
        audioCodec.start();
        videoCodec.start();
    }

    public void stop() {
        isRecording = false;
    }

}
