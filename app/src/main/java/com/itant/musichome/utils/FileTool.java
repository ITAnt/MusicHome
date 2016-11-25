package com.itant.musichome.utils;

import com.itant.musichome.common.Constants;

import java.io.File;

/**
 * Created by 詹子聪 on 2016/11/25.
 */
public class FileTool {
    public static String getUniqueFileName(String father, String fileName, int num) {
        File file = new File(father + fileName);
        if (file != null && file.exists()) {
            int splitIndex = fileName.lastIndexOf(".");
            String pre = fileName.substring(0, splitIndex);
            String suf = fileName.substring(splitIndex, fileName.length());
            return getUniqueFileName(father, pre + "(" + num + ")" + suf, (num+1));
        } else {
            return fileName;
        }
    }
}
