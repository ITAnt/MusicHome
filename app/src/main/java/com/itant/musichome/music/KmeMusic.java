package com.itant.musichome.music;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.activity.MainActivity;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.utils.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

/**
 * Created by 詹子聪 on 2016/11/15.
 * 凉窝音乐
 */
public class KmeMusic {

    private KmeMusic() {}

    private static class DogMusicFactory {
        private static KmeMusic instance = new KmeMusic();
    }

    public static KmeMusic getInstance() {
        return DogMusicFactory.instance;
    }

    /**
     * 获取凉窝歌曲信息
     */
    public void getDogSongs(final List<Music> musics, String keyWords) throws Exception {
        String url = "http://search.kuwo.cn/r.s?all=" + keyWords + "&ft=music&itemset=web_2013&client=kt&pn=" + "0" + "&rn=20&rformat=json&encoding=utf8";
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String rawResult) {
                if (TextUtils.isEmpty(rawResult)) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                String result = rawResult.replaceAll("'", "\"");

                JSONObject jsonObject = JSON.parseObject(result);
                if (jsonObject == null) {
                    return;
                }

                String total = jsonObject.getString("TOTAL");
                if (TextUtils.isEmpty(total) || Integer.parseInt(total) <= 0) {

                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                JSONArray listArray = jsonObject.getJSONArray("abslist");
                if (listArray == null) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                for (Object obj : listArray) {
                    JSONObject object = JSON.parseObject(obj.toString());
                    if (object == null) {
                        continue;
                    }


                    Music music = new Music();
                    music.setMusicType(1);// 音乐来源
                    music.setSinger("未知");// 歌手
                    String songId = object.getString("MUSICRID");
                    music.setSourceId(songId);// 歌曲ID
                    music.setId("kwo" + songId);// 歌曲ID
                    music.setName(object.getString("SONGNAME"));// 歌名
                    music.setSinger(object.getString("ARTIST"));// 歌手
                    music.setAlbum(object.getString("ALBUM"));// 专辑

                    String formats = object.getString("FORMATS");
                    String bitrate = "128";
                    String mp3Url = "";
                    String extension = ".mp3";
                    if (formats.contains("MP3128")) {
                        mp3Url = "http://antiserver.kuwo.cn/anti.s?response=url&type=convert_url&br=128kmp3&format=mp3&rid=" + songId;
                        bitrate = "128";
                        extension = ".mp3";
                    }
                    if (formats.contains("MP3192")) {
                        mp3Url = "http://antiserver.kuwo.cn/anti.s?response=url&type=convert_url&br=192kmp3&format=mp3&rid=" + songId;
                        bitrate = "192";
                        extension = ".mp3";
                    }
                    if (formats.contains("MP3H")) {
                        mp3Url = "http://antiserver.kuwo.cn/anti.s?response=url&type=convert_url&br=320kmp3&format=mp3&rid=" + songId;
                        //mp3Url = "http://antiserver.kuwo.cn/anti.s?response=url&type=convert_url&br=192kmp3&format=mp3&rid=" + songId;
                        bitrate = "320";
                        extension = ".mp3";
                    }
                    if (formats.contains("AL")) {
                        mp3Url = "http://antiserver.kuwo.cn/anti.s?response=url&type=convert_url&br=2000kflac&format=ape&rid=" + songId;
                        bitrate = "无损";
                        extension = ".mp3";
                    }
                    /*if (text1.Contains("MP4")) {
                        item.MvUrl = "http://antiserver.kuwo.cn/anti.s?response=url&type=convert_url&format=mp4&rid=" + item.SongId;
                    }
                    if (text1.Contains("MV")) {
                        item.MvUrl = "http://antiserver.kuwo.cn/anti.s?response=url&type=convert_url&format=mkv&rid=" + item.SongId;
                    }*/
                    music.setBitrate(bitrate);// 音质
                    music.setFileName(music.getName() + "-" + music.getSinger() + extension);// 文件名

                    music.setMp3Url(mp3Url);// 下载地址

                    // 文件路径
                    music.setFilePath(Constants.PATH_KWO + music.getFileName());
                    musics.add(music);
                }

                // 更新搜索结果
                EventBus.getDefault().post(Constants.EVENT_UPDATE_MUSICS);
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
                // 结束加载动画
                EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
            }
        });
    }
}
