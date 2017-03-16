package com.itant.musichome.activity;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.itant.musichome.R;
import com.itant.musichome.common.Constants;
import com.itant.musichome.utils.ToastTool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by Jason on 2016/11/13.
 */
public class WelcomeActivity extends Activity implements View.OnClickListener {
    private TextView tv_words;
    private String[] words = {"时光清浅，愿岁月待你温柔如初。", "愿有岁月可回首，且以深情共白头。", "在有生的瞬间能遇到你，竟花光所有运气。", "白驹过隙，惟愿音乐常伴你。"};

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_logo:
                ToastTool.toastShort(this, "哈哈");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        /*Random random = new Random();
        int index = random.nextInt(4);
        tv_words = (TextView) findViewById(R.id.tv_words);
        tv_words.setText(words[index]);*/

        ImageView iv_logo = (ImageView) findViewById(R.id.iv_logo);
        iv_logo.setOnClickListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.slide_setp_in, R.anim.slide_step_out);
                WelcomeActivity.this.finish();
            }
        }, 3500);
    }
}
