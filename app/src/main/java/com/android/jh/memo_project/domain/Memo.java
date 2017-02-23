package com.android.jh.memo_project.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

@DatabaseTable(tableName = "memo")
public class Memo {
    @DatabaseField(generatedId = true)
    int id;
    @DatabaseField
    String memo;
    @DatabaseField
    Date date;
    @DatabaseField
    String imgUri;
    @DatabaseField
    String voice;

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Memo() {

    }

    //create시에 사용할 생성자
    public Memo(String memo) {
        this.memo = memo;
        this.date = new Date(System.currentTimeMillis());
    }

    //create시에 사용할 생성자
    public Memo(String memo,String imgUri,String voice) {
        this.memo = memo;
        this.date = new Date(System.currentTimeMillis());
        this.imgUri = imgUri;
        this.voice = voice;
    }
}
