package com.moutamid.meusom;

import static com.bumptech.glide.Glide.with;
import static com.bumptech.glide.load.engine.DiskCacheStrategy.DATA;
import static com.moutamid.meusom.R.color.lightBlack;
import static com.moutamid.meusom.R.color.transparent;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.meusom.Services.OnClearFromRecentService;
import com.moutamid.meusom.models.SongIDModel;
import com.moutamid.meusom.models.SongModel;
import com.moutamid.meusom.models.Track;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.CreateNotification;
import com.moutamid.meusom.utilis.Playable;
import com.moutamid.meusom.utilis.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import musicplayer.Utilities;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener, AudioManager.OnAudioFocusChangeListener, Playable, ServiceConnection {
    private static final String TAG = "TAGG";
    private Context context = MainActivity.this;

    private Utils utils = new Utils();

    NotificationManager notificationManager;

    List<Track> tracks;
    OnClearFromRecentService service;
    int position = 0;
    boolean isPlaying = false;

    private LinearLayout bottom_music_layout;
    private RelativeLayout music_player_layout;
    private ImageView btnPlay;
    private ImageView btnPlaySmall;
    //    private ImageView btnForward;
//    private ImageView btnBackward;
    private ImageView btnNext;
    private ImageView btnNextSmall;
    private ImageView btnPrevious;
    //    private ImageView btnPlaylist;
    private ImageView btnRepeat, playVideo;
    private ImageView btnShuffle;
    private SeekBar songProgressBar;
    private SeekBar songProgressBarSmall;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    //    private SongsManager songManager;
    private Utilities utilities;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    //    private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    private ArrayList<SongModel> songsList = new ArrayList<>();
    private ArrayList<SongModel> songsListAll = new ArrayList<>();
    AudioManager am;
    // int result;
    MediaSessionCompat mediaSessionCompat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.ENGLISH)) {
            utils.changeLanguage(context, "en");
        } else if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {
            utils.changeLanguage(context, "pr");
        }

        setContentView(R.layout.activity_main);

        Constants.checkApp(this);

        mediaSessionCompat = new MediaSessionCompat(this, "PlayerAudio");

        Intent i = new Intent(this, OnClearFromRecentService.class);
        bindService(i, this, BIND_AUTO_CREATE);
       // startService(i);

        //am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);


// Request focus for music stream and pass AudioManager.OnAudioFocusChangeListener
// implementation reference
        //result = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        initViewsAndLayouts();
        btnPlay = findViewById(R.id.btnPlay);
        btnPlaySmall = findViewById(R.id.playPauseBtnSmall);
//        btnForward =  findViewById(R.id.btnForward);
//        btnBackward =  findViewById(R.id.btnBackward);
        btnNext = findViewById(R.id.btnNext);
        btnNextSmall = findViewById(R.id.nextBtnSmall);
        btnPrevious = findViewById(R.id.btnPrevious);
//        btnPlaylist =  findViewById(R.id.btnPlaylist);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnShuffle = findViewById(R.id.btnShuffle);
        playVideo = findViewById(R.id.playVideo);
        songProgressBar = findViewById(R.id.songProgressBar);
        songProgressBarSmall = findViewById(R.id.songProgressBar1);
        songTitleLabel = findViewById(R.id.songTitle);
        songTitleLabel.setSelected(true);
        songCurrentDurationLabel = findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = findViewById(R.id.songTotalDurationLabel);

        playVideo.setOnClickListener(v -> {
            String name = songsList.get(currentSongIndex).getSongName();
            if (utils.videoExists(name)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(utils.getVideoPath(name)), "video/*");
                startActivity(Intent.createChooser(intent, "Complete action using"));
                if (mp.isPlaying()) {
                    mp.pause();
                }
            } else {
                Toast.makeText(context, "No video found related to this music", Toast.LENGTH_SHORT).show();
            }
        });

        // Mediaplayer
        mp = new MediaPlayer();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
