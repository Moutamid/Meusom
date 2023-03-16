package com.moutamid.meusom.Services;


import static com.moutamid.meusom.utilis.Constants.ACTION_NEXT;
import static com.moutamid.meusom.utilis.Constants.ACTION_PLAY;
import static com.moutamid.meusom.utilis.Constants.ACTION_PREVIUOS;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.moutamid.meusom.utilis.Playable;

public class OnClearFromRecentService extends Service {

    Playable playable;
    private IBinder iBinder = new MyBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String name = intent.getStringExtra("myAction");
            if (name != null) {
                switch (name) {
                    case ACTION_PLAY:
                        if (playable != null) {
                            playable.onTrackPlay();
                        }
                        break;
                    case ACTION_PREVIUOS:
                        if (playable != null) {
                            playable.onTrackPrevious();
                        }
                        break;
                    case ACTION_NEXT:
                        if (playable != null) {
                            playable.onTrackNext();
                        }
                        break;
                }
            }
        }
        return START_STICKY;
    }

    public void setCallBack(Playable playable){
        this.playable = playable;
    }

    public class MyBinder extends Binder {
        public OnClearFromRecentService getService(){
            return OnClearFromRecentService.this;
        }
    }
}
