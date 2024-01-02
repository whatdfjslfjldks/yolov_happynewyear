package com.example;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.analysis.FullImageAnalyse;
import com.example.analysis.FullScreenAnalyse;
import com.example.detector.Yolov5TFLiteDetector;
import com.example.helper.FileHelper;
import com.example.holder.TarExtractor;
import com.example.userSelected.InputSize;
import com.example.userSelected.ModelAndLabel;
import com.example.userSelected.OutputSize;
import com.example.utils.CameraProcess;
import com.example.yolov5tfliteandroid.JNITools;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import demo.ai_usage.R;


public class MainActivity extends AppCompatActivity {

//    private final Size INPNUT_SIZE = new Size(320, 320);
//    private final int[] OUTPUT_SIZE = new int[]{1, 6300, 85};

    public static boolean selectedModel=false;
    private static boolean IS_FULL_SCREEN = false;

//    public static boolean tracked=false;

    private static PreviewView cameraPreviewMatch;
    private static PreviewView cameraPreviewWrap;
    private static ImageView boxLabelCanvas;

    private Switch immersive;
    private Switch border;
    private Switch track;
    private static TextView inferenceTimeTextView;
    private static TextView frameSizeTextView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private static Yolov5TFLiteDetector yolov5TFLiteDetector;
    private TextView file_btn;
    private static final int PICK_FILE_REQUEST_CODE = 1; // 请求代码，用于标识文件选择器的返回结果
    public static String modelName;

    private static CameraProcess cameraProcess = new CameraProcess();
    private static Size UserSelected_INPUT_SIZE;
    private static int[] UserSelected_OUTPUT_SIZE;
    private static int rotation;
//    public static int color;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private Random random = new Random();
//    public static Context appContext; // 添加一个静态的 Context 对象
//    public static boolean checkBorder=false;

//    private int generateRandomColor() {
//        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
//    }

    public static void updateCameraPreview(Context context){
        if (IS_FULL_SCREEN) {
            cameraPreviewWrap.removeAllViews();
            FullScreenAnalyse fullScreenAnalyse = new FullScreenAnalyse(context,
                    cameraPreviewMatch,
                    boxLabelCanvas,
                    rotation,
                    inferenceTimeTextView,
                    frameSizeTextView,
                    UserSelected_INPUT_SIZE,
                    UserSelected_OUTPUT_SIZE,
                    yolov5TFLiteDetector);
            cameraProcess.startCamera(context, fullScreenAnalyse, cameraPreviewMatch);
        } else {
            cameraPreviewMatch.removeAllViews();
            FullImageAnalyse fullImageAnalyse = new FullImageAnalyse(
                    context,
                    cameraPreviewWrap,
                    boxLabelCanvas,
                    rotation,
                    inferenceTimeTextView,
                    frameSizeTextView,
                    UserSelected_INPUT_SIZE,
                    UserSelected_OUTPUT_SIZE,
                    yolov5TFLiteDetector);
            cameraProcess.startCamera(context, fullImageAnalyse, cameraPreviewWrap);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("resume","restart");
    }


    /**
     * 获取屏幕旋转角度,0表示拍照出来的图片是横屏
     *
     */
    protected int getScreenOrientation() {
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 90;
            default:
                return 0;
        }
    }