//        songManager = new SongsManager();
        utilities = new Utilities();

        // Listeners
        songProgressBar.setOnSeekBarChangeListener(this); // Important
        songProgressBarSmall.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

        // Getting all songs list
//        getSongsList();
//        songsList = songManager.getPlayList();

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!isStoragePermissionGranted()) {
                    Toast.makeText(context, "grant storage permission and retry", Toast.LENGTH_LONG).show();
                    return;
                }

                if (checkIfAdsAreWatched()) {
                    return;
                }

                // check for already playing
                if (mp.isPlaying()) {
                    if (mp != null) {
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.play);
                        btnPlaySmall.setImageResource(R.drawable.play);
                    }
                } else {
                    // Resume song
                    if (mp != null) {
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.pause);
                        btnPlaySmall.setImageResource(R.drawable.pause);
                    }
                }

            }
        });
        btnPlaySmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!isStoragePermissionGranted()) {
                    Toast.makeText(context, "grant storage permission and retry", Toast.LENGTH_LONG).show();
                    return;
                }

                if (checkIfAdsAreWatched()) {
                    return;
                }

                //LARGE MUSIC PLAYER IS SHOWN
                if (slideUpB) {
                    return;
                }
                // check for already playing
                if (mp.isPlaying()) {
                    if (mp != null) {
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.play);
                        btnPlaySmall.setImageResource(R.drawable.play);
                    }
                } else {
                    // Resume song
                    if (mp != null) {
                        if (currentSongIndex < (songsList.size() - 1)) {
                            playSong(currentSongIndex + 1);
                            currentSongIndex = currentSongIndex + 1;
                        } else {
                            // play first song
                            playSong(currentSongIndex);
                            //currentSongIndex = 0;
                        }
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.pause);
                        btnPlaySmall.setImageResource(R.drawable.pause);
                    }
                }

            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
//        btnForward.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
        // get current song position
//                int currentPosition = mp.getCurrentPosition();
//                // check if seekForward time is lesser than song duration
//                if(currentPosition + seekForwardTime <= mp.getDuration()){
//                    // forward song
//                    mp.seekTo(currentPosition + seekForwardTime);
//                }else{
//                    // forward to end position
//                    mp.seekTo(mp.getDuration());
//                }
//            }
//        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
//        btnBackward.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                // get current song position
//                int currentPosition = mp.getCurrentPosition();
//                // check if seekBackward time is greater than 0 sec
//                if(currentPosition - seekBackwardTime >= 0){
//                    // forward song
//                    mp.seekTo(currentPosition - seekBackwardTime);
//                }else{
//                    // backward to starting position
//                    mp.seekTo(0);
//                }
//
//            }
//        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (checkIfAdsAreWatched()) {
                    return;
                }

                if (currentSongIndex > 0) {
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                } else {
                    // play last song
                    playSong(songsList.size() - 1);
                    currentSongIndex = songsList.size() - 1;
                }

            }
        });
        btnNextSmall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (checkIfAdsAreWatched()) {
                    return;
                }

                //LARGE MUSIC PLAYER IS SHOWN
                if (slideUpB) {
                    return;
                }

                if (currentSongIndex > 0) {
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                } else {
                    // play last song
                    playSong(songsList.size() - 1);
                    currentSongIndex = songsList.size() - 1;
                }

            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (checkIfAdsAreWatched()) {
                    return;
                }

                // check if next song is there or not
                if (currentSongIndex < (songsList.size() - 1)) {
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                } else {
                    // play first song
                    playSong(0);
                    currentSongIndex = 0;
                }
            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                if (isRepeat) {
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                    btnRepeat.setImageResource(R.drawable.repeat_off);
                } else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.setImageResource(R.drawable.repeat_current_track);
                    btnShuffle.setImageResource(R.drawable.radom_off);
                }
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (isShuffle) {
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                    btnShuffle.setImageResource(R.drawable.radom_off);
                } else {
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.setImageResource(R.drawable.radom_on);
                    btnRepeat.setImageResource(R.drawable.repeat_off);
                }
            }
        });

        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */
