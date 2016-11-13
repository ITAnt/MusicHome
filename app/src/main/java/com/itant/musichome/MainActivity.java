package com.itant.musichome;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.andexert.library.RippleView;
import com.itant.musichome.activity.AboutActivity;
import com.itant.musichome.activity.BaseActivity;
import com.itant.musichome.adapter.MusicAdapter;
import com.itant.musichome.bean.Music;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private ListView lv_music;
    private MusicAdapter musicAdapter;
    private List<Music> musics;

    private RippleView rv_about;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lv_music = (ListView) findViewById(R.id.lv_music);
        musics = new ArrayList<>();
        musics.add(new Music());
        musics.add(new Music());
        musics.add(new Music());
        musics.add(new Music());
        musics.add(new Music());
        musics.add(new Music());
        musics.add(new Music());
        musics.add(new Music());
        musics.add(new Music());
        musics.add(new Music());
        musicAdapter = new MusicAdapter(this, musics);
        lv_music.setAdapter(musicAdapter);

        rv_about = (RippleView) findViewById(R.id.rv_about);
        rv_about.setOnClickListener(this);
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
            case R.id.rv_about:
                // 在父类做动画===================
                startActivity(new Intent(this, AboutActivity.class));
                break;
            default:
                break;
        }
    }
}
