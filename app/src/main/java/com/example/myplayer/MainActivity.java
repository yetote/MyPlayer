package com.example.myplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.myplayer.player.MyPlayer;
import com.example.myplayer.player.listener.OnPreparedCallBack;

public class MainActivity extends AppCompatActivity {
    MyPlayer player;
    String path;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        path = this.getExternalCacheDir().getPath() + "/res/test.mp4";
        player = new MyPlayer();

        player.setPreparedCallBack(new OnPreparedCallBack() {
            @Override
            public void onPrepared(boolean isSuccess, int errorCode) {
                if (!isSuccess) {
                    Log.e(TAG, "onPrepared: ffmpeg初始化失败,错误代码：" + errorCode);
                } else {
                    Log.e(TAG, "onPrepared: 准备成功");
                }
            }
        });
        player.prepare(path);
    }

}
