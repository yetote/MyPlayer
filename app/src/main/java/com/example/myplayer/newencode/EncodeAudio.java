package com.example.myplayer.newencode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.newencode
 * @class describe
 * @time 2019/6/19 13:58
 * @change
 * @chang time
 * @class describe
 */
public class EncodeAudio {
    private MediaCodec audioCodec;
    private MediaFormat mediaFormat;
    private int sampleRate, channelCount;
    private BlockingQueue<byte[]> audioQueue;
    private boolean isRecording;
    private static final String TAG = "EncodeAudio";

    public EncodeAudio(int sampleRate, int channelCount) {
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        mediaFormat = MediaFormat.createAudioFormat(MIMETYPE_AUDIO_AAC, sampleRate, channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 512 * 1024);
        HandlerThread audioHandlerThread = new HandlerThread("audioEncode");
        audioHandlerThread.start();
        Handler audioHandler = new Handler(audioHandlerThread.getLooper());
        audioQueue = new LinkedBlockingQueue<>();
        try {
            audioCodec = MediaCodec.createEncoderByType(MIMETYPE_AUDIO_AAC);
            audioCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                    if (index >= 0) {
                        ByteBuffer inputBuffer = codec.getInputBuffer(index);
                        if (inputBuffer != null) {
                            Log.e(TAG, "onInputBufferAvailable: 音频入队");
                        }
                        codec.queueInputBuffer(index, 0, 0, System.currentTimeMillis(), 0);
                    }
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    Log.e(TAG, "onOutputBufferAvailable: 音频出队");
                    codec.releaseOutputBuffer(index, false);
                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

                }
            }, audioHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public void pushData(byte[] data) {
        try {
            audioQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        isRecording = true;
        audioCodec.start();
    }

    public void stop() {
        isRecording = false;
    }

    public MediaFormat getAudioFormat() {
        return mediaFormat;
    }
}
