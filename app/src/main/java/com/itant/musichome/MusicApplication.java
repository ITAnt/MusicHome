package com.itant.musichome;

import android.app.Application;

import org.xutils.x;

/**
 * Created by Jason on 2016/11/13.
 */
public class MusicApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.
    }
}
