package com.example.myplayer.newencode;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.Arrays;

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
public class CameraUtil {
    private static final String TAG = "CameraUtil";
    private Context context;
    private String path;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private int recordWidth, recordHeight, displayWidth, displayHeight;
    private CameraManager cameraManager;
    public static final int FRONT_CAMERA = 0;
    public static final int BACK_CAMERA = 1;
    private int backCameraId, frontCameraId;
    private CameraDevice cameraDevice;
    private CameraCharacteristics backCameraCharacteristics, frontCameraCharacteristics;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewCaptureBuilder, recordCaptureBuilder;
    private String[] cameraIds;
    private Size bestFrontSize, bestBackSize;
    private static final SparseIntArray ORIENTATION = new SparseIntArray();
    private ImageReader imageReader;
    private EncodeVideo encodeVideo;
    private ImageReader.OnImageAvailableListener imageAvailableListener = reader -> {
        Image image = reader.acquireLatestImage();
//        Log.e(TAG, ": 接受到了图片");
        encodeData(image);
        image.close();
    };

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    public CameraUtil(Context context, int displayWidth, int displayHeight) {
        this.context = context;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        encodeVideo = new EncodeVideo(1280, 640);
        backCameraId = frontCameraId = -1;
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new android.os.Handler(backgroundThread.getLooper());
        imageReader = ImageReader.newInstance(1280, 640, ImageFormat.YUV_420_888, 1);
        imageReader.setOnImageAvailableListener(imageAvailableListener, backgroundHandler);
        initCamera();
    }

    public boolean initCamera() {
        if (displayWidth == 0 || displayHeight == 0) {
            Log.e(TAG, "openCamera: 获取的宽度和高度不正确");
            return false;
        }
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            Log.e(TAG, "openCamera: 我发获取camera服务");
            return false;
        }
        getCameraInfo();

        if (backCameraId == -1 && frontCameraId == -1) {
            Log.e(TAG, "openCamera: 未在该设备上找到相机，请检查");
            return false;
        }
        return true;
    }

    private void getCameraInfo() {
        try {
            cameraIds = cameraManager.getCameraIdList();
            if (cameraIds.length == 0) {
                Log.e(TAG, "getCameraInfo: 未找到相机");
                return;
            }
            for (int i = 0; i < cameraIds.length; i++) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIds[i]);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT) {
                    Log.e(TAG, "getCameraInfo: 获取到前置摄像机");
                    frontCameraCharacteristics = cameraCharacteristics;
                    checkSupportLevel("前置相机", cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL));
                    frontCameraId = i;
                } else if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK) {
                    Log.e(TAG, "getCameraInfo: 获取到后置摄像机");
                    backCameraCharacteristics = cameraCharacteristics;
                    checkSupportLevel("后置相机", cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL));
                    backCameraId = i;
                }
            }

            if (frontCameraId != -1) {
                StreamConfigurationMap configurationMap = frontCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] bestPreviewSizes = configurationMap.getOutputSizes(ImageFormat.YUV_420_888);
                Log.e(TAG, "getCameraInfo: frontCamera" + Arrays.toString(bestPreviewSizes));
