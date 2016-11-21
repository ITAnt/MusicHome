package com.itant.musichome.music;

import android.text.TextUtils;
import android.util.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.utils.ToastTools;

import org.greenrobot.eventbus.EventBus;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by 詹子聪 on 2016/11/15.
 * 白云音乐
 */
public class YunMusic {

    private YunMusic() {
    }

    private static class YunMusicFactory {
        private static YunMusic instance = new YunMusic();
    }

    public static YunMusic getInstance() {
        return YunMusicFactory.instance;
    }

    /**
     * 获取白云歌曲信息
     */
    public void getYunSongs(final List<Music> musics, String keyWords) throws Exception {
        RequestParams params = new RequestParams("http://music.163.com/api/search/pc");
        params.addBodyParameter("offset", "0");
        params.addBodyParameter("total", "true");
        params.addBodyParameter("limit", "50");
        params.addBodyParameter("type", "1");
        params.addBodyParameter("s", keyWords);
        params.addHeader("Cookie", "os=pc;MUSIC_U=5339640232");
        x.http().post(params, new Callback.CommonCallback<String>() {

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

                JSONObject resultObject = jsonObject.getJSONObject("result");
                if (resultObject == null) {
                    // 结束加载动画
                    EventBus.getDefault().post(Constants.EVENT_LOAD_COMPLETE);
                    ToastTools.toastShort(MusicApplication.applicationContext, "没有找到相关的歌曲");
                    return;
                }


                JSONArray listArray = resultObject.getJSONArray("songs");
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
                    music.setMusicType(3);// 音乐来源
                    music.setSourceId(object.getString("id"));// 歌曲ID
                    music.setId("yun" + object.getString("id"));// 歌曲ID
                    music.setSinger("未知");// 歌手

                    try {
                        JSONArray singers = object.getJSONArray("artists");
                        if (singers != null && singers.size() > 0) {
                            Object singerObj = singers.get(0);
                            if (singerObj != null) {
                                JSONObject singer = JSON.parseObject(singerObj.toString());
                                if (singer != null) {
                                    music.setSinger(singer.getString("name"));// 歌手
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    music.setName(object.getString("name"));// 歌名

                    music.setAlbum("未知");
                    try {
                        music.setAlbum(object.getJSONObject("album").getString("name"));// 专辑
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    music.setBitrate("128");// 音质
                    String encryptId = "";
                    String dfsId = "";
                    String extension = "";

                    JSONObject lObject = object.getJSONObject("lMusic");
                    if (lObject != null) {
                        dfsId = lObject.getString("dfsId");
                        extension = lObject.getString("extension");
                        music.setBitrate("128");// 音质
                    }

                    JSONObject mObject = object.getJSONObject("mMusic");
                    if (mObject != null) {
                        dfsId = mObject.getString("dfsId");
                        extension = mObject.getString("extension");
                        music.setBitrate("160");// 音质
                    }


                    JSONObject bObject = object.getJSONObject("bMusic");
                    if (bObject != null) {
                        dfsId = bObject.getString("dfsId");
                        extension = bObject.getString("extension");
                        music.setBitrate("一般");// 音质
                    }

                    JSONObject hObject = object.getJSONObject("hMusic");
                    if (hObject != null) {
                        dfsId = hObject.getString("dfsId");
                        extension = hObject.getString("extension");
                        music.setBitrate("320");// 音质
                    }


                    String url = "";
                    try {
                        String key = "3go8&$8*3*3h0k(2)2";
                        byte[] keyBytes = key.getBytes();
                        byte[] searchBytes = dfsId.getBytes();
                        for (int i = 0; i < searchBytes.length; ++i) {
                            searchBytes[i] ^= keyBytes[i % keyBytes.length];
                        }
                        MessageDigest mdInst = null;
                        mdInst = MessageDigest.getInstance("MD5");
                        mdInst.update(searchBytes);
                        String params = Base64.encodeToString(mdInst.digest(), Base64.NO_WRAP);
                        params = params.replace("+", "-");
                        params = params.replace("/", "_");
                        url = "http://m2.music.126.net/" + params + "/" + dfsId + "." + extension;
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }

                    music.setMp3Url(url);// 下载地址
                    music.setFileName(music.getName() + "-" + music.getSinger() + "-" + music.getId() + ".mp3");// 文件名

                    // 文件路径
                    music.setFilePath(Constants.PATH_YUN + music.getFileName());
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
