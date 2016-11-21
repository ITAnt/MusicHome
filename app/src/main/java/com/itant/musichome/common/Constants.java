package com.itant.musichome.common;

import org.xutils.common.task.PriorityExecutor;

import java.net.HttpCookie;
import java.util.List;
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
     * 凉窝下载地址
     */
    public static String PATH_KWO;

    /**
     * 企鹅下载地址
     */
    public static String PATH_QIE;

    /**
     * 白云下载地址
     */
    public static String PATH_YUN;

    /**
     * 熊掌下载地址
     */
    public static String PATH_XIONG;

    /**
     * 龙虾下载地址
     */
    public static String PATH_XIA;

    public static Executor EXECUTOR_MUSIC = new PriorityExecutor(5, true);

    //public static Map<String, Cancelable> MUSIC_TASKS;

    /**
     * 停止加载动画
     */
    public static final String EVENT_LOAD_COMPLETE = "load_finish";

    /**
     *  刷新搜索界面的音乐列表
     */
    public static final String EVENT_UPDATE_MUSICS = "update_music";


    public static List<HttpCookie> COOKIE_CONTAINER;
    public static long TIME_XIA_MI;
}
