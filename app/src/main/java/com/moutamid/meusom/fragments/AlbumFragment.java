package com.moutamid.meusom.fragments;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.meusom.R.color.darkerGrey;
import static com.moutamid.meusom.R.color.darkgray;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.R;
import com.moutamid.meusom.utilis.Utils;
import com.moutamid.meusom.models.SongModel;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {
    private Utils utils = new Utils();
    private ArrayList<SongModel> songModelArrayList = new ArrayList<>();
    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context context = getActivity();

        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.ENGLISH)) {
            utils.changeLanguage(context, "en");
        } else if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {
            utils.changeLanguage(context, "pr");
        }
        View view = inflater.inflate(R.layout.album_fragment, container, false);

        initRecyclerView(view);

        return view;
    }

    private void initRecyclerView(View view) {

        conversationRecyclerView = view.findViewById(R.id.albumRecyclerView);
        conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
//        conversationRecyclerView.setNestedScrollingEnabled(false);
        conversationRecyclerView.setItemViewCacheSize(20);

        new LoadData().execute();

    }


    private class LoadData extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Constants.databaseReference().child(Constants.SONGS)
                    .child(Constants.auth().getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (!snapshot.exists()) {
                                return;
                            }

                            songModelArrayList.clear();

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                SongModel songModel1 = dataSnapshot.getValue(SongModel.class);
                                if (utils.fileExists(songModel1.getSongName())) {

                                    songModel1.setSongPushKey(dataSnapshot.getKey());
                                    songModelArrayList.add(songModel1);
                                }
                            }

                            requireActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter = new RecyclerViewAdapterMessages();
                                    conversationRecyclerView.setAdapter(adapter);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
//                            Toast.makeText(getActivity(), error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return null;
        }

    }


    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_download_item, parent, false);
            return new ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position1) {

            int position = holder.getAdapterPosition();

            SongModel model = songModelArrayList.get(position);

            holder.songAlbumName.setText(model.getSongName());
            holder.songName.setText(model.getSongAlbumName());

            Glide.with(requireContext()).load(model.getSongCoverUrl()).placeholder(R.color.red).into(holder.songCoverImage);

            holder.downloadButton.setVisibility(GONE);
            holder.downloadStatus.setVisibility(GONE);
            holder.deleteBtn.setVisibility(GONE);

            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    utils.storeBoolean(getActivity(), Constants.IS_PLAYLIST, false);

                    Intent intent = getActivity().getIntent();
                    intent.putExtra(Constants.SONG_INDEX, position);
                    getActivity().setResult(RESULT_OK, intent);
                    getActivity().finish();

                }
            });

//            if (fileExists(model.getSongName() + ".mp3")) {
//                holder.downloadStatus.setText(Constants.COMPLETED);
//                holder.downloadButton.setImageResource(R.drawable.off_track);
//            } else {
//                holder.downloadStatus.setText(Constants.NOT_DOWNLOADED);
//                holder.downloadButton.setImageResource(R.drawable.donwloadtrack);
//            }
//
//            holder.downloadButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    if (holder.downloadStatus.getText()
//                            .toString().equals(Constants.COMPLETED)) {
//                        Toast.makeText(context, "This song is already available offline!", Toast.LENGTH_SHORT).show();
////                        showDeleteDialog(model.getSongName() + ".mp3");
//                    } else {
//
//                    Toast.makeText(context, model.getSongYTUrl(), Toast.LENGTH_SHORT).show();
////                        runCommand(model.getSongYTUrl(), holder, model.getSongPushKey());
//
//                    }
//                }
//            });

        }

//        private void showDeleteDialog(String fileName) {
//            utils.showDialog(context,
//                    "Are you sure?",
//                    "Do you want to delete this file?",
//                    "Yes",
//                    "No",
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//                        }
//                    }, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            dialogInterface.dismiss();
//                        }
//                    }, true);
//        }

        @Override
        public int getItemCount() {
            if (songModelArrayList == null)
                return 0;
            return songModelArrayList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            ImageView downloadButton, songCoverImage, deleteBtn;
            TextView songName, songAlbumName, downloadStatus;
            RelativeLayout parentLayout;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                downloadButton = v.findViewById(R.id.downloadBtnCommand);
                songCoverImage = v.findViewById(R.id.song_cover_Command);
                songName = v.findViewById(R.id.song_nameCommand);
                songAlbumName = v.findViewById(R.id.song_albumCommand);
                downloadStatus = v.findViewById(R.id.download_statusCommand);
                parentLayout = v.findViewById(R.id.parentLayout_downloadItem);
                deleteBtn = v.findViewById(R.id.deleteBtnCommand);

            }
        }

    }

//    private String getSongPath(String name) {
//        String path_save_vid = "";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            path_save_vid =
//                    Environment
//                            .getExternalStoragePublicDirectory(
//                                    Environment.DIRECTORY_DOWNLOADS) +
//                            File.separator
//                            + "Meusom."
//                            + File.separator
//                            + name
//                            + ".mp3";
//
//        } else {
//            path_save_vid =
//                    Environment
//                            .getExternalStorageDirectory().getAbsolutePath() +
//                            File.separator +
//                            getResources().getString(R.string.app_name)
//                            + "Meusom."
//                            + File.separator
//                            + name
//                            + ".mp3";
//
//        }
//        return path_save_vid;
//    }
}
