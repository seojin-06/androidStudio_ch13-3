package com.cookandroid.project13_3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {
    ListView listViewMP3;
    Button btnPlay, btnPause, btnStop;
    TextView tvMP3, tvTime;
    ProgressBar pbMP3;
    ArrayList<String> mp3List;
    String selectedMP3;
    String mp3Path = Environment.getExternalStorageDirectory().getPath() + "/";
    MediaPlayer mPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MODE_PRIVATE);

        mp3List = new ArrayList<String>();

        File[] listFiles = new File(mp3Path).listFiles();
        String fileName, extName;
        for (File file : listFiles) {
            fileName = file.getName();
            extName = fileName.substring(fileName.length() - 3);
            if (extName.equals((String) "mp3"))
                mp3List.add(fileName);
        }

        listViewMP3 = (ListView) findViewById(R.id.listViewMP3);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, mp3List);
        listViewMP3.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listViewMP3.setAdapter(adapter);
        listViewMP3.setItemChecked(0, true);

        listViewMP3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMP3 = mp3List.get(i);
            }
        });

        selectedMP3 = mp3List.get(0);

        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnStop = (Button) findViewById(R.id.btnStop);
        tvMP3 = (TextView) findViewById(R.id.tvMP3);;
        tvTime = (TextView) findViewById(R.id.tvTime);
        pbMP3 = (ProgressBar) findViewById(R.id.pbMP3);

        Handler mp3Handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                SimpleDateFormat timeFormat = new SimpleDateFormat(" mm:ss ");
                pbMP3.setProgress(mPlayer.getCurrentPosition());
                tvTime.setText("진행 시간 : " + timeFormat.format(mPlayer.getCurrentPosition()));
            }
        };

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(mp3Path + selectedMP3);
                    mPlayer.prepare();
                    mPlayer.start();
                    btnPlay.setClickable(false);
                    btnStop.setClickable(true);
                    tvMP3.setText("실행중인 음악: " + selectedMP3);
                    new Thread() {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
                        public void run() {
                            if (mPlayer==null)
                                return;
                            pbMP3.setMax(mPlayer.getDuration());
                            while(mPlayer.isPlaying()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mp3Handler.sendEmptyMessage(0);
                                    }
                                });
                                SystemClock.sleep(200);
                            }
                        }
                    }.start();
                } catch (IOException e) {
                }
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnPause.getText().equals("일시정지")) {
                    mPlayer.pause();
                    btnPause.setText("이어듣기");
                } else {
                    mPlayer.start();
                    new Thread() {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
                        public void run() {
                            pbMP3.setMax(mPlayer.getDuration());
                            while(mPlayer.isPlaying()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mp3Handler.sendEmptyMessage(0);
                                    }
                                });
                                SystemClock.sleep(200);
                            }
                        }
                    }.start();
                    btnPause.setText("일시정지");
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayer.stop();
                mPlayer.reset();
                btnPlay.setClickable(true);
                btnStop.setClickable(false);
                tvMP3.setText("실행중인 음악 : ");

                pbMP3.setProgress(0);
                tvTime.setText("진행 시간 : ");
            }
        });

        btnStop.setClickable(false);
    }
}