//Author: Shaobo Wang
//shaobow@student.unimelb.edu.au

package com.example.comp90025;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.comp90025.util.Logger;
import com.example.comp90025.util.RecorderManager;
import com.example.comp90025.util.SDcard;
import com.example.comp90025.util.pcmToWav;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class RecordActivity extends AppCompatActivity {
    private Button btn_start,btn_stop,btn_play,btn_upload,btn_data;
    private TextView txt_time;
    private boolean isDoing = false;
    private Timer time;
    private int timeInRecorder = 0;
    private String outPath;
    public static String newPath;
    private RecorderManager manager;
    private LoginActivity logger;
    private TimerTask Recording_Time_Task;
    private static final int DISPLAY_RECORDING_TIME_FLAG = 100000;
    private pcmToWav changeToWav = new pcmToWav();
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private FirebaseAuth mAuth;
    public static String result;
    JSONArray arr = new JSONArray();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mdatabase = database.getReference();



    protected void onCreate(Bundle savedinstanceState) {
        super.onCreate(savedinstanceState);
        setContentView(R.layout.recorder_activity);
        btn_start = findViewById(R.id.btn_1);
        btn_stop = findViewById(R.id.btn_2);
        btn_play = findViewById(R.id.btn_3);
        btn_upload = findViewById(R.id.btn_upload);
        btn_data = findViewById(R.id.btn_data);
        txt_time = findViewById(R.id.txt_1);
        mAuth = FirebaseAuth.getInstance();
        iniEvent();
        setListeners();

    }

    // set initial time
    private void iniEvent(){
        String timeSt = "00:00";
        txt_time.setText(timeSt);
    }

    private void setListeners(){
        // begin record
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDoing) {
                    Toast.makeText(RecordActivity.this, "Stop first", Toast.LENGTH_SHORT ).show();

                } else {
                    mediaPlayer.stop();
                    checking();
                }
            }
        });

        //stop record
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDoing) {
                    isDoing = false;
                    manager.stop();
                    timer.sendEmptyMessage(DISPLAY_RECORDING_TIME_FLAG);
                    Recording_Time_Task.cancel();
                    time.cancel();
                    // transfer pcm file to wav file
                    newPath = outPath.replace(".pcm", ".wav");
                    changeToWav.change(outPath, newPath);
                    Logger.e(newPath);
                } else {
                    Toast.makeText(RecordActivity.this, "Can't stop",
                            Toast.LENGTH_SHORT ).show();

                }

            }
        });

        //play record
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newPath != null) {
                    Intent intent;
                    intent = new Intent(RecordActivity.this,PlayActivity.class);
                    startActivity(intent);
                } else {
                    Logger.e("No file");
                    Toast.makeText(RecordActivity.this, "There is no file exist!!",
                            Toast.LENGTH_SHORT ).show();
                }

            }

        });

        //send wav to server and wait for response
        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newPath != null) {
                    Thread sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String ipAd = "35.189.40.211";
//                            String ipAd = "192.168.0.108";
                            int port = 9999;
                            int start=newPath.lastIndexOf("/");
                            int end=newPath.lastIndexOf(".");
                            String filename = newPath.substring(start+1,end);
                            sendFile(filename,newPath,ipAd,port);
                        }
                    });
                    sendThread.start();
                } else {
                    Logger.e("No file");
                    Toast.makeText(RecordActivity.this, "There is no file exist!!",
                            Toast.LENGTH_SHORT ).show();
                }

            }
        });

        //upload data to webserver
        btn_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newPath != null) {
                    Thread sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Logger.e(arr.toString());
                            upload(arr.toString());
                            newHistory(logger.email,arr.toString());

                        }
                    });
                    sendThread.start();
                } else {
                    Logger.e("No file");
                    Toast.makeText(RecordActivity.this, "There is no data exist!!",
                            Toast.LENGTH_SHORT ).show();
                }

            }
        });

    }

    // Use socket to connect server and transfer wav file to server and receive json response
    public void sendFile(String filename, String path, String ipAddress, int port) {

        try {
            Socket s = new Socket(ipAddress,port);
            FileInputStream fis = new FileInputStream(path);
            InputStream in = s.getInputStream();
            OutputStream out = s.getOutputStream();
            BufferedReader inRead = new BufferedReader(new InputStreamReader(in));
            out.write(filename.getBytes());
            out.flush();

            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = fis.read(buf)) != -1) {
                out.write(buf,0,len);
                out.flush();

            }
            s.shutdownOutput();
            String str = null;
            result = "";
            while((str = inRead.readLine()) != null) {
                result = result + str;
            }
            result = result.replaceAll("], ",":");
            result = result.replaceAll("\\[","");
            result = result.replaceAll("\\'","");
            result = result.replaceAll("\\]","");
            String[] list = result.split(":");
            String[] list1 =list[0].split(", ");
            String[] list2 =list[1].split(", ");
            String[] list3 =list[2].split(", ");
            String[] list4 =list[3].split(", ");
            String[] list5 =list[4].split(", ");
            arr = new JSONArray();
            JSONArray arr1 = new JSONArray();
            JSONArray arr2 = new JSONArray();
            JSONArray arr3 = new JSONArray();
            JSONArray arr4 = new JSONArray();
            JSONArray arr5 = new JSONArray();
            for (int i = 0; i < list1.length;i ++ ) {
                arr1.put(list1[i]);
                arr2.put(list2[i]);
                arr3.put(list3[i]);
                arr4.put(list4[i]);
                arr5.put(list5[i]);
            }
            arr.put(arr1);
            arr.put(arr2);
            arr.put(arr3);
            arr.put(arr4);
            arr.put(arr5);
            Logger.e(result);
            fis.close();
            out.close();
            in.close();
            s.close();

        } catch (IOException e) {
            Logger.e("fail： " + e);
        }
    }

    private final int CODE = 0x001;
    private static final String[] permissionManifest = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    //checking audio permission
    private void checking(){
            Logger.e("Audio Permission", "Permission Checking");
            boolean permissionState = true;
            for (String permission : permissionManifest) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionState = false;
                }
            }
            if (!permissionState) {
                Logger.e("Audio Permission", "Permission Checking false");
                ActivityCompat.requestPermissions(this, permissionManifest, CODE);
            } else {
                Logger.e("Audio Permission", "Permission Checking true");
                start();
            }


    }

    private void start() {
        // create file path if the path does not exist
        String sdPath = SDcard.saveToSDCard(this) + File.separator + "RecorderFile";
        File sdFile = new File(sdPath);
        if (!sdFile.exists()) {
            sdFile.mkdir();
        }

        //settting new path for pcm file
        outPath = new File(sdFile, new Date().getTime() + ".pcm").getAbsolutePath();
        isDoing = true;
        manager = RecorderManager.getInstance();
        try {

            manager.Meta();
            manager.start(outPath);
            timeInRecorder = 0;
            time = new Timer();
            Recording_Time_Task = new TimerTask() {
                @Override
                public void run() {
                    timer.sendEmptyMessage(DISPLAY_RECORDING_TIME_FLAG);
                    timeInRecorder++;
                }
            };
            time.schedule(Recording_Time_Task, 0, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Handler timer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISPLAY_RECORDING_TIME_FLAG:
                    int minutes = timeInRecorder / 60;
                    int seconds = timeInRecorder % 60;
                    String timeSample = String.format("%02d:%02d", minutes, seconds);
                    txt_time.setText(timeSample);
                    break;
                default:
                    break;
            }
        }
    };

    // using http to connect webserver and transfer data to webserver.
    private void upload(String Json) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String path = "http://35.189.40.211:8889/IndexHandler";
        HttpURLConnection conn = null;
        try {
            URL my_url = new URL(path);
            conn = (HttpURLConnection) my_url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setConnectTimeout(60 * 1000);
            conn.setReadTimeout(60 * 1000);
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("logType", "base");
            conn.connect();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
            writer.write(String.valueOf(Json));
            System.out.println("Success");
            writer.close();
            int responseCode = conn.getResponseCode();
            Logger.e("Transferred: " + responseCode + " *** " + conn.getResponseMessage() + String.valueOf(Json));

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

    }


    @IgnoreExtraProperties
    public class history {

        public String userid;
        public String result;

        public history() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public history(String userid,String result) {
            this.userid = userid;
            this.result = result;
        }

    }

    private void newHistory(String email, String result) {
        FirebaseUser fbUser = mAuth.getCurrentUser();
        history mhistory = new history(email,result);
        mdatabase = FirebaseDatabase.getInstance().getReference("user");
        mdatabase.child(fbUser.getUid().trim()).push().setValue(mhistory);
    }




}
