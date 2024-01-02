package com.example.detector;

//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.RectF;
//import android.os.Build;
//import android.util.Log;
//import android.util.Size;
//
//import com.example.MainActivity;
//import com.example.userSelected.InputSize;
//import com.example.userSelected.ModelAndLabel;
//import com.example.userSelected.OutputSize;
//import com.example.utils.Recognition;
//import com.example.yolov5tfliteandroid.JNITools;
//
//import org.tensorflow.lite.DataType;
//import org.tensorflow.lite.Interpreter;
//import org.tensorflow.lite.gpu.CompatibilityList;
//import org.tensorflow.lite.gpu.GpuDelegate;
//import org.tensorflow.lite.nnapi.NnApiDelegate;
//import org.tensorflow.lite.support.common.FileUtil;
//import org.tensorflow.lite.support.common.TensorProcessor;
//import org.tensorflow.lite.support.common.ops.CastOp;
//import org.tensorflow.lite.support.common.ops.DequantizeOp;
//import org.tensorflow.lite.support.common.ops.NormalizeOp;
//import org.tensorflow.lite.support.common.ops.QuantizeOp;
//import org.tensorflow.lite.support.image.ImageProcessor;
//import org.tensorflow.lite.support.image.TensorImage;
//import org.tensorflow.lite.support.image.ops.ResizeOp;
//import org.tensorflow.lite.support.metadata.MetadataExtractor;
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.CharBuffer;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.PriorityQueue;
//import java.util.Random;
//
//public class Yolov5TFLiteDetector {
//
//    JNITools test = new JNITools();
//
//    class box_t{
//        public float x1, y1, x2, y2, score, label;
//        public box_t(float x1, float y1, float x2, float y2, float score, float label){
//            this.x1 = x1;
//            this.y1 = y1;
//            this.x2 = x2;
//            this.y2 = y2;
//            this.score = score;
//            this.label = label;
//        }
//    }
//
////    private final Size INPNUT_SIZE = new Size(320, 320);
////    private final int[] OUTPUT_SIZE = new int[]{1, 6300, 85};
//
//    private Boolean IS_INT8 = false;
//    //    private final float DETECT_THRESHOLD = 0.0f;
////    private final float IOU_THRESHOLD = 0.0f;
////    private final float IOU_CLASS_DUPLICATED_THRESHOLD = 0.0f;
//    private final float IOU_CLASS_DUPLICATED_THRESHOLD = 0.7f;
//    private final float IOU_THRESHOLD = 0.45f;
//    private final float DETECT_THRESHOLD = 0.25f;
//
//    private Size UserSelected_INPUT_SIZE = new Size(InputSize.getInstance().getInput_Param1(), InputSize.getInstance().getInput_Param2());
//    private int[] UserSelected_OUTPUT_SIZE = new int[]{OutputSize.getInstance().getOutput_Param1(), OutputSize.getInstance().getOutput_Param2(), OutputSize.getInstance().getOutput_Param3()};
//    //    private String UserSelected_MODEL = ModelAndLabel.getInstance().getModel();
////    private String UserSelected_LABEL_FILE = ModelAndLabel.getInstance().getLabel();
//    private String UserSelected_MODEL = ModelAndLabel.getInstance().getModel();
//    MetadataExtractor.QuantizationParams input5SINT8QuantParams = new MetadataExtractor.QuantizationParams(0.003921568859368563f, 0);
//    MetadataExtractor.QuantizationParams output5SINT8QuantParams = new MetadataExtractor.QuantizationParams(0.006305381190031767f, 5);
//
//    private Interpreter tflite;
//    private List<String> associatedAxisLabels;
//    private Size Final_inputSize;
//    private  int[] Final_outputSize;
//    Interpreter.Options options = new Interpreter.Options();
//
//    private long detectorPtr;
//    private String MODEL_FILE;
//    private float[] outputBuffer;
//
//    private ArrayList<box_t> boxes = new ArrayList<>();
//
//
//    public static ByteBuffer modelBuffer;
//
//    public static ByteBuffer jsonBuffer;
//
//    public static ByteBuffer txtBuffer;
//
////    public String getLabelFile() {
////        return this.LABEL_FILE;
////    }
//
//    public int getInputSizeWidth() {
//        return InputSize.getInstance().getInput_Param1();
//    }
//    public int getInputSizeHeight() {
//        return InputSize.getInstance().getInput_Param2();
//    }
//
//    public Yolov5TFLiteDetector() {
//    }
//    public String getModelFile() {
//        return this.MODEL_FILE;
//    }
//    public void setModelFile(String modelFile) {
//        switch (modelFile) {
//            case "UserSelected":
//                IS_INT8 = false;
//                MODEL_FILE = UserSelected_MODEL;
//                Log.e("test","加载用户模型（选择）");
//                break;
//            default:
//                Log.e("test","default");
//                Log.i("tfliteSupport", "Only yolov5s/n/m/sint8 can be load!");
//        }
//    }
//
////    public int[] getOutputSize() {
////        return this.OUTPUT_SIZE;
////    }
//
//    // 在 Yolov5TFLiteDetector 类中添加一个新的 HashMap 以存储每个 label 的颜色
//    public static HashMap<String, Integer> labelColors = new HashMap<>();
//    private int trackCounter = 0;
//    private Random random = new Random();
//
//    private int generateRandomColor() {
//        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
//    }
//
//    /**
//     * 初始化模型, 可以通过 addNNApiDelegate(), addGPUDelegate()提前加载相应代理
//     *
//     * @param activity
//     */
//    public void initialModel(Context activity,Size inputsize,int[] outputsize) {
//        // Initialise the model
//        Final_inputSize=inputsize;
//        Final_outputSize=outputsize;
//        Log.e("test-user", "用户选择模型正常运行");
//
//        tflite = new Interpreter(modelBuffer, options);
//
//        // 获取标签文件的路径
////        String labelFilePath = ModelAndLabel.getInstance().getLabel();
//
//        //TODO modelBuffer
//
//        ByteBuffer labelBuffer=Yolov5TFLiteDetector.txtBuffer;
//
//        // 将 ByteBuffer 转换为 CharBuffer
//        labelBuffer.flip(); // 切换为读模式
//        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(labelBuffer);
//
//        // 将 CharBuffer 转换为字符串
//
//        //            String json = readJsonFile(jsonFileAddress);
//        String labelContent = charBuffer.toString();
//
//
//        try {
////            File labelFileFile = new File(labelFilePath);
////            associatedAxisLabels = FileUtil.loadLabels(new FileInputStream(labelFileFile));
//
//            // 使用 ByteArrayInputStream 将字符串转换为输入流
//            InputStream inputStream = new ByteArrayInputStream(labelContent.getBytes());
//
//// 加载标签
//            associatedAxisLabels = FileUtil.loadLabels(inputStream);
//
//            tflite = new Interpreter(modelBuffer, options);
//
//            // 获取标签文件的内容作为字符串
//
//
////            Log.i("tfliteSupport", "Success reading label: " + labelFilePath);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Log.e("tfliteSupport", "Error reading label: " + e.getMessage());
//        }
//
//    }
//
//
//    /**
//     * 检测步骤
//     *
//     * @param bitmap
//     * @return
//     */
//    public ArrayList<Recognition> detect(Bitmap bitmap) {
//        // 检查传入的位图是否为空
//        if (bitmap == null) {
//            Log.e("test", "Bitmap is null. Unable to create TensorImage.");
//            return new ArrayList<>();
//        }
//
//        // 创建 TensorImage 以加载位图
//        TensorImage yolov5sTfliteInput;
//        ImageProcessor imageProcessor;
//
//        // 选择图像处理器和输入 TensorImage 类型
//        if (IS_INT8) {
//            imageProcessor = new ImageProcessor.Builder()
//                    .add(new ResizeOp(InputSize.getInstance().getInput_Param2(), InputSize.getInstance().getInput_Param1(), ResizeOp.ResizeMethod.BILINEAR))
//                    .add(new NormalizeOp(0, 255))
//                    .add(new QuantizeOp(input5SINT8QuantParams.getZeroPoint(), input5SINT8QuantParams.getScale()))
//                    .add(new CastOp(DataType.UINT8))
//                    .build();
//            yolov5sTfliteInput = new TensorImage(DataType.UINT8);
//        } else {
//            imageProcessor = new ImageProcessor.Builder()
//                    .add(new ResizeOp(InputSize.getInstance().getInput_Param2(), InputSize.getInstance().getInput_Param1(), ResizeOp.ResizeMethod.BILINEAR))
//                    .add(new NormalizeOp(0, 255))
//                    .build();
//            yolov5sTfliteInput = new TensorImage(DataType.FLOAT32);
//        }
//
//        // 加载位图到 TensorImage
//        yolov5sTfliteInput.load(bitmap);
//        if (yolov5sTfliteInput == null) {
//            Log.e("test", "TensorImage is null after loading bitmap. Unable to continue detection.");
//            return new ArrayList<>();
//        }
//
//        // 对 TensorImage 进行图像处理
//        yolov5sTfliteInput = imageProcessor.process(yolov5sTfliteInput);
//
//        // yolov5s-tflite的输出是:[1, 6300, 85], 可以从v5的GitHub release处找到相关tflite模型, 输出是[0,1], 处理到320.
//        TensorBuffer probabilityBuffer;
//
////        Log.e("TTT1", String.valueOf(Final_inputSize.getHeight()));
//
//        if (IS_INT8) {
//            probabilityBuffer = TensorBuffer.createFixedSize(Final_outputSize, DataType.UINT8);
////            probabilityBuffer = TensorBuffer.createFixedSize(OUTPUT_SIZE, DataType.UINT8);
//        } else {
//            probabilityBuffer = TensorBuffer.createFixedSize(Final_outputSize, DataType.FLOAT32);
////            probabilityBuffer = TensorBuffer.createFixedSize(OUTPUT_SIZE, DataType.FLOAT32);
//        }
//
//        // 推理计算
//        if (null != tflite) {
//            // 这里tflite默认会加一个batch=1的纬度
//            tflite.run(yolov5sTfliteInput.getBuffer(), probabilityBuffer.getBuffer());
//        }
//
//        // 这里输出反量化,需要是模型tflite.run之后执行.
//        if (IS_INT8) {
//            TensorProcessor tensorProcessor = new TensorProcessor.Builder()
//                    .add(new DequantizeOp(output5SINT8QuantParams.getZeroPoint(), output5SINT8QuantParams.getScale()))
//                    .build();
//            probabilityBuffer = tensorProcessor.process(probabilityBuffer);
//        }
//
//        // 输出数据被平铺了出来
//        float[] recognitionArray = probabilityBuffer.getFloatArray();
//        // 这里将flatten的数组重新解析(xywh,obj,classes).
//        ArrayList<Recognition> allRecognitions = new ArrayList<>();
//        for (int i = 0; i < Final_outputSize[1]; i++) {
//            int gridStride = i * Final_outputSize[2];
//            // 由于yolov5作者在导出tflite的时候对输出除以了image size, 所以这里需要乘回去
////            float x = recognitionArray[0 + gridStride] * Final_inputSize.getWidth();
////            float y = recognitionArray[1 + gridStride] * Final_inputSize.getHeight();
////            float w = recognitionArray[2 + gridStride] * Final_inputSize.getWidth();
////            float h = recognitionArray[3 + gridStride] * Final_inputSize.getHeight();
//            float x = recognitionArray[0 + gridStride] * InputSize.getInstance().getInput_Param1();
//            float y = recognitionArray[1 + gridStride] * InputSize.getInstance().getInput_Param2();
//            float w = recognitionArray[2 + gridStride] * InputSize.getInstance().getInput_Param1();
//            float h = recognitionArray[3 + gridStride] * InputSize.getInstance().getInput_Param2();
//            int xmin = (int) Math.max(0, x - w / 2.);
//            int ymin = (int) Math.max(0, y - h / 2.);
//            int xmax = (int) Math.min(InputSize.getInstance().getInput_Param1(), x + w / 2.);
//            int ymax = (int) Math.min(InputSize.getInstance().getInput_Param2(), y + h / 2.);
//            float confidence = recognitionArray[4 + gridStride];
//            float[] classScores = Arrays.copyOfRange(recognitionArray, 5 + gridStride, this.Final_outputSize[2] + gridStride);
////            if(i % 1000 == 0){
////                Log.i("tfliteSupport","x,y,w,h,conf:"+x+","+y+","+w+","+h+","+confidence);
////            }
//            int labelId = 0;
//            float maxLabelScores = 0.f;
//            for (int j = 0; j < classScores.length; j++) {
//                if (classScores[j] > maxLabelScores) {
//                    maxLabelScores = classScores[j];
//                    labelId = j;
//                }
//            }
//
//            Recognition r = new Recognition(
//                    labelId,
//                    "",
//                    maxLabelScores,
//                    confidence,
//                    new RectF(xmin, ymin, xmax, ymax));
//            allRecognitions.add(
//                    r);
//
//        }
////        Log.i("tfliteSupport", "recognize data size: "+allRecognitions.size());
//
//
////        // 非极大抑制输出
////        ArrayList<Recognition> nmsRecognitions = nms(allRecognitions);
////        // 第二次非极大抑制, 过滤那些同个目标识别到2个以上目标边框为不同类别的
////        ArrayList<Recognition> nmsFilterBoxDuplicationRecognitions = nmsAllClass(nmsRecognitions);
////
////        // 更新label信息
////        for (Recognition recognition : nmsFilterBoxDuplicationRecognitions) {
////            int labelId = recognition.getLabelId();
////            String labelName = associatedAxisLabels.get(labelId);
////            recognition.setLabelName(labelName);
////        }
////
////        return nmsFilterBoxDuplicationRecognitions;
//
//        //以下是识别到目标后的处理部分
//        ArrayList<Recognition> nmsRecognitions = nms(allRecognitions);
//        ArrayList<Recognition> nmsFilterBoxDuplicationRecognitions = nmsAllClass(nmsRecognitions);
//
//        ArrayList<Recognition> trackedRecognitions = new ArrayList<>();
//
//        // 处理跟踪的逻辑
//        for (Recognition recognition : nmsFilterBoxDuplicationRecognitions) {
//            int labelId = recognition.getLabelId();
//            String label = associatedAxisLabels.get(labelId);
//
//            //判断选择模式
//            if(MainActivity.checkBorder){
//                if(MainActivity.tracked) {
//                    if ("person".equals(label)) {
//
//                        Log.e("TSST", label);
//
//                        synchronized (labelColors) {
//                            if (!labelColors.containsKey(label)) {
//                                int color = generateRandomColor();
//                                labelColors.put(label, color);
//                                MainActivity.color = color;
//                            }
//                        }
//                        recognition.setLabelName(label);
//
//                        trackedRecognitions.add(recognition);
//                    }
//                }
//                else {
//                    // TODO 追踪模式
//                    synchronized (labelColors) {
//                        if (!labelColors.containsKey(label)) {
//                            int color = generateRandomColor();
//                            labelColors.put(label, color);
//                            MainActivity.color = color;
//                        }
//                    }
//                    recognition.setLabelName(label);
//
//                    trackedRecognitions.add(recognition);
//
//                }
//
//            }else{
//                if(MainActivity.tracked) {
//
//                    if ("person".equals(label)) {
//
//                        Log.e("TSST", label);
//
//                        synchronized (labelColors) {
//                            if (!labelColors.containsKey(label)) {
//                                MainActivity.color = Color.RED;
//                            }
//                        }
//                        recognition.setLabelName(label);
//
//                        trackedRecognitions.add(recognition);
//                    }
//                }
//                else {
//                    // 加入跟踪列表
//                    synchronized (labelColors) {
//                        if (!labelColors.containsKey(label)) {
//                            int color = generateRandomColor();
//                            labelColors.put(label, color);
//                            MainActivity.color = color;
//                        }
//                    }
//                    recognition.setLabelName(label);
//                    trackedRecognitions.add(recognition);
//                }
//            }
//        }
//        return trackedRecognitions;
//    }
//
//
//
//    /**
//     * 非极大抑制
//     *
//     * @param allRecognitions
//     * @return
//     */
//    protected ArrayList<Recognition> nms(ArrayList<Recognition> allRecognitions) {
//        ArrayList<Recognition> nmsRecognitions = new ArrayList<Recognition>();
//
//        // 遍历每个类别, 在每个类别下做nms
//        for (int i = 0; i < Final_outputSize[2]-5; i++) {
//            // 这里为每个类别做一个队列, 把labelScore高的排前面
//            PriorityQueue<Recognition> pq =
//                    new PriorityQueue<Recognition>(
//                            6300,
//                            new Comparator<Recognition>() {
//                                @Override
//                                public int compare(final Recognition l, final Recognition r) {
//                                    // Intentionally reversed to put high confidence at the head of the queue.
//                                    return Float.compare(r.getConfidence(), l.getConfidence());
//                                }
//                            });
//
//            // 相同类别的过滤出来, 且obj要大于设定的阈值
//            for (int j = 0; j < allRecognitions.size(); ++j) {
////                if (allRecognitions.get(j).getLabelId() == i) {
//                if (allRecognitions.get(j).getLabelId() == i && allRecognitions.get(j).getConfidence() > DETECT_THRESHOLD) {
//                    pq.add(allRecognitions.get(j));
////                    Log.i("tfliteSupport", allRecognitions.get(j).toString());
//                }
//            }
//
//            // nms循环遍历
//            while (pq.size() > 0) {
//                // 概率最大的先拿出来
//                Recognition[] a = new Recognition[pq.size()];
//                Recognition[] detections = pq.toArray(a);
//                Recognition max = detections[0];
//                nmsRecognitions.add(max);
//                pq.clear();
//
//                for (int k = 1; k < detections.length; k++) {
//                    Recognition detection = detections[k];
//                    if (boxIou(max.getLocation(), detection.getLocation()) < IOU_THRESHOLD) {
//                        pq.add(detection);
//                    }
//                }
//            }
//        }
//        return nmsRecognitions;
//    }
//
//    /**
//     * 对所有数据不区分类别做非极大抑制
//     *
//     * @param allRecognitions
//     * @return
//     */
//    protected ArrayList<Recognition> nmsAllClass(ArrayList<Recognition> allRecognitions) {
//        ArrayList<Recognition> nmsRecognitions = new ArrayList<Recognition>();
//
//        PriorityQueue<Recognition> pq =
//                new PriorityQueue<Recognition>(
//                        100,
//                        new Comparator<Recognition>() {
//                            @Override
//                            public int compare(final Recognition l, final Recognition r) {
//                                // Intentionally reversed to put high confidence at the head of the queue.
//                                return Float.compare(r.getConfidence(), l.getConfidence());
//                            }
//                        });
//
//        // 相同类别的过滤出来, 且obj要大于设定的阈值
//        for (int j = 0; j < allRecognitions.size(); ++j) {
//            if (allRecognitions.get(j).getConfidence() > DETECT_THRESHOLD) {
//                pq.add(allRecognitions.get(j));
//            }
//        }
//
//        while (pq.size() > 0) {
//            // 概率最大的先拿出来
//            Recognition[] a = new Recognition[pq.size()];
//            Recognition[] detections = pq.toArray(a);
//            Recognition max = detections[0];
//            nmsRecognitions.add(max);
//            pq.clear();
//
//            for (int k = 1; k < detections.length; k++) {
//                Recognition detection = detections[k];
//                if (boxIou(max.getLocation(), detection.getLocation()) < IOU_CLASS_DUPLICATED_THRESHOLD) {
//                    pq.add(detection);
//                }
//            }
//        }
//        return nmsRecognitions;
//    }
//
//
//    protected float boxIou(RectF a, RectF b) {
//        float intersection = boxIntersection(a, b);
//        float union = boxUnion(a, b);
//        if (union <= 0) return 1;
//        return intersection / union;
//    }
//
//    protected float boxIntersection(RectF a, RectF b) {
//        float maxLeft = a.left > b.left ? a.left : b.left;
//        float maxTop = a.top > b.top ? a.top : b.top;
//        float minRight = a.right < b.right ? a.right : b.right;
//        float minBottom = a.bottom < b.bottom ? a.bottom : b.bottom;
//        float w = minRight -  maxLeft;
//        float h = minBottom - maxTop;
//
//        if (w < 0 || h < 0) return 0;
//        float area = w * h;
//        return area;
//    }
//
//    protected float boxUnion(RectF a, RectF b) {
//        float i = boxIntersection(a, b);
//        float u = (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
//        return u;
//    }
//
//    /**
//     * 添加NNapi代理
//     */
//    public void addNNApiDelegate() {
//        NnApiDelegate nnApiDelegate = null;
//        // Initialize interpreter with NNAPI delegate for Android Pie or above
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//
////            NnApiDelegate.Options nnApiOptions = new NnApiDelegate.Options();
////            nnApiOptions.setAllowFp16(true);
////            nnApiOptions.setUseNnapiCpu(true);
////            ANEURALNETWORKS_PREFER_LOW_POWER：//倾向于以最大限度减少电池消耗的方式执行。这种设置适合经常执行的编译。
////            ANEURALNETWORKS_PREFER_FAST_SINGLE_ANSWER：//倾向于尽快返回单个答案，即使这会耗费更多电量。这是默认值。
////            ANEURALNETWORKS_PREFER_SUSTAINED_SPEED：//倾向于最大限度地提高连续帧的吞吐量，例如，在处理来自相机的连续帧时。
////            nnApiOptions.setExecutionPreference(NnApiDelegate.Options.EXECUTION_PREFERENCE_SUSTAINED_SPEED);
////            nnApiDelegate = new NnApiDelegate(nnApiOptions);
//
//            nnApiDelegate = new NnApiDelegate();
//            options.addDelegate(nnApiDelegate);
//            Log.i("tfliteSupport", "using nnapi delegate.");
//        }
//    }
//
//    /**
//     * 添加GPU代理
//     */
//    public void addGPUDelegate() {
//        CompatibilityList compatibilityList = new CompatibilityList();
//        if(compatibilityList.isDelegateSupportedOnThisDevice()){
//            GpuDelegate.Options delegateOptions = compatibilityList.getBestOptionsForThisDevice();
//            GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
//            options.addDelegate(gpuDelegate);
//            Log.i("tfliteSupport", "using gpu delegate.");
//        } else {
//            addThread(4);
//        }
//    }
//
//    /**
//     * 添加线程数
//     * @param thread
//     */
//    public void addThread(int thread) {
//        options.setNumThreads(thread);
//    }
//
//}

