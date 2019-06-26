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

import static android.media.MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.newencode
 * @class describe
 * @time 2019/6/19 13:59
 * @change
 * @chang time
 * @class describe
 */
public class EncodeVideo {
    private static final String TAG = "EncodeVideo";
    private MediaCodec videoCodec;
    private MediaFormat videoFormat;
    private MutexUtil mutexUtil;
    private BlockingQueue<byte[]> videoQueue;
    private boolean isRecording;
    private long pts = 0;

    public EncodeVideo(int recordWidth, int recordHeight) {
        videoQueue = new LinkedBlockingQueue<>();
        videoFormat = MediaFormat.createVideoFormat(MIMETYPE_VIDEO_AVC, recordWidth, recordHeight);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, recordWidth * recordHeight * 30 * 3);
        videoFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, BITRATE_MODE_VBR);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        HandlerThread videoHandlerThread = new HandlerThread("videoEncode");
        videoHandlerThread.start();
        Handler videoHandler = new Handler(videoHandlerThread.getLooper());
        try {
            videoCodec = MediaCodec.createEncoderByType(MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        videoCodec.setCallback(new MediaCodec.Callback() {

            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                if (index >= 0) {

                    ByteBuffer inputBuffer = codec.getInputBuffer(index);
                    if (inputBuffer != null) {
                        try {
                            byte[] data = videoQueue.take();
                            int flag = 0;
                            if (!isRecording && videoQueue.isEmpty()) {
                                Log.e(TAG, "onInputBufferAvailable: 最后一帧");
                                flag = MediaCodec.BUFFER_FLAG_END_OF_STREAM;
                            }
                            inputBuffer.clear();
                            inputBuffer.put(data);
                            if (pts == 0) {
                                pts = System.currentTimeMillis() * 1000L;
                            }
                            codec.queueInputBuffer(index, 0, data.length, System.currentTimeMillis() * 1000L - pts, flag);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                Log.e(TAG, "onOutputBufferAvailable: 视频送出编码区");
                if (index >= 0) {
                    ByteBuffer outBuffer = codec.getOutputBuffer(index);
                    if (outBuffer != null) {
                        if (info.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                            Log.e(TAG, "onOutputBufferAvailable: 写入封包器的视频flag为" + info.flags);

                            mutexUtil.writeData(outBuffer, info, false);
                        }
                    }
                }

                if (info.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    Log.e(TAG, "onOutputBufferAvailable: 视频编码结束");
                    mutexUtil.stop(false);
                }
                codec.releaseOutputBuffer(index, false);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                Log.e(TAG, "onOutputFormatChanged: " + format);
                mutexUtil.addTrack(format, false);
            }
        }, videoHandler);

        videoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public void pushData(byte[] data) {
        try {
            videoQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start(MutexUtil mutexUtil) {
        this.mutexUtil = mutexUtil;
        isRecording = true;
        videoCodec.start();
    }

    public void stop() {
        isRecording = false;
    }

    public MediaFormat getVideoFormat() {
        return videoFormat;
    }
}
