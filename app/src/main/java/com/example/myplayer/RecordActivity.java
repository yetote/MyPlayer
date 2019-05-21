package com.example.myplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.example.myplayer.encode.MyCamera;

public class RecordActivity extends AppCompatActivity {
    private int width, height;
    private MyCamera camera;
    public static final int PERMISSION_CAMERA = 1;
    private TextureView textureView;
    boolean isCameraOpen;
    private int bestWidth, bestHeight;
    private Button startBtn;
    private Surface surface;
    private ImageReader imageReader;
    private static final String TAG = "RecordActivity";
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        Display dm = this.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        dm.getSize(point);
        width = point.x;
        height = point.y;

        camera = new MyCamera(this, width, height);
        textureView = findViewById(R.id.record_textureView);
        startBtn = findViewById(R.id.record_start_btn);
        backgroundThread = new HandlerThread("ImageBackgroundThread");
        backgroundThread.start();
        backgroundHandler = new android.os.Handler(backgroundThread.getLooper());

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
        } else {
            isCameraOpen = camera.openCamera();
        }
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                if (isCameraOpen) {
                    // TODO: 2019/5/21 surface需要release
                    bestWidth = camera.getBestSize(MyCamera.BACK_CAMERA)[0];
                    bestHeight = camera.getBestSize(MyCamera.BACK_CAMERA)[1];
                    imageReader = ImageReader.newInstance(bestWidth, bestHeight, ImageFormat.YUV_420_888, 1);
                    surface = new Surface(surfaceTexture);
                    surfaceTexture.setDefaultBufferSize(bestWidth, bestHeight);
                    camera.openPreview(surface);

                } else {
                    Toast.makeText(RecordActivity.this, "相机未打开，无法开启预览", Toast.LENGTH_SHORT).show();
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
        });

        startBtn.setOnClickListener(v -> {
            if (isCameraOpen) {
                imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                      Image img= reader.acquireNextImage();
                        Log.e(TAG, "onImageAvailable: imageReader接受图片");
//                        reader.close();
                        img.close();
                    }
                }, backgroundHandler);

                camera.openPreview(surface, imageReader.getSurface());
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isCameraOpen = camera.openCamera();
                }
                break;
            default:
                break;
        }
    }
}
