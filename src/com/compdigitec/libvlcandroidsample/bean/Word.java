package com.compdigitec.libvlcandroidsample.bean;

/**
 * Created by junjun on 2017/5/4.
 */

public class Word {
    private int id;
    private String word;
    private String mean_cn;
    private String accent;
    private String audio_file;
    private String word_variants;

    public Word(int id, String word, String mean_cn, String accent, String audio_file,String word_variants) {
        this.id = id;
        this.word = word;
        this.mean_cn = mean_cn;
        this.accent = accent;
        this.audio_file = audio_file;
        this.word_variants = word_variants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMean_cn() {
        return mean_cn;
    }

    public void setMean_cn(String mean_cn) {
        this.mean_cn = mean_cn;
    }

    public String getAccent() {
        return accent;
    }

    public void setAccent(String accent) {
        this.accent = accent;
    }

    public String getAudio_file() {
        return audio_file;
    }

    public void setAudio_file(String audio_file) {
        this.audio_file = audio_file;
    }

    public String getWord_variants() {
        return word_variants;
    }

    public void setWord_variants(String word_variants) {
        this.word_variants = word_variants;
    }

    @Override
    public String toString() {
        return "Word{" +
                "id=" + id +
                ", word='" + word + '\'' +
                ", mean_cn='" + mean_cn + '\'' +
                ", accent='" + accent + '\'' +
                ", audio_file='" + audio_file + '\'' +
                '}';
    }
}
