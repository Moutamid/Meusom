package com.moutamid.meusom.fragments;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.meusom.R.color.darkerGrey;
import static com.moutamid.meusom.R.color.darkgray;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.stash.Stash;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.meusom.VideoPlayerActivity;
import com.moutamid.meusom.models.SongIDModel;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.R;
import com.moutamid.meusom.utilis.Utils;
import com.moutamid.meusom.models.SongModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class VideoListFragment extends Fragment {
    private Utils utils = new Utils();
    private ArrayList<SongModel> currentPlaylistList = new ArrayList<>();
    private ArrayList<SongModel> songModelArrayList = new ArrayList<>();
    private RecyclerView conversationRecyclerView;
    private RecyclerView playlistRecyclerView;
    private ArrayList<SongModel> songModelArrayListAll = new ArrayList<>();
    private RecyclerViewAdapterMessages adapter;
    private View view;
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Context context = getActivity();

        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.ENGLISH)) {
            utils.changeLanguage(context, "en");
        } else if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {
            utils.changeLanguage(context, "pr");
        }

        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please Wait...");

        view = inflater.inflate(R.layout.videolist_fragment, container, false);

        initRecyclerView(view);

        EditText searchEt = view.findViewById(R.id.searchEditTextTracks);
        view.findViewById(R.id.crossBtnTracks).setOnClickListener(view1 -> {
            searchEt.setText("");
        });
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapter.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        view.findViewById(R.id.sortBtnTracks).setOnClickListener(view1 -> {
            PopupMenu popupMenu = new PopupMenu(requireContext(), view.findViewById(R.id.sortBtnTracks));
            popupMenu.getMenuInflater().inflate(
                    R.menu.popup_menu_options,
                    popupMenu.getMenu()
            );
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    String sortType = "null";
                    if (menuItem.getItemId() == R.id.titleAscending) {
                        Collections.sort(songModelArrayList,
                                (songModel, t1) -> songModel.getSongName().compareTo(t1.getSongName()));
                        sortType = Constants.T_ASCENDING;
                    }
                    if (menuItem.getItemId() == R.id.titleDescending) {
                        Collections.sort(songModelArrayList,
                                (songModel, t1) -> songModel.getSongName().compareTo(t1.getSongName()));
                        Collections.reverse(songModelArrayList);
                        sortType = Constants.T_DESCENDING;
                    }
                    if (menuItem.getItemId() == R.id.albumAscending) {
                        Collections.sort(songModelArrayList,
                                (songModel, t1) -> songModel.getSongAlbumName().compareTo(t1.getSongAlbumName()));
                        sortType = Constants.ALB_ASCENDING;
                    }
                    if (menuItem.getItemId() == R.id.albumDescending) {
                        Collections.sort(songModelArrayList,
                                (songModel, t1) -> songModel.getSongAlbumName().compareTo(t1.getSongAlbumName()));
                        Collections.reverse(songModelArrayList);
                        sortType = Constants.ALB_DESCENDING;
                    }
                    if (menuItem.getItemId() == R.id.originalOrder) {
                        songModelArrayList = songModelArrayListAll;
                        sortType = Constants.ORIGINAL;
                    }
                    if (menuItem.getItemId() == R.id.reverseOrder) {
                        Collections.reverse(songModelArrayList);
                        sortType = Constants.REVERSED;
                    }
                    adapter.notifyDataSetChanged();

                    utils.storeString(requireContext(), Constants.SORT, sortType);

                    return true;
                }
            });
            popupMenu.show();
        });

        return view;
    }

    /*public void showTracksSearchBar() {
        view.findViewById(R.id.searchLayoutTracks).setVisibility(View.VISIBLE);
    }

    public void hideTracksSearchBar() {
        view.findViewById(R.id.searchLayoutTracks).setVisibility(GONE);
    }*/

    private void initRecyclerView(View view) {
        progressDialog.show();

        conversationRecyclerView = view.findViewById(R.id.tracksRecyclerView);
//        conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
//        conversationRecyclerView.setNestedScrollingEnabled(false);
        conversationRecyclerView.setItemViewCacheSize(20);

        LoadData();
    }

    private void LoadData() {
        ArrayList<SongModel> list = Stash.getArrayList(Constants.OFF_DATA, SongModel.class);
        songModelArrayList.clear();
        songModelArrayListAll.clear();
        for (SongModel model : list) {
            if (utils.videoExists(model.getSongName())) {
                songModelArrayList.add(model);
                songModelArrayListAll.add(model);
            }
        }
        sortTheList();
        adapter = new RecyclerViewAdapterMessages();
        conversationRecyclerView.setAdapter(adapter);

        progressDialog.dismiss();

    }


    private void sortTheList() {
        String sortType = utils.getStoredString(requireContext(), Constants.SORT);

        if (sortType.equals(Constants.T_ASCENDING)) {
            Collections.sort(songModelArrayList,
                    Comparator.comparing(SongModel::getSongName));
        }
        if (sortType.equals(Constants.T_DESCENDING)) {
            Collections.sort(songModelArrayList,
                    Comparator.comparing(SongModel::getSongName));
            Collections.reverse(songModelArrayList);
        }
        if (sortType.equals(Constants.ALB_ASCENDING)) {
            Collections.sort(songModelArrayList,
                    Comparator.comparing(SongModel::getSongAlbumName));
        }
        if (sortType.equals(Constants.ALB_DESCENDING)) {
            Collections.sort(songModelArrayList,
                    Comparator.comparing(SongModel::getSongAlbumName));
            Collections.reverse(songModelArrayList);
        }
        if (sortType.equals(Constants.ORIGINAL)) {
            songModelArrayList = songModelArrayListAll;
        }
        if (sortType.equals(Constants.REVERSED)) {
            Collections.reverse(songModelArrayList);
        }

    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> implements Filterable {

        @NonNull
        @Override
        public RecyclerViewAdapterMessages.ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_download_item, parent, false);
            return new RecyclerViewAdapterMessages.ViewHolderRightMessage(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerViewAdapterMessages.ViewHolderRightMessage holder, int position1) {

            int position = holder.getAdapterPosition();

            SongModel model = songModelArrayList.get(position);

            holder.songName.setText(model.getSongName());
            holder.songAlbumName.setText(model.getSongAlbumName());

            Glide.with(requireContext()).load(model.getSongCoverUrl()).placeholder(R.color.red).into(holder.songCoverImage);

            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(requireContext(), VideoPlayerActivity.class);
                    i.putExtra("name", model.getSongName());
                    startActivity(i);

                }
            });

            holder.downloadButton.setVisibility(GONE);
            holder.downloadStatus.setVisibility(GONE);
            holder.deleteBtn.setVisibility(GONE);
