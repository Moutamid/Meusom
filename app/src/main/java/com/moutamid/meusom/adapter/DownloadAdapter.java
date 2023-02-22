package com.moutamid.meusom.adapter;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.meusom.R.color.darkerGrey;
import static com.moutamid.meusom.R.color.darkgray;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.fxn.stash.Stash;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.moutamid.meusom.CommandExampleActivity;
import com.moutamid.meusom.R;
import com.moutamid.meusom.models.SongModel;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.DownloadVH> {

    Context context;
    ArrayList<SongModel> list;
    private Utils utils = new Utils();

    public DownloadAdapter(Context context, ArrayList<SongModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DownloadVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.download_item, parent, false);
        return new DownloadVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DownloadVH holder, int position) {
        SongModel model = list.get(position);

        holder.songName.setText(model.getSongName());
//            holder.songAlbumName.setText(model.getSongAlbumName());
        holder.songAlbumName.setVisibility(View.GONE);
        //Toast.makeText(context, "img " + model.getSongCoverUrl(), Toast.LENGTH_SHORT).show();
        Glide.with(context).load(model.getSongCoverUrl()).placeholder(R.color.red).into(holder.songCoverImage);

        holder.cancel.setOnClickListener(v -> {
            PRDownloader.cancel(holder.item);
            holder.progress.setVisibility(View.GONE);
            holder.cancel.setVisibility(View.GONE);
            holder.downloadButton.setVisibility(View.VISIBLE);
            holder.downloadStatus.setText("Canceled");
        });

        holder.downloadButton.setOnClickListener(v -> {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Meusom./");

            String d = model.getSongName();

            if (model.getType().equals("video")) {
                d = d + ".mp4";
            } else {
                d = d + ".mp3";
            }

            String downloadUrl = model.getType().equals("video") ? model.getSongVideoURL() : model.getSongYTUrl();

            holder.item = PRDownloader.download(downloadUrl, file.getPath(), d)
                    .build()
                    .setOnStartOrResumeListener(() -> {
                        holder.progress.setVisibility(View.VISIBLE);
                        holder.downloadButton.setVisibility(View.GONE);
                        holder.cancel.setVisibility(View.VISIBLE);
                    })
                    .setOnPauseListener(() -> {

                    })
                    .setOnCancelListener(() -> {

                    })
                    .setOnProgressListener(progress -> {
                        long n = progress.currentBytes * 100 / progress.totalBytes;
                        holder.progress.setProgress((int) n, true);
                        holder.downloadStatus.setText("Completed : " + n + "%");
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            Map<String, Object> map = new HashMap<>();
                            map.put("songYTUrl", model.getId());
                            if (Constants.auth().getCurrentUser()!=null){
                                Constants.databaseReference().child(Constants.SONGS)
                                        .child(Constants.auth().getCurrentUser().getUid()).push()
                                        .setValue(map).addOnCompleteListener(task -> {
                                            holder.progress.setVisibility(View.GONE);
                                            holder.cancel.setVisibility(View.GONE);
                                            if (model.getType().equals("video")) {
                                                holder.audiovideo.setText("Download Video");
                                            } else {
                                                holder.audiovideo.setText("Download Audio");
                                            }
                                            holder.audiovideo.setVisibility(View.VISIBLE);
                                            holder.downloadStatus.setText("Download Complete");
                                            
                                            ArrayList<SongModel> songModelArrayList = Stash.getArrayList(Constants.OFF_DATA, SongModel.class);
                                            songModelArrayList.add(model);
                                            Stash.put(Constants.OFF_DATA, songModelArrayList);

                                            Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }

                        @Override
                        public void onError(Error error) {
                            holder.progress.setVisibility(View.GONE);
                            holder.cancel.setVisibility(View.GONE);
                            holder.downloadButton.setVisibility(View.VISIBLE);
                            holder.downloadStatus.setText("Something went wrong");
                            if (error.isServerError()) {
                                Log.d("VideoSError", "Server : " + error.getServerErrorMessage());
                                Toast.makeText(context, "Server Error: " + error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                            } else if (error.isConnectionError()) {
                                Log.d("VideoSError", "Connection : " + error.getConnectionException().getMessage());
                                Log.d("VideoSError", "Connection : " + model.getType());
                                Log.d("VideoSError", "Connection : " + model.getSongName());
                                Log.d("VideoSError", "Connection : " + model.getSongVideoURL());
                                Log.d("VideoSError", "Connection : " + model.getId());
                                Toast.makeText(context, "Connection Error: " + error.getConnectionException().getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("VideoSError", "Error : " + error);
                                Toast.makeText(context, "" + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        holder.audiovideo.setOnClickListener(v -> {
            Log.d("VideoSError", "name : " + model.getSongName());
            Log.d("VideoSError", "link Audio : " + model.getSongYTUrl());
            Log.d("VideoSError", "link video : " + model.getSongVideoURL());
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Meusom./");

            String d = model.getSongName();

            if (holder.audiovideo.getText().toString().contains("Video")) {
                d = d + ".mp4";
            } else {
                d = d + ".mp3";
            }

            String downloadUrl = holder.audiovideo.getText().toString().contains("Video") ? model.getSongVideoURL() : model.getSongYTUrl();

            holder.item = PRDownloader.download(downloadUrl, file.getPath(), d)
                    .build()
                    .setOnStartOrResumeListener(() -> {
                        holder.progress.setVisibility(View.VISIBLE);
                        holder.downloadButton.setVisibility(View.GONE);
                       // holder.cancel.setVisibility(View.VISIBLE);

                        holder.audiovideo.setVisibility(View.GONE);
                    })
                    .setOnPauseListener(() -> {

                    })
                    .setOnCancelListener(() -> {

                    })
                    .setOnProgressListener(progress -> {
                        long n = progress.currentBytes * 100 / progress.totalBytes;
                        holder.progress.setProgress((int) n, true);
                        holder.downloadStatus.setText("Completed : " + n + "%");
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            /*Map<String, Object> map = new HashMap<>();
                            map.put("songYTUrl", model.getId());
                            if (Constants.auth().getCurrentUser()!=null){
                                Constants.databaseReference().child(Constants.SONGS)
                                        .child(Constants.auth().getCurrentUser().getUid()).push()
                                        .setValue(map).addOnCompleteListener(task -> {
                                            holder.progress.setVisibility(View.GONE);
                                            holder.cancel.setVisibility(View.GONE);
                                            holder.downloadStatus.setText("Completed");

                                            ArrayList<SongModel> songModelArrayList = Stash.getArrayList(Constants.OFF_DATA, SongModel.class);
                                            songModelArrayList.add(model);
                                            Stash.put(Constants.OFF_DATA, songModelArrayList);

                                            Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
                                        });
                            }*/
                            holder.progress.setVisibility(View.GONE);
                            holder.cancel.setVisibility(View.GONE);
                            holder.type.setText("MP4 | MP3");
                            holder.downloadStatus.setText("Download Complete");
                            Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Error error) {
                            holder.progress.setVisibility(View.GONE);
                            holder.cancel.setVisibility(View.GONE);
                            holder.downloadButton.setVisibility(View.GONE);
                            holder.audiovideo.setVisibility(View.VISIBLE);
                            holder.downloadStatus.setText("Something went wrong");
                            if (error.isServerError()) {
                                Log.d("VideoSError", "Server : " + error.getServerErrorMessage());
                                Toast.makeText(context, "Server Error: " + error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                            } else if (error.isConnectionError()) {
                                Log.d("VideoSError", "Connection : " + error.getConnectionException().getMessage());
                                Log.d("VideoSError", "Connection : " + model.getType());
                                Log.d("VideoSError", "Connection : " + model.getSongName());
                                Log.d("VideoSError", "Connection : " + model.getSongVideoURL());
                                Log.d("VideoSError", "Connection : " + model.getId());
                                Toast.makeText(context, "Connection Error: " + error.getConnectionException().getMessage(), Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("VideoSError", "Error : " + error);
                                Toast.makeText(context, "" + error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        });

        if (utils.fileExists(model.getSongName()) && utils.videoExists(model.getSongName())){
            holder.audiovideo.setVisibility(View.GONE);
            holder.type.setText("MP4 | MP3");
        } else if (utils.fileExists(model.getSongName())){
            holder.audiovideo.setVisibility(View.VISIBLE);
            holder.audiovideo.setText("Download Video");
            holder.type.setText("MP3");
        } else if (utils.videoExists(model.getSongName())){
            holder.audiovideo.setVisibility(View.VISIBLE);
            holder.audiovideo.setText("Download Audio");
            holder.type.setText("MP4");
        }

        if (utils.fileExists(model.getSongName()) || utils.videoExists(model.getSongName())) {
            holder.downloadStatus.setText(Constants.COMPLETED);
            holder.downloadButton.setVisibility(View.GONE);
            //  holder.downloadButton.setImageResource(0);
        } else {
            holder.downloadStatus.setText(Constants.NOT_DOWNLOADED);
            holder.downloadButton.setImageResource(R.drawable.donwloadtrack);
            holder.downloadButton.setVisibility(View.VISIBLE);
        }

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog(model);
            }
        });


    }

    private void showDeleteDialog(SongModel model) {
        utils.showDialog(context,
                "Are you sure?",
                "Do you want to delete this file?",
                "Yes",
                "No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        utils.showDialog(context,
                                "Choose",
                                "Do you want to retain data?",
                                "Delete file",
                                "Delete(File+Data)",
                                (dialogInterface1, i1) -> {
                                    /*Constants.databaseReference().child(Constants.SONGS)
                                            .child(Constants.auth().getCurrentUser().getUid())
                                            .child(model.getSongPushKey())
                                            .removeValue();*/
                                    list.remove(model);
                                    notifyItemRemoved(list.indexOf(model));
                                    dialogInterface1.dismiss();
                                }, (dialogInterface12, i12) -> {
                                    /*Constants.databaseReference().child(Constants.SONGS)
                                            .child(Constants.auth().getCurrentUser().getUid())
                                            .child(model.getSongPushKey())
                                            .removeValue();*/
                                    if (model.getType().equals("video")) {
                                        File fdelete = new File(utils.getVideoPath(model.getSongName()));
                                        if (fdelete.exists()) {
                                            if (fdelete.delete()) {
                                                Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        File fdelete = new File(utils.getSongPath(model.getSongName()));
                                        if (fdelete.exists()) {
                                            if (fdelete.delete()) {
                                                Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    list.remove(model);
                                    notifyItemRemoved(list.indexOf(model));
                                    dialogInterface12.dismiss();
                                }, true);
                    }
                }, (dialogInterface, i) -> dialogInterface.dismiss(), true);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class DownloadVH extends RecyclerView.ViewHolder {
        ImageView downloadButton, songCoverImage, deleteBtn;
        TextView songName, songAlbumName, downloadStatus, type;
        CircularProgressIndicator progress;
        Button cancel, audiovideo;
        int item;

        public DownloadVH(@NonNull View v) {
            super(v);
            downloadButton = v.findViewById(R.id.downloadBtnCommand);
            songCoverImage = v.findViewById(R.id.song_cover_Command);
            songName = v.findViewById(R.id.song_nameCommand);
            songAlbumName = v.findViewById(R.id.song_albumCommand);
            downloadStatus = v.findViewById(R.id.download_statusCommand);
            deleteBtn = v.findViewById(R.id.deleteBtnCommand);
            type = v.findViewById(R.id.type);
            progress = v.findViewById(R.id.progress);
            cancel = v.findViewById(R.id.cancel);
            audiovideo = v.findViewById(R.id.audiovideo);
        }
    }

}
