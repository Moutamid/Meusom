package com.moutamid.meusom.Services;

import static com.moutamid.meusom.utilis.Constants.ACTION_NEXT;
import static com.moutamid.meusom.utilis.Constants.ACTION_PLAY;
import static com.moutamid.meusom.utilis.Constants.ACTION_PREVIUOS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationActionService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, OnClearFromRecentService.class);
        if (intent.getAction()!=null){
            switch (intent.getAction()){
                case ACTION_PLAY:
                case ACTION_PREVIUOS:
                case ACTION_NEXT:
                    intent1.putExtra("myAction", intent.getAction());
                    context.startService(intent1);
                    break;
            }
        }
    }
}
