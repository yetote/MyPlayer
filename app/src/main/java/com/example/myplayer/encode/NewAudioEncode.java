package com.example.myplayer.encode;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.myplayer.utils.WriteFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.encode
 * @class describe
 * @time 2019/5/28 11:18
 * @change
 * @chang time
 * @class describe
 */
public class NewAudioEncode {
    private static final String TAG = "NewAudioEncode";
    private int sampleRate;
    private int channelLayout;
    private AudioRecord audioRecord;
    private MediaCodec mediaCodec;
    private MediaFormat mediaFormat;
    private boolean isRecording;
    private BlockingQueue<byte[]> blockingQueue;
    private Thread recordThread, encodeThread;
    private WriteFile writeFile;

    public NewAudioEncode(int sampleRate, int channelLayout, String aacPath) {
        this.sampleRate = sampleRate;
        this.channelLayout = channelLayout;
        blockingQueue = new LinkedBlockingQueue<>();
        writeFile = new WriteFile(aacPath);
    }

    public void startRecord() {
        if (audioRecord == null) {
            initAudioRecord();
        }
        if (mediaCodec == null) {
            initMediaCodec();
        }
        isRecording = true;

        startRecordThread();
        startEncodeThread();
    }

    public void stopRecord() {
        isRecording = false;
        if (audioRecord == null) {
            audioRecord.stop();
        }
    }


    private void startRecordThread() {
        if (audioRecord == null) {
            Log.e(TAG, "startRecordThread: 未初始化录音器");
            return;
        }
        audioRecord.startRecording();
        byte[] data = new byte[sampleRate * channelLayout];
        new Thread(() -> {
            while (isRecording) {
                audioRecord.read(data, 0, data.length);
                try {
                    blockingQueue.put(data);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void startEncodeThread() {
        if (mediaCodec == null) {
            Log.e(TAG, "startEncodeThread: 未初始化音频编码器");
            return;
        }
        mediaCodec.start();
        new Thread(() -> {
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            while (true) {
                if (!isRecording && blockingQueue.isEmpty()) {
                    mediaCodec.stop();
                    Log.e(TAG, "run: 编码完成");
                    break;
                }
                int inputIndex = mediaCodec.dequeueInputBuffer(-1);
                Log.e(TAG, "run: 输入区缓冲索引" + inputIndex);
                if (inputIndex >= 0) {
                    ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputIndex);
                    if (inputBuffer != null) {
                        try {
                            byte[] data = blockingQueue.take();
                            inputBuffer.clear();
                            inputBuffer.put(data);
                            mediaCodec.queueInputBuffer(inputIndex, 0, data.length, 0, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }


                int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                while (outputIndex >= 0) {
                    Log.e(TAG, "run: 进入编码循环");
                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputIndex);
                    if (outputBuffer != null) {
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        byte[] outData = new byte[bufferInfo.size + 7];
                        outputBuffer.get(outData, 7, bufferInfo.size);
                        addADTStoPacket(outData, bufferInfo.size + 7);
                        writeFile.write(outData);
                    }
                    mediaCodec.releaseOutputBuffer(outputIndex, false);
                    Log.e(TAG, "run: outputIndex" + outputIndex);
                    outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                }
            }
            Log.e(TAG, "run: ?????");
        }).start();
    }


    /**
     * 初始化录音器
     */
    private void initAudioRecord() {
        int channelConfig = chooseChannel(channelLayout);
        if (channelConfig == -1) {
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT, sampleRate * 4);
    }

    /**
     * 初始化音频编码器
     */
    private void initMediaCodec() {
        mediaFormat = MediaFormat.createAudioFormat(MIMETYPE_AUDIO_AAC, sampleRate, channelLayout);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, sampleRate * channelLayout);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 512 * 1024);
        try {
            mediaCodec = MediaCodec.createEncoderByType(MIMETYPE_AUDIO_AAC);
            mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private int chooseChannel(int channelLayout) {
        switch (channelLayout) {
            case 1:
                return AudioFormat.CHANNEL_OUT_MONO;
            case 2:
                return AudioFormat.CHANNEL_OUT_STEREO;
            default:
                return -1;
        }
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 3; // 44.1KHz
        int chanCfg = 2; // CPE

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF1;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) | 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
