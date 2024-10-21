package com.moutamid.meusom;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.meusom.adapter.DownloadAdapter;
import com.moutamid.meusom.models.SongIDModel;
import com.moutamid.meusom.models.SongModel;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.VolleySingleton;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class WaitingActivity extends AppCompatActivity {
    private ArrayList<SongModel> songModelArrayList = new ArrayList<>();
    private ArrayList<Model> list = new ArrayList<>();
    RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        Stash.clear(Constants.OFF_DATA);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        Constants.databaseReference().child(Constants.SONGS)
                .child(Constants.auth().getCurrentUser().getUid())
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        songModelArrayList.clear();
                        list.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            SongIDModel songModel1 = snapshot.getValue(SongIDModel.class);
                            if (songModel1 != null) {
                                Model model = new Model();
                                model.id = songModel1.getSongYTUrl();
                                model.key = snapshot.getKey();
                                list.add(model);
                            }
                        }
                        getData(0);
                    } else {
                        Intent intent = new Intent(WaitingActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(e -> {
                    Constants.auth().signOut();
                    Intent intent = new Intent(WaitingActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private static final String TAG = "WaitingActivity";
    @SuppressLint("StaticFieldLeak")
    private void getData(int i) {
        String link = "https://www.youtube.com/watch?v=" + list.get(i).id;
        String url = "https://youtube-to-mp315.p.rapidapi.com/download?url=" + link + "&format=mp3";

        Log.d(TAG, "getSong: link " + link);
        Log.d(TAG, "getSong: URL " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    // Response
                    Log.d(TAG, "getSong: " + response);
                    try {
                        String title = response.getString("title");
                        String downloadUrl = response.getString("downloadUrl");

                        SongModel model = new SongModel();
                        model.setId(list.get(i).id);
                        model.setSongYTUrl(downloadUrl);
                        model.setSongName(title);
                        model.setType("audio");
                        model.setSongAlbumName(title);
                        model.setSongCoverUrl("");
                        model.setSongVideoURL(downloadUrl);
                        model.setSongPushKey(list.get(i).key);

                        songModelArrayList.add(model);

                        Stash.put(Constants.OFF_DATA, songModelArrayList);

                        if (i == list.size() - 1) {
                            Log.d("LOGINOFF", "iNTENT");
                            Intent intent = new Intent(WaitingActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            getData(i + 1);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    String errorMessage = "Unknown error";
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String responseData = new String(error.networkResponse.data);
                        errorMessage = "Status Code: " + statusCode + ", Response: " + responseData;
                    } else if (error.getLocalizedMessage() != null) {
                        errorMessage = error.getLocalizedMessage();
                    }
                    Log.e(TAG, "Request failed. URL: " + url + ", Error: " + errorMessage);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-rapidapi-key", "d7385e342bmshb432933b0fb0e71p101f9ejsne8db1ce60a84");
                headers.put("x-rapidapi-host", "youtube-to-mp315.p.rapidapi.com");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    class Model {
        String id, key;

        public Model() {
        }
    }

}