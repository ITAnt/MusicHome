package com.itant.musichome.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.R;
import com.itant.musichome.adapter.MusicAdapter;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.utils.ToastTools;
import com.wang.avi.AVLoadingIndicatorView;

import org.xutils.common.Callback;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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

                avliv_loading.show();
                switch (index) {
                    case 0:
                        // 搜索小狗
                        break;
                    case 1:
                        // 搜索龙虾
                        break;
                    case 2:
                        // 搜索企鹅
                        getQieSongs();
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

        org.xutils.http.RequestParams params = new org.xutils.http.RequestParams(music.getMp3Url());
        params.setAutoResume(true);
        params.setAutoRename(false);
        switch (index) {
            case 0:
                params.setSaveFilePath(Constants.PATH_DOG + music.getFileName());
                break;

            case 1:
                params.setSaveFilePath(Constants.PATH_XIA + music.getFileName());
                break;

            case 2:
                params.setSaveFilePath(Constants.PATH_QIE + music.getFileName());
                break;

            case 3:
                params.setSaveFilePath(Constants.PATH_YUN + music.getFileName());
                break;

            default:
                params.setSaveFilePath(Constants.PATH_DOG + music.getFileName());
                break;
        }

        params.setExecutor(Constants.EXECUTOR_MUSIC);
        params.setCancelFast(true);

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

        Callback.Cancelable cancelable = x.http().get(params, new Callback.ProgressCallback<File>() {

            @Override
            public void onSuccess(File result) {
                ToastTools.toastShort(getApplicationContext(), music.getFileName() + "下载成功啦");
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
                // 将任务放从集合中移除
                //Constants.MUSIC_TASKS.remove(music.getId());
                music.setProgress(100);
                try {
                    MusicApplication.db.update(music, "progress");
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

                //int progress = (int)(current*100/total);

            }
        });

        // 将当前任务放进集合
        //Constants.MUSIC_TASKS.put(music.getId(), cancelable);
    }
    /**
     * 获取企鹅歌曲信息
     */
    private void getQieSongs() {
        String url = "http://soso.music.qq.com/fcgi-bin/music_search_new_platform?t=0&n=20&g_tk=157256710&loginUin=584586119&hostUin=0&format=jsonp&inCharset=GB2312&outCharset=utf-8&notice=0&platform=newframe&jsonpCallback=jsnp_callback&needNewCode=0&w=" + keyWords + "&p=0&catZhida=1&remoteplace=sizer.newclient.song_all&searchid=11040987310239770213&clallback=jsnp_callback&lossless=0";
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                String json = result.substring(result.indexOf("{"), result.lastIndexOf(")"));
                if (TextUtils.isEmpty(json)) {
                    return;
                }

                JSONObject jsonObject = JSON.parseObject(json);
                if (jsonObject == null) {
                    return;
                }

                JSONObject dataObject = jsonObject.getJSONObject("data");
                if (dataObject == null) {
                    return;
                }

                JSONObject songObject = dataObject.getJSONObject("song");
                if (songObject == null) {
                    return;
                }

                String totalNum = songObject.getString("totalnum");
                if (TextUtils.equals(totalNum, "0")) {
                    return;
                }

                JSONArray listArray = songObject.getJSONArray("list");
                if (listArray == null) {
                    return;
                }

                if (musics != null) {
                    musics.clear();
                }

                for (Object obj : listArray) {
                    JSONObject object = JSON.parseObject(obj.toString());
                    if (object == null) {
                        continue;
                    }
                    String isWeiYun = object.getString("isweiyun");
                    if (TextUtils.equals(isWeiYun, "1")) {
                        continue;
                    }

                    String f = object.getString("f");
                    if (TextUtils.isEmpty(f)) {
                        continue;
                    }

                    Music music = new Music();
                    // 音乐的唯一ID
                    music.setId(String.valueOf(System.currentTimeMillis()));
                    if (f.contains("@@")) {
                        music.setBitrate("128");
                        String[] infos = f.split("@@");
                        if (infos == null) {
                            continue;
                        }
                        music.setId(infos[0].trim());// 歌曲ID
                        music.setName(infos[1].trim());// 歌名
                        String singer1 = object.getString("fsinger");
                        String singer2 = object.getString("fsinger2");
                        if (!TextUtils.isEmpty(singer2)) {
                            singer1 = singer1 + "、" + singer2;
                        }
                        music.setSinger(singer1);// 歌手
                        music.setAlbum("无");// 专辑
                        music.setMp3Url(infos[infos.length-4]);// 下载地址

                        music.setFileName(music.getName() + "-" + music.getSinger() + ".m4a");// 文件名
                    } else {
                        String[] infos = f.split("\\|");
                        if (infos == null) {
                            continue;
                        }
                        music.setId(infos[0].trim());// 歌曲ID
                        music.setName(object.getString("fsong"));// 歌名
                        String singer1 = object.getString("fsinger");
                        String singer2 = object.getString("fsinger2");
                        if (!TextUtils.isEmpty(singer2)) {
                            singer1 = singer1 + "、" + singer2;
                        }
                        music.setSinger(singer1);// 歌手
                        music.setAlbum(infos[5]);// 专辑

                        music.setBitrate("128");
                        try {
                            // 低音质下载地址
                            music.setMp3Url("http://tsmusic128.tc.qq.com/" + (Integer.parseInt(music.getId()) + 30000000) + ".mp3");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (f.contains("320000|0|")) {
                            // 高音质下载地址
                            music.setBitrate("320");
                            music.setMp3Url("http://vsrc.music.tc.qq.com/M800" + infos[infos.length-5] + ".mp3");

                            if (!f.contains("320000|0|0|")) {
                                // 无损音质下载地址
                                music.setBitrate("无损");
                                music.setMp3Url("http://vsrc.music.tc.qq.com/F000" + infos[infos.length-5] + ".flac");
                            }
                        }

                        String suffix = music.getMp3Url().substring(music.getMp3Url().lastIndexOf("."), music.getMp3Url().length());
                        music.setFileName(music.getName() + "-" + music.getSinger() + suffix);// 文件名

                        // 音乐相册
                        try {
                            music.setImageUrl("http://imgcache.qq.com/music/photo/album/" + (Integer.parseInt(infos[4]) % 100) + "/albumpic_" + infos[4] + "_0.jpg");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    musics.add(music);
                }

                musicAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                avliv_loading.hide();
            }
        });
    }


}
