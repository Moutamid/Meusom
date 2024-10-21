package com.moutamid.meusom;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;
import com.moutamid.meusom.utilis.VolleySingleton;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class DownloadActivity extends AppCompatActivity {
    private static final String TAG = "DownloadActivity";
    private Context context = DownloadActivity.this;
    private Utils utils = new Utils();
    private EditText editText;
    private boolean isIntent = false;
    public ProgressDialog progressDialog;
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.ENGLISH)) {
            utils.changeLanguage(context, "en");
        } else if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {
            utils.changeLanguage(context, "pr");
        }
        setContentView(R.layout.activity_download);

        editText = findViewById(R.id.edittextdownload);

        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if ("android.intent.action.SEND".equals(action) && "text/plain".equals(type)) {
            editText.setText(intent.getStringExtra("android.intent.extra.TEXT"));
            isIntent = true;
            executeDownloadTask();
        }

        findViewById(R.id.backBtnDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.gotoCommandActivityBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DownloadActivity.this, CommandExampleActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.ajgh).setOnClickListener(view -> executeDownloadTask());
    }

    private void executeDownloadTask() {
        url = editText.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            editText.setError("Please enter a url!");
            return;
        }

        if (TextUtils.isEmpty(Constants.getVideoId(url))) {
            editText.setError("Wrong url!");
        } else {
            Log.d(TAG, "executeDownloadTask: ID :  " + Constants.getVideoId(url));
            progressDialog.show();
            getSong(url);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void getSong(String videoLink) {
        Log.d(TAG, "getSong: " + videoLink);

        String link = "https://www.youtube.com/watch?v=" + Constants.getVideoId(videoLink);

        String url = "https://youtube-to-mp315.p.rapidapi.com/download?url=" + link + "&format=mp3";

        Log.d(TAG, "getSong: link " + link);
        Log.d(TAG, "getSong: URL " + url);

        RequestQueue requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                response -> {
                    // Response
                    Log.d(TAG, "getSong: " + response);
                    try {
                        String title = response.getString("title");
                        String downloadUrl = response.getString("downloadUrl");

                        String d = title;
                        for (String s : Constants.special) {
                            if (d.contains(s)) {
                                d = d.replace(s, "");
                            }
                        }

                        d = d.trim();

//                        String coverUrl = vMeta.getHqImageUrl();
//                        coverUrl = coverUrl.replace("http", "https");

                        boolean check = utils.fileExists(d) || utils.videoExists(d);
                        progressDialog.dismiss();
                        if (check) {
                            Toast.makeText(context, "Already Downloaded", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(DownloadActivity.this, CommandExampleActivity.class);
                            intent.putExtra(Constants.URL, downloadUrl);
                            intent.putExtra(Constants.SONG_NAME, d);
                            intent.putExtra(Constants.ID, Constants.getVideoId(videoLink));
                            intent.putExtra(Constants.videoLink, downloadUrl);
                            intent.putExtra(Constants.SONG_ALBUM_NAME, title);
                            intent.putExtra(Constants.SONG_COVER_URL, "");
                            intent.putExtra(Constants.FROM_INTENT, true);
                            startActivity(intent);
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


//        YTExtractor ytExtractor = new YTExtractor(this, true, true, 3);
//        ytExtractor.extract(Constants.getVideoId(videoLink), new Continuation<Unit>() {
//            @NonNull
//            @Override
//            public CoroutineContext getContext() {
//                return EmptyCoroutineContext.INSTANCE;
//            }
//
//            @Override
//            public void resumeWith(@NonNull Object o) {
//                Log.d(TAG, "resumeWith: SUCCESS");
//                SparseArray<com.maxrave.kotlinyoutubeextractor.YtFile> ytFiles = ytExtractor.getYTFiles();
//                if (ytFiles != null) {
//                    for (int atag : Constants.audio_iTag) {
//                        if (ytFiles.get(atag) != null) {
//                            YtFile ytFile = ytFiles.valueAt(atag);
//                            String downloadUrl = ytFile.getUrl();
//                            Log.d(TAG, "getSong: DOWNLOAD : " + downloadUrl);
//                        }
//                    }
//                } else {
//                    Log.d(TAG, "Failed to extract video information.");
//                }
//            }
//        });

/*
        Log.d(TAG, "link: " + link);
        new YouTubeExtractor(this) {
            @Override
            protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                Log.d(TAG, "onExtractionComplete");
                if (ytFiles != null) {
                    String downloadUrl = "";
                    String audioURL = "";
                    try {
                        for (int vtag : Constants.video_iTag) {
                            if (ytFiles.get(vtag) != null) {
                                downloadUrl = ytFiles.get(vtag).getUrl();
                                Log.d("VideoSError", "vTag " + vtag);
                                break;
                            }
                        }
                        for (int atag : Constants.audio_iTag) {
                            if (ytFiles.get(atag) != null) {
                                audioURL = ytFiles.get(atag).getUrl();
                                Log.d("VideoSError", "aTag " + atag);
                                break;
                            }
                        }

                        String d = vMeta.getTitle();
                        for (String s : Constants.special) {
                            if (d.contains(s)) {
                                d = d.replace(s, "");
                            }
                        }

                        d = d.trim();

                        String coverUrl = vMeta.getHqImageUrl();
                        coverUrl = coverUrl.replace("http", "https");

                        boolean check = utils.fileExists(d) || utils.videoExists(d);

                        if (check) {
                            Toast.makeText(context, "Already Downloaded", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent intent = new Intent(DownloadActivity.this, CommandExampleActivity.class);
                            intent.putExtra(Constants.URL, audioURL);
                            intent.putExtra(Constants.SONG_NAME, d);
                            intent.putExtra(Constants.ID, Constants.getVideoId(videoLink));
                            intent.putExtra(Constants.videoLink, downloadUrl);
                            intent.putExtra(Constants.SONG_ALBUM_NAME, vMeta.getAuthor());
                            intent.putExtra(Constants.SONG_COVER_URL, coverUrl);
                            intent.putExtra(Constants.FROM_INTENT, true);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Video link is not valid", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                } else {
                    progressDialog.dismiss();
                    Log.d(TAG, "onExtractionComplete: ytFiles == NULL" );
                    Toast.makeText(context, "No Video Found", Toast.LENGTH_SHORT).show();
                }
            }
        }.extract(link, true, true);
     */
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}