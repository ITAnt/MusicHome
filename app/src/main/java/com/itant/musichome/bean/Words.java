package com.itant.musichome.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by Jason on 2017/1/16.
 */
public class Words extends BmobObject {
    private String words;
    private String version;
    private String notice;

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
}
