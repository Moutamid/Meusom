package com.moutamid.meusom.models;

public class SongIDModel {
    String songYTUrl;

    public SongIDModel() {
    }

    public SongIDModel(String songYTUrl) {
        this.songYTUrl = songYTUrl;
    }

    public String getSongYTUrl() {
        return songYTUrl;
    }

    public void setSongYTUrl(String songYTUrl) {
        this.songYTUrl = songYTUrl;
    }
}
