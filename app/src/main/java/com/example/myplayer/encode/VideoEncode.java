package com.example.myplayer.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;

import com.example.myplayer.utils.WriteFile;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaCodec.BUFFER_FLAG_END_OF_STREAM;
import static android.media.MediaCodec.CONFIGURE_FLAG_ENCODE;
import static android.media.MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

/**
 * @author ether QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.encode
 * @class describe
 * @time 2019/5/20 15:50
 * @change
 * @chang time
 * @class describe
 */
public class VideoEncode {
    private final WriteFile writeFile;
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private static final String TAG = "VideoEncode";
    byte[] pts;

    public VideoEncode(int w, int h, String path) {
        writeFile = new WriteFile(path);
        mediaFormat = MediaFormat.createVideoFormat(MIMETYPE_VIDEO_AVC, w, h);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, w * h * 30 * 3);
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
        mediaCodec.start();
        pts = null;
    }


    public void encode(ByteBuffer dataBuffer, boolean isFinish) {
        dataBuffer.flip();
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
        inputBuffer.clear();
        inputBuffer.put(dataBuffer);
        int flag = 0;
        if (isFinish) {
            Log.e(TAG, "run: 最后一帧");
            flag = BUFFER_FLAG_END_OF_STREAM;
        }
        mediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.limit(), System.currentTimeMillis(), flag);

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
        while (outBufferIndex >= 0) {
            ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outBufferIndex);
            if (outputBuffer == null) {
                Log.e(TAG, "encode: 未找到编码后容器");
                break;
            }
            if (pts == null) {
                if (bufferInfo.flags == 2) {
                    Log.e(TAG, "run: 第一帧");
                    pts = new byte[bufferInfo.size];
                    Log.e(TAG, "run:第一帧长度 " + outputBuffer.limit());
                    Log.e(TAG, "run:第一帧falsg " + bufferInfo.flags);
                    Log.e(TAG, "run: ptsSize" + pts.length);
                    outputBuffer.get(pts);
                }
            }
            if (bufferInfo.flags == 1) {
                Log.e(TAG, "run: 关键帧");
                writeFile.write(pts);
            } else {
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
            }
            writeFile.write(outputBuffer);
            mediaCodec.releaseOutputBuffer(outBufferIndex, false);
            outBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        }

    }
}
