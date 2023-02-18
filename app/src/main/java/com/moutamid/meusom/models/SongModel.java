package com.moutamid.meusom.models;

public class SongModel {
    private String songName, songAlbumName, songCoverUrl, songYTUrl, songPushKey, type, songVideoURL;

    public SongModel(String songName, String songAlbumName, String songCoverUrl, String songYTUrl, String songPushKey, String type, String songVideoURL) {
        this.songName = songName;
        this.songAlbumName = songAlbumName;
        this.songCoverUrl = songCoverUrl;
        this.songYTUrl = songYTUrl;
        this.songPushKey = songPushKey;
        this.type = type;
        this.songVideoURL = songVideoURL;
    }

    public SongModel() {
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongAlbumName() {
        return songAlbumName;
    }

    public void setSongAlbumName(String songAlbumName) {
        this.songAlbumName = songAlbumName;
    }

    public String getSongCoverUrl() {
        return songCoverUrl;
    }

    public void setSongCoverUrl(String songCoverUrl) {
        this.songCoverUrl = songCoverUrl;
    }

    public String getSongYTUrl() {
        return songYTUrl;
    }

    public void setSongYTUrl(String songYTUrl) {
        this.songYTUrl = songYTUrl;
    }

    public String getSongPushKey() {
        return songPushKey;
    }

    public void setSongPushKey(String songPushKey) {
        this.songPushKey = songPushKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSongVideoURL() {
        return songVideoURL;
    }

    public void setSongVideoURL(String songVideoURL) {
        this.songVideoURL = songVideoURL;
    }
}
