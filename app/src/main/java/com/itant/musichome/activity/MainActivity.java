package com.itant.musichome.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.R;
import com.itant.musichome.adapter.MusicAdapter;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.music.DogMusic;
import com.itant.musichome.music.QieMusic;
import com.itant.musichome.utils.ToastTools;
import com.wang.avi.AVLoadingIndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.common.Callback;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemClickListener {
    private static String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private ListView lv_music;
    private MusicAdapter musicAdapter;
    private List<Music> musics;

    private RelativeLayout rl_about;
    private RelativeLayout rl_task;

    private int index = 0;// 0小狗 1龙虾 2企鹅 3白云
    private String keyWords;// 搜索的关键字，一般为歌曲名
    private EditText et_key;

    private InputMethodManager inputMethodManager;
    private AVLoadingIndicatorView avliv_loading;

    /**
     * 初始化权限
     */
    private void initPermission() {
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    REQUIRED_PERMISSIONS,
                    1
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            int grantResult = grantResults[0];
            boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;

            if (!granted) {
                ToastTools.toastShort(this, "应用没有足够的权限");
                System.exit(0);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 申请6.0的权限，如果拒绝了，则退出应用
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            initPermission();
        }

        // 初始化文件夹目录
        initDirectory();

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //Constants.MUSIC_TASKS = new HashMap<>();

        et_key = (EditText) findViewById(R.id.et_key);

        lv_music = (ListView) findViewById(R.id.lv_music);
        musics = new ArrayList<>();
        musicAdapter = new MusicAdapter(this, musics);
        lv_music.setAdapter(musicAdapter);
        lv_music.setOnItemClickListener(this);

        rl_about = (RelativeLayout) findViewById(R.id.rl_about);
        rl_about.setOnClickListener(this);
        rl_task = (RelativeLayout) findViewById(R.id.rl_task);
        rl_task.setOnClickListener(this);

        RadioGroup rg_type = (RadioGroup) findViewById(R.id.rg_type);
        rg_type.setOnCheckedChangeListener(this);

        BootstrapButton bb_search = (BootstrapButton) findViewById(R.id.bb_search);
        bb_search.setOnClickListener(this);

        avliv_loading = (AVLoadingIndicatorView) findViewById(R.id.avliv_loading);


        /*RequestParams params = new RequestParams("http://music.163.com/api/search/pc");
        params.addBodyParameter("offset", "0");
        params.addBodyParameter("total", "true");
        params.addBodyParameter("limit", "50");
        params.addBodyParameter("type", "1");
        params.addBodyParameter("s", "残酷月光");
        params.addHeader("Cookie", "os=pc;MUSIC_U=5339640232");


        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(x.app(), result, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {
                Toast.makeText(x.app(), "cancelled", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFinished() {

            }
        });*/

    }

    /**
     * 初始化文件夹目录
     */
    private void initDirectory() {
        Constants.PATH_DOG = Constants.PATH_DOWNLOAD + "/dog/";
        Constants.PATH_XIA = Constants.PATH_DOWNLOAD + "/xia/";
        Constants.PATH_QIE = Constants.PATH_DOWNLOAD + "/qie/";
        Constants.PATH_YUN = Constants.PATH_DOWNLOAD + "/yun/";
        File file = new File(Constants.PATH_DOG);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(Constants.PATH_XIA);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(Constants.PATH_QIE);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(Constants.PATH_YUN);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);// true对任何Activity都适用
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_about:
                // 在父类做动画===================
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.rl_task:
                startActivity(new Intent(this, TaskActivity.class));
                break;

            case R.id.bb_search:
                // 收起软键盘并搜索
                inputMethodManager.hideSoftInputFromWindow(et_key.getWindowToken(), 0); //强制隐藏键盘
                keyWords = et_key.getText().toString();
                if (TextUtils.isEmpty(keyWords)) {
                    ToastTools.toastShort(this, "关键字不能为空");
                    return;
                }

                // 加载中
                if (musics != null) {
                    musics.clear();
                }
                musicAdapter.notifyDataSetChanged();
                avliv_loading.show();

                switch (index) {
                    case 0:
                        // 搜索小狗
                        DogMusic.getInstance().getDogSongs(musics, keyWords);
                        break;
                    case 1:
                        // 搜索龙虾
                        break;
                    case 2:
                        // 搜索企鹅
                        QieMusic.getInstance().getQieSongs(musics, keyWords);
                        break;
                    case 3:
                        // 搜索白云
                        break;
                    default:
                        // 搜索小狗
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_dog:
                index = 0;
                break;
            case R.id.rb_xia:
                index = 1;
                break;
            case R.id.rb_qie:
                index = 2;
                break;
            case R.id.rb_yun:
                index = 3;
                break;
            default:
                index = 0;
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Music music = musics.get(position);
        if (TextUtils.isEmpty(music.getMp3Url())) {
            ToastTools.toastShort(this, "没有相应的下载地址");
            return;
        }

        File localFile = new File(music.getFilePath());
        if (localFile.exists()) {
            ToastTools.toastShort(this, "该歌曲已下载完成");
            return;
        }

        try {
            Music dbMusic = MusicApplication.db.selector(Music.class).where("id", "=", music.getId()).findFirst();
            if (dbMusic != null) {
                ToastTools.toastShort(getApplicationContext(), "这首歌曲已经在下载列表中了");
                return;
            }
        } catch (Exception e) {
            ToastTools.toastShort(getApplicationContext(), "这首歌曲已经在下载列表中了");
            return;
        }

        try {
            MusicApplication.db.save(music);
        } catch (DbException e) {
            e.printStackTrace();
        }

        ToastTools.toastShort(getApplicationContext(), "已将" + music.getFileName() + "添加到下载列表");

        switch (music.getMusicType()) {
            case 0:
                // 小狗，步骤多一步，必须先获取真正的下载地址
                org.xutils.http.RequestParams params = new org.xutils.http.RequestParams(music.getMp3Url());
                params.setExecutor(Constants.EXECUTOR_MUSIC);
                params.setCancelFast(true);

                x.http().get(params, new Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        JSONObject jsonObject = JSON.parseObject(result);
                        if (jsonObject == null) {
                            return;
                        }

                        String url = jsonObject.getString("url");
                        if (!TextUtils.isEmpty(url)) {
                            music.setMp3Url(url);
                            try {
                                MusicApplication.db.save(music);
                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                            downloadMusic(music);
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {

                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
                break;

            case 1:

                break;

            case 2:
                // 企鹅，直接下
                downloadMusic(music);
                break;

            case 3:

                break;
            default:
                break;
        }
    }

    private void downloadMusic(final Music music) {
        org.xutils.http.RequestParams params = new org.xutils.http.RequestParams(music.getMp3Url());
        params.setAutoResume(true);
        params.setAutoRename(false);
        params.setSaveFilePath(music.getFilePath());
        params.setExecutor(Constants.EXECUTOR_MUSIC);
        params.setCancelFast(true);

        x.http().get(params, new Callback.ProgressCallback<File>() {

            @Override
            public void onSuccess(File result) {
                ToastTools.toastShort(getApplicationContext(), music.getFileName() + "下载成功啦");
                music.setProgress(100);
                try {
                    MusicApplication.db.update(music, "progress");
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastTools.toastShort(getApplicationContext(), "错误：" + ex.toString());
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {
                // 更新进度
                int progress = (int) (current * 100 / total);
                music.setProgress(progress);
                try {
                    MusicApplication.db.update(music, "progress");
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String event) {
        if (TextUtils.equals(event, Constants.EVENT_LOAD_COMPLETE)) {
            // 停止加载动画
            avliv_loading.hide();
        }

        if (TextUtils.equals(event, Constants.EVENT_UPDATE_MUSICS)) {
            // 刷新音乐列表
            musicAdapter.notifyDataSetChanged();
        }
    }

}
