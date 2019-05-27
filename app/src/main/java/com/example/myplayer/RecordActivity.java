package com.example.myplayer;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import com.example.myplayer.encode.MyCamera;
import com.example.myplayer.encode.VideoData;
import com.example.myplayer.encode.VideoEncode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static android.animation.ValueAnimator.INFINITE;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

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
    public static BlockingQueue<byte[]> blockingQueue = new LinkedBlockingDeque<>();
    private VideoEncode videoEncode;
    private boolean isRecording;
    private Thread encodeThread;
    private ByteBuffer dataBuffer;
    private String path;
    private NotifyLightView notifyLightView;
    private int recordWith = 1280, recordHeight = 640;
    private SurfaceTexture surfaceTexture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | SYSTEM_UI_FLAG_FULLSCREEN
                | SYSTEM_UI_FLAG_HIDE_NAVIGATION);
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
        path = getExternalCacheDir().getPath() + "/res/test.h264";
        notifyLightView = findViewById(R.id.record_notifyLightView);
        ObjectAnimator oa = ObjectAnimator.ofFloat(notifyLightView, "alpha", 1, 0);
        oa.setDuration(2000);
        oa.setInterpolator(new AccelerateDecelerateInterpolator());
        oa.setRepeatMode(ValueAnimator.REVERSE);
        oa.setRepeatCount(INFINITE);

        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
        } else {
            isCameraOpen = camera.openCamera();
            bestWidth = camera.getBestSize(MyCamera.BACK_CAMERA)[0];
            bestHeight = camera.getBestSize(MyCamera.BACK_CAMERA)[1];
        }
        videoEncode = new VideoEncode(recordWith, recordHeight, path);
        dataBuffer = ByteBuffer.allocate(bestHeight * bestWidth * 3 / 2).order(ByteOrder.nativeOrder());

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture st, int width, int height) {
                if (isCameraOpen) {
                    // TODO: 2019/5/21 surface需要release

                    imageReader = ImageReader.newInstance(recordWith, recordHeight, ImageFormat.YUV_420_888, 1);
                    surfaceTexture = st;
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
                if (!isRecording) {
                    isRecording = true;
                    encodeThread.start();
                    imageReader.setOnImageAvailableListener(reader -> {
                        long now = System.currentTimeMillis();
                        Image img = reader.acquireNextImage();
                        Log.e(TAG, "onImageAvailable: imageReader接受图片");
                        dataEnqueue(img);
                        img.close();
                        Log.e(TAG, "onImageAvailable: 处理图片共耗时" + (System.currentTimeMillis() - now));
                    }, backgroundHandler);
                    surfaceTexture.setDefaultBufferSize(bestWidth, bestHeight);
                    camera.openRecord(getWindowManager().getDefaultDisplay().getRotation(), new Surface(surfaceTexture), imageReader.getSurface());
                    oa.start();
                } else {
                    Log.e(TAG, "onCreate: stop");
                    camera.closeRecord(surface);
                    isRecording = false;
                    oa.cancel();
                }
            }
        });
        encodeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (; ; ) {
                    Log.e(TAG, "run: 是否完成" + !isRecording + blockingQueue.isEmpty());
                    if (!isRecording && blockingQueue.isEmpty()) {
                        Log.e(TAG, "run: 编码完成");
                        break;
                    }
                    Log.e(TAG, "run: 开始编码");
                    dataDequeue(dataBuffer);
                    Log.e(TAG, "run: 出队数据" + dataBuffer.get(11890));
//                    dataBuffer.position(0);
                    if (!isRecording && blockingQueue.isEmpty()) {
                        videoEncode.encode(dataBuffer, true);
                    } else {
                        videoEncode.encode(dataBuffer, false);
                    }
                }
            }

        });
    }

    private void dataEnqueue(Image img) {
        long now = System.currentTimeMillis();
        int w = img.getWidth();
        int h = img.getHeight();
        Log.e(TAG, "dataEnqueue: 图片宽高" + w + h);
        byte[] yBuffer = new byte[w * h];
        byte[] uvBuffer = new byte[w * h / 2];
        byte[] dataBuffer = new byte[w * h * 3 / 2];
        img.getPlanes()[0].getBuffer().get(yBuffer);
        img.getPlanes()[1].getBuffer().get(uvBuffer, 0, w * h / 2 - 1);
        uvBuffer[w * h / 2 - 1] = img.getPlanes()[2].getBuffer().get(w * h / 2 - 2);
        System.arraycopy(yBuffer, 0, dataBuffer, 0, yBuffer.length);
        System.arraycopy(uvBuffer, 0, dataBuffer, yBuffer.length, uvBuffer.length);
        try {
            blockingQueue.put(dataBuffer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "dataEnqueue: 耗时" + (System.currentTimeMillis() - now));
    }

    private void dataDequeue(ByteBuffer dataBuffer) {
        if (dataBuffer.position() != 0) {
            dataBuffer.position(0);
        }
        try {
            byte[] data = blockingQueue.take();
            Log.e(TAG, "dataDequeue: dataSize" + data.length);
            Log.e(TAG, "dataDequeue: bufferSize" + dataBuffer.limit());
            dataBuffer.put(data);
            Log.e(TAG, "dataDequeue: 出队");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    isCameraOpen = camera.openCamera();
                    bestWidth = camera.getBestSize(MyCamera.BACK_CAMERA)[0];
                    bestHeight = camera.getBestSize(MyCamera.BACK_CAMERA)[1];
                }
                break;
            default:
                break;
        }
    }
}
