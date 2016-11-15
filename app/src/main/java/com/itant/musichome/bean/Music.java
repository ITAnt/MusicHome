package com.itant.musichome.bean;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by Jason on 2016/11/13.
 */
@Table(name = "music")
public class Music {

    @Column(name = "id", isId = true)
    private String id;// 歌曲的ID

    @Column(name = "fileName")
    private String fileName;// 本地文件名，包括后缀名

    @Column(name = "name")
    private String name;// 歌曲名字

    @Column(name = "singer")
    private String singer;// 歌手

    @Column(name = "album")
    private String album;// 专辑

    @Column(name = "size")
    private String size;// 文件大小

    @Column(name = "bitrate")
    private String bitrate;// 比特率

    @Column(name = "mp3Url")
    private String mp3Url;// 歌曲下载地址

    @Column(name = "imageUrl")
    private String imageUrl;// 封面图片地址

    @Column(name = "progress")
    private int progress;// 下载进度

    @Column(name = "filePath")
    private String filePath;// 本地文件路径

    @Column(name = "musicType")
    private int musicType;// 音乐来源 0小狗、1龙虾、2企鹅、3白云

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getBitrate() {
        return bitrate;
    }

    public void setBitrate(String bitrate) {
        this.bitrate = bitrate;
    }

    public String getMp3Url() {
        return mp3Url;
    }

    public void setMp3Url(String mp3Url) {
        this.mp3Url = mp3Url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getMusicType() {
        return musicType;
    }

    public void setMusicType(int musicType) {
        this.musicType = musicType;
    }
}
