package com.moutamid.meusom.utilis;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.fxn.stash.Stash;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.moutamid.meusom.models.SongModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;

public class YourService  extends Service {
    public int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public String songName, urlYTAudio, urlYTVideo, ID, albumName, coverURL, type;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        songName = intent.getStringExtra(Constants.SONG_NAME);
        urlYTAudio = intent.getStringExtra(Constants.URL);
        urlYTVideo = intent.getStringExtra(Constants.videoLink);
        ID = intent.getStringExtra(Constants.ID);
        albumName = intent.getStringExtra(Constants.SONG_ALBUM_NAME);
        coverURL = intent.getStringExtra(Constants.SONG_COVER_URL);
        type = intent.getStringExtra(Constants.TYPE);

       runCommand();

        return START_STICKY;
    }

    private void runCommand() {
        Context context = getApplicationContext();
        NotificationHelper helper = new NotificationHelper(context);

        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Meusom/");
        SongModel model = new SongModel();

        model.setSongYTUrl(urlYTAudio);
        model.setId(ID);
        model.setSongName(songName);
        model.setSongAlbumName(albumName);
        model.setSongCoverUrl(coverURL);
        model.setSongVideoURL(urlYTVideo);
        model.setType(type);

        String d = songName;
        String downloadUrl = "";

        if (type.equals("video")) {
            d = d + ".mp4";
            downloadUrl = urlYTVideo;
        } else {
            d = d + ".mp3";
            downloadUrl = urlYTAudio;
        }

        PRDownloader.download(downloadUrl, file.getPath(), d)
                .build()
                .setOnStartOrResumeListener(() -> {
                    helper.sendDownloadingNotification(songName, "loading...");
                })
                .setOnPauseListener(() -> {

                })
                .setOnCancelListener(() -> {

                })
                .setOnProgressListener(progress -> {
                    long n = progress.currentBytes * 100 / progress.totalBytes;
                    helper.sendDownloadingNotification(songName,
                            (int) n + "% completed");
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                            Map<String, Object> map = new HashMap<>();
                            map.put("songYTUrl", YourService.this.ID);
                            if (Constants.auth().getCurrentUser()!=null){
                                String pushkey = Constants.databaseReference().child(Constants.SONGS)
                                        .child(Constants.auth().getCurrentUser().getUid()).push().getKey();
                                Constants.databaseReference().child(Constants.SONGS)
                                        .child(Constants.auth().getCurrentUser().getUid()).child(pushkey)
                                        .setValue(map).addOnCompleteListener(task -> {
                                            completed++;
                                            helper.sendDownloadingNotification(songName, "Download Completed!");
                                            model.setSongPushKey(pushkey);
                                            ArrayList<SongModel> songModelArrayList = Stash.getArrayList(Constants.OFF_DATA, SongModel.class);
                                            songModelArrayList.add(model);
                                            Stash.put(Constants.OFF_DATA, songModelArrayList);
                                            stopSelf();
                                            Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        Toast.makeText(getApplicationContext(), "Download Complete", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Error error) {

                        if (error.isServerError()) {
                            Log.d("VideoSError", "Server : " + error.getServerErrorMessage());
                            Toast.makeText(getApplicationContext(), "Server Error: " + error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                        } else if (error.isConnectionError()) {

                            Toast.makeText(getApplicationContext(), "Connection Error: " + error.getConnectionException().getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("VideoSError", "Error : " + error);
                            Toast.makeText(getApplicationContext(), "" + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    /*private DownloadProgressCallback callback = new DownloadProgressCallback() {
        @Override
        public void onProgressUpdate(float progress, long etaInSeconds) {
//                        progressBar.setProgress((int) progress);

            NotificationHelper helper = new NotificationHelper(getApplicationContext());
            helper.sendDownloadingNotification(songName,
                    progress + "% (ETA " + etaInSeconds + " seconds)");

        }
    };*/

 /*   private void runCommand(String songYTUrll, Context context, String songPushKey) {

        NotificationHelper helper = new NotificationHelper(context);

        if (!songYTUrll.contains("http")) {
            songYTUrll = "https://www.youtube.com/watch?v=" + songYTUrll;
        }

//        String command = "--extract-audio --audio-format mp3 -o /sdcard/Download/Meusom./%(title)s.%(ext)s " + songYTUrll;
        String command = "--extract-audio --audio-format mp3 -o " + new Utils().getPath() + "%(title)s.%(ext)s " + songYTUrll;

        YoutubeDLRequest request = new YoutubeDLRequest(Collections.emptyList());
        String commandRegex = "\"([^\"]*)\"|(\\S+)";
        Matcher m = Pattern.compile(commandRegex).matcher(command);
        while (m.find()) {
            if (m.group(1) != null) {
                request.addOption(m.group(1));
            } else {
                request.addOption(m.group(2));
            }
        }

        helper.sendDownloadingNotification(songName, "loading...");

        Disposable disposable = Observable.fromCallable(() -> YoutubeDL.getInstance().execute(request, callback))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(youtubeDLResponse -> {
                    helper.sendDownloadingNotification(songName, "Uploading new name...");
                    String outputStr = youtubeDLResponse.getOut();
                    extractNewNameAndUpload(helper, outputStr, songPushKey);
                }, e -> {
                    if (BuildConfig.DEBUG) Log.e("TAG", "command failed", e);
                    stopSelf();
                });
        compositeDisposable.add(disposable);

    }

    private void extractNewNameAndUpload(NotificationHelper helper, String outputStr,
                                         String songPushKey) {
        Pattern urlP = Pattern.compile("Meusom./(.*?).mp3");
        Matcher urlM = urlP.matcher(outputStr);

        String urlStr = "null";

        while (urlM.find()) {
            urlStr = urlM.group(1);
        }

        databaseReference.child(Constants.SONGS)
                .child(auth.getCurrentUser().getUid())
                .child(songPushKey)
                .child(Constants.SONG_NAME)
                .setValue(urlStr).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        completed++;
                        helper.sendDownloadingNotification(songName, "Download Completed!");
                        stopSelf();
                    }
                });
    }
*/
    int completed = 0;

    @Override
    public void onDestroy() {
        Log.e("TAG", "onDestroy: ");
        compositeDisposable.dispose();
        super.onDestroy();

        if (completed > 0) {
            return;
        }
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        broadcastIntent.putExtra(Constants.URL, urlYTAudio);
        broadcastIntent.putExtra(Constants.SONG_NAME, songName);
        broadcastIntent.putExtra(Constants.ID, ID);
        broadcastIntent.putExtra(Constants.videoLink, urlYTVideo);
        broadcastIntent.putExtra(Constants.SONG_ALBUM_NAME, albumName);
        broadcastIntent.putExtra(Constants.SONG_COVER_URL, coverURL);
        broadcastIntent.putExtra(Constants.TYPE, type);
        this.sendBroadcast(broadcastIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("onTaskRemoved: ", "called.");

        if (completed > 0) {
            return;
        }

        Intent restartServiceIntent = new Intent(getApplicationContext(),
                this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent =
                PendingIntent.getService(getApplicationContext(),
                        1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE); //TODO PendingIntent.FLAG_ONE_SHOT
        AlarmManager alarmService = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        broadcastIntent.putExtra(Constants.URL, urlYTAudio);
        broadcastIntent.putExtra(Constants.SONG_NAME, songName);
        broadcastIntent.putExtra(Constants.ID, ID);
        broadcastIntent.putExtra(Constants.videoLink, urlYTVideo);
        broadcastIntent.putExtra(Constants.SONG_ALBUM_NAME, albumName);
        broadcastIntent.putExtra(Constants.SONG_COVER_URL, coverURL);
        broadcastIntent.putExtra(Constants.TYPE, type);
        this.sendBroadcast(broadcastIntent);

        super.onTaskRemoved(rootIntent);
    }
}
