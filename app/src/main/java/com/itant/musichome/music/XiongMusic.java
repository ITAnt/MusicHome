package com.itant.musichome.music;

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
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.msebera.android.httpclient.Header;

/**
 * Created by 詹子聪 on 2016/11/15.
 * 熊掌音乐
 */
public class XiongMusic {

    private XiongMusic() {
    }

    private static class DogMusicFactory {
        private static XiongMusic instance = new XiongMusic();
    }

    public static XiongMusic getInstance() {
        return DogMusicFactory.instance;
    }

    private int index = 0;

    /**
     * 获取熊掌歌曲信息
     */
    public void getXiongSongs(final List<Music> musics, String keyWords) throws Exception {
        String url = "http://music.baidu.com/search/song?s=1&key=" + keyWords + "&start=0&size=20";

        AsyncHttpClient client = new AsyncHttpClient();
        client.setUserAgent("Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)");
        client.get(url, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String html) {
                //ToastTools.toastShort(MusicApplication.applicationContext, responseString);
                if (TextUtils.isEmpty(html)) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }


                // 这里获得了html代码
                String advanceResult = html.replaceAll("&quot;", "\"");
                String regex = "\"sid\":\\d+";
                //String regex = "data-sid=\"\\d+";


                Matcher matcher = null;
                try {
                    Pattern pattern = Pattern.compile(regex);
                    matcher = pattern.matcher(advanceResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (matcher == null) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                final List<String> ids = new ArrayList<>();
                while (matcher.find()) {
                    String[] raw = matcher.group().split(":");
                    if (raw != null && raw.length == 2) {
                        ids.add(raw[1]);
                    }
                }

                if (ids.size() <= 0) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                for (String id : ids) {
                    String infoUrl = "http://music.baidu.com/data/music/fmlink?songIds=" + id + "&type=mp3&rate=320";
                    RequestParams params = new RequestParams(infoUrl);
                    x.http().get(params, new Callback.CommonCallback<String>() {

                        @Override
                        public void onSuccess(String result) {
                            JSONObject jsonObject = JSON.parseObject(result);
                            if (jsonObject == null) {
                                // 结束加载动画
                                EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                                ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                                return;
                            }

                            int errorCode = jsonObject.getIntValue("errorCode");
                            if (errorCode != 22000) {
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

                            JSONArray listArray = dataObject.getJSONArray("songList");
                            if (listArray == null) {
                                // 结束加载动画
                                EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                                ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                                return;
                            }

                            if (listArray.size() >= 1) {
                                JSONObject info = listArray.getJSONObject(0);
                                if (info == null) {
                                    // 结束加载动画
                                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                                    return;
                                }

                                Music music = new Music();
                                music.setMusicType(4);// 音乐来源
                                music.setSourceId(info.getString("queryId"));// 歌曲ID
                                music.setId("xiong" + music.getSourceId());// 歌曲ID
                                music.setName(info.getString("songName"));// 歌名
                                music.setSinger(info.getString("artistName"));// 歌手
                                music.setAlbum(info.getString("albumName"));// 专辑


                                music.setBitrate(info.getString("rate"));// 音质
                                String format = info.getString("format");
                                music.setFileName(music.getName() + "-" + music.getSinger() + "-" + music.getSourceId() + "." +format);// 文件名


                                music.setMp3Url(info.getString("songLink"));// 下载地址

                                // 文件路径
                                music.setFilePath(Constants.PATH_XIONG + music.getFileName());
                                musics.add(music);
                            }
                        }

                        @Override
                        public void onError(Throwable ex, boolean isOnCallback) {
                            ToastTools.toastShort(MusicApplication.applicationContext, "未知错误");
                        }

                        @Override
                        public void onCancelled(CancelledException cex) {

                        }

                        @Override
                        public void onFinished() {
                            notifyIfNeed(ids.size());
                        }
                    });
                }
            }
        });
    }

    private synchronized void notifyIfNeed(int size) {
        index++;
        if (index >= size-1) {
            // 结束加载动画
            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
            // 更新搜索结果
            EventBus.getDefault().post(Constants.EVENT_UPDATE_MUSICS);
            index = 0;
        }
    }
}