    /**
     * 加载模型
     *
     * @param modelName
     */
    private void initModel(String modelName,Size inputsize,int[] outputsize) {
        // 加载模型
        try {
            this.yolov5TFLiteDetector = new Yolov5TFLiteDetector();
            this.yolov5TFLiteDetector.setModelFile(modelName);
//            this.yolov5TFLiteDetector.addNNApiDelegate();
            this.yolov5TFLiteDetector.addGPUDelegate();
            this.yolov5TFLiteDetector.initialModel(this,inputsize,outputsize);
            Log.i("model", "Success loading model" + this.yolov5TFLiteDetector.getModelFile());
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,"Invalid file format selected! Please choose again.",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 将当前 Activity 的上下文赋给静态对象
//        appContext = getApplicationContext();
//        track=findViewById(R.id.track_switch);


        // 打开app的时候隐藏顶部状态栏
//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // 全屏画面
        cameraPreviewMatch = findViewById(R.id.camera_preview_match);
        cameraPreviewMatch.setScaleType(PreviewView.ScaleType.FILL_START);

        // 全图画面
        cameraPreviewWrap = findViewById(R.id.camera_preview_wrap);
//        cameraPreviewWrap.setScaleType(PreviewView.ScaleType.FILL_START);

        // box/label画面
        boxLabelCanvas = findViewById(R.id.box_label_canvas);

//        // 下拉按钮
//        modelSpinner = findViewById(R.id.model);

        // 沉浸式体验按钮
        immersive = findViewById(R.id.immersive);

        //控制边框按钮
//        border=findViewById(R.id.switch_border);

        // 实时更新的一些view
        inferenceTimeTextView = findViewById(R.id.inference_time);
        frameSizeTextView = findViewById(R.id.frame_size);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // 申请摄像头权限
        if (!cameraProcess.allPermissionsGranted(this)) {
            cameraProcess.requestPermissions(this);
        }

        // 获取手机摄像头拍照旋转参数
        rotation = getWindowManager().getDefaultDisplay().getRotation();
        Log.i("image", "rotation: " + rotation);

        cameraProcess.showCameraSupportSize(MainActivity.this);

        // 初始化加载,目前没有初始化加载模型
//        initModel("yolov5s","coco_label.txt",INPNUT_SIZE,OUTPUT_SIZE);//位置？
        //打开用户本地文件按钮
        file_btn=findViewById(R.id.file_btn);
        
        file_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    // 如果没有权限，请求权限
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                } else {
                    // 已经有权限，执行文件访问的操作
                    // 在这里调用显示文件选择器的方法
                    showFileChooser();
                }

            }
        });

//        border.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                    //打开边框，不同label不同显示，并在上面显示label、置信度等
//                    checkBorder=true;
//                }else{
//                    //仅显示边框，其他不显示
//                    checkBorder=false;
//                }
//            }
//        });

//        track.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if(isChecked){
//                    //打开追踪
//                    tracked=true;
//                }else{
//                    tracked=false;
//                }
//            }
//        });

        // 监听视图变化按钮
        immersive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (MainActivity.selectedModel == true) {
                    Log.e("test", "b：" + b);
                    IS_FULL_SCREEN = b;
                    if (b) {
                        // 进入全屏模式
                        Log.e("test", "全屏模式");
                        cameraPreviewWrap.removeAllViews();
                        FullScreenAnalyse fullScreenAnalyse = new FullScreenAnalyse(MainActivity.this,
                                cameraPreviewMatch,
                                boxLabelCanvas,
                                rotation,
                                inferenceTimeTextView,
                                frameSizeTextView,
                                UserSelected_INPUT_SIZE,
                                UserSelected_OUTPUT_SIZE,
                                yolov5TFLiteDetector);
                        cameraProcess.startCamera(MainActivity.this, fullScreenAnalyse, cameraPreviewMatch);

                    } else {
                        // 进入全图模式
                        Log.e("test", "全图模式");
                        cameraPreviewMatch.removeAllViews();
                        FullImageAnalyse fullImageAnalyse = new FullImageAnalyse(
                                MainActivity.this,
                                cameraPreviewWrap,
                                boxLabelCanvas,
                                rotation,
                                inferenceTimeTextView,
                                frameSizeTextView,
                                UserSelected_INPUT_SIZE,
                                UserSelected_OUTPUT_SIZE,
                                yolov5TFLiteDetector);
                        cameraProcess.startCamera(MainActivity.this, fullImageAnalyse, cameraPreviewWrap);
                    }
                }
                else {
                    Toast.makeText(MainActivity.this,"请先选择一个模型",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // 允许选择所有类型的文件

        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "选择一个文件"),
                    PICK_FILE_REQUEST_CODE);
            Log.e("test", "选择到一个文件");
        } catch (android.content.ActivityNotFoundException ex) {
            // 如果用户没有安装文件管理器应用，可以添加适当的处理
        }

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri selectedFileUri = data.getData();

            if (selectedFileUri != null) {
                String mimeType = getContentResolver().getType(selectedFileUri);
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedFileUri.toString());
                Log.d("MIME Type", mimeType);
                Log.d("File Extension", fileExtension);