//        btnPlaylist.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View arg0) {
//                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
//                startActivityForResult(i, 100);
//            }
//        });

        findViewById(R.id.queueBtnHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_queue);
                dialog.setCancelable(true);
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

                initRecyclerView(dialog);

                dialog.findViewById(R.id.downMusicBtnQueue).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // CODE HERE
                        dialog.dismiss();
                    }
                });

                dialog.show();
                dialog.getWindow().setAttributes(layoutParams);
            }
        });

        // INIT VOLUME SEEKBAR
        initVolumeSeekbar();

        /*songTotalDurationLabel.setOnClickListener(view -> {
            SongModel currentSongModel = songsList.get(currentSongIndex);
            utils.storeInteger(MainActivity.this,
                    currentSongModel.getSongPushKey() + Constants.END_INDEX, mp.getCurrentPosition());
            Toast.makeText(MainActivity.this, "Will end on: " + utilities.milliSecondsToTimer(mp.getCurrentPosition()), Toast.LENGTH_SHORT).show();
        });

        songCurrentDurationLabel.setOnClickListener(view -> {
            SongModel currentSongModel = songsList.get(currentSongIndex);
            utils.storeInteger(MainActivity.this,
                    currentSongModel.getSongPushKey() + Constants.START_INDEX, mp.getCurrentPosition());
            Toast.makeText(MainActivity.this, "Will start on: " + utilities.milliSecondsToTimer(mp.getCurrentPosition()), Toast.LENGTH_SHORT).show();

        });*/

    }

    private void initVolumeSeekbar() {
        Log.d(TAG, "initVolumeSeekbar: ");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar volControl = (SeekBar) findViewById(R.id.volumeSeekbar);
        volControl.setMax(maxVolume);
        volControl.setProgress(curVolume);
        volControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, arg1, 0);
            }
        });
    }

    //-----------------------------------------------------

    private RecyclerView conversationRecyclerView;
    private RecyclerViewAdapterMessages adapter;

    private void initRecyclerView(Dialog dialog) {
        Log.d(TAG, "initRecyclerView: ");
        conversationRecyclerView = dialog.findViewById(R.id.queueRecyclerView);
        //conversationRecyclerView.addItemDecoration(new DividerItemDecoration(conversationRecyclerView.getContext(), DividerItemDecoration.VERTICAL));
        adapter = new RecyclerViewAdapterMessages();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);


        conversationRecyclerView.setLayoutManager(linearLayoutManager);
        conversationRecyclerView.setHasFixedSize(true);