//                chooseBestSize(FRONT_CAMERA, bestPreviewSizes);
            }
            if (backCameraId != -1) {
                StreamConfigurationMap configurationMap = backCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] bestPreviewSizes = configurationMap.getOutputSizes(ImageFormat.YUV_420_888);
                Log.e(TAG, "getCameraInfo: backCamera" + Arrays.toString(bestPreviewSizes));
                chooseBestSize(BACK_CAMERA, bestPreviewSizes);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void openCamera(int cameraType, Surface surface) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "openCamera: 请打开相机权限");
            return;
        }
        int cameraId = cameraType == BACK_CAMERA ? backCameraId : frontCameraId;
        try {
            cameraManager.openCamera(cameraIds[cameraId], new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    Log.e(TAG, "onOpened: 相机打开成功");
                    openPreview(surface);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.e(TAG, "onDisconnected: 相机失去连接");
                    releaseCamera();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.e(TAG, "onDisconnected: 相机打开失败");
                    releaseCamera();
                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void openPreview(Surface surface) {
        try {
            previewCaptureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewCaptureBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    Toast.makeText(context, "开始录制", Toast.LENGTH_SHORT).show();
                    previewCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    CaptureRequest captureRequest = previewCaptureBuilder.build();
                    try {
                        captureSession.setRepeatingRequest(captureRequest, null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord(Surface surfaces) {
        if (captureSession != null) {
            captureSession.close();
        }
        openPreview(surfaces);
        encodeVideo.stop();
    }

    public void startRecord(int orientation, Surface surface, MutexUtil mutexUtil) {
        encodeVideo.start(mutexUtil);
        if (captureSession != null) {
            captureSession.close();
        }
        try {
            recordCaptureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            recordCaptureBuilder.addTarget(surface);
            recordCaptureBuilder.addTarget(imageReader.getSurface());
            recordCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(orientation));
            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    recordCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    try {
                        captureSession.setRepeatingRequest(recordCaptureBuilder.build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void checkSupportLevel(String camera, int supportLevel) {
        switch (supportLevel) {
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                Toast.makeText(context, camera + "支持级别为:不支持", Toast.LENGTH_LONG).show();
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                Toast.makeText(context, camera + "支持级别为:简单支持", Toast.LENGTH_LONG).show();
                break;
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL:
                Toast.makeText(context, camera + "支持级别为:部分支持", Toast.LENGTH_LONG).show();
                break;
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                Toast.makeText(context, camera + "设备还支持传感器，闪光灯，镜头和后处理设置的每帧手动控制，以及高速率的图像捕获",
                        Toast.LENGTH_LONG).show();
                break;
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                Toast.makeText(context, camera + "设备还支持YUV重新处理和RAW图像捕获，以及其他输出流配置", Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(context, camera + "未检测到相机信息", Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void chooseBestSize(int cameraType, Size[] bestPreviewSizes) {
        float diff = Float.MAX_VALUE;
        int bestWidth = 0, bestHeight = 0;

        float bestRatio = (float) displayHeight / (float) displayWidth;

        for (int j = 0; j < bestPreviewSizes.length - 1; j++) {

            float newDiff = Math.abs((float) bestPreviewSizes[j].getWidth() / (float) bestPreviewSizes[j].getHeight() - bestRatio);
            if (newDiff == 0) {
                bestWidth = bestPreviewSizes[j].getWidth();
                bestHeight = bestPreviewSizes[j].getHeight();
                break;
            }

            if (newDiff < diff) {
                bestWidth = bestPreviewSizes[j].getWidth();
                bestHeight = bestPreviewSizes[j].getHeight();
                diff = newDiff;
            }
        }
        if (bestWidth == 0) {
            Log.e(TAG, "chooseBestSize: 最佳分辨率为0");
            return;
        }
        Log.e(TAG, "chooseBestSize: bestPreviewSize" + bestWidth + "\n" + bestHeight);
        if (cameraType == FRONT_CAMERA) {
            bestFrontSize = new Size(bestWidth, bestHeight);

        } else {
            bestBackSize = new Size(bestWidth, bestHeight);
        }
    }

    public void releaseCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
        }
    }

    public Size getBestSize(int cameraType) {
        if (cameraType == FRONT_CAMERA) {
            return bestFrontSize;
        }
        if (cameraType == BACK_CAMERA) {
            return bestBackSize;
        }
        Log.e(TAG, "getBestSize: 参数不合法");
        return null;
    }

    public void encodeData(Image img) {
        long now = System.currentTimeMillis();
        int w = img.getWidth();
        int h = img.getHeight();
        byte[] yBuffer = new byte[w * h];
        byte[] uvBuffer = new byte[w * h / 2];
        byte[] dataBuffer = new byte[w * h * 3 / 2];
        img.getPlanes()[0].getBuffer().get(yBuffer);
        img.getPlanes()[1].getBuffer().get(uvBuffer, 0, w * h / 2 - 1);
        uvBuffer[w * h / 2 - 1] = img.getPlanes()[2].getBuffer().get(w * h / 2 - 2);
        System.arraycopy(yBuffer, 0, dataBuffer, 0, yBuffer.length);
        System.arraycopy(uvBuffer, 0, dataBuffer, yBuffer.length, uvBuffer.length);
        encodeVideo.pushData(dataBuffer);
    }
}
