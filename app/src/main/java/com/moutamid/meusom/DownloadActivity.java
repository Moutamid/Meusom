package com.moutamid.meusom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
                finish();
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

        if (TextUtils.isEmpty(getVideoId(url))) {
            editText.setError("Wrong url!");
        } else {
            Constants.databaseReference().child(Constants.SONGS)
                    .child(Constants.auth().getCurrentUser().getUid())
                    .orderByChild("songYTUrl")
                    .equalTo(getVideoId(url))
                    .get().addOnSuccessListener(dataSnapshot -> {
                        if (dataSnapshot.exists()) {
                            Toast.makeText(DownloadActivity.this, "ALREADY DOWNLOADED", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.show();
                            getSong(url);
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
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
                        int vtag = 22;
                        int atag = 140;
                        downloadUrl = ytFiles.get(vtag).getUrl();
                        audioURL = ytFiles.get(atag).getUrl();
                        Intent intent = new Intent(DownloadActivity.this, CommandExampleActivity.class);
                        intent.putExtra(Constants.URL, audioURL);

                        String d = vMeta.getTitle();
                        for (String s : Constants.special){
                            if (d.contains(s)){
                                d = d.replace(s, "");
                            }
                        }
                        String coverUrl = vMeta.getHqImageUrl();
                        coverUrl = coverUrl.replace("http", "https");

                        intent.putExtra(Constants.SONG_NAME, d);
                        intent.putExtra(Constants.ID, getVideoId(videoLink));
                        intent.putExtra(Constants.videoLink, downloadUrl);
                        intent.putExtra(Constants.SONG_ALBUM_NAME, vMeta.getAuthor());
                        intent.putExtra(Constants.SONG_COVER_URL, coverUrl);
                        intent.putExtra(Constants.FROM_INTENT, true);
                        startActivity(intent);
                    } catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(context, "Video link is not valid", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            }
        }.extract(videoLink);

    }

    private static String getVideoId(@NonNull String videoUrl) {
        String videoId = "";
        String regex = "http(?:s)?:\\/\\/(?:m.)?(?:www\\.)?youtu(?:\\.be\\/|be\\.com\\/(?:watch\\?(?:feature=youtu.be\\&)?v=|v\\/|embed\\/|user\\/(?:[\\w#]+\\/)+))([^&#?\\n]+)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);
        if (matcher.find()) {
            videoId = matcher.group(1);
        }
        return videoId;
    }
}