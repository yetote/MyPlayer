package com.example.myplayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.myplayer.player.MediaCodecSupport;
import com.example.myplayer.player.MyPlayer;
import com.example.myplayer.player.gl.utils.TextRecourseReader;
import com.example.myplayer.player.listener.FFmpegCallBack;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

public class MainActivity extends AppCompatActivity {
    private MyPlayer player;
    private String path;
    private static final String TAG = "MainActivity";
    private ImageView start, fill;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private String vertexCode, fragCode;
    private int w, h;
    private boolean isPlaying;
    private String networkPath = "http://gslb.miaopai.com/stream/J7NezBpr68nZtH8chOL9hyzkiRsk0rw6.mp4?vend=miaopai&ssig=6c1263f8cc78ac51aaacce2293dbab87&time_stamp=1556778520561&mpflag=32";
    private SeekBar seekBar;
    private TextView currentTv, totalTv;
    private String playingKey = "isPlaying";
    private String rotateKey = "isRotate";
    private boolean isRotate;
    private Button recordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_main);
        path = this.getExternalCacheDir().getPath() + "/res/output.h264";
        init();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (savedInstanceState != null) {
                    isPlaying = savedInstanceState.getBoolean(playingKey);
                }
                if (!isPlaying) {
                    player.prepare(path, vertexCode, fragCode, holder.getSurface());
                }
                w = width;
                h = height;
                if (isRotate) {
                    Toast.makeText(MainActivity.this, "宽度" + w + "高度" + h, Toast.LENGTH_SHORT).show();
                    player.ratote(w, h);
                    player.recover();
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        click();

        callback();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Log.e(TAG, "onProgressChanged: progress" + progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.e(TAG, "onStartTrackingTouch: ");
                player.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.e(TAG, "onStopTrackingTouch: " + seekBar.getProgress());
                player.seek(seekBar.getProgress());
                player.recover();
            }
        });
    }

    private void callback() {

        player.setFFmpegCallBack(new FFmpegCallBack() {
            @Override
            public void onPrepared(boolean isSuccess, int totalTime) {
                if (!isSuccess) {
                    Log.e(TAG, "onPrepared: ffmpeg准备失败");
                } else {
                    Log.e(TAG, "onPrepared: 准备成功");
                    Observable.create((ObservableOnSubscribe<Integer>) emitter -> emitter.onNext(totalTime))
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe(integer -> {
                                seekBar.setMax(totalTime);
                                totalTv.setText(pts2Time(totalTime));
                                player.play(w, h);
                                isPlaying = true;
                            });
                }
            }

            @Override
            public void onPause() {
                Log.e(TAG, "callback: 播放暂停");
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "callback: 播放结束");
            }

            @Override
            public void onPlaying(int currentTime) {
                Observable.create((ObservableOnSubscribe<Integer>) emitter -> emitter.onNext(currentTime))
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(integer -> {
                            seekBar.setProgress(currentTime);
                            currentTv.setText(pts2Time(currentTime));
                        });
            }
        });
    }

    private void click() {
        fill.setOnClickListener(v -> {
            player.pause();
            setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            surfaceView.setLayoutParams(lp);
            isRotate = true;
            Log.e(TAG, "click: " + "旋转完成");
        });
        start.setOnClickListener(v -> {
            if (isPlaying) {
                start.setImageDrawable(getResources().getDrawable(R.mipmap.play, null));
                player.pause();
            } else {
                start.setImageDrawable(getResources().getDrawable(R.mipmap.pause, null));
                player.recover();
            }
            isPlaying = !isPlaying;
        });
        surfaceView.setOnClickListener(v -> {
            Log.e(TAG, "onClick: surface");
            MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | SYSTEM_UI_FLAG_FULLSCREEN
                    | SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        });
        recordBtn.setOnClickListener(v -> startActivity(new Intent().setClass(MainActivity.this, RecordActivity.class)));
    }

    private void init() {
        start = findViewById(R.id.start);
        fill = findViewById(R.id.fill);
        surfaceView = findViewById(R.id.surfaceView);
        player = new MyPlayer();
        surfaceHolder = surfaceView.getHolder();
        vertexCode = TextRecourseReader.readTextFileFromResource(this, R.raw.yuv_vertex_shader);
        fragCode = TextRecourseReader.readTextFileFromResource(this, R.raw.yuv_frag_shader);
        MediaCodecSupport codecSupport = new MediaCodecSupport();
        seekBar = findViewById(R.id.seekBar);
        currentTv = findViewById(R.id.currentTimeTv);
        totalTv = findViewById(R.id.totalTimeTv);
        recordBtn = findViewById(R.id.record);
    }

    private String pts2Time(int ptsTime) {
        int min, sec;
        String time;
        min = ptsTime / 60;
        sec = ptsTime % 60;
        if (min < 10) {
            time = "0" + min + ":";
        } else {
            time = min + ":";
        }
        if (sec < 10) {
            time += "0" + sec;
        } else {
            time += sec;
        }
        return time;
    }

    @Override
    protected void onPause() {
        player.pause();
        super.onPause();
    }
}
