package com.example.myplayer.encode;

import android.media.Image;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.example.myplayer.utils.WriteFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.CONFIGURE_FLAG_ENCODE;
import static android.media.MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.encode
 * @class describe
 * @time 2019/5/28 15:20
 * @change
 * @chang time
 * @class describe
 */
public class NewVideoEncode {
    private WriteFile writeFile;
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private static final String TAG = "RecordVideo";
    byte[] pps;
    private long frameCount = 0;
    private int bitrate;
    private int width, height;
    private BlockingQueue<byte[]> blockingQueue;
    private boolean isRecording;
    private Thread encodeThread;
    private MutexMp4 mutexMp4;

    public void setMutexMp4(MutexMp4 mutexMp4) {
        this.mutexMp4 = mutexMp4;
        Log.e(TAG, "setMutexMp4: video's mutex" + mutexMp4);
    }

    public NewVideoEncode(int width, int height, String path) {
        this.width = width;
        this.height = height;
        writeFile = new WriteFile(path);
        blockingQueue = new LinkedBlockingQueue<>();
        mediaFormat = MediaFormat.createVideoFormat(MIMETYPE_VIDEO_AVC, width, height);
        bitrate = width * height * 30 * 3;
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_BITRATE_MODE, BITRATE_MODE_VBR);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        try {
            mediaCodec = MediaCodec.createEncoderByType(MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaCodec.configure(mediaFormat, null, null, CONFIGURE_FLAG_ENCODE);

        pps = null;
    }

    public void startEncode() {
        isRecording = true;
        mediaCodec.start();
        new Thread(() -> {
            byte[] data = new byte[width * height * 3 / 2];
            while (true) {
                if (!isRecording && blockingQueue.isEmpty()) {
                    Log.e(TAG, "run: 视频编码完成");
                    mediaCodec.stop();
                    break;
                }
                long pts = frameCount++ * 1000000 / bitrate;

                int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                if (inputBufferIndex == -1) {
                    Log.e(TAG, "encode: " + "未获取到入队索引");
                    return;
                }

                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex);
                if (inputBuffer == null) {
                    Log.e(TAG, "encode: 为获取入队容器");
                    return;
                }
                try {
                    data = blockingQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                inputBuffer.clear();
                inputBuffer.put(data);
                int flag = 0;
//                    if (isFinish) {
//                        Log.e(TAG, "run: 最后一帧");
//                        flag = BUFFER_FLAG_END_OF_STREAM;
//                    }
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.limit(), pts, flag);

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                while (outBufferIndex >= 0) {
                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outBufferIndex);
                    if (outputBuffer == null) {
                        Log.e(TAG, "encode: 未找到编码后容器");
                        break;
                    }
                    if (pps == null) {
                        if (bufferInfo.flags == 2) {
                            Log.e(TAG, "run: 第一帧");
                            pps = new byte[bufferInfo.size];
                            Log.e(TAG, "run:第一帧长度 " + outputBuffer.limit());
                            Log.e(TAG, "run:第一帧falsg " + bufferInfo.flags);
                            Log.e(TAG, "run: ptsSize" + pps.length);
                            outputBuffer.get(pps);
                        }
                    }
//                    if (bufferInfo.flags == 1) {
//                        Log.e(TAG, "run: 关键帧");
////                        writeFile.write(pps);
//                    } else {
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
//                    }
                    mutexMp4.startMutex(MutexMp4.TRACK_VIDEO, outputBuffer, bufferInfo);
                    mediaCodec.releaseOutputBuffer(outBufferIndex, false);
                    outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                }
            }
        }).start();
    }


    public void encodeData(Image img) {
        long now = System.currentTimeMillis();
        int w = img.getWidth();
        int h = img.getHeight();
        byte[] yBuffer = new byte[w * h];
        byte[] uvBuffer = new byte[w * h / 2];
        byte[] dataBuffer = new byte[w * h * 3 / 2];
        img.getPlanes()[0].getBuffer().get(yBuffer);
        img.getPlanes()[1].getBuffer().get(uvBuffer, 0, w * h / 2 - 1);
        uvBuffer[w * h / 2 - 1] = img.getPlanes()[2].getBuffer().get(w * h / 2 - 2);
        System.arraycopy(yBuffer, 0, dataBuffer, 0, yBuffer.length);
        System.arraycopy(uvBuffer, 0, dataBuffer, yBuffer.length, uvBuffer.length);
        try {
            blockingQueue.put(dataBuffer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopEncode() {
        isRecording = false;
        mutexMp4.requestStop(MutexMp4.TRACK_VIDEO);
    }

    public MediaFormat getMediaFormat() {
        return mediaFormat;
    }
}
