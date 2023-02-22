package com.moutamid.meusom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

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
            progressDialog.show();
            getSong(url);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void getSong(String videoLink) {
        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
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
                }
            }
        }.extract(videoLink);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}