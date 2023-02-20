package com.moutamid.meusom;

import static com.bumptech.glide.Glide.with;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.meusom.adapter.DownloadAdapter;
import com.moutamid.meusom.models.SongIDModel;
import com.moutamid.meusom.models.SongModel;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;

import java.util.ArrayList;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import io.reactivex.disposables.CompositeDisposable;

public class CommandExampleActivity extends AppCompatActivity {
    private static final String TAG = "CommandExampleActivity";
    private Context context = CommandExampleActivity.this;
    private ArrayList<SongModel> songModelArrayList = new ArrayList<>();

    private RecyclerView conversationRecyclerView;
    private DownloadAdapter adapter;
    private Utils utils = new Utils();
    private boolean isIntent = false;
    String videoLink;
    ProgressDialog progressDialog;
    private SongModel songModel = new SongModel();
    String[] permission = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.ENGLISH)) {
            utils.changeLanguage(context, "en");
        } else if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {
            utils.changeLanguage(context, "pr");
        }
        setContentView(R.layout.activity_command_example);

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setReadTimeout(30_000)
                .setConnectTimeout(30_000)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);

        findViewById(R.id.backBtnDownloadCommand).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        initRecyclerView();


        if (getIntent().hasExtra(Constants.URL)) {
            songModel.setSongYTUrl(getIntent().getStringExtra(Constants.URL));
            songModel.setId(getIntent().getStringExtra(Constants.ID));
            songModel.setSongName(getIntent().getStringExtra(Constants.SONG_NAME));
            songModel.setSongAlbumName(getIntent().getStringExtra(Constants.SONG_ALBUM_NAME));
            songModel.setSongCoverUrl(getIntent().getStringExtra(Constants.SONG_COVER_URL));
            songModel.setSongVideoURL(getIntent().getStringExtra(Constants.videoLink));
            isIntent = getIntent().getBooleanExtra(Constants.FROM_INTENT, false);
            videoLink = getIntent().getStringExtra(Constants.videoLink);

            ActivityCompat.requestPermissions(CommandExampleActivity.this, permission, 1);

            new AlertDialog.Builder(this)
                    .setTitle("").setMessage("Do you want to download the both video and audio or just the audio song?")
                    .setPositiveButton("Video", (dialog, which) -> {
                        songModel.setType("video");
                        startDownload();
                        dialog.dismiss();
                        Stash.put(songModel.getId(), "video");
                    })
                    .setNegativeButton("Audio", (dialog, which) -> {
                        songModel.setType("audio");
                        startDownload();
                        Stash.put(songModel.getId(), "audio");
                        dialog.dismiss();
                    }).show();

        } else {
            startDownload();
        }

    }

    private void startDownload() {
        songModelArrayList = Stash.getArrayList(Constants.OFF_DATA, SongModel.class);
        if (isIntent){
            songModelArrayList.add(songModel);
        }

        TextView tv = findViewById(R.id.songCountTextView);
        tv.setText("(" + songModelArrayList.size() + ")");
        adapter = new DownloadAdapter(CommandExampleActivity.this, songModelArrayList);
        conversationRecyclerView.setAdapter(adapter);
    }


    private void initRecyclerView() {
        conversationRecyclerView = findViewById(R.id.downloadRecyclerView);
        conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
//        conversationRecyclerView.setNestedScrollingEnabled(false);
        conversationRecyclerView.setItemViewCacheSize(20);
    }

}