//        conversationRecyclerView.setNestedScrollingEnabled(false);
        conversationRecyclerView.setItemViewCacheSize(20);


        conversationRecyclerView.setAdapter(adapter);

    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        OnClearFromRecentService.MyBinder binder = (OnClearFromRecentService.MyBinder) iBinder;
        service = binder.getService();
        service.setCallBack(MainActivity.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        service = null;
    }

    private class RecyclerViewAdapterMessages extends RecyclerView.Adapter
            <RecyclerViewAdapterMessages.ViewHolderRightMessage> {

        @NonNull
        @Override
        public ViewHolderRightMessage onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_queue_items, parent, false);
            return new ViewHolderRightMessage(view);
        }

        RelativeLayout prevLayout;

        @Override
        public void onBindViewHolder(@NonNull final ViewHolderRightMessage holder, int position1) {
            Log.d(TAG, "onBindViewHolder: ");
            int position = holder.getAdapterPosition();

            holder.title.setText(songsList.get(position).getSongName());

            if (position == currentSongIndex) {
                prevLayout = holder.parentLayout;
                prevLayout.setBackgroundColor(getResources().getColor(R.color.lightBlack));
            } else {
                holder.parentLayout.setBackgroundColor(getResources().getColor(R.color.black));
            }

            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (prevLayout != null) {
                        setBgColor(R.color.black);
                    }
                    prevLayout = holder.parentLayout;
                    setBgColor(R.color.lightBlack);

                    // CODE HERE

                    currentSongIndex = position;
                    playSong(position);
                }

                private void setBgColor(int p) {
                    prevLayout.setBackgroundColor(getResources().getColor(p));
                }

            });

        }

        @Override
        public int getItemCount() {
            if (songsList == null)
                return 0;
            return songsList.size();
        }

        public class ViewHolderRightMessage extends RecyclerView.ViewHolder {

            TextView title;
            RelativeLayout parentLayout;

            public ViewHolderRightMessage(@NonNull View v) {
                super(v);
                title = v.findViewById(R.id.songNameDialogQueueHome);
                parentLayout = v.findViewById(R.id.parentLayoutQueueItem);

            }
        }

    }

    boolean firstTime = true;

    //-----------------------------------------------------
    private void getSongsList() {
        Log.d(TAG, "getSongsList: ");
        songsList.clear();
        songsListAll.clear();

        ArrayList<SongModel> list = Stash.getArrayList(Constants.OFF_DATA, SongModel.class);
        // Toast.makeText(context, ""+list.size(), Toast.LENGTH_SHORT).show();
        if (list.size() > 0) {

            for (SongModel model : list) {
                Log.d(TAG, "getSongsList: loop");
                if (utils.fileExists(model.getSongName())) {
                    songsList.add(model);
                    songsListAll.add(model);
                }
            }
            if (songsList.size() > 0) {
                if (firstTime) {
                    Log.d(TAG, "getSongsList: firstTime");

                    // PLAYLIST LOADED
                    // By default play first song
                    currentSongIndex = utils.getStoredInteger(MainActivity.this, Constants.LAST_SONG_INDEX);

                    SongModel currentSongModel = songsList.get(currentSongIndex);

                    // TITLE BIG PLAYER
                    String songTitle = currentSongModel.getSongName();
                    songTitleLabel.setText(songTitle);

                    //TITLE SMALL PLAYER
                    TextView titleSmall = findViewById(R.id.title_small_playerHome);
//            titleSmall.setSelected(true);
                    titleSmall.setText(songTitle);

                    // ALBUM NAME SMALL PLAYER
                    TextView albumName = findViewById(R.id.albumNameHome);
//            albumName.setSelected(true);
                    albumName.setText(currentSongModel.getSongAlbumName());

                    // COVER IMAGE BIG PLAYER
                    with(context)
                            .asBitmap()
                            .load(currentSongModel.getSongCoverUrl())
                            .apply(new RequestOptions()
                                    .placeholder(R.color.lightBlack)
                                    .error(lightBlack)
                            )
                            .diskCacheStrategy(DATA)
                            .into((ImageView) findViewById(R.id.songCoverImage));

                    // COVER IMAGE SMALL PLAYER
                    with(context)
                            .asBitmap()
                            .load(currentSongModel.getSongCoverUrl())
                            .apply(new RequestOptions()
                                    .placeholder(R.color.lightBlack)
                                    .error(lightBlack)
                            )
                            .diskCacheStrategy(DATA)
                            .into((ImageView) findViewById(R.id.current_music_player_image_view));

                    // set Progress bar values
                    songProgressBar.setProgress(0);
                    songProgressBarSmall.setProgress(0);
                    songProgressBar.setMax(100);
                    songProgressBarSmall.setMax(100);

            /*playSong(currentSongIndex);/

            // check for already playing
            if (mp.isPlaying()) {
                if (mp != null) {
                    Log.d(TAG, "getSongsList: mp.pause()");
                    mp.pause();
                    // Changing button image to play button
                    btnPlay.setImageResource(R.drawable.play);
                    btnPlaySmall.setImageResource(R.drawable.play);
                }
            }*/
                    firstTime = false;
                } else {
                    Log.d(TAG, "getSongsList: else {");

                    if (Stash.getBoolean(Constants.IS_CLICKED, false)) {
                        for (int i = 0; i <= songsList.size() - 1; i++) {
                            if (songsList.get(i).getSongName().equals(Stash.getString(Constants.PUSH_KEY))) {
                                currentSongIndex = i;
                                break;
                            }
                        }
                        Log.d(TAG, "getSongsList: playSong(): " + currentSongIndex);
                        playSong(currentSongIndex);
                        Stash.put(Constants.IS_CLICKED, false);
                    }
                }
            }
        } else return;
    }


    /**
     * Receiving song index from playlist view
     * and play the song
     */
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK) {
//            currentSongIndex = data.getIntExtra(Constants.SONG_INDEX, 0);
//            SongModel model = new SongModel();
//            model.setSongName(data.getStringExtra(Constants.SONG_NAME));
//            model.setSongAlbumName(data.getStringExtra(Constants.SONG_ALBUM_NAME));
//            model.setSongPushKey(data.getStringExtra(Constants.PUSH_KEY));
//            model.setSongCoverUrl(data.getStringExtra(Constants.SONG_COVER_URL));
//            model.setSongYTUrl(data.getStringExtra(Constants.YT_URL));


        }

    }*/
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        getSongsList();

        /*if (utils.getStoredBoolean(context, Constants.IS_PLAYLIST)) {
            String playListName = utils.getStoredString(context, Constants.NAME);
            getPlaylist(playListName);
        } else {

        }*/
    }


    /**
     * Function to play a song
     *
     * @param songIndex - index of song
     */
    public void playSong(int songIndex) {
        Log.d(TAG, "playSong: songIndex: " + songIndex);
        if (!isStoragePermissionGranted()) {
            Log.d(TAG, "playSong: ");
            Toast.makeText(context, "grant storage permission and retry", Toast.LENGTH_LONG).show();
            return;
        }

        if (checkIfAdsAreWatched()) {
            return;
        }

        if (songIndex >= songsList.size()) {
            Toast.makeText(context, "Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        utils.storeInteger(MainActivity.this, Constants.LAST_SONG_INDEX, songIndex);

        SongModel currentSongModel = songsList.get(songIndex);
        try {
            mp.reset();
            mp.setDataSource(utils.getSongPath(currentSongModel.getSongName()));
            mp.prepare();
            mp.start();

            /*int startIndex = utils.getStoredInteger(MainActivity.this,
                    currentSongModel.getSongPushKey() + Constants.START_INDEX);
            if (startIndex != 0) {
                mp.seekTo(startIndex);
            }*/

            // TITLE BIG PLAYER
            String songTitle = currentSongModel.getSongName();
            songTitleLabel.setText(songTitle);

            //TITLE SMALL PLAYER
            TextView titleSmall = findViewById(R.id.title_small_playerHome);
//            titleSmall.setSelected(true);
            titleSmall.setText(songTitle);

            // ALBUM NAME SMALL PLAYER
            TextView albumName = findViewById(R.id.albumNameHome);
//            albumName.setSelected(true);
            albumName.setText(currentSongModel.getSongAlbumName());

            // COVER IMAGE BIG PLAYER
            with(context)
                    .asBitmap()
                    .load(currentSongModel.getSongCoverUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.color.lightBlack)
                            .error(lightBlack)
                    )
                    .diskCacheStrategy(DATA)
                    .into((ImageView) findViewById(R.id.songCoverImage));

            // COVER IMAGE SMALL PLAYER
            with(context)
                    .asBitmap()
                    .load(currentSongModel.getSongCoverUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.color.lightBlack)
                            .error(lightBlack)
                    )
                    .diskCacheStrategy(DATA)
                    .into((ImageView) findViewById(R.id.current_music_player_image_view));


            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.pause);
            btnPlaySmall.setImageResource(R.drawable.pause);

            // set Progress bar values
            songProgressBar.setProgress(0);
            songProgressBarSmall.setProgress(0);
            songProgressBar.setMax(100);
            songProgressBarSmall.setMax(100);

            // Updating progress bar
            updateProgressBar();
            CreateNotification.createNotification(MainActivity.this, songsList.get(songIndex),
                    R.drawable.ic_pause_black_24dp, songIndex, songsList.size()-1);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "playSong: EXCEPTION: " + e.getMessage());

        }
    }

    public boolean isStoragePermissionGranted() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (
                    (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED)
            ) {
                return true;
            } else {

                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_AUDIO);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_VIDEO);
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS);

                requestPermissions(Constants.permissions13, 1);
                /* ActivityCompat.requestPermissions(this, Constants.permissions, 1); */
                return false;
            }
        } else {

//        Log.d(TAGG, "isStoragePermissionGranted: ");
            if (
                    (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            ) {
                return true;
            } else {

                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE);

                requestPermissions(Constants.permissions, 1);
               // request_result_launcher.launch(Constants.permissions);
                /* ActivityCompat.requestPermissions(this, Constants.permissions, 1);*/
                return false;
            }
        }
    }

    private ActivityResultLauncher<String> request_result_launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGraned -> {
        if (isGraned){

        }
    });

    private boolean checkIfAdsAreWatched() {
        //TODO: THESE BELOW LINES SHOULD NOT BE COMMENTED
//        if (utils.getAdsInteger(context, utils.getLastSunday()) > 0) {
//            Toast.makeText(context, "Please watch ads first!", Toast.LENGTH_SHORT).show();
//            startActivity(new Intent(context, AdvertisementACtivity.class));
//            return true;
//        }
//
//        if (utils.isTodaySunday()) {
//
//            if (utils.getAdsInteger(context, utils.getDate()) > 0) {
//                Toast.makeText(context, "Please watch ads first!", Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(context, AdvertisementACtivity.class));
//                return true;
//            }
//
//        }

        return false;
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            Log.d(TAG, "run: 824");
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            TextView currentDurationTv = findViewById(R.id.currentDurationSmallPlayer);
            TextView totalDurationTv = findViewById(R.id.totalDurationSmallPlayer);

            // Displaying Total Duration time
            songTotalDurationLabel.setText("" + utilities.milliSecondsToTimer(totalDuration));
            totalDurationTv.setText("" + utilities.milliSecondsToTimer(totalDuration));

            // Displaying time completed playing
            songCurrentDurationLabel.setText("" + utilities.milliSecondsToTimer(currentDuration));
            currentDurationTv.setText("" + utilities.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (utilities.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            songProgressBar.setProgress(progress);
            songProgressBarSmall.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        /*int endIndex = utils.getStoredInteger(MainActivity.this,
                songsList.get(currentSongIndex).getSongPushKey() + Constants.END_INDEX);
        if (endIndex != 0 && progress >= endIndex) {
            mp.seekTo(mp.getDuration());
        }*/
    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utilities.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again

        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {
        Log.d(TAG, "onCompletion: ");
        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if (isShuffle) {
            // shuffle is on - play a random song
            Random rand = new Random();
            currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            playSong(currentSongIndex);
        } else {
            if (currentSongIndex > 0) {
                playSong(currentSongIndex - 1);
                currentSongIndex = currentSongIndex - 1;
            } else {
                // play last song
                playSong(songsList.size() - 1);
                currentSongIndex = songsList.size() - 1;
            }

        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        mHandler.removeCallbacks(mUpdateTimeTask);
        mp.release();

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unbindService(this);
    }

    //--------------------------------------------------------------------------------
    private LinearLayout buttonsLayout;

    private void initViewsAndLayouts() {
        Log.d(TAG, "initViewsAndLayouts: ");
        bottom_music_layout = findViewById(R.id.bottom_music_layout);
        music_player_layout = findViewById(R.id.music_player_layout);

        buttonsLayout = findViewById(R.id.buttonsLayoutHome);

        findViewById(R.id.title_small_playerHome).setSelected(true);
        findViewById(R.id.albumNameHome).setSelected(true);

        bottom_music_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!slideUpB)
                    slideUp();
            }
        });

        findViewById(R.id.downMusicLayoutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slideDown();
            }
        });

        findViewById(R.id.downloadBtnHomeScreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //LARGE MUSIC PLAYER IS SHOWN
                if (slideUpB) {
                    return;
                }
                startActivity(new Intent(MainActivity.this, DownloadActivity.class));
            }
        });

        findViewById(R.id.settingsButtonHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //LARGE MUSIC PLAYER IS SHOWN
                if (slideUpB) {
                    return;
                }
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });

        findViewById(R.id.advertisementBtnHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //LARGE MUSIC PLAYER IS SHOWN
                if (slideUpB) {
                    return;
                }
                startActivity(new Intent(MainActivity.this, AdvertisementACtivity.class));
            }
        });

        findViewById(R.id.mySoundBtnHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //LARGE MUSIC PLAYER IS SHOWN
                if (slideUpB) {
                    return;
                }
                startActivityForResult(new Intent(MainActivity.this, MySoundActivity.class), Constants.REQUEST_CODE);
            }
        });

        findViewById(R.id.exitButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //LARGE MUSIC PLAYER IS SHOWN
                if (slideUpB) {
                    return;
                }
                finish();
            }
        });

        findViewById(R.id.equalizerBtnMain).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEqualizerSettings();
            }
        });

        //TODO: BELOW LINES SHOULD NOT BE COMMENTED
