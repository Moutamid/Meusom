package com.moutamid.meusom;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                                        moveIntent(finalTitle, downloadUrl, videoLink);
                                    });
                                } catch (IOException e) {
                                    mainThreadHandler.post(() -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(DownloadActivity.this, "Failed to fetch the video title.", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                        } else {
                            for (String s : Constants.special) {
                                if (title.contains(s)) {
                                    title = title.replace(s, "");
                                }
                            }
                            moveIntent(title, downloadUrl, videoLink);
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

    private void moveIntent(String title, String downloadUrl, String videoLink) {
        title = title.trim();
        boolean check = utils.fileExists(title) || utils.videoExists(title);
        progressDialog.dismiss();
        if (check) {
            Toast.makeText(context, "Already Downloaded", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(DownloadActivity.this, CommandExampleActivity.class);
            intent.putExtra(Constants.URL, downloadUrl);
            intent.putExtra(Constants.SONG_NAME, title);
            intent.putExtra(Constants.ID, Constants.getVideoId(videoLink));
            intent.putExtra(Constants.videoLink, downloadUrl);
            intent.putExtra(Constants.SONG_ALBUM_NAME, title);
            intent.putExtra(Constants.SONG_COVER_URL, "");
            intent.putExtra(Constants.FROM_INTENT, true);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}