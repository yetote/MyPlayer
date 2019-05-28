package com.example.myplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import com.example.myplayer.encode.MyCamera2;
import com.example.myplayer.encode.NewAudioEncode;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

public class NewRecordActivity extends AppCompatActivity {
    private Button btn;
    private NewAudioEncode audioEncode;
    private static final int PERMISSION_RECORD_CODE = 1;
    private boolean isRecording;
    private String aacPath;
    private String videoPath;
    private SurfaceTexture surfaceTexture;
    private TextureView textureView;
    private MyCamera2 myCamera;
    boolean isCameraInit;
    private int[] bestPreviewSize;
    private int width, height;
    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            if (isCameraInit) {
                myCamera.openCamera(myCamera.getBackCameraId());
                surfaceTexture = surface;
                bestPreviewSize = myCamera.getBestSize(MyCamera2.BACK_CAMERA);
                surfaceTexture.setDefaultBufferSize(bestPreviewSize[0], bestPreviewSize[1]);
                myCamera.openPreview(new Surface(surfaceTexture));
            }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | SYSTEM_UI_FLAG_FULLSCREEN
                | SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        setContentView(R.layout.activity_new_record);
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        width = point.x;
        height = point.y;
        init();


        btn.setOnClickListener(v -> {
            List<String> permissions = new ArrayList<>();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (!permissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), PERMISSION_RECORD_CODE);
            } else {
                if (!isRecording) {
                    startRecord();
                } else {
                    surfaceTexture.setDefaultBufferSize(bestPreviewSize[0], bestPreviewSize[1]);
                    audioEncode.stopRecord();
                    myCamera.stopRecord(new Surface(surfaceTexture));
                    isRecording = false;
                }
            }
        });

    }

    private void init() {
        btn = findViewById(R.id.new_record_start__btn);
        textureView = findViewById(R.id.new_record_textureView);
        aacPath = getExternalCacheDir().getPath() + "/res/test.aac";
        videoPath = getExternalCacheDir().getPath() + "/res/test.h264";
        myCamera = new MyCamera2(this, videoPath, width, height);
        isCameraInit = myCamera.initCamera();
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    private void startRecord() {
        isRecording = true;
        surfaceTexture.setDefaultBufferSize(bestPreviewSize[0], bestPreviewSize[1]);
        audioEncode = new NewAudioEncode(48000, 2, aacPath);
        audioEncode.startRecord();
        myCamera.startRecord(getWindowManager().getDefaultDisplay().getRotation(), new Surface(surfaceTexture));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_RECORD_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                startRecord();
                break;
            default:
                break;
        }
    }
}
