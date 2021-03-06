package com.itant.musichome;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.itant.musichome.common.Constants;

import org.xutils.DbManager;
import org.xutils.x;

/**
 * Created by Jason on 2016/11/13.
 */
public class MusicApplication extends Application {

    public static DbManager db;
    public static Context applicationContext;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();

        x.Ext.init(this);
        x.Ext.setDebug(BuildConfig.DEBUG); // 是否输出debug日志, 开启debug会影响性能.

        Constants.PATH_DOWNLOAD = Environment.getExternalStorageDirectory() + "/MusicHome/";


        DbManager.DaoConfig daoConfig = new DbManager.DaoConfig()
                .setDbName("music.db")
                // 不设置dbDir时, 默认存储在app的私有目录.
                //.setDbDir(new File("/sdcard")) // "sdcard"的写法并非最佳实践, 这里为了简单, 先这样写了.
                .setDbVersion(2)
                .setDbOpenListener(new DbManager.DbOpenListener() {
                    @Override
                    public void onDbOpened(DbManager db) {
                        // 开启WAL, 对写入加速提升巨大
                        db.getDatabase().enableWriteAheadLogging();
                    }
                })
                .setDbUpgradeListener(new DbManager.DbUpgradeListener() {
                    @Override
                    public void onUpgrade(DbManager db, int oldVersion, int newVersion) {
                        // TODO: ...
                        // db.addColumn(...);
                        // db.dropTable(...);
                        // ...
                        // or
                        // db.dropDb();
                    }
                });
        db = x.getDb(daoConfig);

    }
}
