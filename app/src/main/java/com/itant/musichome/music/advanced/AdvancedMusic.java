package com.itant.musichome.music.advanced;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.utils.FileTool;
import com.itant.musichome.utils.SecureTool;
import com.itant.musichome.utils.ToastTool;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;

/**
 * Created by 詹子聪 on 2016/11/15.
 * 小狗音乐
 */
public class AdvancedMusic {

    private AdvancedMusic() {}

    private static class DogMusicFactory {
        private static AdvancedMusic instance = new AdvancedMusic();
    }

    public static AdvancedMusic getInstance() {
        return DogMusicFactory.instance;
    }

    /**
     * 获取小狗歌曲信息
     * @param type 0小狗 1凉窝 2企鹅 3白云 4熊掌 5龙虾
     */
    public void getAdvancedSongs(final List<Music> musics, final String parent, final int type, final String engine, int page, String keyWords) throws Exception {
        String url = "http://api.itwusun.com/music/search/" + engine + "/" + page + "?format=json&sign=a5cc0a8797539d3a1a4f7aeca5b695b9&keyword=" + keyWords;
        RequestParams params = new RequestParams(url);
        x.http().get(params, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                if (TextUtils.isEmpty(result)) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE_AD);
                    ToastTool.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                if (result.contains("ErrorCode")) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE_AD);
                    ToastTool.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                JSONArray listArray = null;
                try {
                    listArray = JSON.parseArray(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (listArray == null) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE_AD);
                    ToastTool.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }

                for (Object obj : listArray) {
                    JSONObject object = JSON.parseObject(obj.toString());
                    if (object == null) {
                        continue;
                    }

                    Music music = new Music();
                    music.setMusicType(type);// 音乐来源
                    music.setSourceId(object.getString("SongId"));// 歌曲最原始的ID
                    music.setId(engine + music.getSourceId());

                    music.setName(object.getString("SongName"));// 歌名
                    music.setSinger(object.getString("Artist"));// 歌手
                    music.setAlbum(object.getString("Album"));// 专辑
                    music.setBitrate(object.getString("BitRate"));// 音质
                    music.setFilePath(parent);


                    music.setMvUrl(object.getString("MvUrl"));// MV
                    music.setVideoUrl(object.getString("VideoUrl"));// video

                    music.setFlacUrl(object.getString("FlacUrl"));// FLAC
                    music.setApeUrl(object.getString("AacUrl"));// APE
                    music.setSqUrl(object.getString("SqUrl"));// SQ
                    music.setHqUrl(object.getString("HqUrl"));// HQ
                    music.setLqUrl(object.getString("LqUrl"));// LQ
                    musics.add(music);
                }

                // 更新搜索结果
                EventBus.getDefault().post(Constants.EVENT_UPDATE_MUSICS_AD);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ToastTool.toastShort(MusicApplication.applicationContext, "未知错误");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                // 结束加载动画
                EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE_AD);
            }
        });
    }
}