//                if (mimeType != null && (mimeType.equals("application/x-gzip") || mimeType.equals("application/x-tar"))) {
                if(mimeType != null && (mimeType.contains("gzip") || fileExtension.equals("tar")||fileExtension.equals("gz"))){
                    // 正确类型的文件被选择了，进行下一步操作
                    // 可以在这里获取文件路径等操作
                    String fileName = getFileNameWithoutTarExtension(selectedFileUri);
                    Log.d("File Name", fileName);
                    modelName=fileName;

                    byte[] fileBytes = FileHelper.getFileBytesFromUri(this, selectedFileUri);

                    // 处理文件内容字节数组

// 处理文件内容字节数组
                    if (fileBytes != null) {
                        // 固定文件名为 "YourFileName.extension"
                        String filePath = saveBytesToFile(this, fileBytes, "MyFileName.extension");
                        if (filePath != null) {
                            Log.d("FilePath", filePath);

                            // 提取Tar文件的操作放在后台线程中执行
                            ExtractTask extractTask = new ExtractTask();
                            extractTask.execute(filePath);

                        }
                    }
                } else {
                    // 提示用户选择正确的文件类型
                    Log.e("TNTcuowu", "请选择正确的文件类型（.tar.gz）");
                    Toast.makeText(this, "请选择正确的文件类型（.tar.gz）", Toast.LENGTH_SHORT).show();
                }
            }

        }

    }

    // 获取文件名的方法，并去除扩展名
