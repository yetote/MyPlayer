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
public class Encode {
    private Context context;
    private AudioEncode audioEncode;
    private VideoEncode videoEncode;

    public Encode(Context context, int sampleRate, int channelCount, int displayWidth, int displayHeight) {
        this.context = context;
        audioEncode = new AudioEncode(sampleRate, channelCount);
        videoEncode = new VideoEncode(context, displayWidth, displayHeight);
    }

    public void openCamera(Surface surface) {
        videoEncode.openCamera(surface);
    }

    public void start(Surface surface) {
        audioEncode.start();
//        videoEncode.(surface);
    }

    public Size getBestSize() {
        if (videoEncode != null) {
            return videoEncode.getBestSize();
        }
        return null;
    }
}
