package com.moutamid.meusom.utilis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.moutamid.meusom.CommandExampleActivity;
import com.moutamid.meusom.R;

public class NotificationHelper extends ContextWrapper {

    private static final String TAG = "NotificationHelper";
    private Context base1;

    public NotificationHelper(Context base) {
        super(base);
        base1 = base;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels();
        }

    }

    private String CHANNEL_NAME = "Downloading Channel";
    private String CHANNEL_ID = "com.example.notifications" + CHANNEL_NAME;

    private String MEDIA_CHANNEL_NAME = "Music Player Channel";
    private String MEDIA_CHANNEL_ID = "com.example.notifications" + MEDIA_CHANNEL_NAME;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
//        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.enableLights(false);
//        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(false);
//        notificationChannel.enableVibration(true);
        notificationChannel.setDescription("It is used to display downloading notification.");
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        // MEDIA PLAYER NOTIFICATION
        NotificationChannel mediaPlayerChannel = new NotificationChannel(MEDIA_CHANNEL_ID, MEDIA_CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
//        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        mediaPlayerChannel.enableLights(false);
//        notificationChannel.enableLights(true);
        mediaPlayerChannel.enableVibration(false);
//        notificationChannel.enableVibration(true);
        mediaPlayerChannel.setDescription("Music Player");
        mediaPlayerChannel.setLightColor(Color.RED);
        mediaPlayerChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(notificationChannel);
        manager.createNotificationChannel(mediaPlayerChannel);
    }

    Bitmap bitmap;

    public void sendDownloadingNotification(String title, String body) {
//    public void sendHighPriorityNotification(String title, String body, Class activityName) {

        Intent intent = new Intent(this, CommandExampleActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 267, intent, PendingIntent.FLAG_IMMUTABLE); //  PendingIntent.FLAG_UPDATE_CURRENT

        int iconN = R.drawable.donwloadtrack;

        if (body.equals("Download Completed!")) {
            iconN = R.drawable.off_track;
        }

        Notification notification = new NotificationCompat.Builder(base1, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(iconN)
//                .setLargeIcon(resource)
                .setPriority(NotificationCompat.PRIORITY_LOW)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().setSummaryText(
                                "Downloading"
                        )
                        .setBigContentTitle(title).bigText(body))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        NotificationManagerCompat.from(base1).notify(111, notification);


//        bitmap = BitmapFactory.decodeResource(this.getResources(),
//                R.drawable.ic_icon_launcher);
//
//        Glide.with(this)
//                .asBitmap()
//                .apply(new RequestOptions()
//                        .placeholder(lighterGrey)
//                        .error(lighterGrey)
//                )
//                .diskCacheStrategy(DiskCacheStrategy.DATA)
//                .load(url)
//                .into(new CustomTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//
//                        Notification notification = new NotificationCompat.Builder(base1, CHANNEL_ID)
//                                .setContentTitle(title)
//                                .setContentText(body)
//                                .setSmallIcon(R.drawable.donwloadtrack)
//                                .setLargeIcon(resource)
//                                .setPriority(NotificationCompat.PRIORITY_LOW)
////                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                                .setStyle(new NotificationCompat.BigTextStyle().setSummaryText(
//                                        "Downloading..."
//                                )
//                                        .setBigContentTitle(title).bigText(body))
//                                .setContentIntent(pendingIntent)
//                                .setAutoCancel(true)
//                                .build();
//
//                        NotificationManagerCompat.from(base1).notify(111, notification);
//
//                    }
//
//                    @Override
//                    public void onLoadCleared(@Nullable Drawable placeholder) {
//                    }
//                });

    }

}