//        TextView textview = findViewById(R.id.email_Txt_home);
//        if (auth.getCurrentUser() != null)
//            textview.setText(auth.getCurrentUser().getEmail());

    }

    private void openEqualizerSettings() {
        Intent intent = new Intent(AudioEffect
                .ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        if ((intent.resolveActivity(getPackageManager()) != null)) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your device does not support an equalizer!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean slideUpB = false;

    private void slideDown() {
        YoYo.with(Techniques.SlideOutDown).duration(1500).onStart(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {

            }
        }).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                slideUpB = false;
                music_player_layout
                        .setVisibility(View.GONE);

                bottom_music_layout.setEnabled(true);
                buttonsLayout.setEnabled(true);
            }
        }).playOn(music_player_layout);
    }

    private void slideUp() {
        YoYo.with(Techniques.SlideInUp).duration(1500).onStart(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                music_player_layout
                        .setVisibility(View.VISIBLE);
            }
        }).onEnd(new YoYo.AnimatorCallback() {
            @Override
            public void call(Animator animator) {
                slideUpB = true;
                bottom_music_layout.setEnabled(false);
                buttonsLayout.setEnabled(false);

            }
        }).playOn(music_player_layout);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        if (slideUpB) {
            slideDown();
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            mp.pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Resume
            mp.start();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Stop or pause depending on your need
            mp.stop();
        }
    }

    @Override
    public void onTrackPrevious() {
        --currentSongIndex;
        CreateNotification.createNotification(MainActivity.this, songsList.get(currentSongIndex),
                R.drawable.ic_pause_black_24dp, currentSongIndex, songsList.size()-1);
        playSong(currentSongIndex);
    }

    @Override
    public void onTrackPlay() {
        if(isPlaying){
            mp.pause();
            CreateNotification.createNotification(MainActivity.this, songsList.get(currentSongIndex),
                    R.drawable.ic_play_arrow_black_24dp, currentSongIndex, songsList.size()-1);
            isPlaying = false;
        } else {
            mp.start();
            CreateNotification.createNotification(MainActivity.this, songsList.get(currentSongIndex),
                    R.drawable.ic_pause_black_24dp, currentSongIndex, songsList.size()-1);
            isPlaying = true;
        }
    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(MainActivity.this, songsList.get(currentSongIndex),
                R.drawable.ic_pause_black_24dp, currentSongIndex, songsList.size()-1);
        isPlaying = false;
        mp.pause();
    }

    @Override
    public void onTrackNext() {
        ++currentSongIndex;
        CreateNotification.createNotification(MainActivity.this, songsList.get(currentSongIndex),
                R.drawable.ic_pause_black_24dp, currentSongIndex, songsList.size()-1);
        playSong(currentSongIndex);
    }


}