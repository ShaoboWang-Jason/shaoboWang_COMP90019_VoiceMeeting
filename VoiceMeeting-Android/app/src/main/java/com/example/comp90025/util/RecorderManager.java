//Author: Shaobo Wang
//shaobow@student.unimelb.edu.au

package com.example.comp90025.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioRecordingConfiguration;
import android.media.MediaRecorder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecorderManager {
    private AudioRecord aR;
    private Thread audioThread;
    private int buffer = 0;
    private boolean isDoing = false;
    private final static int channel = AudioFormat.CHANNEL_IN_MONO; //16 bits
    private final static int format = AudioFormat.ENCODING_PCM_16BIT;
    private final static int HZ = 44100;
    private final static int source = MediaRecorder.AudioSource.MIC;
    private FileOutputStream outputStream;
    private String outputPath;
    private static final RecorderManager instance = new RecorderManager();


    public static RecorderManager getInstance() {
        return instance;
    }


    public void Meta() {
        if (null != aR) {
            aR.release();
        }

        try {
            buffer = AudioRecord.getMinBufferSize(HZ, channel, format);
            aR = new AudioRecord(source,HZ,channel,format,buffer);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public void start (String path) {
        if (aR != null && aR.getState() == AudioRecord.STATE_INITIALIZED ) {
            try {
                aR.startRecording();
            } catch (Exception e){
                System.out.println("Fail");
            }
        } else {
            System.out.println("Fail");
        }
        isDoing = true;
        audioThread = new Thread (new thread(), "Thread");
        try {
            this.outputPath = path;
            audioThread.start();
        } catch (Exception e) {
            System.out.println("Fail");
        }
    }

    public void stop() {
        try {
            if (aR != null) {
                isDoing = false;
                try {
                    if (audioThread != null) {
                        audioThread.join();
                        audioThread = null;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    }
                releaseRecord();
                }

            } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public  void releaseRecord() {
        if (aR.getState() == AudioRecord.STATE_INITIALIZED)
            aR.stop();
        aR.release();
        aR = null;
    }

    public class thread implements Runnable {

        public void run() {
            try{
                outputStream = new FileOutputStream(outputPath);
                byte[] samples = new byte[buffer];
                while (isDoing) {
                    int size = getBuffer(buffer,samples);
                    if (size > 0) {
                        outputStream.write(samples);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != outputStream) {
                    try {
                        outputStream.close();
                        outputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private int getBuffer(int size, byte[] samples ) {
        if (aR != null) {
            int bufferSize = aR.read(samples, 0 , size);
            return bufferSize;
        } else {
            return 0;
        }
    }
}
