package com.moutamid.meusom.utilis;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.moutamid.meusom.MainActivity;
import com.moutamid.meusom.R;
import com.moutamid.meusom.Services.NotificationActionService;
import com.moutamid.meusom.models.SongModel;

public class CreateNotification {

    public static final String CHANNEL_ID = "channel1";
    public static final String ACTION_PREVIUOS = "actionprevious";
    public static final String ACTION_PLAY = "actionplay";
    public static final String ACTION_NEXT = "actionnext";
    private static final int NOTIFICATION_ID = 0;

    public static Notification notification;

    public static void createNotification(Context context, SongModel track, int playbutton, int pos, int size, MediaPlayer mp) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "Meusom");

            mediaSessionCompat.setActive(true);

            MediaSessionCompat.Token token = mediaSessionCompat.getSessionToken();



            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.music);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            PendingIntent pendingIntentPrevious;
            int drw_previous;
            if (pos == 0) {
                pendingIntentPrevious = null;
                drw_previous = 0;
            } else {
                Intent intentPrevious = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_PREVIUOS);
                pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,
                        intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_previous = R.drawable.ic_skip_previous_black_24dp;
            }

            Intent intentPlay = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
                    intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent pendingIntentNext;
            int drw_next;
            if (pos == size) {
                pendingIntentNext = null;
                drw_next = 0;
            } else {
                Intent intentNext = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context, 0,
                        intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_next = R.drawable.ic_skip_next_black_24dp;
            }

            //create notification
            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_home_img)
                    .setOngoing(true)
                    .setContentTitle(track.getSongName())
                    .setContentText(track.getSongAlbumName())
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)//show notification for only first time
                    .setShowWhen(true)
                    .addAction(drw_previous, "Previous", pendingIntentPrevious)
                    .addAction(playbutton, "Play", pendingIntentPlay)
                    .addAction(drw_next, "Next", pendingIntentNext)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(token))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();


            mediaSessionCompat.setMetadata(
                    new MediaMetadataCompat.Builder()
                            .putString(MediaMetadata.METADATA_KEY_TITLE, track.getSongName())
                            .putString(MediaMetadata.METADATA_KEY_ARTIST, track.getSongAlbumName())
                            .putLong(MediaMetadata.METADATA_KEY_DURATION, mp.getDuration())
                            .build()
            );


            mediaSessionCompat.setPlaybackState(
                    new PlaybackStateCompat.Builder()
                            .setState(
                                    PlaybackStateCompat.STATE_PLAYING,
                                    mp.getCurrentPosition(),
                                    SystemClock.elapsedRealtime()
                            )
                            .setBufferedPosition(mp.getDuration())
                            .setActions(PlaybackStateCompat.ACTION_PLAY |
                                    PlaybackStateCompat.ACTION_PAUSE |
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                    PlaybackStateCompat.ACTION_SEEK_TO |
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE)
                            .build()
            );


            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
               // ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
                return;
            }


            notificationManagerCompat.notify(0, notification);

        }
    }
}
