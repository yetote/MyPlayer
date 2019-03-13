package com.example.myplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.myplayer.player.MyPlayer;

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
        player.prepare(path);
    }

}
