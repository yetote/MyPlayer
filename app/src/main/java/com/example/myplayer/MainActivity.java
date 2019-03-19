package com.example.myplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myplayer.player.MyPlayer;
import com.example.myplayer.player.gl.MyRenderer;
import com.example.myplayer.player.listener.OnPreparedCallBack;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

public class MainActivity extends AppCompatActivity {
    MyPlayer player;
    String path;
    private static final String TAG = "MainActivity";
    private ImageView start, fill;
    private GLSurfaceView surfaceView;
    MyRenderer renderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = this.getExternalCacheDir().getPath() + "/res/test.mp4";
        init();

        click();


        callback();

    }

    private void callback() {

        player.setPreparedCallBack((isSuccess, errorCode) -> {
            if (!isSuccess) {
                Log.e(TAG, "onPrepared: ffmpeg初始化失败,错误代码：" + errorCode);
            } else {
                Log.e(TAG, "onPrepared: 准备成功");
                player.play();
            }
        });
    }

    private void click() {
        start.setOnClickListener(v -> player.prepare(path));
        fill.setOnClickListener(v -> {
            setRequestedOrientation(SCREEN_ORIENTATION_LANDSCAPE);
            MainActivity.this.getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            surfaceView.setLayoutParams(lp);
        });
    }

    private void init() {
        start = findViewById(R.id.start);
        fill = findViewById(R.id.fill);
        renderer = new MyRenderer();
        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setRenderer(renderer);
        player = new MyPlayer();
    }

}
