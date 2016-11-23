package com.itant.musichome.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.R;
import com.itant.musichome.adapter.MusicAdapter;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.music.DogMusic;
import com.itant.musichome.music.KmeMusic;
import com.itant.musichome.music.QieMusic;
import com.itant.musichome.music.XiaMusic;
import com.itant.musichome.music.XiongMusic;
import com.itant.musichome.music.YunMusic;
import com.itant.musichome.utils.ActivityTool;
import com.itant.musichome.utils.ToastTools;
import com.umeng.analytics.MobclickAgent;

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

    private int index = 0;// 0小狗 1凉窝 2企鹅 3白云 4熊掌 5
    private String keyWords;// 搜索的关键字，一般为歌曲名
    private EditText et_key;

    private InputMethodManager inputMethodManager;
    private AlertDialog loadingDialog;

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
                MobclickAgent.onEvent(MainActivity.this, "Permission");// 统计权限拒绝次数

                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
                dialog.show();
                dialog.setContentView(R.layout.dialog_permission);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);

                BootstrapButton bb_confirm = (BootstrapButton) dialog.findViewById(R.id.bb_confirm);
                bb_confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        // 退出
                        System.exit(0);
                    }
                });
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
        // 不能输入空格
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                //返回null表示接收输入的字符,返回空字符串表示不接受输入的字符
                /*if (TextUtils.equals(source, " ")) {
                    return "";
                }*/

                if (TextUtils.equals(source, "  ")) {
                    return "";
                }
                return null;
            }
        };
        et_key.setFilters(new InputFilter[]{filter});
        // 点击回车则搜索(onSearchClicked方法有隐藏键盘)
        et_key.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // 搜索
                    onSearchClicked();
                }
                return false;
            }
        });

        lv_music = (ListView) findViewById(R.id.lv_music);
        musics = new ArrayList<>();
        musicAdapter = new MusicAdapter(this, musics);
        lv_music.setAdapter(musicAdapter);
        lv_music.setOnItemClickListener(this);
        musicAdapter.setOnDownloadClickListener(new MusicAdapter.OnDownloadClickListener() {
            @Override
            public void onIconClick(int position) {
                onDownloadClick(musics.get(position));
            }
        });

        rl_about = (RelativeLayout) findViewById(R.id.rl_about);
        rl_about.setOnClickListener(this);
        rl_task = (RelativeLayout) findViewById(R.id.rl_task);
        rl_task.setOnClickListener(this);

        RadioGroup rg_type = (RadioGroup) findViewById(R.id.rg_type);
        rg_type.setOnCheckedChangeListener(this);

        BootstrapButton bb_search = (BootstrapButton) findViewById(R.id.bb_search);
        bb_search.setOnClickListener(this);

        initDialog();
    }

    private void initDialog() {
        loadingDialog = new AlertDialog.Builder(this).create();
        loadingDialog.show();
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(true);
        loadingDialog.setCanceledOnTouchOutside(true);
        loadingDialog.cancel();
    }

    /**
     * 初始化文件夹目录
     */
    private void initDirectory() {
        Constants.PATH_DOG = Constants.PATH_DOWNLOAD + "dog/";
        Constants.PATH_KWO = Constants.PATH_DOWNLOAD + "lwo/";
        Constants.PATH_QIE = Constants.PATH_DOWNLOAD + "qie/";
        Constants.PATH_YUN = Constants.PATH_DOWNLOAD + "yun/";
        Constants.PATH_XIONG = Constants.PATH_DOWNLOAD + "xiong/";
        Constants.PATH_XIA = Constants.PATH_DOWNLOAD + "xia/";

        File file = new File(Constants.PATH_DOG);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(Constants.PATH_KWO);
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

        file = new File(Constants.PATH_XIONG);
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(Constants.PATH_XIA);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);// true对任何Activity都适用
        /*Intent mHomeIntent = new Intent(Intent.ACTION_MAIN, null);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(mHomeIntent);*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_about:
                // 在父类做动画
                MobclickAgent.onEvent(this, "About");// 统计关于
                ActivityTool.startActivity(this, new Intent(this, AboutActivity.class));
                break;

            case R.id.rl_task:
                MobclickAgent.onEvent(this, "Task");// 查看下载列表
                ActivityTool.startActivity(this, new Intent(this, TaskActivity.class));
                break;

            case R.id.bb_search:
                onSearchClicked();
                break;
            default:
                break;
        }
    }

    private void onSearchClicked() {
        MobclickAgent.onEvent(this, "Search");// 统计搜索次数
        // 收起软键盘并搜索
        inputMethodManager.hideSoftInputFromWindow(et_key.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); //强制隐藏键盘
        keyWords = et_key.getText().toString().replaceAll(" ", "");
        if (TextUtils.isEmpty(keyWords)) {
            ToastTools.toastShort(this, "关键字不能为空");
            return;
        }

        // 加载中
        if (musics != null) {
            musics.clear();
        }
        musicAdapter.notifyDataSetChanged();
        loadingDialog.show();

        try {
            switch (index) {
                case 0:
                    // 搜索小狗
                    MobclickAgent.onEvent(this, "Dog");// 搜索小狗
                    DogMusic.getInstance().getDogSongs(musics, keyWords);
                    break;
                case 1:
                    // 搜索凉我
                    MobclickAgent.onEvent(this, "Kwo");// 统计凉窝
                    KmeMusic.getInstance().getDogSongs(musics, keyWords);
                    break;
                case 2:
                    // 搜索企鹅
                    MobclickAgent.onEvent(this, "Qie");// 统计企鹅
                    QieMusic.getInstance().getQieSongs(musics, keyWords);
                    break;
                case 3:
                    // 搜索白云
                    MobclickAgent.onEvent(this, "Yun");// 统计白云
                    YunMusic.getInstance().getYunSongs(musics, keyWords);
                    break;
                case 4:
                    // 搜索熊掌
                    MobclickAgent.onEvent(this, "Xiong");// 统计熊掌
                    XiongMusic.getInstance().getXiongSongs(musics, keyWords);
                    break;

                case 5:
                    // 搜索龙虾
                    MobclickAgent.onEvent(this, "Xia");// 统计龙虾
                    XiaMusic.getInstance().getXiaSongs(musics, keyWords);
                    break;
                default:
                    // 搜索小狗
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ToastTools.toastShort(MainActivity.this, "歌曲有误");
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_dog:
                index = 0;
                break;
            case R.id.rb_lwo:
                index = 1;
                break;
            case R.id.rb_qie:
                index = 2;
                break;
            case R.id.rb_yun:
                index = 3;
                break;

            case R.id.rb_xiong:
                index = 4;
                break;

            case R.id.rb_xia:
                index = 5;
                break;
            default:
                index = 0;
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MobclickAgent.onEvent(this, "Download");// 统计下载次数
        Music music = musics.get(position);
        onDownloadClick(music);
    }

    private void onDownloadClick(final Music music) {
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

        ToastTools.toastShort(getApplicationContext(), "下载" + music.getName());
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
                            ToastTools.toastShort(MainActivity.this, "没有相应的下载地址");
                            return;
                        }

                        String url = jsonObject.getString("url");
                        if (!TextUtils.isEmpty(url)) {
                            music.setMp3Url(url);
                            try {
                                MusicApplication.db.update(music, "mp3Url");
                            } catch (DbException e) {
                                e.printStackTrace();
                            }
                            downloadMusic(music);
                        }
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        ToastTools.toastShort(MainActivity.this, "这首歌不能下载了");
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
                // 凉窝
                // 需要多一步，获取真正的mp3地址
                org.xutils.http.RequestParams kmeParams = new org.xutils.http.RequestParams(music.getMp3Url());
                kmeParams.setExecutor(Constants.EXECUTOR_MUSIC);
                kmeParams.setCancelFast(true);

                x.http().get(kmeParams, new Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        music.setMp3Url(result.trim().replaceAll(" ", ""));
                        try {
                            MusicApplication.db.update(music, "mp3Url");
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        downloadMusic(music);
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        ToastTools.toastShort(MainActivity.this, "这首歌不能下载了");
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
                break;

            case 2:
                // 企鹅，直接下
                downloadMusic(music);
                break;

            case 3:
                // 白云，直接下
                downloadMusic(music);
                break;

            case 4:
                // 熊掌，直接下
                downloadMusic(music);
                break;

            case 5:
                // 龙虾，直接下
                downloadMusic(music);
                break;
            default:
                break;
        }
    }

    /**
     * 下载音乐
     *
     * @param music
     */
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
                ToastTools.toastShort(getApplicationContext(), music.getName() + "下载成功");
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
            loadingDialog.dismiss();
        }

        if (TextUtils.equals(event, Constants.EVENT_UPDATE_MUSICS)) {
            // 刷新音乐列表
            musicAdapter.notifyDataSetChanged();
        }
    }

}