// 获取文件名的方法，并去除.tar.gz扩展名
// 获取文件名的方法，并去除.tar.gz或.tar扩展名
    private String getFileNameWithoutTarExtension(Uri uri) {
        String fileName = getFileName(uri);

        if (fileName != null) {
            // 检查文件名是否以 .tar.gz 结尾
            if (fileName.endsWith(".tar.gz")) {
                // 截取掉 .tar.gz
                fileName = fileName.substring(0, fileName.length() - ".tar.gz".length());
            } else if (fileName.endsWith(".tar")) {
                // 检查文件名是否以 .tar 结尾
                // 截取掉 .tar
                fileName = fileName.substring(0, fileName.length() - ".tar".length());
            }
        }

        return fileName;
    }



    private String getFileName(Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();

        if (scheme != null && scheme.equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }

        return fileName;
    }

    public static String saveBytesToFile(Context context, byte[] fileBytes, String fileName) {
        if (fileBytes == null || fileName == null) {
            return null;
        }

        try {
            File dir = new File(context.getExternalFilesDir(null) + File.separator + "YourDirectoryName");
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(dir, fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(fileBytes);
            outputStream.close();

            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    private class ExtractTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... filePaths) {
            // 在后台进行解压操作
            if (filePaths != null && filePaths.length > 0) {
                String filePath = filePaths[0];
                Log.e("TNT_path0", "filePath:" + filePaths[0]);

                JNITools test = new JNITools();
                byte[] input = test.extractTarGz(filePath);

//                // 假设有一个方法从 byte 流中提取 .json 文件的内容为 jsonBuffer，.tflite 文件内容为 tfliteBuffer，.txt 文件内容为 txtBuffer
//
//                byte[] jsonBuffer = extractJSON(input); // 假设 extractJSON 是提取 .json 文件的方法
//                byte[] tfliteBuffer = extractTFLite(input); // 假设 extractTFLite 是提取 .tflite 文件的方法
//                byte[] txtBuffer = extractTXT(input); // 假设 extractTXT 是提取 .txt 文件的方法
//
//// 现在你有了单独的缓冲区，可以在程序中使用它们

//                writeFile("test",input);

                File outputDirectory = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    outputDirectory = getApplicationContext().getDataDir();
                }

                TarExtractor tarExtractor = new TarExtractor();
//                tarExtractor.getExtension();

//                Log.e("NAMEINMAIN222",tarExtractor.getExtension());

                try {
                    tarExtractor.extractTar(input, outputDirectory);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//            writeFile("test",input);
            }
            return null;

        }

        @Override
        protected void onPostExecute(Void result) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                File modelDirectory = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    modelDirectory = getApplicationContext().getDataDir();
                } else {
                    modelDirectory = new File(getApplicationContext().getExternalFilesDir(null) + "/yolo");
                }

                Log.e("TNT", "modelDirectory:" + modelDirectory);


                ByteBuffer jsonBuffer = Yolov5TFLiteDetector.jsonBuffer;
                ByteBuffer modelBuffer = Yolov5TFLiteDetector.modelBuffer;
                ByteBuffer txtBuffer = Yolov5TFLiteDetector.txtBuffer;

                // 创建临时文件
                File txtTempFile = null;
                try {
                    txtTempFile = File.createTempFile("temp", null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try (FileChannel outChannel = new FileOutputStream(txtTempFile).getChannel()) {
                    // 将ByteBuffer的数据写入临时文件
                    outChannel.write(txtBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String labelFilePath = txtTempFile.getAbsolutePath();
                ModelAndLabel.getInstance().setLabel(labelFilePath);


                // 创建临时文件
                File modelTempFile = null;
                try {
                    modelTempFile = File.createTempFile("modelTemp", null);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                try (FileChannel outChannel = new FileOutputStream(modelTempFile).getChannel()) {
                    // 将ByteBuffer的数据写入临时文件
                    outChannel.write(modelBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String modelFilePath = modelTempFile.getAbsolutePath();
                ModelAndLabel.getInstance().setModel(modelFilePath);





//                modelBuffer.flip();
//                CharBuffer modelCharBuffer = StandardCharsets.UTF_8.decode(modelBuffer);
//
//                txtBuffer.flip();
//                CharBuffer txtCharBuffer = StandardCharsets.UTF_8.decode(txtBuffer);

//                String modelString=modelCharBuffer.toString();
//                String txtString=txtCharBuffer.toString();

//                Log.e("yuandan",modelString);
//                Log.e("yuandan",txtString);
//路径
//                ModelAndLabel.getInstance().setModel(modelString);
//                ModelAndLabel.getInstance().setLabel(txtString);

                // 将 ByteBuffer 转换为 CharBuffer
                jsonBuffer.flip(); // 切换为读模式
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(jsonBuffer);

                // 将 CharBuffer 转换为字符串

                String json = charBuffer.toString();
                Log.e("test", json);
                try {
                    Log.e("test", "开始解析json");
                    JSONObject jsonObject = new JSONObject(json);

                    // 只获取 output_size 的数字
                    JSONArray outputSizeArray = jsonObject.getJSONArray("output_size");
                    StringBuilder outputSizeStringBuilder = new StringBuilder();
                    for (int i = 0; i < outputSizeArray.length(); i++) {
                        outputSizeStringBuilder.append(outputSizeArray.getInt(i));
                        if (i < outputSizeArray.length()) {
                            Log.e("test", String.valueOf(outputSizeArray.getInt(i)));
                            if (i == 0) {
                                OutputSize.getInstance().setOutput_Param1(outputSizeArray.getInt(i));
                            } else if (i == 1) {
                                OutputSize.getInstance().setOutput_Param2(outputSizeArray.getInt(i));
                            } else if (i == 2) {
                                OutputSize.getInstance().setOutput_Param3(outputSizeArray.getInt(i));
                            }
                        }
                    }
                    // 只获取 input_size 的数字
                    JSONArray inputSizeArray = jsonObject.getJSONArray("input_size");
                    StringBuilder inputSizeStringBuilder = new StringBuilder();
                    for (int i = 0; i < inputSizeArray.length(); i++) {
                        inputSizeStringBuilder.append(inputSizeArray.getInt(i));
                        if (i < inputSizeArray.length()) {
                            Log.e("test", String.valueOf(inputSizeArray.getInt(i)));
                            // 根据索引 i 设置对应的输入参数值
                            if (i == 0) {
                                InputSize.getInstance().setInput_Param1(inputSizeArray.getInt(i));
                            } else if (i == 1) {
                                InputSize.getInstance().setInput_Param2(inputSizeArray.getInt(i));
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

//                Log.e("test", ModelAndLabel.getInstance().getLabel());

                rotation = getWindowManager().getDefaultDisplay().getRotation();

                UserSelected_INPUT_SIZE = new Size(InputSize.getInstance().getInput_Param1(), InputSize.getInstance().getInput_Param2());
                UserSelected_OUTPUT_SIZE = new int[]{OutputSize.getInstance().getOutput_Param1(), OutputSize.getInstance().getOutput_Param2(), OutputSize.getInstance().getOutput_Param3()};
//                color = generateRandomColor();
//                detectLabel=true;
                initModel("UserSelected",UserSelected_INPUT_SIZE, UserSelected_OUTPUT_SIZE);

//                MainActivity.selectedModel=true;
                MainActivity.updateCameraPreview(MainActivity.this);

            }

            if(MainActivity.modelName!=null) {
                file_btn.setText(MainActivity.modelName);
                MainActivity.selectedModel=true;
            }else{
                Toast.makeText(MainActivity.this,"加载失败，请重新选择模型！",Toast.LENGTH_SHORT).show();
            }
        }

    }



    public void writeFile(String fileName, byte[] content) {
        Context context = getApplicationContext();
        try (FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)) {
            fos.write(content);
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}