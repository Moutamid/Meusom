package com.moutamid.meusom;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.meusom.adapter.DownloadAdapter;
import com.moutamid.meusom.models.SongIDModel;
import com.moutamid.meusom.models.SongModel;
import com.moutamid.meusom.utilis.Constants;

import java.util.ArrayList;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class WaitingActivity extends AppCompatActivity {
    private ArrayList<SongModel> songModelArrayList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        Constants.databaseReference().child(Constants.SONGS)
                .child(Constants.auth().getCurrentUser().getUid())
                .get().addOnSuccessListener(dataSnapshot -> {
                   if (dataSnapshot.exists()){
                       songModelArrayList.clear();
                       for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                           SongIDModel songModel1 = snapshot.getValue(SongIDModel.class);
                           if (songModel1!=null){
                               getData(songModel1, snapshot.getKey());
                           }
                       }
                       Intent intent = new Intent(WaitingActivity.this, MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                       finish();
                   } else {
                       Intent intent = new Intent(WaitingActivity.this, MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                       finish();
                   }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    @SuppressLint("StaticFieldLeak")
    private void getData(SongIDModel songModel1, String key) {
        String link = "https://www.youtube.com/watch?v=" + songModel1.getSongYTUrl();
        Log.d("videoID", songModel1.getSongYTUrl());
        Log.d("videoID", link);
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

                        String d = vMeta.getTitle();
                        for (String s : Constants.special){
                            if (d.contains(s)){
                                d = d.replace(s, "");
                            }
                        }

                        SongModel model = new SongModel();
                        model.setId(songModel1.getSongYTUrl());
                        model.setSongYTUrl(audioURL);
                        model.setSongName(d);

                        model.setType(Stash.getString(songModel1.getSongYTUrl()));

                        model.setSongAlbumName(vMeta.getAuthor());

                        String coverUrl = vMeta.getHqImageUrl();
                        coverUrl = coverUrl.replace("http", "https");
                        model.setSongCoverUrl(coverUrl);

                        model.setSongVideoURL(downloadUrl);
                        model.setSongPushKey(key);

                        songModelArrayList.add(model);

                        Stash.put(Constants.OFF_DATA, songModelArrayList);

                    } catch (Exception e){
                        e.printStackTrace();
                        //Toast.makeText(WaitingActivity.this, "Video link is not valid", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }.extract(link);
    }
}