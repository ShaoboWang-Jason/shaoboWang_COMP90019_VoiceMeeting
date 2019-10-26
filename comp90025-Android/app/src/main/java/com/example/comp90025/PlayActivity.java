//Author: Shaobo Wang
//shaobow@student.unimelb.edu.au

package com.example.comp90025;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.comp90025.util.Logger;

import java.io.IOException;


public class PlayActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnPlay, btnBack, btnFor;
    private SeekBar seekBar;
    private MediaPlayer mp = new MediaPlayer();
    private Runnable runnable;
    private Handler handler;
    private TextView name, length;
    RecordActivity recordActivity = new RecordActivity();
    private int timeRecord = 0;
    private static final int DISPLAY_RECORDING_TIME_FLAG = 100000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        // set view
        btnPlay = findViewById(R.id.start);
        btnBack = findViewById(R.id.left);
        btnFor = findViewById(R.id.right);
        handler = new Handler();
        seekBar = findViewById(R.id.seekBar);
        length = findViewById(R.id.total);
        name = findViewById(R.id.name);
        btnFor.setOnClickListener(this);
        btnBack.setOnClickListener(this);
        btnPlay.setOnClickListener(this);

        //set path and start to play
        String path = recordActivity.newPath;
        String tim = "00:00";
        name.setText(path.replace("/storage/emulated/0/RecorderFile/", ""));
        length.setText(tim);
        if (path != null) {
            try {
                mp.setDataSource(path);
                mp.prepare();
            } catch (IllegalStateException e) {
                mp = null;
                mp = new MediaPlayer();
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Logger.e("No file");
            Toast.makeText(PlayActivity.this, "There is no file exist!!", Toast.LENGTH_SHORT).show();
        }

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                seekBar.setMax(mediaPlayer.getDuration());
                mediaPlayer.start();
                changeSeekbar();

            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    mp.seekTo(i);

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void changeSeekbar() {
        seekBar.setProgress(mp.getCurrentPosition());

        if (mp.isPlaying()) {
            runnable = new Runnable() {
                @Override
                public void run() {
                    changeSeekbar();
                    timer.sendEmptyMessage(DISPLAY_RECORDING_TIME_FLAG);
                    timeRecord++;
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    private Handler timer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DISPLAY_RECORDING_TIME_FLAG:
                    int minutes = timeRecord / 60;
                    int seconds = timeRecord % 60;
                    String timeSample = String.format("%02d:%02d", minutes, seconds);
                    length.setText(timeSample);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                if (mp.isPlaying()) {
                    mp.pause();
                    btnPlay.setText(">");
                } else {
                    mp.start();
                    btnPlay.setText("||");
                    changeSeekbar();
                }
                break;
            case R.id.left:
                mp.seekTo(mp.getCurrentPosition() - 5000);
                break;
            case R.id.right:
                mp.seekTo(mp.getCurrentPosition() + 5000);
                break;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mp.stop();
        mp.release();
    }

}