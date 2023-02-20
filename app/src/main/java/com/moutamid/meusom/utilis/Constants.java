package com.moutamid.meusom.utilis;

import android.app.Activity;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class Constants {
    public static final String[] special = {"$","%","^","&", "*","|", ":", "\"", "\'", ";", "@", "#", "!", "~", "`", "?", "+", "=", "/", "\\", ".", ","};
    public static final String URL = "url";
    public static final String SONG_NAME = "songName";
    public static final String OFF_DATA = "OFF_DATA";
    public static final String ID = "ID";
    public static final String videoLink = "videoLink";
    public static final String SONG_ALBUM_NAME = "songAlbumName";
    public static final String SONG_COVER_URL = "songCoverUrl";
    public static final String SONGS = "songs";
    public static final String COMPLETED = "COMPLETED 100%";
    public static final String NOT_DOWNLOADED = "NOT DOWNLOADED";
    public static final String SONG_INDEX = "songIndex";
    public static final int REQUEST_CODE = 100;
    public static final String PLAYLIST = "Playlists";
    public static final String NAME = "name";
    public static final String KEY = "key";
    public static final String IS_PLAYLIST = "isPlaylist";
    public static final String LANGUAGE = "language";
    public static final String ENGLISH = "en";
    public static final String PORTUGUESE = "pr";
    public static final String WATCHED_ADS_COUNT = "watched_ads_count";
    public static final String CURRENT_SPACE_AMOUNT = "current_space_amount";
    public static final String USER_EMAIL = "user_name";
    public static final String USER_PASSWORD = "user_password";
    public static final String FROM_INTENT = "from_intent";
    public static final String LAST_SONG_INDEX = "lastsongindex";

    public static final String YT_URL = "yt_url";
    public static final String PUSH_KEY = "push_key";
    public static final String NULL = "null";
    public static final String TITLE = "title";
    public static final String SORT = "sort";
    public static final String REVERSED = "reversed";
    public static final String ORIGINAL = "original";
    public static final String ALB_DESCENDING = "alb_descending";
    public static final String ALB_ASCENDING = "alb_ascending";
    public static final String T_DESCENDING = "t_descending";
    public static final String T_ASCENDING = "t_ascending";
    public static final String IS_CLICKED = "IS_CLICKED";

    public static void checkApp(Activity activity) {
        String appName = "meusom";

        new Thread(() -> {
            java.net.URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/Moutamid/Moutamid/main/apps.txt");
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if ((input = in != null ? in.readLine() : null) == null) break;
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(appName);

                boolean value = myAppObject.getBoolean("value");
                String msg = myAppObject.getString("msg");

                if (value) {
                    activity.runOnUiThread(() -> {
                        new AlertDialog.Builder(activity)
                                .setMessage(msg)
                                .setCancelable(false)
                                .show();
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }
    public static FirebaseAuth auth() {
        return FirebaseAuth.getInstance();
    }
    public static DatabaseReference databaseReference() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.keepSynced(true);
        return db;
    }
}
