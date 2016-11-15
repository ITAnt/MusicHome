package com.itant.musichome.activity;

import android.content.pm.PackageManager;
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

        TextView tv_version = (TextView) findViewById(R.id.tv_version);
        try{
            tv_version.setText(getApplicationInfo().loadLabel(getPackageManager()) + " v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_about;
    }
}
