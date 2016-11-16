package com.itant.musichome.music;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

/**
 * Created by 詹子聪 on 2016/11/15.
 * 企鹅音乐
 */
public class QieMusic {

    private QieMusic() {}

    private static class QieMusicFactory {
        private static QieMusic instance = new QieMusic();
    }

    public static QieMusic getInstance() {
        return QieMusicFactory.instance;
    }

    /**
     * 获取企鹅歌曲信息
     */
    public void getQieSongs(final List<Music> musics, String keyWords) {
        String url = "http://soso.music.qq.com/fcgi-bin/music_search_new_platform?t=0&n=20&g_tk=157256710&loginUin=584586119&hostUin=0&format=jsonp&inCharset=GB2312&outCharset=utf-8&notice=0&platform=newframe&jsonpCallback=jsnp_callback&needNewCode=0&w=" + keyWords + "&p=0&catZhida=1&remoteplace=sizer.newclient.song_all&searchid=11040987310239770213&clallback=jsnp_callback&lossless=0";
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (TextUtils.isEmpty(result)) {
                    return;
                }

                String json = result.substring(result.indexOf("{"), result.lastIndexOf(")"));
                if (TextUtils.isEmpty(json)) {
                    return;
                }

                JSONObject jsonObject = JSON.parseObject(json);
                if (jsonObject == null) {
                    return;
                }

                JSONObject dataObject = jsonObject.getJSONObject("data");
                if (dataObject == null) {
                    return;
                }

                JSONObject songObject = dataObject.getJSONObject("song");
                if (songObject == null) {
                    return;
                }

                String totalNum = songObject.getString("totalnum");
                if (TextUtils.equals(totalNum, "0")) {
                    return;
                }

                JSONArray listArray = songObject.getJSONArray("list");
                if (listArray == null) {
                    return;
                }

                for (Object obj : listArray) {
                    JSONObject object = JSON.parseObject(obj.toString());
                    if (object == null) {
                        continue;
                    }
                    String isWeiYun = object.getString("isweiyun");
                    if (TextUtils.equals(isWeiYun, "1")) {
                        continue;
                    }

                    String f = object.getString("f");
                    if (TextUtils.isEmpty(f)) {
                        continue;
                    }

                    Music music = new Music();// 音乐来源
                    music.setMusicType(2);
                    // 音乐的唯一ID
                    music.setId(String.valueOf(System.currentTimeMillis()));
                    if (f.contains("@@")) {
                        music.setBitrate("128");
                        String[] infos = f.split("@@");
                        if (infos == null) {
                            continue;
                        }
                        music.setId(infos[0].trim());// 歌曲ID
                        music.setName(infos[1].trim());// 歌名
                        String singer1 = object.getString("fsinger");
                        String singer2 = object.getString("fsinger2");
                        if (!TextUtils.isEmpty(singer2)) {
                            singer1 = singer1 + "、" + singer2;
                        }
                        music.setSinger(singer1);// 歌手
                        music.setAlbum("");// 专辑
                        music.setMp3Url(infos[infos.length-4]);// 下载地址

                        music.setFileName(music.getName() + "-" + music.getSinger() + ".m4a");// 文件名
                    } else {
                        String[] infos = f.split("\\|");
                        if (infos == null) {
                            continue;
                        }
                        music.setId(infos[0].trim());// 歌曲ID
                        music.setName(object.getString("fsong"));// 歌名
                        String singer1 = object.getString("fsinger");
                        String singer2 = object.getString("fsinger2");
                        if (!TextUtils.isEmpty(singer2)) {
                            singer1 = singer1 + "、" + singer2;
                        }
                        music.setSinger(singer1);// 歌手
                        music.setAlbum(infos[5]);// 专辑

                        music.setBitrate("128");
                        try {
                            // 低音质下载地址
                            music.setMp3Url("http://tsmusic128.tc.qq.com/" + (Integer.parseInt(music.getId()) + 30000000) + ".mp3");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (f.contains("320000|0|")) {
                            // 高音质下载地址
                            music.setBitrate("320");
                            music.setMp3Url("http://vsrc.music.tc.qq.com/M800" + infos[infos.length-5] + ".mp3");

                            if (!f.contains("320000|0|0|")) {
                                // 无损音质下载地址
                                music.setBitrate("无损");
                                music.setMp3Url("http://vsrc.music.tc.qq.com/F000" + infos[infos.length-5] + ".flac");
                            }
                        }

                        String suffix = music.getMp3Url().substring(music.getMp3Url().lastIndexOf("."), music.getMp3Url().length());
                        music.setFileName(music.getName() + "-" + music.getSinger() + "-" + music.getId() + suffix);// 文件名

                        // 音乐相册
                        try {
                            music.setImageUrl("http://imgcache.qq.com/music/photo/album/" + (Integer.parseInt(infos[4]) % 100) + "/albumpic_" + infos[4] + "_0.jpg");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    music.setFilePath(Constants.PATH_QIE + music.getFileName());
                    musics.add(music);
                }

                // 更新搜索结果
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
}
