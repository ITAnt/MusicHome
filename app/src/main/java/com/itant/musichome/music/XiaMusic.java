package com.itant.musichome.music;

import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.utils.ToastTools;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.cookie.DbCookieStore;
import org.xutils.x;

import java.net.HttpCookie;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

/**
 * Created by 詹子聪 on 2016/11/15.
 * 龙虾音乐
 */
public class XiaMusic {

    private XiaMusic() {
    }

    private static class DogMusicFactory {
        private static XiaMusic instance = new XiaMusic();
    }

    public static XiaMusic getInstance() {
        return DogMusicFactory.instance;
    }

    /**
     * 获取龙虾歌曲信息
     */
    public void getXiaSongs(final List<Music> musics, final String keyWords) throws Exception {

        //return _xmRequest.CookieContainer.GetCookieHeader(_xmRequest.RequestUri).Split(separator);
        RequestParams loginParams = new RequestParams("https://login.xiami.com/member/login");
        loginParams.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;*/*");
        loginParams.setHeader("Referer", "http://www.xiami.com");
        loginParams.setHeader("Connection", "Keep-Alive");
        loginParams.setHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)");
        x.http().get(loginParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                //DbCookieStore instance = DbCookieStore.INSTANCE;
                //List<HttpCookie> cookieContainer = instance.getCookies();
                //Constants.COOKIE_CONTAINER = cookieContainer;
                /*String cookieStr = "";
                for (HttpCookie cookie : cookieContainer) {
                    String name = cookie.getName();
                    String value = cookie.getValue();
                    if ("JSESSIONID".equals(name)) {
                        // 将cookie保存下来
                        Constants.COOKIE_XIA = value;// cookie保存到内存
                        cookieStr = value;
                        break;
                    }
                }*/

                getSongInfos(musics, keyWords);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 获取歌曲信息
     * @param musics
     * @param keyWords
     */
    private void getSongInfos(final List<Music> musics, String keyWords) {
        String url = "http://www.xiami.com/web/search-songs/page/0?spm=0.0.0.0.82mhoN&key=" + keyWords + "&_xiamitoken=abchdjah6264817";
        RequestParams params = new RequestParams(url);
        params.setHeader("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0))");
        params.setHeader("Connection", "Keep-Alive");
        // 设置cookie
        //params.setHeader("Cookie", "JSESSIONID="+cookie);
//        for (HttpCookie cookie : cookieContainer) {
//            String name = cookie.getName();
//            String value = cookie.getValue();
//            params.setHeader(name, value);
//        }

        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String html) {
                //ToastTools.toastShort(MusicApplication.applicationContext, responseString);
                if (TextUtils.isEmpty(html)) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }


                // 这里获得了html代码
                JSONArray listArray = JSON.parseArray(html);
                if (listArray == null || listArray.size() == 0) {
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                StringBuilder builder = new StringBuilder();
                for (Object obj : listArray) {
                    JSONObject idObj = JSON.parseObject(obj.toString());
                    if (idObj == null) {
                        EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                        ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                        continue;
                    }

                    builder.append(idObj.getString("id"));
                    builder.append(",");
                }

                String songListUrl = "http://www.xiami.com/song/playlist/id/" + builder.toString() + "/type/0/cat/json";
                RequestParams params = new RequestParams(songListUrl);
                x.http().get(params, new Callback.CommonCallback<String>() {

                    @Override
                    public void onSuccess(String result) {
                        if (TextUtils.isEmpty(result)) {
                            // 结束加载动画
                            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                            ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                            return;
                        }

                        JSONObject jsonObject = JSON.parseObject(result);
                        if (jsonObject == null) {
                            // 结束加载动画
                            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                            ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                            return;
                        }

                        boolean status = jsonObject.getBoolean("status");
                        if (!status) {
                            // 结束加载动画
                            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                            ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                            return;
                        }

                        JSONObject dataObject = jsonObject.getJSONObject("data");
                        if (dataObject == null) {
                            // 结束加载动画
                            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                            ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                            return;
                        }

                        JSONArray listArray = dataObject.getJSONArray("trackList");
                        if (listArray == null) {
                            // 结束加载动画
                            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                            ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                            return;
                        }

                        if (listArray.size() >= 1) {

                            for (Object obj : listArray) {
                                JSONObject info = JSON.parseObject(obj.toString());
                                if (info == null) {
                                    // 结束加载动画
                                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                                    return;
                                }

                                Music music = new Music();
                                music.setMusicType(5);// 音乐来源
                                music.setSourceId(info.getString("songId"));// 歌曲最原始的ID
                                music.setId("xia" + info.getString("songId"));// 歌曲ID
                                music.setName(info.getString("songName"));// 歌名
                                music.setSinger(info.getString("singers"));// 歌手
                                music.setAlbum(info.getString("album_name"));// 专辑
                                music.setBitrate("128");// 音质
                                String format = "mp3";
                                music.setMp3Url(info.getString("location"));// 下载地址

                                music.setFileName(music.getName() + "-" + music.getSinger() + "-" + music.getId() + format);// 文件名


                                // 文件路径
                                music.setFilePath(Constants.PATH_XIA + music.getFileName());
                                musics.add(music);
                            }

                        }

                        EventBus.getDefault().post(Constants.EVENT_UPDATE_MUSICS);
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {

                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {
                        // 结束加载动画
                        EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    }
                });
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    /**
     * 获取下载地址
     * @param raw
     * @return
     */
    public String getMP3Url(String raw) {
        String url = "";
        try {
            int num = Integer.parseInt(raw.substring(0, 1));
            String str = raw.substring(1);
            int num2 = str.length() % num;
            int length = (int) Math.ceil((double) (((double) str.length()) / ((double) num)));
            String[] strArray = new String[num];

            int startIndex = 0;
            for (int i = 0; i < num; i++) {
                if (i < num2) {
                    strArray[i] = str.substring(startIndex, length);
                    startIndex += length;
                } else if (num2 == 0) {
                    strArray[i] = str.substring(startIndex, length);
                    startIndex += length;
                } else {
                    strArray[i] = str.substring(startIndex, length - 1);
                    startIndex += length - 1;
                }
            }


            StringBuilder builder = new StringBuilder();
            if (num2 == 0) {
                for (int j = 0; j < length; j++) {
                    for (int k = 0; k < num; k++) {
                        builder.append(strArray[k].substring(j, 1));
                    }
                }
            } else {
                for (int m = 0; m < length; m++) {
                    if (m == (length - 1)) {
                        for (int n = 0; n < num2; n++) {
                            builder.append(strArray[n].substring(m, 1));
                        }
                    } else {
                        for (int num10 = 0; num10 < num; num10++) {
                            builder.append(strArray[num10].substring(m, 1));
                        }
                    }
                }
            }

            String input = URLDecoder.decode(builder.toString());

            if (input != null) {
                String one = input.replaceAll("^", "0");
                String two = one.replaceAll("\\+", " ");
                return two.replaceAll(".mp$", ".mp3");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
