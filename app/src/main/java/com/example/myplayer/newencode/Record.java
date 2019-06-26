package com.example.myplayer.newencode;

import android.content.Context;
import android.util.Size;
import android.view.Surface;

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
public class Record {
    private Context context;
    private RecordAudio recordAudio;
    private RecordVideo recordVideo;
    private Encode encode;
    private MutexUtil mutexUtil;

    public Record(Context context, int sampleRate, int channelCount, int displayWidth, int displayHeight, String path) {
        this.context = context;
        recordAudio = new RecordAudio(sampleRate, channelCount);
        recordVideo = new RecordVideo(context, displayWidth, displayHeight);
//        encode = new Encode(1280, 640, sampleRate, channelCount);
        mutexUtil = new MutexUtil(path);
    }

    public void openCamera(Surface surface) {
        recordVideo.openCamera(surface);
    }

    public void start(int orientation, Surface surface) {
        recordAudio.start(mutexUtil);
        recordVideo.start(orientation, surface,mutexUtil);

    }

    public void stop(Surface surface) {
        recordAudio.stop();
        recordVideo.stop(surface);
    }

    public Size getBestSize() {
        if (recordVideo != null) {
            return recordVideo.getBestSize();
        }
        return null;
    }
}