/*
            if (fileExists(model.getSongName() + ".mp3")) {
                holder.downloadStatus.setText(Constants.COMPLETED);
                holder.downloadButton.setImageResource(R.drawable.off_track);
            } else {
                holder.downloadStatus.setText(Constants.NOT_DOWNLOADED);
                holder.downloadButton.setImageResource(R.drawable.donwloadtrack);
            }

            holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (holder.downloadStatus.getText()
                            .toString().equals(Constants.COMPLETED)) {
                        Toast.makeText(context, "This song is already available offline!", Toast.LENGTH_SHORT).show();
//                        showDeleteDialog(model.getSongName() + ".mp3");
                    } else {

                    Toast.makeText(context, model.getSongYTUrl(), Toast.LENGTH_SHORT).show();
//                        runCommand(model.getSongYTUrl(), holder, model.getSongPushKey());

                    }
                }
            });
*/

        }

/*
        private void showDeleteDialog(String fileName) {
            utils.showDialog(context,
                    "Are you sure?",
                    "Do you want to delete this file?",
                    "Yes",
                    "No",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    }, true);
        }
*/

//        private boolean fileExists(String name) {
//            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
//                    File.separator + "Meusom." + File.separator
//                    + name;
////                    + "Shot on iPhone mem.mp3";
//
//            File file = new File(path);
//            return file.exists();
//
//        }

        @Override
        public int getItemCount() {
            if (songModelArrayList == null)
                return 0;
            return songModelArrayList.size();
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String key = charSequence.toString();
                    if (key.isEmpty()) {
                        songModelArrayList = songModelArrayListAll;
                    } else {
                        ArrayList<SongModel> filtered = new ArrayList<>();

                        for (SongModel model : songModelArrayListAll) {
                            if (model.getSongName().toLowerCase().contains(key.toLowerCase())) {
                                filtered.add(model);
                            } else if (model.getSongAlbumName().toLowerCase().contains(key.toLowerCase())) {
                                filtered.add(model);
                            } else if (model.getSongYTUrl().toLowerCase().contains(key.toLowerCase())) {
                                filtered.add(model);
                            }
                        }
                        songModelArrayList = filtered;
                    }
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = songModelArrayList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    songModelArrayList = (ArrayList<SongModel>) filterResults.values;
                    adapter.notifyDataSetChanged();
                }
            };
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

}
