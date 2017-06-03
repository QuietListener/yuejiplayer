package com.compdigitec.libvlcandroidsample.bean;

import java.util.Date;

/**
 * Created by junjun on 2017/5/4.
 */

public class Record {

    private int id;
    private int word_id;
    private String word;
    private String movie_path;
    private String movie_name;
    private Date   date;
    private String  subtitle;
    private int   status = 0;
    private long time_from = 0l;
    private long time_to = 0l;

    public Record(int id, int word_id, String word, String movie_path, String movie_name, Date date, String subtitle, long from, long to , int status) {
        this.id = id;
        this.word_id = word_id;
        this.word = word;
        this.movie_path = movie_path;
        this.movie_name = movie_name;
        this.date = date;
        this.subtitle = subtitle;
        this.status = status;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWord_id() {
        return word_id;
    }

    public void setWord_id(int word_id) {
        this.word_id = word_id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMovie_path() {
        return movie_path;
    }

    public void setMovie_path(String movie_path) {
        this.movie_path = movie_path;
    }

    public String getMovie_name() {
        return movie_name;
    }

    public void setMovie_name(String movie_name) {
        this.movie_name = movie_name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTime_from() {
        return time_from;
    }

    public void setTime_from(long time_from) {
        this.time_from = time_from;
    }

    public long getTime_to() {
        return time_to;
    }

    public void setTime_to(long time_to) {
        this.time_to = time_to;
    }
}
