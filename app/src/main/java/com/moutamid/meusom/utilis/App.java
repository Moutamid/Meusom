package com.moutamid.meusom.utilis;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.util.Log;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.firebase.database.FirebaseDatabase;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class App extends Application {
    private static final String TAG = "App";
    @Override
    public void onCreate() {
        super.onCreate();
        Stash.init(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        PRDownloader.initialize(getApplicationContext());
        createNotification();
        configureRxJavaErrorHandler();
        /*Completable.fromAction(this::initLibraries).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new DisposableCompletableObserver() {
            @Override
            public void onComplete() {
                // it worked
            }

            @Override
            public void onError(Throwable e) {
                if(BuildConfig.DEBUG) Log.e(TAG, "failed to initialize youtubedl-android", e);
                Toast.makeText(getApplicationContext(), "initialization failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    private void createNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "Meusom Music", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Music Chanel");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    private void configureRxJavaErrorHandler() {
        RxJavaPlugins.setErrorHandler(e -> {

            if (e instanceof UndeliverableException) {
                // As UndeliverableException is a wrapper, get the cause of it to get the "real" exception
                e = e.getCause();
            }

            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }

            Log.e(TAG, "Undeliverable exception received, not sure what to do", e);
        });
    }

    /*private void initLibraries() throws YoutubeDLException {
        YoutubeDL.getInstance().init(this);
        FFmpeg.getInstance().init(this);
    }*/
}
