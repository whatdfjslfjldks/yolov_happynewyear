package com.example.holder;

import android.util.Log;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.MainActivity;
import com.example.detector.Yolov5TFLiteDetector;

public class TarExtractor {

    private String extension;

    public  void extractTar(byte[] tarFileStream, File outputDirectory) throws IOException {


        try (TarArchiveInputStream tis = new TarArchiveInputStream(new ByteArrayInputStream(tarFileStream))) {
            TarArchiveEntry entry;
            while ((entry = tis.getNextTarEntry()) != null) {
                extractEntry(tis, entry, outputDirectory);
            }
        }
    }



    private void extractEntry(TarArchiveInputStream tis, TarArchiveEntry entry, File outputDirectory) throws IOException {

        if (entry.isFile() && entry.getName().endsWith(".tflite")) {
            Log.d("TarExtractor", "model extractEntry: " + entry.getName());

            String fileName = new File(entry.getName()).getName(); // 获取文件名
            String modelName = fileName.substring(0, fileName.lastIndexOf(".")); // 去除扩展名
            Log.d("TarExtractor", "Model name: " + modelName);

            byte[] fileBytes = new byte[(int) entry.getSize()];
            // not strong
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) entry.getSize());
            byteBuffer.order(ByteOrder.nativeOrder());
            tis.read(fileBytes);
            byteBuffer.put(fileBytes);
            Yolov5TFLiteDetector.modelBuffer = byteBuffer;



            Log.d("TarExtractor", "model extractEntry: " + Yolov5TFLiteDetector.modelBuffer.capacity());
        } else if (entry.isFile() && entry.getName().endsWith(".json")) {
            Log.d("TarExtractor", "model extractEntry: " + entry.getName());

            byte[] fileBytes = new byte[(int) entry.getSize()];
            // not strong
            ByteBuffer jsonByteBuffer = ByteBuffer.allocateDirect((int) entry.getSize());
            jsonByteBuffer.order(ByteOrder.nativeOrder());
            tis.read(fileBytes);
            jsonByteBuffer.put(fileBytes);
            Yolov5TFLiteDetector.jsonBuffer = jsonByteBuffer;



//            MainActivity.selectedModel=true;

//            Log.d("TarExtractor", "model extractEntry: " + Yolov5TFLiteDetector.modelBuffer.capacity());
        }
        else if (entry.isFile() && entry.getName().endsWith(".txt")) {
            Log.d("TarExtractor", "model extractEntry: " + entry.getName());

            byte[] fileBytes = new byte[(int) entry.getSize()];
            // not strong
            ByteBuffer txtByteBuffer = ByteBuffer.allocateDirect((int) entry.getSize());
            txtByteBuffer.order(ByteOrder.nativeOrder());
            tis.read(fileBytes);
            txtByteBuffer.put(fileBytes);
            Yolov5TFLiteDetector.txtBuffer = txtByteBuffer;


        }

    }



}

