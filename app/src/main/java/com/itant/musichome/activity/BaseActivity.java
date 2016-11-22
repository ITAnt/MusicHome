package com.itant.musichome.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itant.musichome.R;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Jason on 2016/11/13.
 */
public class BaseActivity extends Activity {

    private RelativeLayout rl_back;
    private TextView tv_title;

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        rl_back = (RelativeLayout) findViewById(R.id.rl_back);
        rl_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        tv_title = (TextView) findViewById(R.id.tv_title);
    }

    /**
     * 设置标题栏
     * @param title
     */
    public void setTitleBar(String title) {
        tv_title.setText(title);
        ViewGroup view = (ViewGroup) findViewById(R.id.fl_content_view);
        view.addView(View.inflate(this, getContentView(), null));
    }

    protected int getContentView() {
        return R.layout.layout_title;// 任意非空的view
    }

    public void setBackable(boolean backable) {
        if (backable) {
            rl_back.setVisibility(View.VISIBLE);
        } else {
            rl_back.setVisibility(View.GONE);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition( R.anim.slide_left_in,R.anim.slide_right_out);
    }
}
