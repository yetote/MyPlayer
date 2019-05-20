package com.example.myplayer.encode;

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
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;

import java.util.Arrays;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

/**
 * @author ether QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer.encode
 * @class describe
 * @time 2019/5/20 15:50
 * @change
 * @chang time
 * @class describe
 */
public class MyCamera {
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private int width, height;
    private int frontBestWidth, frontBestHeight;
    private int backBestWidth, backBestHeight;
    private CameraManager cameraManager;
    public static final int FRONT_CAMERA = 0;
    public static final int BACK_CAMERA = 1;

    private int backCameraId, frontCameraId;
    private CameraDevice cameraDevice;
    private CameraCharacteristics backCameraCharacteristics, frontCameraCharacteristics;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder previewCaptureBuilder, recordCaptureBuilder;
    private Context context;
    private static final String TAG = "MyCamera";
    private String[] cameraIds;


    public MyCamera(@NonNull Context context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;
        backCameraId = frontCameraId = -1;
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new android.os.Handler(backgroundThread.getLooper());
    }

    public int openCamera() {
        if (width == 0 || height == 0) {
            Log.e(TAG, "openCamera: 获取的宽度和高度不正确");
            return -1;
        }
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (cameraManager == null) {
            Log.e(TAG, "openCamera: 我发获取camera服务");
            return -1;
        }
        getCameraInfo();

        if (backCameraId == -1 && frontCameraId == -1) {
            Log.e(TAG, "openCamera: 未在该设备上找到相机，请检查");
            return -1;
        }
        int openCameraId = -1;
        if (backCameraId != -1) {
            openCameraId = backCameraId;
        } else if (frontCameraId != -1) {
            openCameraId = frontCameraId;
        } else {
            Log.e(TAG, "openCamera: 未找到相机");
            return -1;
        }
        if (checkSelfPermission(context, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {
            Toast.makeText(context, "请开启相机权限", Toast.LENGTH_SHORT).show();
            return -1;
        }
        try {
            cameraManager.openCamera(cameraIds[backCameraId], new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    Log.e(TAG, "onOpened: 相机打开成功");

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
        return 0;
    }

    public void openPreview(Surface... surfaces) {
        try {
            previewCaptureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            for (int i = 0; i < surfaces.length; i++) {
                previewCaptureBuilder.addTarget(surfaces[i]);
            }
            cameraDevice.
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
                chooseBestSize(FRONT_CAMERA, bestPreviewSizes);
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

    private void chooseBestSize(int cameraType, Size[] bestPreviewSizes) {
        int diff = Integer.MAX_VALUE;
        int bestWidth = 0, bestHeight = 0;
        for (int j = 0; j < bestPreviewSizes.length - 1; j++) {

            int newDiff = Math.abs(width - bestPreviewSizes[j].getHeight()) + Math.abs(height - bestPreviewSizes[j].getWidth());
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

        if (cameraType == FRONT_CAMERA) {
            frontBestWidth = bestWidth;
            frontBestHeight = bestHeight;
        } else {
            backBestWidth = bestWidth;
            backBestHeight = bestHeight;
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

    public void releaseCamera() {
        if (cameraDevice != null) {
            cameraDevice.close();
        }
    }

    public int[] getBestSize(int cameraType) {
        if (cameraType == FRONT_CAMERA) {
            return new int[]{frontBestWidth, frontBestHeight};
        }
        if (cameraType == BACK_CAMERA) {
            return new int[]{backBestWidth, backBestHeight};
        }
        Log.e(TAG, "getBestSize: 参数不合法");
        return null;
    }
}
