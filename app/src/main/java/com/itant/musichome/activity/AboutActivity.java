package com.itant.musichome.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.itant.musichome.R;
import com.itant.musichome.common.Constants;
import com.itant.musichome.utils.ToastTools;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by Jason on 2016/11/13.
 */
public class AboutActivity extends BaseActivity implements View.OnClickListener {
    private ClipboardManager clipboardManager;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_qq_qun:
                ClipData clipData = ClipData.newPlainText("qq_group", "484111083");
                //clipboardManager.setText("484111083");
                clipboardManager.setPrimaryClip(clipData);
                ToastTools.toastShort(AboutActivity.this, "已将QQ群号复制到剪贴板");
                MobclickAgent.onEvent(this, "QQ");// 统计QQ群复制次数
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBar("关于");
        setBackable(true);

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        TextView tv_qq_qun = (TextView) findViewById(R.id.tv_qq_qun);
        tv_qq_qun.setOnClickListener(this);

        TextView tv_version = (TextView) findViewById(R.id.tv_version);
        try{
            tv_version.setText(getApplicationInfo().loadLabel(getPackageManager()) + " v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        }catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ImageView iv_money = (ImageView) findViewById(R.id.iv_money);
        iv_money.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                File file = new File(Constants.PATH_DOWNLOAD+"money.png");
                if (file.exists()) {
                    file.delete();
                }

                try {
                    Bitmap tmpBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.money);
                    saveMyBitmap("money.png", tmpBitmap);
                    insertIntoAlbum("money.png");
                    ToastTools.toastShort(getApplicationContext(), "图片保存成功");
                    MobclickAgent.onEvent(AboutActivity.this, "Money");// 统计保存图片次数
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

    }

    /**
     * 保存图片到SD卡
     */
    public void saveMyBitmap(String fileName, Bitmap mBitmap) throws IOException {
        File tmp = new File(Constants.PATH_DOWNLOAD);
        if (!tmp.exists()) {
            tmp.mkdir();
        }
        File f = new File(Constants.PATH_DOWNLOAD+fileName);
        f.createNewFile();
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void insertIntoAlbum(String picName) {
        // 插入到相册
        try {
            MediaStore.Images.Media.insertImage(getContentResolver(), Constants.PATH_DOWNLOAD, picName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 通知相册更新
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(Constants.PATH_DOWNLOAD+picName))));
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_about;
    }
}
