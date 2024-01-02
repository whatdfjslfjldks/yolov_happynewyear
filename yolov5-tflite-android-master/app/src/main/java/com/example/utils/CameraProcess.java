package com.example.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Handler;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

public class CameraProcess {

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};

    /**
     * 判断摄像头权限
     * @param context
     * @return
     */
    public boolean allPermissionsGranted(Context context) {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请摄像头权限
     * @param activity
     */
    public void requestPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    /**
     * 打开摄像头，提供对应的preview, 并且注册analyse事件, analyse就是要对摄像头每一帧进行分析的操作
     */
    //TODO 修改前：
    public void startCamera(Context context, ImageAnalysis.Analyzer analyzer, PreviewView previewView) {

        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                            .setTargetResolution(new Size(1080, 1920))
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                            .setTargetAspectRatioCustom(new Rational(16,9))
//                            .setTargetRotation(Surface.ROTATION_90)
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();
                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer);
                  //TODO
                    Preview previewBuilder = new Preview.Builder()
//                            .setTargetResolution(new Size(1080,1440))
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                            .setTargetRotation(Surface.ROTATION_90)
                            .build();
//                    Log.i("builder", previewView.getHeight()+"/"+previewView.getWidth());
                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                    previewBuilder.setSurfaceProvider(previewView.createSurfaceProvider());
                    // 加多这一步是为了切换不同视图的时候能释放上一视图所有绑定事件
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, imageAnalysis, previewBuilder);

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(context));
    }

    //TODO 修改后：
//    public void startCamera(Context context, ImageAnalysis.Analyzer analyzer, PreviewView previewView) {
//        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
//        cameraProviderFuture.addListener(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                            .build();
//                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), new ImageAnalysis.Analyzer() {
//                        @Override
//                        public void analyze(@NonNull ImageProxy image) {
//                            // 获取相机捕获的图像数据
//                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                            int[] imageArray = // 从 buffer 中获取图像数据，转为 int 数组
//
//                                    // 调用 JNI 接口，传递图像数据给 C++ 处理
//                                    ImageProcessor.processImage(imageArray, image.getWidth(), image.getHeight());
//
//                            // 在 TextureView 或 SurfaceView 中显示处理后的图像
//                            Bitmap bitmap = // 将处理后的图像数据转为 Bitmap
//                                    runOnUiThread(() -> showImage(bitmap));
//
//                            // 图像处理完成后需要关闭 ImageProxy
//                            image.close();
//                        }
//                    });
//
//                    Preview previewBuilder = new Preview.Builder()
//                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                            .build();
//
//                    CameraSelector cameraSelector = new CameraSelector.Builder()
//                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                            .build();
//
//                    previewBuilder.setSurfaceProvider(previewView.createSurfaceProvider());
//
//                    // 加多这一步是为了切换不同视图的时候能释放上一视图所有绑定事件
//                    cameraProvider.unbindAll();
//                    cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, imageAnalysis, previewBuilder);
//
//                } catch (ExecutionException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, ContextCompat.getMainExecutor(context));
//    }

