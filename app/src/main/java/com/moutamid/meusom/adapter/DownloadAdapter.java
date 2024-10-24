package com.moutamid.meusom.adapter;

import static com.bumptech.glide.Glide.with;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.fxn.stash.Stash;
import com.google.android.material.progressindicator.CircularProgressIndicator;
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
        Glide.with(context).load(model.getSongCoverUrl()).placeholder(R.drawable.music).into(holder.songCoverImage);

        holder.cancel.setOnClickListener(v -> {
            PRDownloader.cancel(holder.item);
            model.setType("");
            holder.progress.setVisibility(View.GONE);
            holder.cancel.setVisibility(View.GONE);
            holder.downloadButton.setVisibility(View.VISIBLE);
            holder.downloadStatus.setText("Canceled");
        });

        holder.downloadButton.setOnClickListener(v -> {
            if (model.getType().isEmpty()){
                downloadBothDialoge(model);
            } else {
                File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Meusom/");

                String d = model.getSongName();

                if (model.getType().equals("video")) {
                    d = d + ".mp4";
                } else {
                    d = d + ".mp3";
                }
                Log.d("VideoSError", "d : " + d);
                String downloadUrl = model.getType().equals("video") ? model.getSongVideoURL() : model.getSongYTUrl();
                Log.d("VideoSError", "downloadUrl : " + downloadUrl);
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
                                if (model.getSongPushKey() == null){
                                    if (Constants.auth().getCurrentUser() != null) {
                                        String pushkey = Constants.databaseReference().child(Constants.SONGS)
                                                .child(Constants.auth().getCurrentUser().getUid()).push().getKey();
                                        Constants.databaseReference().child(Constants.SONGS)
                                                .child(Constants.auth().getCurrentUser().getUid()).child(pushkey)
                                                .setValue(map).addOnCompleteListener(task -> {
                                                    model.setSongPushKey(pushkey);
                                                    Toast.makeText(context, "Download Complete", Toast.LENGTH_SHORT).show();
                                                });
                                        ArrayList<SongModel> songModelArrayList = Stash.getArrayList(Constants.OFF_DATA, SongModel.class);

                                        if (!songModelArrayList.contains(model)){
                                            songModelArrayList.add(model);
                                            Stash.put(Constants.OFF_DATA, songModelArrayList);
                                        }
                                    }
                                }
                                holder.progress.setVisibility(View.GONE);
                                holder.cancel.setVisibility(View.GONE);
                                if (model.getType().equals("video")) {
                                    holder.audiovideo.setText("Download Video");
                                    holder.audiovideo.setEnabled(false);
                                } else {
                                    holder.audiovideo.setText("Download Audio");
                                }
                                holder.audiovideo.setVisibility(View.VISIBLE);
                                holder.downloadStatus.setText("Download Complete");
                                int i = list.indexOf(model);
                                notifyItemChanged(i);

                            }

                            @Override
                            public void onError(Error error) {
                                holder.progress.setVisibility(View.GONE);
                                holder.cancel.setVisibility(View.GONE);
                                holder.downloadButton.setVisibility(View.VISIBLE);
                                holder.downloadStatus.setText("Something went wrong");
                                model.setType("");
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
            }
        });

        holder.audiovideo.setOnClickListener(v -> {
            File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Meusom/");

            String d = model.getSongName().trim();

            if (holder.audiovideo.getText().toString().contains("Video")) {
                d = d + ".mp4";
            } else {
                d = d + ".mp3";
            }
            Log.d("VideoSError", "d : " + d);
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
                            int i = list.indexOf(model);
                            notifyItemChanged(i);
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

        holder.type.setVisibility(View.VISIBLE);

        if (utils.fileExists(model.getSongName()) && utils.videoExists(model.getSongName())) {
            holder.audiovideo.setVisibility(View.GONE);
            holder.type.setText("MP4 | MP3");
        } else if (utils.fileExists(model.getSongName())) {
            holder.audiovideo.setVisibility(View.VISIBLE);
            holder.audiovideo.setText("Download Video");
            holder.type.setText("MP3");
        } else if (utils.videoExists(model.getSongName())) {
            holder.audiovideo.setVisibility(View.VISIBLE);
            holder.audiovideo.setText("Download Audio");
            holder.type.setText("MP4");
        } else {
            holder.audiovideo.setVisibility(View.GONE);
            /*holder.audiovideo.setText("Download Audio/Video");*/
            holder.type.setVisibility(View.GONE);
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
                if (utils.fileExists(model.getSongName()) || utils.videoExists(model.getSongName())) {
                    showDeleteDialog(model, holder.type);
                } else {
                    Constants.databaseReference().child(Constants.SONGS)
                            .child(Constants.auth().getCurrentUser().getUid())
                            .child(model.getSongPushKey())
                            .removeValue();
                    int i = list.indexOf(model);
                    list.remove(model);
                    Stash.put(Constants.OFF_DATA, list);
                    notifyItemRemoved(i);
                }
            }
        });
    }

    private void showDeleteDialog(SongModel model, TextView type) {

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delete_cnfrm_layout);

        final Dialog fileDialog = new Dialog(context);
        fileDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fileDialog.setContentView(R.layout.delete_layout);

        Button yes = dialog.findViewById(R.id.yes);
        Button no = dialog.findViewById(R.id.no);

        Button file = fileDialog.findViewById(R.id.file);
        Button fileData = fileDialog.findViewById(R.id.fileData);

        int i = list.indexOf(model);

        Log.d("FileDelete", "File " + type.getText());

        fileData.setOnClickListener(v -> {
            if(type.getText().equals("MP4 | MP3")) {
                showBothDialoge(true, model);
                Constants.databaseReference().child(Constants.SONGS)
                        .child(Constants.auth().getCurrentUser().getUid())
                        .child(model.getSongPushKey())
                        .removeValue();
            } else if (type.getText().equals("MP4")) {
                File fdelete = new File(utils.getVideoPath(model.getSongName()));
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        Constants.databaseReference().child(Constants.SONGS)
                                .child(Constants.auth().getCurrentUser().getUid())
                                .child(model.getSongPushKey())
                                .removeValue();
                        Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
                    }
                }
                list.remove(model);
                Stash.put(Constants.OFF_DATA, list);
                notifyItemRemoved(i);
            } else if (type.getText().equals("MP3")) {
                File fdelete = new File(utils.getSongPath(model.getSongName()));

                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        Constants.databaseReference().child(Constants.SONGS)
                                .child(Constants.auth().getCurrentUser().getUid())
                                .child(model.getSongPushKey())
                                .removeValue();
                        Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
                    }
                }
                list.remove(model);
                Stash.put(Constants.OFF_DATA, list);
                notifyItemRemoved(i);
            }

            fileDialog.dismiss();
        });

        file.setOnClickListener(v -> {
            if(type.getText().equals("MP4 | MP3")) {
                showBothDialoge(false, model);
            } else if (type.getText().equals("MP4")) {
                File fdelete = new File(utils.getVideoPath(model.getSongName()));
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
                        model.setType("");
                        notifyItemChanged(i);
                    }
                }
            } else if (type.getText().equals("MP3")) {
                File fdelete = new File(utils.getSongPath(model.getSongName()));
                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
                        model.setType("");
                        notifyItemChanged(i);
                    }
                }
            }


            fileDialog.dismiss();
        });

        yes.setOnClickListener(v -> {
            dialog.dismiss();
            fileDialog.show();
        });

        no.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        fileDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fileDialog.getWindow().setGravity(Gravity.CENTER);

    }

    private void showBothDialoge(boolean b, SongModel model) {
        final Dialog fileDialog = new Dialog(context);
        fileDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fileDialog.setContentView(R.layout.delete_layout);

        Button audio = fileDialog.findViewById(R.id.file);
        Button video = fileDialog.findViewById(R.id.fileData);
        TextView text = fileDialog.findViewById(R.id.text);


        int i = list.indexOf(model);

        text.setText("Which file you want to delete?");
        audio.setText("Audio");
        video.setText("Video");

        audio.setOnClickListener(v -> {
            File fdelete = new File(utils.getSongPath(model.getSongName()));
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    model.setType("audio");
                    notifyItemChanged(i);
                    fileDialog.dismiss();
                    Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        video.setOnClickListener(v -> {
            File fdelete = new File(utils.getVideoPath(model.getSongName()));

            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    model.setType("audio");
                    notifyItemChanged(i);
                    fileDialog.dismiss();
                    Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
                }
            }

//            if (b){
//                if (fdelete.exists()) {
//                    if (fdelete.delete()) {
//                        Constants.databaseReference().child(Constants.SONGS)
//                                .child(Constants.auth().getCurrentUser().getUid())
//                                .child(model.getSongPushKey())
//                                .removeValue();
//                        notifyItemChanged(i);
//                        fileDialog.dismiss();
//                        Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            } else {
//                if (fdelete.exists()) {
//                    if (fdelete.delete()) {
//                        model.setType("audio");
//                        notifyItemChanged(i);
//                        fileDialog.dismiss();
//                        Toast.makeText(context, "File Deleted", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }

        });

        fileDialog.show();
        fileDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fileDialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void downloadBothDialoge(SongModel model) {
        final Dialog fileDialog = new Dialog(context);
        fileDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        fileDialog.setContentView(R.layout.delete_layout);

        Button audio = fileDialog.findViewById(R.id.file);
        Button video = fileDialog.findViewById(R.id.fileData);

        video.setEnabled(false);
        TextView text = fileDialog.findViewById(R.id.text);

        text.setText("Which file you want to download");
        audio.setText("Audio");
        video.setText("Video (Not Available)");

        audio.setOnClickListener(v -> {
            model.setType("audio");
            fileDialog.dismiss();
        });

        video.setOnClickListener(v -> {
            model.setType("video");
            fileDialog.dismiss();
        });

        fileDialog.show();
        fileDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fileDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        fileDialog.getWindow().setGravity(Gravity.CENTER);
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
