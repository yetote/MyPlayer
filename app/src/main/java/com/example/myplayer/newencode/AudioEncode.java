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
public class AudioEncode {
    private int sampleRate, channelCount;
    private AudioRecord audioRecord;
    private int channelLayout;
    private static final String TAG = "AudioEncode";
    private Thread thread;
    private boolean isRecording;
    private byte[] audioData;

    public AudioEncode(int sampleRate, int channelCount) {
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
                Log.e(TAG, "AudioEncode: 不合法的音道数");
                break;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelLayout, AudioFormat.ENCODING_PCM_16BIT, sampleRate * channelCount);
        audioData = new byte[sampleRate * channelCount];
        thread = new Thread(() -> {
            while (isRecording) {
                int ret = audioRecord.read(audioData, 0, sampleRate * channelCount);
                Log.e(TAG, "AudioEncode: 读取了" + ret + "个字节");
            }
        });
    }

    public void start() {
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "start: 录音器未启动");
            return;
        }
        isRecording = true;
        audioRecord.startRecording();
        thread.start();
    }
}
