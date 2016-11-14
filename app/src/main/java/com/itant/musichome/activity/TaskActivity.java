package com.itant.musichome.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ListView;
import android.widget.TextView;

import com.itant.musichome.MusicApplication;
import com.itant.musichome.R;
import com.itant.musichome.adapter.MusicAdapter;
import com.itant.musichome.adapter.TaskAdapter;
import com.itant.musichome.bean.Music;

import org.xutils.DbManager;
import org.xutils.common.util.KeyValue;
import org.xutils.db.sqlite.WhereBuilder;
import org.xutils.db.table.DbModel;
import org.xutils.x;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Jason on 2016/11/13.
 */
public class TaskActivity extends BaseActivity {
    private TextView tv_content;
    private ListView lv_task;
    private TaskAdapter musicAdapter;
    private List<Music> musics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBar("任务");
        setBackable(true);

        lv_task = (ListView) findViewById(R.id.lv_task);
        musics = new ArrayList<>();
        musicAdapter = new TaskAdapter(this, musics);
        lv_task.setAdapter(musicAdapter);

        try {
            List<Music> dbMusics = MusicApplication.db.selector(Music.class).findAll();
            musics.addAll(dbMusics);
            musicAdapter.notifyDataSetChanged();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_task;
    }
}
