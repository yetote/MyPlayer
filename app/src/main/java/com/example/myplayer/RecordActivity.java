package com.example.myplayer;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myplayer.newencode.Record;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

/**
 * @author yetote QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer
 * @class describe
 * @time 2019/6/17 11:15
 * @change
 * @chang time
 * @class describe
 */
public class RecordActivity extends AppCompatActivity {
    private Button btn;
    private Record record;
    private int dw, dh;
    private TextureView textureView;
    private SurfaceTexture surfaceTexture;
    private Size bestPreviewSize;
    private static final String TAG = "RecordActivity";
    private boolean isRecording;
    private String path;
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            surfaceTexture = surface;
            resetBestPreviewSize(surfaceTexture);
            record.openCamera(new Surface(surfaceTexture));
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private void resetBestPreviewSize(SurfaceTexture surfaceTexture) {
        if (bestPreviewSize == null) {
            if (record != null) {
                bestPreviewSize = record.getBestSize();
                if (bestPreviewSize != null) {
                    surfaceTexture.setDefaultBufferSize(bestPreviewSize.getWidth(), bestPreviewSize.getHeight());
                }
            }
        } else {
            surfaceTexture.setDefaultBufferSize(bestPreviewSize.getWidth(), bestPreviewSize.getHeight());
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | SYSTEM_UI_FLAG_FULLSCREEN
                | SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(R.layout.activity_new_record);
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metric);

        dw = metric.widthPixels;
        dh = metric.heightPixels;
        Log.e(TAG, "onCreate: 屏幕分辨率为 " + dw + "      " + dh);
        path = this.getExternalFilesDir(null).getPath() + "/test.mp4";
        btn = findViewById(R.id.new_record_start__btn);
        textureView = findViewById(R.id.new_record_textureView);

        record = new Record(this, 48000, 2, dw, dh, path);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        btn.setOnClickListener(v -> {
            if (!isRecording) {
                resetBestPreviewSize(surfaceTexture);
                record.start(getWindowManager().getDefaultDisplay().getRotation(), new Surface(surfaceTexture));
                isRecording = true;
            } else {
                resetBestPreviewSize(surfaceTexture);
                record.stop(new Surface(surfaceTexture));
            }
        });
    }
}
