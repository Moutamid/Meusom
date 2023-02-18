package com.moutamid.meusom.utilis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class Restarter extends BroadcastReceiver {

    private String songName;

    private String urlYT, pushKeyYT;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Broadcast Listened", "Service tried to stop");

        songName = intent.getStringExtra(Constants.TITLE);

        urlYT = intent.getStringExtra(Constants.YT_URL);

        pushKeyYT = intent.getStringExtra(Constants.PUSH_KEY);

        Intent mServiceIntent;
        mServiceIntent = new Intent(context, YourService.class);
        mServiceIntent.putExtra(Constants.YT_URL, urlYT);
        mServiceIntent.putExtra(Constants.PUSH_KEY, pushKeyYT);
        mServiceIntent.putExtra(Constants.TITLE, songName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.startForegroundService(mServiceIntent);
        } else {

            context.startService(mServiceIntent);
        }
    }
}
