package com.example.myplayer.player;

import android.media.MediaCodec;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static android.media.MediaCodecList.REGULAR_CODECS;

/**
 * @author ether QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.player
 * @class describe
 * @time 2019/4/29 17:21
 * @change
 * @chang time
 * @class describe
 */
public class MediaCodecSupport {
    static Map<String, String> map = new HashMap<>();
    private static MediaCodecList list = new MediaCodecList(REGULAR_CODECS);
    private static final String TAG = "MediaCodecSupport";

    static {
        map.put("audio/mp4a-latm", "aac");
        map.put("video/avc", "h264");
        map.put("video/hevc", "h265");

    }


    public boolean isSupport(String name) {
        Log.e(TAG, "isSupport: " + name + " ?");
        for (int i = 0; i < list.getCodecInfos().length; i++) {
            String[] support = list.getCodecInfos()[i].getSupportedTypes();
            list.getCodecInfos()[i].getCapabilitiesForType(support[0]);
            for (int j = 0; j < support.length; j++) {
                if (name.equals(map.get(support[j]))) {
                    Log.e(TAG, list.getCodecInfos()[i] + "支持: " + support[j]);
                    return true;
                }
            }
        }
        return false;
    }

}
