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
import java.util.HashMap;
import java.util.Map;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class WaitingActivity extends AppCompatActivity {
    private ArrayList<SongModel> songModelArrayList = new ArrayList<>();
    private ArrayList<Model> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        Stash.clear(Constants.OFF_DATA);

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
                        getData();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    @SuppressLint("StaticFieldLeak")
    private void getData() {
        for (int i = 0; i < list.size(); i++) {
            String link = "https://www.youtube.com/watch?v=" + list.get(i).id;
            int finalI = i;
            Log.d("LOGINOFF", "loop : " + i);
            Log.d("LOGINOFF", "finalI 1  : " + finalI);
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
                                    break;
                                }
                            }
                            for (int atag : Constants.audio_iTag) {
                                if (ytFiles.get(atag) != null) {
                                    audioURL = ytFiles.get(atag).getUrl();
                                    break;
                                }
                            }

                            String d = vMeta.getTitle();
                            for (String s : Constants.special) {
                                if (d.contains(s)) {
                                    d = d.replace(s, "");
                                }
                            }

                            Log.d("LOGINOFF", "finalI 2  : " + finalI);
                            SongModel model = new SongModel();
                            model.setId(list.get(finalI).id);
                            model.setSongYTUrl(audioURL);
                            model.setSongName(d);

                            model.setType("");

                            model.setSongAlbumName(vMeta.getAuthor());

                            String coverUrl = vMeta.getHqImageUrl();
                            coverUrl = coverUrl.replace("http", "https");
                            model.setSongCoverUrl(coverUrl);

                            model.setSongVideoURL(downloadUrl);
                            model.setSongPushKey(list.get(finalI).key);

                            songModelArrayList.add(model);

                            Stash.put(Constants.OFF_DATA, songModelArrayList);

                            if (finalI == list.size() - 1) {
                                Log.d("LOGINOFF", "iNTENT");
                                Intent intent = new Intent(WaitingActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            //Toast.makeText(WaitingActivity.this, "Video link is not valid", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }.extract(link);

        }
    }

    class Model {
        String id, key;

        public Model() {
        }
    }

}