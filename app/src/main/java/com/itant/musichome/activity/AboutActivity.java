package com.itant.musichome.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.itant.musichome.R;

/**
 * Created by Jason on 2016/11/13.
 */
public class AboutActivity extends BaseActivity {
    private TextView tv_content;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBar("关于");
        setBackable(true);


    }

    @Override
    protected int getContentView() {
        return R.layout.activity_about;
    }
}