//TODO look at me


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.widget.Toast;

import com.example.userSelected.InputSize;
import com.example.userSelected.ModelAndLabel;
import com.example.userSelected.OutputSize;
import com.example.utils.Recognition;
import com.example.yolov5tfliteandroid.JNITools;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.gpu.CompatibilityList;
import org.tensorflow.lite.nnapi.NnApiDelegate;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.DequantizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.common.ops.QuantizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.metadata.MetadataExtractor;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;



public class Yolov5TFLiteDetector {
    public static ByteBuffer modelBuffer;

    public static ByteBuffer jsonBuffer;

    public static ByteBuffer txtBuffer;

    class box_t{
        public float x1, y1, x2, y2, score, label;
        public box_t(float x1, float y1, float x2, float y2, float score, float label){
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.score = score;
            this.label = label;
        }
    }

//    private final Size INPNUT_SIZE = new Size(320, 320);
//    private final int[] OUTPUT_SIZE = new int[]{1, 6300, 85};

    private Boolean IS_INT8 = false;
    //    private final float DETECT_THRESHOLD = 0.0f;
//    private final float IOU_THRESHOLD = 0.0f;
//    private final float IOU_CLASS_DUPLICATED_THRESHOLD = 0.0f;
    private final float IOU_CLASS_DUPLICATED_THRESHOLD = 0.7f;
    private final float IOU_THRESHOLD = 0.45f;
    private final float DETECT_THRESHOLD = 0.25f;

