package ch.zhaw.engineering.tbdappname;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistWithSongs;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.database.repository.PlaylistRepository;
import ch.zhaw.engineering.tbdappname.services.database.repository.SongRepository;
import ch.zhaw.engineering.tbdappname.ui.songs.SimpleItemTouchHelperCallback;
import ch.zhaw.engineering.tbdappname.ui.songs.SongAdapter;

public class AddOrEditPlaylistActivity extends AppCompatActivity {

    public static final String EXTRA_PLAYLIST_ID = "EXTRA_PLAYLIST_ID";

    private PlaylistRepository playlistRepository;
    private SongRepository songRepository;
    private Playlist playlist = new Playlist();
    private PlaylistWithSongs playlistWithSongs = new PlaylistWithSongs();

    private Handler mHandler;
    private boolean isEdit = false;
    private SelectionTracker<Long> mSelectionTracker;
    private List<Long> mStartingSelection = new ArrayList<>();
    private ItemTouchHelper mTouchHelper;
    private SongAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_edit_playlist);
        RecyclerView recyclerView = findViewById(R.id.song_list);

        playlistRepository = PlaylistRepository.getInstance(getApplication());
        songRepository = SongRepository.getInstance(getApplication());

        HandlerThread mHandlerThread = new HandlerThread("BackgroundFragmentThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        FloatingActionButton fab = findViewById(R.id.save_button);
        EditText playlistName = findViewById(R.id.playlist_name);

        Button reorder = findViewById(R.id.reorder_songs_mode);
        reorder.setOnClickListener(v -> {
            mAdapter.setDragEnabled(!mAdapter.isDragEnabled());
            if (mAdapter.isDragEnabled()) {
                reorder.setText("Modify added Songs");
                mTouchHelper.attachToRecyclerView(recyclerView);
            } else {
                reorder.setText("Reorder Songs");
                mTouchHelper.attachToRecyclerView(null);
            }
        });

        if (getIntent() != null) {
            long id = getIntent().getIntExtra(EXTRA_PLAYLIST_ID, 0);
            if (id != 0) {
                isEdit = true;
                mHandler.post(() -> {
                    playlistWithSongs = playlistRepository.findPlaylistById(id);


                    for (Song song : playlistWithSongs.songs) {
                        mStartingSelection.add(song.getSongId());
                    }

                    runOnUiThread(() -> playlistName.setText(playlistWithSongs.playlist.getName()));
                });
            }
        }

        songRepository.getSongs().observe(this, songs -> AsyncTask.execute(() -> {
            List<Song> songsInPlaylist = songRepository.getSongsForPlaylist(playlistWithSongs.playlist);
            runOnUiThread(() -> {
                mAdapter = new SongAdapter(songs, songsInPlaylist, true, new SongAdapter.SongListInteractionListener() {
                    @Override
                    public void onSongClick(Song song) {
                    }

                    @Override
                    public void onSongLongClick(Song song) {
                    }

                    @Override
                    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                        mTouchHelper.startDrag(viewHolder);
                    }
                });
                recyclerView.setAdapter(mAdapter);
                mSelectionTracker = new SelectionTracker.Builder<>(
                        "selected-songs",
                        recyclerView,
                        new ItemKeyProvider<Long>(ItemKeyProvider.SCOPE_MAPPED) {
                            @Override
                            public Long getKey(int position) {
                                return mAdapter.getItemId(position);
                            }

                            @Override
                            public int getPosition(@NonNull Long key) {
                                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForItemId(key);
                                return viewHolder == null ? RecyclerView.NO_POSITION : viewHolder.getLayoutPosition();
                            }
                        },
                        new SongAdapter.SongItemDetailsLookup(recyclerView),
                        StorageStrategy.createLongStorage()
                ).withSelectionPredicate(SelectionPredicates.createSelectAnything())
                        .build();
                mAdapter.selectionTracker = mSelectionTracker;

                SimpleItemTouchHelperCallback touchCallback = new SimpleItemTouchHelperCallback(mAdapter);
                mTouchHelper = new ItemTouchHelper(touchCallback);
                mSelectionTracker.setItemsSelected(mStartingSelection, true);
            });
        }));


        fab.setEnabled(false);
        fab.setOnClickListener(v -> {
            if (!isEdit) {
                playlistWithSongs.playlist = playlist;
            }
            playlistWithSongs.playlist.setName(playlistName.getText().toString());
            playlistWithSongs.songs = mAdapter.getSongsInOrder();

            mHandler.post(() -> {
                if (isEdit) {
                    playlistRepository.update(playlistWithSongs);
                } else {
                    playlistRepository.insert(playlistWithSongs);
                }
            });
            finish();
        });

        playlistName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    fab.setEnabled(true);
                } else {
                    fab.setEnabled(false);
                }
            }
        });
    }
}
