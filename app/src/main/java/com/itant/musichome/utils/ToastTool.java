package com.itant.musichome.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by 詹子聪 on 2016/11/14.
 */
public class ToastTool {

    public static void toastShort(Context context, String content) {
        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
    }
}
