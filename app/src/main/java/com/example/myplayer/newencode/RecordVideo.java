package com.example.myplayer.newencode;

import android.content.Context;
import android.util.Size;
import android.view.Surface;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.newencode
 * @class describe
 * @time 2019/6/17 10:45
 * @change
 * @chang time
 * @class describe
 */
public class RecordVideo {
    private RecordVideo videoEncode;
    private Context context;
    private CameraUtil cameraUtil;

    public RecordVideo(Context context, int displayWidth, int displayHeight) {
        this.context = context;
        cameraUtil = new CameraUtil(context, displayWidth, displayHeight);
    }

    public void openCamera(Surface surface) {
        cameraUtil.openCamera(CameraUtil.BACK_CAMERA, surface);

    }

    public void start(int orientation, Surface surface, MutexUtil mutexUtil) {
        cameraUtil.startRecord(orientation, surface,mutexUtil);
    }

    public void stop(Surface surface) {
        cameraUtil.stopRecord(surface);
    }

    public Size getBestSize() {
        if (cameraUtil != null) {
            return cameraUtil.getBestSize(CameraUtil.BACK_CAMERA);
        }
        return null;
    }
}
