package com.example.myplayer;

import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myplayer.encode.MyCamera2;
import com.example.myplayer.newencode.Encode;

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
    private Encode encode;
    private int dw, dh;
    private TextureView textureView;
    private SurfaceTexture surfaceTexture;
    private Size bestPreviewSize;
    private static final String TAG = "RecordActivity";
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            surfaceTexture = surface;
            resetBestPreviewSize(surfaceTexture);
            encode.openCamera(new Surface(surfaceTexture));
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
            if (encode != null) {
                bestPreviewSize = encode.getBestSize();
                if (bestPreviewSize != null) {
                    surfaceTexture.setDefaultBufferSize(bestPreviewSize.getWidth(), bestPreviewSize.getHeight());
                }
            }
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


        btn = findViewById(R.id.new_record_start__btn);
        textureView = findViewById(R.id.new_record_textureView);

        encode = new Encode(this, 48000, 2, dw, dh);
        textureView.setSurfaceTextureListener(surfaceTextureListener);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
