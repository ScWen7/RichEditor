package com.scwen.note;

import com.alibaba.fastjson.JSON;

import org.litepal.crud.LitePalSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by scwen on 2019/4/27.
 * QQ ：811733738
 * 作用：
 */
public class NoteBean  extends LitePalSupport {


    private int id;
    private String title;
    private String content;
    private int is_del = 0;
    private int uid;
    private long createtime;
    private String noteLabels;
    private String noteImg;
    private String content_html;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content == null ? "" : content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIs_del() {
        return is_del;
    }

    public void setIs_del(int is_del) {
        this.is_del = is_del;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public String getNoteLabels() {
        return noteLabels == null ? "" : noteLabels;
    }

    public void setNoteLabels(String noteLabels) {
        this.noteLabels = noteLabels;
    }


    public String getContent_html() {
        return content_html == null ? "" : content_html;
    }

    public void setContent_html(String content_html) {
        this.content_html = content_html;
    }

    public String getNoteImg() {
        return noteImg == null ? "" : noteImg;
    }

    public void setNoteImg(String noteImg) {
        this.noteImg = noteImg;
    }

    @Override
    public String toString() {
        return "NoteBean{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", is_del=" + is_del +
                ", uid=" + uid +
                ", createtime=" + createtime +
                ", noteLabels='" + noteLabels + '\'' +
                ", noteImg='" + noteImg + '\'' +
                ", content_html='" + content_html + '\'' +
                '}';
    }
}
