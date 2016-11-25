package com.itant.musichome.music;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.utils.StringTool;
import com.itant.musichome.utils.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.http.cookie.DbCookieStore;
import org.xutils.x;

import java.net.HttpCookie;
import java.util.List;

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
        loginParams.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)");
        x.http().get(loginParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {

                DbCookieStore instance = DbCookieStore.INSTANCE;
                List<HttpCookie> cookieContainer = instance.getCookies();
                Constants.COOKIE_CONTAINER = cookieContainer;
                for (HttpCookie cookie : cookieContainer) {
                    String name = cookie.getName();
                    String value = cookie.getValue();
                    if ("JSESSIONID".equals(name)) {
                        // 将cookie保存下来
                        //Constants.COOKIE_XIA = value;// cookie保存到内存
                        break;
                    }
                }
                initVip(musics, keyWords);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastTools.toastShort(MusicApplication.applicationContext, "账号有误");
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
     * 初始化VIP
     */
    private void initVip(final List<Music> musics, final String keyWords) {
        RequestParams vipParams = new RequestParams("https://login.xiami.com/member/login?" + Constants.COOKIE_CONTAINER.get(0).getName() + "=" + Constants.COOKIE_CONTAINER.get(0).getValue() + "&done=http%253A%252F%252Fwww.xiami.com%252F&type=&email=iloveb44%40163.com&password=a23187&autologin=1&submit=%E7%99%BB+%E5%BD%95");
        Constants.TIME_XIA_MI = System.currentTimeMillis() / 10000;
        x.http().get(vipParams, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                getSongInfos(musics, keyWords);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastTools.toastShort(MusicApplication.applicationContext, "不是VIP");
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
     *
     * @param musics
     * @param keyWords
     */
    private void getSongInfos(final List<Music> musics, String keyWords) {
        String url = "http://www.xiami.com/web/search-songs/page/0?spm=0.0.0.0.82mhoN&key=" + keyWords + "&_xiamitoken=abchdjah6264817";
        RequestParams params = new RequestParams(url);
        params.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)");
        params.setHeader("Connection", "Keep-Alive");
        // 设置cookie
        //params.setHeader("Cookie", "JSESSIONID="+cookie);
        for (HttpCookie cookie : Constants.COOKIE_CONTAINER) {
            String name = cookie.getName();
            String value = cookie.getValue();
            params.setHeader(name, value);
        }

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
                                music.setMusicTime(info.getString("length"));// 音乐时长
                                music.setId("xia" + music.getSourceId());// 歌曲ID
                                music.setName(info.getString("songName"));// 歌名
                                music.setSinger(info.getString("singers"));// 歌手
                                music.setAlbum(info.getString("album_name"));// 专辑
                                music.setBitrate("128");// 音质
                                String format = ".mp3";
                                music.setMp3Url(info.getString("location"));// 下载地址

                                music.setFileName(music.getName() + "-" + music.getSinger() + "-" + music.getSourceId() + format);// 文件名


                                // 文件路径
                                music.setFilePath(Constants.PATH_XIA + music.getFileName());
                                musics.add(music);
                            }

                        }
                        updateHQUrl(musics);
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        ToastTools.toastShort(MusicApplication.applicationContext, "获取歌曲列表出错");
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastTools.toastShort(MusicApplication.applicationContext, "获取歌曲列表出错");
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
     * 更新龙虾高音质地址
     */
    private int i = 0;

    private void updateHQUrl(final List<Music> musics) {
        for (final Music music : musics) {
            String url = "http://www.xiami.com/song/gethqsong/sid/" + music.getSourceId();

            final RequestParams mp3Params = new RequestParams(url);
            mp3Params.setHeader("Referer", "http://img.xiami.net/static/swf/seiya/1.5/player.swf?v=" + Constants.TIME_XIA_MI);// ==========拼上playertime
            mp3Params.setHeader("Accept", "*/*");
            mp3Params.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows 7)");
            //mp3Params.setHeader("Accept-Encoding", "gzip,deflate,sdch,");
            //mp3Params.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
            //mp3Params.setHeader("Content-Type", "text/html; charset=utf-8");
            mp3Params.setHeader("Connection", "Keep-Alive");
            mp3Params.setHeader("Charset", "UTF-8");
            // 设置cookie
            //params.setHeader("Cookie", "JSESSIONID="+cookie);
            for (HttpCookie cookie : Constants.COOKIE_CONTAINER) {
                String name = cookie.getName();
                String value = cookie.getValue();
                mp3Params.setHeader(name, value);
            }

            x.http().get(mp3Params, new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        JSONObject resultObj = JSON.parseObject(result);
                        if (resultObj == null) {
                            // 结束加载动画
                            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                            return;
                        }
                        String location = resultObj.getString("location");
                        if (TextUtils.isEmpty(location)) {
                            // 结束加载动画
                            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                            return;
                        }

                        String mp3Url = StringTool.getXiaMp3Url(location);
                        if (TextUtils.isEmpty(mp3Url)) {
                            // 结束加载动画
                            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                            return;
                        }

                        music.setMp3Url(mp3Url);
                        if (mp3Url.contains("m6")) {
                            music.setBitrate("320");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有获取高音质的权限");
                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {
                    notifyIfNeed(musics.size());
                }
            });
        }
    }

    private synchronized void notifyIfNeed(int size) {
        i++;
        if (i >= size - 1) {
            // 更新列表
            EventBus.getDefault().post(Constants.EVENT_UPDATE_MUSICS);
            // 结束加载动画
            EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
            i = 0;
        }
    }
}
