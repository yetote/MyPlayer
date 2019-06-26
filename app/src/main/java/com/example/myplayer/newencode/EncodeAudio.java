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
import java.util.concurrent.TimeUnit;

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
    private MutexUtil mutexUtil;
    long pts = 0;
    long lastPts = 0;

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
                    if (pts == 0) {
                        pts = System.currentTimeMillis() * 1000L;
                    }
                    if (index >= 0) {
                        ByteBuffer inputBuffer = codec.getInputBuffer(index);
                        if (inputBuffer != null) {
                            int flag = 0;
                            try {
                                byte[] audioData = audioQueue.poll(2, TimeUnit.SECONDS);
                                if (!isRecording && audioQueue.size() == 0) {
                                    flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                                    Log.e(TAG, "onInputBufferAvailable: 音频最后一帧");
                                }
                                Log.e(TAG, "onInputBufferAvailable: isRecording" + isRecording);
                                Log.e(TAG, "onInputBufferAvailable: isEmpty" + audioQueue.size());
                                inputBuffer.clear();
                                if (audioData != null) {
                                    inputBuffer.put(audioData);
                                    codec.queueInputBuffer(index, 0, audioData.length, System.currentTimeMillis() * 1000L - pts, flag);
                                    Log.e(TAG, "onInputBufferAvailable: 音频送入编码区");
                                } else {
                                    codec.queueInputBuffer(index, 0, 0, System.currentTimeMillis(), flag);
                                    Log.e(TAG, "onInputBufferAvailable: 未取到数据");
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e(TAG, "onInputBufferAvailable: inputbuffer为空");
                        }
                    }
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    Log.e(TAG, "onOutputBufferAvailable: 音频送出编码区");
                    if (index >= 0) {
                        ByteBuffer outBuffer = codec.getOutputBuffer(index);
                        if (outBuffer != null) {
                            if (info.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                                Log.e(TAG, "onOutputBufferAvailable: 写入封包器的flag" + info.flags);
                                Log.e(TAG, "onOutputBufferAvailable: 写入封包器的音频数据大小为" + info.size);
                                Log.e(TAG, "onOutputBufferAvailable: 写入封包器的时间戳为" + info.presentationTimeUs);
                                if (lastPts < info.presentationTimeUs) {
                                    mutexUtil.writeData(outBuffer, info, true);
                                    lastPts = info.presentationTimeUs;
                                } else {
                                    Log.e(TAG, "onOutputBufferAvailable: 音频时间戳不正确");
                                }
                            }
                        }
                    }
                    if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                        Log.e(TAG, "onOutputBufferAvailable: 音频编码结束");
                        mutexUtil.stop(true);
                    }
                    codec.releaseOutputBuffer(index, false);
                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                    Log.e(TAG, "onOutputFormatChanged: " + format);
                    mutexUtil.addTrack(format, true);
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

    public void start(MutexUtil mutexUtil) {
        this.mutexUtil = mutexUtil;
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