    private Size UserSelected_INPUT_SIZE = new Size(InputSize.getInstance().getInput_Param1(), InputSize.getInstance().getInput_Param2());
    private int[] UserSelected_OUTPUT_SIZE = new int[]{OutputSize.getInstance().getOutput_Param1(), OutputSize.getInstance().getOutput_Param2(), OutputSize.getInstance().getOutput_Param3()};
    private String UserSelected_MODEL = ModelAndLabel.getInstance().getModel();
    MetadataExtractor.QuantizationParams input5SINT8QuantParams = new MetadataExtractor.QuantizationParams(0.003921568859368563f, 0);
    MetadataExtractor.QuantizationParams output5SINT8QuantParams = new MetadataExtractor.QuantizationParams(0.006305381190031767f, 5);
    private String MODEL_FILE;
    private List<String> associatedAxisLabels;
    private Size Final_inputSize;
    private  int[] Final_outputSize;

    private long detectorPtr;

    private float[] outputBuffer;

    private ArrayList<box_t> boxes = new ArrayList<>();


    Interpreter.Options options = new Interpreter.Options();

    public Yolov5TFLiteDetector() {
    }
    public String getModelFile() {
        return this.MODEL_FILE;
    }

    public void setModelFile(String modelFile) {
        switch (modelFile) {
            case "UserSelected":
                IS_INT8 = false;
                MODEL_FILE = UserSelected_MODEL;
                Log.e("test","加载用户模型（选择）");
                break;
            default:
                Log.e("test","default");
                Log.i("tfliteSupport", "Only yolov5s/n/m/sint8 can be load!");
        }
    }

//    public String getLabelFile() {
//        return this.LABEL_FILE;
//    }

