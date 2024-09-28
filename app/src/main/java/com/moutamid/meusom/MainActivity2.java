package com.moutamid.meusom;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;


import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;
import com.moutamid.meusom.utilis.YourService;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

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

                audio.setOnClickListener(v->{
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

                        d = d.trim();

                        String coverUrl = vMeta.getHqImageUrl();
                        coverUrl = coverUrl.replace("http", "https");

                        boolean check = utils.fileExists(d) || utils.videoExists(d);

                        if (check) {
                            Toast.makeText(getApplicationContext(), "Already Downloaded", Toast.LENGTH_SHORT).show();
                        } else {
                            mYourService = new YourService();
                            mServiceIntent = new Intent(getApplicationContext(), mYourService.getClass());
                            mServiceIntent.putExtra(Constants.URL, audioURL);
                            mServiceIntent.putExtra(Constants.SONG_NAME, d);
                            mServiceIntent.putExtra(Constants.ID, Constants.getVideoId(videoLink));
                            mServiceIntent.putExtra(Constants.videoLink, downloadUrl);
                            mServiceIntent.putExtra(Constants.SONG_ALBUM_NAME, vMeta.getAuthor());
                            mServiceIntent.putExtra(Constants.SONG_COVER_URL, coverUrl);
                            mServiceIntent.putExtra(Constants.TYPE, type);
                            // intent.putExtra(Constants.FROM_INTENT, true);
//        if (!isMyServiceRunning(mYourService.getClass())) {
                            startService(mServiceIntent);
                            doneLoading("Download started");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Video link is not valid", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.dismiss();
                }
            }
        }.extract(videoLink,false, false);

    }

    private void doneLoading(String e) {
        Log.e(TAG, "Json parsing error: " + e);

        if (progressDialog.isShowing())
            progressDialog.dismiss();

        Toast.makeText(getApplicationContext(), e, Toast.LENGTH_LONG).show();

        finish();
    }

}