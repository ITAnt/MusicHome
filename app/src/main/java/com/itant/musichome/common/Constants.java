package com.itant.musichome.common;

import org.xutils.common.Callback.Cancelable;
import org.xutils.common.task.PriorityExecutor;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by 詹子聪 on 2016/11/14.
 */
public class Constants {
    /**
     * 下载路径
     */
    public static String PATH_DOWNLOAD;

    /**
     * 小狗下载地址
     */
    public static String PATH_DOG;

    /**
     * 龙虾下载地址
     */
    public static String PATH_XIA;

    /**
     * 企鹅下载地址
     */
    public static String PATH_QIE;

    /**
     * 白云下载地址
     */
    public static String PATH_YUN;

    public static Executor EXECUTOR_MUSIC = new PriorityExecutor(5, true);

    //public static Map<String, Cancelable> MUSIC_TASKS;
}
