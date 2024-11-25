package com.example.notebook11;

public class Note {

    private long id;
    private String title; // 标题
    private String content; //内容
    private String time;
    private int tag;

    public Note(){}

    public Note(String title,String content, String time, int tag ) {
        this.title = title;
        this.content = content;
        this.tag = tag;//意思是笔记的分类
        this.time = time;

    }
    public Note(long id,String title,String content, String time, int tag ) {
        this.title = title;
        this.content = content;
        this.tag = tag;//意思是笔记的分类
        this.time = time;
        this.id = id;

    }
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString(){
        return title+"\n"+content+"\n"+time.substring(5,16)+" "+id;
    }


}
