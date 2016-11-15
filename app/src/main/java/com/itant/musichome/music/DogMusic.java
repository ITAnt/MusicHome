package com.itant.musichome.music;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.utils.SecureTool;
import com.itant.musichome.utils.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

/**
 * Created by 詹子聪 on 2016/11/15.
 * 小狗音乐
 */
public class DogMusic {

    private DogMusic() {}

    private static class DogMusicFactory {
        private static DogMusic instance = new DogMusic();
    }

    public static DogMusic getInstance() {
        return DogMusicFactory.instance;
    }

    /**
     * 获取企鹅歌曲信息
     */
    public void getDogSongs(final List<Music> musics, String keyWords) {
        String url = "http://mobilecdn.kugou.com/api/v3/search/song?format=jsonp&keyword=" + keyWords + "&page=1&pagesize=20&showtype=1";
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
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

                int total = dataObject.getIntValue("total");
                if (total <= 0) {
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                JSONArray listArray = dataObject.getJSONArray("info");
                if (listArray == null) {
                    return;
                }

                for (Object obj : listArray) {
                    JSONObject object = JSON.parseObject(obj.toString());
                    if (object == null) {
                        continue;
                    }


                    Music music = new Music();
                    music.setMusicType(0);// 音乐来源
                    music.setSinger("未知");// 歌手
                    music.setId(object.getString("hash"));// 歌曲ID
                    music.setName(object.getString("filename"));// 歌名
                    music.setSinger(object.getString("singername"));// 歌手
                    music.setAlbum("");// 专辑


                    music.setBitrate(object.getString("bitrate"));// 音质
                    music.setFileName(music.getName() + "-" + music.getSinger() + ".mp3");// 文件名

                    String hash = object.getString("hash");
                    String hash320 = object.getString("320hash");
                    if (!TextUtils.isEmpty(hash320)) {
                        hash = hash320;
                        music.setBitrate("320");// 音质
                    }

                    String sqhash = object.getString("sqhash");
                    if (!TextUtils.isEmpty(sqhash)) {
                        hash = sqhash;
                        music.setBitrate("无损");// 音质
                    }


                    String key = SecureTool.getMD5String(hash + "kgcloud");
                    music.setMp3Url("http://trackercdn.kugou.com/i/?key=" + key + "&cmd=4&acceptMp3=1&hash=" + hash + "&pid=1");// 下载地址

                    // 文件路径
                    music.setFilePath(Constants.PATH_DOG + music.getFileName());
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
