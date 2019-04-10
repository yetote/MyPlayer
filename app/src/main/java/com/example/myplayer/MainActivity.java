package com.example.myplayer;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.myplayer.player.MyPlayer;
import com.example.myplayer.player.gl.MyRenderer;
import com.example.myplayer.player.gl.utils.TextRecourseReader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

public class MainActivity extends AppCompatActivity {
    private MyPlayer player;
    private String path;
    private static final String TAG = "MainActivity";
    private ImageView start, fill;
    private SurfaceView surfaceView;
    private MyRenderer renderer;
    private SurfaceHolder surfaceHolder;
    private Surface surface;
    private String vertexCode, fragCode;
    private int w, h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = this.getExternalCacheDir().getPath() + "/res/test.mp4";
        init();

        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                w = width;
                h = height;
                player.prepare(path, vertexCode, fragCode, holder.getSurface());
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        click();

        callback();

    }

    private void callback() {

        player.setPreparedCallBack((isSuccess, errorCode) -> {
            if (!isSuccess) {
                Log.e(TAG, "onPrepared: ffmpeg初始化失败,错误代码：" + errorCode);
            } else {
                Log.e(TAG, "onPrepared: 准备成功");
                player.play(w, h);
            }
        });

        player.setFinishCallBack(() -> {
            Log.e(TAG, "callback: 播放结束");
        });
    }

    private void click() {
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
//        surfaceView.setRenderer(renderer);
        player = new MyPlayer();
        surfaceHolder = surfaceView.getHolder();
        vertexCode = TextRecourseReader.readTextFileFromResource(this, R.raw.yuv_vertex_shader);
        fragCode = TextRecourseReader.readTextFileFromResource(this, R.raw.yuv_frag_shader);
    }

}
