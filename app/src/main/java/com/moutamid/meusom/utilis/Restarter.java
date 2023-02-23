package com.moutamid.meusom.utilis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class Restarter extends BroadcastReceiver {

    private String songName, urlYTAudio, urlYTVideo, ID, albumName, coverURL, type;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");

        songName = intent.getStringExtra(Constants.SONG_NAME);
        urlYTAudio = intent.getStringExtra(Constants.URL);
        urlYTVideo = intent.getStringExtra(Constants.videoLink);
        ID = intent.getStringExtra(Constants.ID);
        albumName = intent.getStringExtra(Constants.SONG_ALBUM_NAME);
        coverURL = intent.getStringExtra(Constants.SONG_COVER_URL);
        type = intent.getStringExtra(Constants.TYPE);

        Intent mServiceIntent;
        mServiceIntent = new Intent(context, YourService.class);
        mServiceIntent.putExtra(Constants.URL, urlYTAudio);
        mServiceIntent.putExtra(Constants.SONG_NAME, songName);
        mServiceIntent.putExtra(Constants.ID, ID);
        mServiceIntent.putExtra(Constants.videoLink, urlYTVideo);
        mServiceIntent.putExtra(Constants.SONG_ALBUM_NAME, albumName);
        mServiceIntent.putExtra(Constants.SONG_COVER_URL, coverURL);
        mServiceIntent.putExtra(Constants.TYPE, type);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(mServiceIntent);
        } else {
            context.startService(mServiceIntent);
        }
    }
}