//    public void startCamera(Context context, ImageAnalysis.Analyzer analyzer, PreviewView previewView) {
//        cameraProviderFuture = ProcessCameraProvider.getInstance(context);
//        cameraProviderFuture.addListener(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                            .build();
//                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context), new ImageAnalysis.Analyzer() {
//                        @Override
//                        public void analyze(@NonNull ImageProxy image) {
//                            // 1. 获取相机捕获的图像数据
//                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//
//                            // 2. 将图像数据转为 int 数组（示例，实际需要根据图像格式进行解析）
//                            int[] imageArray = convertBufferToIntArray(buffer, image.getWidth(), image.getHeight());
//
//                            // 3. 调用 JNI 接口，传递图像数据给 C++ 处理
//                            ImageProcessor.processImage(imageArray, image.getWidth(), image.getHeight());
//
//                            // 4. 将处理后的图像数据转为 Bitmap
//                            Bitmap bitmap = convertIntArrayToBitmap(imageArray, image.getWidth(), image.getHeight());
//
//                            // 5. 在 TextureView 或 SurfaceView 中显示处理后的图像
//                            runOnUiThread(() -> showImage(bitmap));
//
//                            // 6. 图像处理完成后需要关闭 ImageProxy，释放资源
//                            image.close();
//                        }
//                    });
//
//                    Preview previewBuilder = new Preview.Builder()
//                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
//                            .build();
//
//                    CameraSelector cameraSelector = new CameraSelector.Builder()
//                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
//                            .build();
//
//                    previewBuilder.setSurfaceProvider(previewView.createSurfaceProvider());
//
//                    // 加多这一步是为了切换不同视图的时候能释放上一视图所有绑定事件
//                    cameraProvider.unbindAll();
//                    cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, imageAnalysis, previewBuilder);
//
//                } catch (ExecutionException | InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, ContextCompat.getMainExecutor(context));
//    }
//
//    private Thread mUiThread;
//    final Handler mHandler = new Handler();
//    public final void runOnUiThread(Runnable action) {
//        if (Thread.currentThread() != mUiThread) {
//            mHandler.post(action);
//        } else {
//            action.run();
//        }
//    }
//
//    // 辅助方法：从 ByteBuffer 转换为 int 数组
//    private int[] convertBufferToIntArray(ByteBuffer buffer, int width, int height) {
//        int[] pixels = new int[width * height];
//
//        // 示例：将 buffer 转为 int 数组的过程，实际需要根据图像格式进行解析
//        // 这里假设 buffer 中的数据是 ARGB 格式的图像数据
//        buffer.rewind();
//        for (int i = 0; i < pixels.length; ++i) {
//            int a = buffer.get() & 0xff;
//            int r = buffer.get() & 0xff;
//            int g = buffer.get() & 0xff;
//            int b = buffer.get() & 0xff;
//            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
//        }
//
//        return pixels;
//    }
//
//    // 辅助方法：将 int 数组转换为 Bitmap
//    private Bitmap convertIntArrayToBitmap(int[] pixels, int width, int height) {
//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
//        return bitmap;
//    }
//
//    // 显示 Bitmap 的方法
//    private void showImage(Bitmap bitmap) {
//        // 在 UI 界面中显示 Bitmap
//        // 例如：在 ImageView 中显示处理后的图像
//        imageView.setImageBitmap(bitmap);
//    }
    /**
     * 打印输出摄像头支持的宽和高
     * @param activity
     */
    public void showCameraSupportSize(Activity activity) {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics cc = manager.getCameraCharacteristics(id);
                if (cc.get(CameraCharacteristics.LENS_FACING) == 1) {
                    Size[] previewSizes = cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                            .getOutputSizes(SurfaceTexture.class);
                    for (Size s : previewSizes){
                        Log.i("camera", s.getHeight()+"/"+s.getWidth());
                    }
                    break;

                }
            }
        } catch (Exception e) {
            Log.e("image", "can not open camera", e);
        }
    }

}


//TODO jni:
//public class ImageProcessor {
//
//    // 加载 C++ 库
//    static {
//        System.loadLibrary("your_cpp_library_name");
//    }
//
//    // JNI 接口，用于调用 C++ 的图像处理函数
//    private native void processImage(int[] imageArray, int width, int height);
//
//    // 示例方法，用于从相机获取图像并进行处理
//    public void processCameraFrame() {
//        // 获取相机图像数据，这部分需要根据你的相机实现
//        int[] imageArray = getCameraImageData();
//        int width = getCameraImageWidth();
//        int height = getCameraImageHeight();
//
//        // 调用 JNI 接口传递图像数据给 C++
//        processImage(imageArray, width, height);
//
//        // 在这里可以继续处理 C++ 返回的结果或进行其他操作
//    }
//
//    // 示例方法，模拟相机获取图像数据
//    private int[] getCameraImageData() {
//        // 这里需要根据实际情况获取相机图像数据
//        // 返回一个包含图像像素的数组
//        return new int[/* size */];
//    }
//
//    private int getCameraImageWidth() {
//        // 获取相机图像宽度
//        return /* width */;
//    }
//
//    private int getCameraImageHeight() {
//        // 获取相机图像高度
//        return /* height */;
//    }
//
//    // 其他方法和逻辑
//}
