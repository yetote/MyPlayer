package com.example.myplayer.newencode;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.newencode
 * @class describe
 * @time 2019/6/17 10:44
 * @change
 * @chang time
 * @class describe
 */
public class RecordAudio {
    private int sampleRate, channelCount;
    private AudioRecord audioRecord;
    private int channelLayout;
    private static final String TAG = "RecordAudio";
    private Thread thread;
    private boolean isRecording;
    private byte[] audioData;
    private EncodeAudio encodeAudio;

    public RecordAudio(int sampleRate, int channelCount) {
        this.sampleRate = sampleRate;
        this.channelCount = channelCount;
        switch (channelCount) {
            case 1:
                channelLayout = AudioFormat.CHANNEL_IN_MONO;
                break;
            case 2:
                channelLayout = AudioFormat.CHANNEL_IN_STEREO;
                break;
            default:
                channelLayout = -1;
                Log.e(TAG, "RecordAudio: 不合法的音道数");
                break;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelLayout, AudioFormat.ENCODING_PCM_16BIT, sampleRate * channelCount);
        audioData = new byte[sampleRate * channelCount];
        encodeAudio = new EncodeAudio(sampleRate, channelCount);
        thread = new Thread(() -> {
            while (isRecording) {
                int ret = audioRecord.read(audioData, 0, sampleRate * channelCount);
                Log.e(TAG, "RecordAudio: 读取了" + ret + "个字节");
                encodeAudio.pushData(audioData);
            }
            audioRecord.stop();
            encodeAudio.stop();
            audioRecord.release();
        });
    }

    public void start() {
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "start: 录音器未启动");
            return;
        }
        isRecording = true;
        audioRecord.startRecording();
        encodeAudio.start();
        thread.start();
    }

    public void stop() {
        isRecording = false;
    }
}
