package com.moutamid.meusom;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;
import com.moutamid.meusom.utilis.VolleySingleton;
import com.moutamid.meusom.utilis.YourService;

import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity2 extends AppCompatActivity {

    ProgressDialog progressDialog;
    Utils utils = new Utils();
    YourService mYourService;
    Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main2);

        progressDialog = new ProgressDialog(MainActivity2.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String url = intent.getStringExtra(Intent.EXTRA_TEXT);

                String ID = Constants.getVideoId(url);

                Log.d("VideoSError", "URL : " + url);
                Log.d("VideoSError", "ID : " + ID);

                final Dialog dialog = new Dialog(MainActivity2.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.audio_video_layout);

                Button audio = dialog.findViewById(R.id.audio);
                Button video = dialog.findViewById(R.id.video);

                audio.setOnClickListener(v -> {
                    getSong(url, "audio");
                    dialog.dismiss();
                    progressDialog.show();

                });

                video.setOnClickListener(v -> {
                    getSong(url, "video");
                    dialog.dismiss();
                    progressDialog.show();
                });

                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setGravity(Gravity.CENTER);

            } else {
                progressDialog.dismiss();
                Toast.makeText(this, "URL not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private void getSong(String videoLink, String type) {


        String link = "https://www.youtube.com/watch?v=" + Constants.getVideoId(videoLink);

        String url = "https://youtube-to-mp315.p.rapidapi.com/download?url=" + link + "&format=mp3";

        Log.d(TAG, "getSong: link " + link);
        Log.d(TAG, "getSong: URL " + url);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    // Response
                    Log.d(TAG, "getSong: " + response);
                    try {
                        String title = response.getString("title");
                        String downloadUrl = response.getString("downloadUrl");
                        if (title.equals("null")) {
                            executorService.execute(() -> {
                                try {
                                    Document doc2 = Jsoup.connect(link).get();
                                    String title2 = doc2.title();
                                    title2 = title2.replace(" - YouTube", "").trim();
                                    if (title2.isEmpty()) {
                                        title2 = "NULL";
                                    } else {
                                        for (String s : Constants.special) {
                                            if (title2.contains(s)) {
                                                title2 = title2.replace(s, "");
                                            }
                                        }
                                    }

                                    String finalTitle = title2;
                                    mainThreadHandler.post(() -> {
                                        Log.d(TAG, "getSong: title2  " + finalTitle);
                                        moveIntent(finalTitle, downloadUrl, type, videoLink);
                                    });
                                } catch (IOException e) {
                                    mainThreadHandler.post(() -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(mYourService, "Failed to fetch the video title.", Toast.LENGTH_SHORT).show();
                                    });

                                }
                            });
                        } else {
                            for (String s : Constants.special) {
                                if (title.contains(s)) {
                                    title = title.replace(s, "");
                                }
                            }
                            moveIntent(title, downloadUrl, type, videoLink);
                        }

                    } catch (JSONException e) {
                        progressDialog.dismiss();
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressDialog.dismiss();
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

    private void moveIntent(String title, String downloadUrl, String type, String videoLink) {
        title = title.trim();
        boolean check = utils.fileExists(title) || utils.videoExists(title);
        progressDialog.dismiss();
        if (check) {
            Toast.makeText(getApplicationContext(), "Already Downloaded", Toast.LENGTH_SHORT).show();
        } else {
            mYourService = new YourService();
            mServiceIntent = new Intent(getApplicationContext(), mYourService.getClass());
            mServiceIntent.putExtra(Constants.URL, downloadUrl);
            mServiceIntent.putExtra(Constants.SONG_NAME, title);
            mServiceIntent.putExtra(Constants.ID, Constants.getVideoId(videoLink));
            mServiceIntent.putExtra(Constants.videoLink, downloadUrl);
            mServiceIntent.putExtra(Constants.SONG_ALBUM_NAME, title);
            mServiceIntent.putExtra(Constants.SONG_COVER_URL, "");
            mServiceIntent.putExtra(Constants.TYPE, type);
            startService(mServiceIntent);
            doneLoading("Download started");
        }
    }

    private void doneLoading(String e) {
        Log.e(TAG, "Json parsing error: " + e);

        if (progressDialog.isShowing())
            progressDialog.dismiss();

        Toast.makeText(getApplicationContext(), e, Toast.LENGTH_LONG).show();

        finish();
    }

}