    public int getInputSizeWidth() {
        return InputSize.getInstance().getInput_Param1();
    }
    public int getInputSizeHeight() {
        return InputSize.getInstance().getInput_Param2();
    }

//    public int[] getOutputSize() {
//        return this.OUTPUT_SIZE;
//    }

    /**
     * 初始化模型, 可以通过 addNNApiDelegate(), addGPUDelegate()提前加载相应代理
     *
     * @param activity
     */
    public void initialModel(Context activity, Size inputSize, int[] outputSize) {
        // Initialize the model
        Final_inputSize = inputSize;
        Final_outputSize = outputSize;
        Log.e("test-user", "用户选择模型正常运行");

        try {
            // 获取模型ByteBuffer
            ByteBuffer modelBuffer = Yolov5TFLiteDetector.modelBuffer;
            if (modelBuffer != null) {
                byte[] modelBytes = new byte[modelBuffer.remaining()];
                modelBuffer.get(modelBytes); // 将 ByteBuffer 中的数据读取到 byte 数组中

                JNITools test = new JNITools();
                detectorPtr = test.createDetector(modelBytes, modelBytes.length);
                Log.i("tfliteSupport", "Success reading and loading model");
            } else {
                Log.e("tfliteSupport", "Model ByteBuffer is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("tfliteSupport", "Error reading or loading model: " + e.getMessage());
        }

        try {
            // 获取标签ByteBuffer
            ByteBuffer labelBuffer = Yolov5TFLiteDetector.txtBuffer;
            if (labelBuffer != null) {
                byte[] labelBytes = new byte[labelBuffer.remaining()];
                labelBuffer.get(labelBytes); // 将 ByteBuffer 中的数据读取到 byte 数组中

                associatedAxisLabels = FileUtil.loadLabels(new ByteArrayInputStream(labelBytes));
                Log.i("tfliteSupport", "Success reading and loading labels");
            } else {
                Log.e("tfliteSupport", "Label ByteBuffer is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("tfliteSupport", "Error reading or loading labels: " + e.getMessage());
        }
    }



    /**
     * 检测步骤
     *
     * @param bitmap
     * @return
     */
    public ArrayList<Recognition> detect(Bitmap bitmap) {
        ArrayList<Recognition> allRecognitions = new ArrayList<>();
        // flatten image >> detect
        // 检查传入的位图是否为空
        if (bitmap == null) {
            Log.e("test", "Bitmap is null. Unable to create TensorImage.");
            return new ArrayList<>();
        }
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(byteBuffer);
        // 创建 TensorImage 以加载位图
//        TensorImage yolov5sTfliteInput;
//
//        yolov5sTfliteInput = new TensorImage(DataType.UINT8);
//        // 加载位图到 TensorImage
//        yolov5sTfliteInput.load(bitmap);
//        if (yolov5sTfliteInput == null) {
//            Log.e("test", "TensorImage is null after loading bitmap. Unable to continue detection.");
//            return new ArrayList<>();
//        }
        // yolov5s-tflite的输出是:[1, 6300, 85], 可以从v5的GitHub release处找到相关tflite模型, 输出是[0,1], 处理到320.
        // 推理计算
        if (detectorPtr != 0) {
            JNITools test = new JNITools();
            outputBuffer = test.detect(
                    detectorPtr,
                    byteBuffer.array(),
                    bitmap.getWidth(),
                    bitmap.getHeight());
            Log.d("test", "===>" + outputBuffer.length);
            if (outputBuffer.length != 0){
                boxes.clear();

                for (int i = 0; i < outputBuffer.length; i += 6){
                    boxes.add(new box_t(
                            outputBuffer[i],
                            outputBuffer[i+1],
                            outputBuffer[i+2],
                            outputBuffer[i+3],
                            outputBuffer[i+4],
                            outputBuffer[i+5])
                    );
                }
            }

        }

        for (int i = 0; i < boxes.size(); i++) {
            box_t box = boxes.get(i);
            Recognition r = new Recognition(
                    (int) box.label,
                    associatedAxisLabels.get((int) box.label),
                    box.score,
                    box.score,
                    new RectF(box.x1, box.y1, box.x2, box.y2));
            allRecognitions.add(r);
            Log.d("test", "this is:" + associatedAxisLabels.get((int) box.label) + "\nbox"+box.x1 + " " + box.y1 + " " + box.x2 + " " + box.y2);
        }

        return allRecognitions;
    }

    protected float boxIou(RectF a, RectF b) {
        float intersection = boxIntersection(a, b);
        float union = boxUnion(a, b);
        if (union <= 0) return 1;
        return intersection / union;
    }

    protected float boxIntersection(RectF a, RectF b) {
        float maxLeft = a.left > b.left ? a.left : b.left;
        float maxTop = a.top > b.top ? a.top : b.top;
        float minRight = a.right < b.right ? a.right : b.right;
        float minBottom = a.bottom < b.bottom ? a.bottom : b.bottom;
        float w = minRight -  maxLeft;
        float h = minBottom - maxTop;

        if (w < 0 || h < 0) return 0;
        float area = w * h;
        return area;
    }

    protected float boxUnion(RectF a, RectF b) {
        float i = boxIntersection(a, b);
        float u = (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i;
        return u;
    }

    /**
     * 添加NNapi代理
     */
    public void addNNApiDelegate() {
        NnApiDelegate nnApiDelegate = null;
        // Initialize interpreter with NNAPI delegate for Android Pie or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

//            NnApiDelegate.Options nnApiOptions = new NnApiDelegate.Options();
//            nnApiOptions.setAllowFp16(true);
//            nnApiOptions.setUseNnapiCpu(true);
//            ANEURALNETWORKS_PREFER_LOW_POWER：//倾向于以最大限度减少电池消耗的方式执行。这种设置适合经常执行的编译。
//            ANEURALNETWORKS_PREFER_FAST_SINGLE_ANSWER：//倾向于尽快返回单个答案，即使这会耗费更多电量。这是默认值。
//            ANEURALNETWORKS_PREFER_SUSTAINED_SPEED：//倾向于最大限度地提高连续帧的吞吐量，例如，在处理来自相机的连续帧时。
//            nnApiOptions.setExecutionPreference(NnApiDelegate.Options.EXECUTION_PREFERENCE_SUSTAINED_SPEED);
//            nnApiDelegate = new NnApiDelegate(nnApiOptions);

            nnApiDelegate = new NnApiDelegate();
            options.addDelegate(nnApiDelegate);
            Log.i("tfliteSupport", "using nnapi delegate.");
        }
    }

    /**
     * 添加GPU代理
     */
    public void addGPUDelegate() {
        CompatibilityList compatibilityList = new CompatibilityList();
        if(compatibilityList.isDelegateSupportedOnThisDevice()){
            GpuDelegate.Options delegateOptions = compatibilityList.getBestOptionsForThisDevice();
            GpuDelegate gpuDelegate = new GpuDelegate(delegateOptions);
            options.addDelegate(gpuDelegate);
            Log.i("tfliteSupport", "using gpu delegate.");
        } else {
            addThread(4);
        }
    }

    /**
     * 添加线程数
     * @param thread
     */
    public void addThread(int thread) {
        options.setNumThreads(thread);
    }

}
