package com.example.myplayer;

import android.app.Application;

import com.didichuxing.doraemonkit.DoraemonKit;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author ether QQ:503779938
 * @name MyPlayer
 * @class name：com.example.myplayer
 * @class describe
 * @time 2019/5/7 18:08
 * @change
 * @chang time
 * @class describe
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DoraemonKit.install(this);
        CrashReport.initCrashReport(this,"f00addfb0f",false);
    }
}
