package com.itant.musichome.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.itant.musichome.R;

/**
 * Created by 詹子聪 on 2016/11/22.
 */
public class ActivityTool {
    public static void startActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
    }
}
