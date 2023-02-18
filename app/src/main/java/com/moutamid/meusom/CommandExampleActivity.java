package com.moutamid.meusom;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.meusom.R.color.darkerGrey;
import static com.moutamid.meusom.R.color.darkgray;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.meusom.adapter.DownloadAdapter;
import com.moutamid.meusom.models.SongModel;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;

import java.io.File;
import java.util.ArrayList;

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
    private Button btnRunCommand;
    private EditText etCommand;
    private ProgressBar progressBar;
    private TextView tvCommandStatus;
    private TextView tvCommandOutput;
    private ProgressBar pbLoading;
    ProgressDialog progressDialog;
    private SongModel songModel = new SongModel();
    private boolean running = false;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
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

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...");

        if (getIntent().hasExtra(Constants.URL)) {
            songModel.setSongYTUrl(getIntent().getStringExtra(Constants.URL));
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
                        progressDialog.show();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Audio", (dialog, which) -> {
                        songModel.setType("audio");
                        startDownload();
                        progressDialog.show();
                        dialog.dismiss();
                    }).show();

        } else {
            startDownload();
        }

    }

    private void startDownload() {
        Constants.databaseReference().child(Constants.SONGS)
                .child(Constants.auth().getCurrentUser().getUid())
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        songModelArrayList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            SongModel songModel1 = snapshot.getValue(SongModel.class);
                            songModel1.setSongPushKey(snapshot.getKey());
                            songModelArrayList.add(songModel1);
                        }
                        if (isIntent){
                            songModelArrayList.add(songModel);
                        }
                        TextView tv = findViewById(R.id.songCountTextView);
                        tv.setText("(" + songModelArrayList.size() + ")");
                        adapter = new DownloadAdapter(CommandExampleActivity.this, songModelArrayList);
                        conversationRecyclerView.setAdapter(adapter);

                        progressDialog.dismiss();
                    } else {
                        if (isIntent){
                            songModelArrayList.add(songModel);
                            TextView tv = findViewById(R.id.songCountTextView);
                            tv.setText("(" + songModelArrayList.size() + ")");
                            adapter = new DownloadAdapter(CommandExampleActivity.this, songModelArrayList);
                            conversationRecyclerView.setAdapter(adapter);
                        }

                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                });
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