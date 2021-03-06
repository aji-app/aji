package ch.zhaw.engineering.aji;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.aji.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.aji.services.database.entity.Playlist;
import ch.zhaw.engineering.aji.services.database.entity.RadioStation;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.album.AlbumFragment;
import ch.zhaw.engineering.aji.ui.artist.ArtistFragment;
import ch.zhaw.engineering.aji.ui.contextmenu.ContextMenuFragment;
import ch.zhaw.engineering.aji.ui.expandedcontrols.ExpandedControlsFragment;
import ch.zhaw.engineering.aji.ui.library.AlbumArtistListFragment;
import ch.zhaw.engineering.aji.ui.library.FavoriteFragment;
import ch.zhaw.engineering.aji.ui.playlist.PlaylistDetailsFragment;
import ch.zhaw.engineering.aji.ui.playlist.PlaylistFragment;
import ch.zhaw.engineering.aji.ui.playlist.PlaylistListFragment;
import ch.zhaw.engineering.aji.ui.playlist.PlaylistSelectionFragment;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationDetailsFragment;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationListFragment;
import ch.zhaw.engineering.aji.ui.song.SongDetailsFragment;
import ch.zhaw.engineering.aji.ui.song.list.SongListFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import lombok.Builder;
import lombok.Value;

public abstract class FragmentInteractionActivity extends AudioInterfaceActivity implements SongListFragment.SongListFragmentListener,
        PlaylistListFragment.PlaylistFragmentListener, PlaylistFragment.PlaylistFragmentListener, PlaylistDetailsFragment.PlaylistDetailsFragmentListener,
        RadioStationListFragment.RadioStationFragmentInteractionListener, RadioStationDetailsFragment.RadioStationDetailsFragmentListener, ExpandedControlsFragment.ExpandedControlsFragmentListener,
        SongDetailsFragment.SongDetailsFragmentListener, AlbumArtistListFragment.AlbumArtistListFragmentListener, PlaylistSelectionFragment.PlaylistSelectionListener,
        FavoriteFragment.FavoritesFragmentListener, ArtistFragment.ArtistFragmentListener, AlbumFragment.AlbumFragmentListener {

    private static final String TAG = "FragmentInteractions";
    private static final int REQUEST_CODE_PLS_SELECT = 2;
    private SongDao mSongDao;
    private PlaylistDao mPlaylistDao;
    private RadioStationDao mRadioStationDao;
    private ContextMenuFragment mContextMenuFragment;
    protected AppViewModel mAppViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSongDao = SongDao.getInstance(this);
        mPlaylistDao = PlaylistDao.getInstance(this);
        mRadioStationDao = RadioStationDao.getInstance(this);
        mAppViewModel = new ViewModelProvider(this).get(AppViewModel.class);
    }

    @Override
    public void onSongSelected(long songId, int position) {
        navigateToSongDetails(songId);
    }

    @Override
    public void onSongPlay(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            playMusic(song, false);
            Log.i(TAG, "onSongPlay: " + song.getTitle());
        });
    }

    @Override
    public void onSongAddToPlaylist(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            Log.i(TAG, "onSongAddToPlaylist: " + song.getTitle());
            LiveData<List<ContextMenuFragment.ItemConfig>> contextMenuEntries =
                    Transformations.map(mPlaylistDao.getPlaylistsWhereSongCanBeAdded(songId), playlists -> {
                        List<ContextMenuFragment.ItemConfig> configs = new ArrayList<>();
                        configs.add(ContextMenuFragment.ItemConfig.builder()
                                .imageId(R.drawable.ic_playlist_add)
                                .textId(R.string.create_playlist)
                                .callback($ -> onCreatePlaylist(songId)).build());
                        for (Playlist playlist : playlists) {
                            ContextMenuFragment.ItemConfig<Playlist> entry = ContextMenuFragment.ItemConfig.<Playlist>builder()
                                    .value(playlist)
                                    .imageId(R.drawable.ic_menu_playlist)
                                    .text(playlist.getName())
                                    .callback(pl -> {
                                        onSongAddToPlaylist(songId, pl.getPlaylistId());
                                    }).build();
                            configs.add(entry);
                        }
                        return configs;
                    });
            mContextMenuFragment = ContextMenuFragment.newInstance(contextMenuEntries);
            runOnUiThread(() -> {

                mContextMenuFragment.show(getSupportFragmentManager(), ContextMenuFragment.TAG);
            });
        });

    }

    @Override
    public void onSongQueue(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            playMusic(song, true);
            Log.i(TAG, "onSongQueue: " + song.getTitle());
            runOnUiThread(() -> {
                Toast.makeText(this, getResources().getString(R.string.song_queued, song.getTitle()), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onSongMenu(long songId, Integer position, ContextMenuItem... additionalItems) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongByIdInclusiveHidden(songId);
            Log.i(TAG, "onSongMenu: " + song.getTitle());
            MutableLiveData<List<ContextMenuFragment.ItemConfig>> contextMenuEntries = new MutableLiveData<>();
            List<ContextMenuFragment.ItemConfig> entries = new ArrayList<>();
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_play)
                    .textId(R.string.play)
                    .callback($ -> onSongPlay(songId)).build());
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_queue)
                    .textId(R.string.queue)
                    .callback($ -> onSongQueue(songId)).build());
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_details)
                    .textId(R.string.details)
                    .callback($ -> navigateToSongDetails(songId)).build());
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_playlist_add)
                    .textId(R.string.add_to_playlist)
                    .callback($ -> onSongAddToPlaylist(songId)).build());
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_hide)
                    .textId(R.string.delete_song)
                    .callback($ -> onSongDelete(songId)).build());
            for (ContextMenuItem item : additionalItems) {
                entries.add(item.getPosition(), item.getItemConfig());
            }
            mContextMenuFragment = ContextMenuFragment.newInstance(contextMenuEntries);
            runOnUiThread(() -> {
                mContextMenuFragment.show(getSupportFragmentManager(), ContextMenuFragment.TAG);
                contextMenuEntries.setValue(entries);
            });
        });
    }

    @Override
    public void onSongAddToPlaylist(long songId, int playlistId) {
        AsyncTask.execute(() -> {
            mPlaylistDao.addSongToPlaylist(songId, playlistId);
            Song song = mSongDao.getSongById(songId);
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            Log.i(TAG, "onSongAddToPlaylist: " + song.getTitle() + ", " + playlist.getName());
            runOnUiThread(() -> Toast.makeText(this, getString(R.string.song_added_to_playlist, song.getTitle(), playlist.getName()), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public void onSongDelete(long songId) {
        AsyncTask.execute(() -> {
            removeSongFromQueue(songId, null);
            mSongDao.hideSong(songId);
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), R.string.song_removed_from_library, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, view -> {
                        AsyncTask.execute(() -> {
                            mSongDao.unhideSong(songId);
                        });
                    }).addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            if (event != DISMISS_EVENT_ACTION) {
                                AsyncTask.execute(() -> {
                                    Song song = mSongDao.getSongByIdInclusiveHidden(songId);
                                    Log.i(TAG, "onSongDelete: " + song.getTitle());
                                });
                            }
                        }
                    });
            snackbar.show();

        });
    }

    @Override
    public void onCreatePlaylist(Long songToAdd) {
        showCreatePlaylistDialog(songToAdd, songToAdd == null);
        Log.i(TAG, "onCreatePlaylist: ");
    }

    @Override
    public void onToggleFavorite(long songId) {
        AsyncTask.execute(() -> {
            mSongDao.toggleFavorite(songId);

            Song song = mSongDao.getSongById(songId);
            Log.i(TAG, "onToggleFavorite: " + song.getTitle());
        });
    }

    @Override
    public void onPlaylistModified(int playlistId, List<Long> songIds) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            mPlaylistDao.modifyPlaylist(songIds, playlistId);
            Log.i(TAG, "onPlaylistModified: " + playlist.getName());
        });
    }

    @Override
    public void onPlaylistSelected(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            Log.i(TAG, "onPlaylistSelected: " + playlist.getName());
            runOnUiThread(() -> {
                navigateToPlaylist(playlistId);
            });
        });
    }

    @Override
    public void onPlaylistMenu(int playlistId, FragmentInteractionActivity.ContextMenuItem... additionalItems) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            Log.i(TAG, "onPlaylistEdit: " + playlist.getName());
            MutableLiveData<List<ContextMenuFragment.ItemConfig>> contextMenuEntries = new MutableLiveData<>();
            List<ContextMenuFragment.ItemConfig> entries = new ArrayList<>();
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_play)
                    .textId(R.string.play)
                    .callback($ -> onPlaylistPlay(playlistId)).build());
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_queue)
                    .textId(R.string.queue)
                    .callback($ -> onPlaylistQueue(playlistId)).build());
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_details)
                    .textId(R.string.details)
                    .callback($ -> navigateToPlaylist(playlistId)).build());
            for (ContextMenuItem item : additionalItems) {
                entries.add(item.getPosition(), item.getItemConfig());
            }
            mContextMenuFragment = ContextMenuFragment.newInstance(contextMenuEntries);
            runOnUiThread(() -> {
                mContextMenuFragment.show(getSupportFragmentManager(), ContextMenuFragment.TAG);
                contextMenuEntries.setValue(entries);
            });
        });

    }

    @Override
    public void onPlaylistPlay(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            playMusic(playlist, false);
            Log.i(TAG, "onPlaylistPlay: " + playlist.getName());
        });

    }

    @Override
    public void onPlaylistQueue(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            playMusic(playlist, true);
            Log.i(TAG, "onPlaylistQueue: " + playlist.getName());
        });
    }

    @Override
    public void onPlaylistDelete(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            Log.i(TAG, "onPlaylistDelete: " + playlist.getName());
            mPlaylistDao.deletePlaylist(playlistId);
        });
    }

    @Override
    public void onPlaylistNameChanged(int playlistId, String newName) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            playlist.setName(newName);
            mPlaylistDao.update(playlist);
            Log.i(TAG, "onPlaylistNameChanged: " + playlist.getName());
        });
    }

    @Override
    public void onRadioStationSelected(long radioStationId) {
        AsyncTask.execute(() -> {
            RadioStation radio = mRadioStationDao.getRadioStation(radioStationId);
            Log.i(TAG, "onRadioStationSelected: " + radio.getName());
            runOnUiThread(() -> {
                navigateToRadioStation(radioStationId);
            });
        });
    }

    @Override
    public void onRadioStationPlay(long radioStationId) {
        AsyncTask.execute(() -> {
            RadioStation radio = mRadioStationDao.getRadioStation(radioStationId);
            playMusic(radio);
            Log.i(TAG, "onRadioStationPlay: " + radio.getName());
        });
    }

    @Override
    public void onRadioStationMenu(long radioStationId) {
        AsyncTask.execute(() -> {
            RadioStation radio = mRadioStationDao.getRadioStation(radioStationId);
            Log.i(TAG, "onRadioStationEdit: " + radio.getName());
            MutableLiveData<List<ContextMenuFragment.ItemConfig>> contextMenuEntries = new MutableLiveData<>();
            List<ContextMenuFragment.ItemConfig> entries = new ArrayList<>();
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_play)
                    .textId(R.string.play)
                    .callback($ -> onRadioStationPlay(radioStationId)).build());
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_details)
                    .textId(R.string.details)
                    .callback($ -> navigateToRadioStation(radioStationId)).build());
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_delete)
                    .textId(R.string.delete_radiostation)
                    .callback($ -> onRadioStationDelete(radioStationId)).build());
            mContextMenuFragment = ContextMenuFragment.newInstance(contextMenuEntries);
            runOnUiThread(() -> {
                mContextMenuFragment.show(getSupportFragmentManager(), ContextMenuFragment.TAG);
                contextMenuEntries.setValue(entries);
            });
        });
    }

    @Override
    public void onRadioStationDelete(long radioStationId) {
        AsyncTask.execute(() -> {
            RadioStation radio = mRadioStationDao.getRadioStation(radioStationId);
            if (radio != null) {
                Log.i(TAG, "onRadioStationDelete: " + radio.getName());
            }
            mRadioStationDao.deleteRadioStationById(radioStationId);
        });
    }

    @Override
    public void onCreateRadioStation() {
        AsyncTask.execute(() -> {
            runOnUiThread(() -> {
                Log.i(TAG, "onCreateRadioStation");
                navigateToRadioStation(null);
            });
        });
    }

    @Override
    public void onRadioStationEdited(RadioStationDto updatedRadioStation) {
        if (updatedRadioStation != null) {
            AsyncTask.execute(() -> {
                if (updatedRadioStation.getId() != null) {
                    mRadioStationDao.updateRadioStation(updatedRadioStation);
                }
                Log.i(TAG, "onRadioStationEdit: " + updatedRadioStation.getName());
            });
        }
    }

    @Override
    public void onRadioStationSaved(RadioStationDto updatedRadioStation) {
        AsyncTask.execute(() -> {
            if (updatedRadioStation.getId() != null) {
                mRadioStationDao.updateRadioStation(updatedRadioStation);
            } else {
                long id = mRadioStationDao.createRadioStation(updatedRadioStation);
                mAppViewModel.setOpenFirstInList(false);
                runOnUiThread(() -> navigateToRadioStation(id == -1 ? null : id));
            }
            Log.i(TAG, "onRadioStationSaved: " + updatedRadioStation.getName());
        });
    }

    @Override
    public void onPlayPause() {
        playPause();
        Log.i(TAG, "onPlayPause");
    }

    @Override
    public void onNext() {
        next();
        Log.i(TAG, "onNext");
    }

    @Override
    public void onPrevious() {
        previous();
        Log.i(TAG, "onPrevious");
    }

    @Override
    public void onStopPlayback() {
        stop();
        Log.i(TAG, "onPrevious");
    }

    @Override
    public void onToggleShuffle() {
        toggleShuffle();
    }

    @Override
    public void onChangeRepeatMode() {
        toggleRepeatMode();
    }

    @Override
    public void onToggleAutoQueue() {
        toggleAutoQueue();
    }

    @Override
    public void seek(long position) {
        seekTo(position);
        Log.i(TAG, "seek to : " + position);
    }

    @Override
    public void onAlbumPlay(String album) {
        AsyncTask.execute(() -> {
            List<Song> songs = mSongDao.getSongsForAlbumAsList(album);
            playMusic(songs, false);
        });
        Log.i(TAG, "onAlbumPlay: " + album);
    }

    @Override
    public void onAlbumQueue(String album) {
        AsyncTask.execute(() -> {
            List<Song> songs = mSongDao.getSongsForAlbumAsList(album);
            playMusic(songs, true);
        });
        Log.i(TAG, "onAlbumQueue: " + album);
    }

    @Override
    public void onAlbumMenu(String album) {
        Log.i(TAG, "onAlbumMenu: " + album);
        MutableLiveData<List<ContextMenuFragment.ItemConfig>> contextMenuEntries = new MutableLiveData<>();
        List<ContextMenuFragment.ItemConfig> entries = new ArrayList<>();
        entries.add(ContextMenuFragment.ItemConfig.builder()
                .imageId(R.drawable.ic_play)
                .textId(R.string.play)
                .callback($ -> onAlbumPlay(album)).build());
        entries.add(ContextMenuFragment.ItemConfig.builder()
                .imageId(R.drawable.ic_queue)
                .textId(R.string.queue)
                .callback($ -> onAlbumQueue(album)).build());
        entries.add(ContextMenuFragment.ItemConfig.builder()
                .imageId(R.drawable.ic_details)
                .textId(R.string.details)
                .callback($ -> navigateToAlbum(album)).build());
        entries.add(ContextMenuFragment.ItemConfig.builder()
                .imageId(R.drawable.ic_hide)
                .textId(R.string.hide_artist_from_library)
                .callback($ -> hideSongsByAlbum(album)).build());
        if (mAppViewModel.showHiddenSongs()) {
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_hide)
                    .exclusive(true)
                    .textId(R.string.unhide_album_from_library)
                    .callback($ -> unhideSongsByAlbum(album)).build());
        }
        mContextMenuFragment = ContextMenuFragment.newInstance(contextMenuEntries);
        runOnUiThread(() -> {
            mContextMenuFragment.show(getSupportFragmentManager(), ContextMenuFragment.TAG);
            contextMenuEntries.setValue(entries);
        });
    }

    @Override
    public void onAlbumSelected(String album) {
        Log.i(TAG, "onAlbumSelected: " + album);
        runOnUiThread(() -> {
            navigateToAlbum(album);
        });
    }

    @Override
    public void onArtistPlay(String artist) {
        AsyncTask.execute(() -> {
            List<Song> songs = mSongDao.getSongsForArtistAsList(artist);
            playMusic(songs, false);
        });
        Log.i(TAG, "onArtistPlay: " + artist);
    }

    @Override
    public void onArtistQueue(String artist) {
        AsyncTask.execute(() -> {
            List<Song> songs = mSongDao.getSongsForArtistAsList(artist);
            playMusic(songs, true);
        });
        Log.i(TAG, "onArtistQueue: " + artist);
    }

    @Override
    public void onArtistMenu(String artist) {
        Log.i(TAG, "onArtistMenu: " + artist);
        MutableLiveData<List<ContextMenuFragment.ItemConfig>> contextMenuEntries = new MutableLiveData<>();
        List<ContextMenuFragment.ItemConfig> entries = new ArrayList<>();
        entries.add(ContextMenuFragment.ItemConfig.builder()
                .imageId(R.drawable.ic_play)
                .textId(R.string.play)
                .callback($ -> onArtistPlay(artist)).build());
        entries.add(ContextMenuFragment.ItemConfig.builder()
                .imageId(R.drawable.ic_queue)
                .textId(R.string.queue)
                .callback($ -> onArtistQueue(artist)).build());
        entries.add(ContextMenuFragment.ItemConfig.builder()
                .imageId(R.drawable.ic_details)
                .textId(R.string.details)
                .callback($ -> navigateToArtist(artist)).build());
        entries.add(ContextMenuFragment.ItemConfig.builder()
                .imageId(R.drawable.ic_show)
                .textId(R.string.hide_artist_from_library)
                .callback($ -> hideSongsByArtist(artist)).build());
        if (mAppViewModel.showHiddenSongs()) {
            entries.add(ContextMenuFragment.ItemConfig.builder()
                    .imageId(R.drawable.ic_hide)
                    .exclusive(true)
                    .textId(R.string.unhide_artist_from_library)
                    .callback($ -> unhideSongsByArtist(artist)).build());
        }
        mContextMenuFragment = ContextMenuFragment.newInstance(contextMenuEntries);
        runOnUiThread(() -> {
            mContextMenuFragment.show(getSupportFragmentManager(), ContextMenuFragment.TAG);
            contextMenuEntries.setValue(entries);
        });
    }

    @Override
    public void onArtistSelected(String artist) {
        Log.i(TAG, "onArtistSelected: " + artist);
        runOnUiThread(() -> {
            navigateToArtist(artist);
        });
    }

    @Override
    public void onPlayFavorites() {
        AsyncTask.execute(() -> {
            List<Song> favorites = mSongDao.getFavoritesAsList();
            playMusic(favorites, false);
        });
    }

    @Override
    public void onFilterSelected(AudioService.Filter filter) {
        runOnUiThread(() -> {
            navigateToFilterDetails(filter);
        });
    }

    private void showCreatePlaylistDialog(Long songToAdd, boolean showConfirmation) {
        View dialogView = View.inflate(this, R.layout.alert_create_playlist, null);
        EditText editText = dialogView.findViewById(R.id.playlist_name);
        editText.setOnFocusChangeListener((View v, boolean hasFocus) -> {
            editText.post(() -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                }
            });
        });

        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    editText.setError(null);
                }
            }
        });

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(R.string.create_playlist)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        editText.setSingleLine();

        AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.setOnShowListener(dialogInterface -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (editText.getText().length() == 0) {
                    editText.setError(getResources().getString(R.string.playlist_name_empty));
                    return;
                }
                AsyncTask.execute(() -> {
                    Playlist newPlaylist = Playlist.builder()
                            .name(editText.getText().toString())
                            .build();
                    Playlist playlistByName = mPlaylistDao.getPlaylistByName(newPlaylist.getName());
                    if (playlistByName != null) {
                        runOnUiThread(() -> {
                            Toast.makeText(this, getString(R.string.playlist_exists, newPlaylist.getName()), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        int newPlaylistId = (int) mPlaylistDao.insert(newPlaylist);
                        alertDialog.dismiss();
                        if (showConfirmation) {
                            runOnUiThread(() -> {
                                Toast.makeText(this, getString(R.string.playlist_created, newPlaylist.getName()), Toast.LENGTH_SHORT).show();
                            });
                        }
                        if (songToAdd != null) {
                            onSongAddToPlaylist(songToAdd, newPlaylistId);
                        }
                    }
                });
            });
        });

        alertDialog.show();
        editText.requestFocus();
    }

    private void hideSongsByArtist(String artist) {
        AsyncTask.execute(() -> {
            mSongDao.hideSongsByArtist(artist);
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), getString(R.string.artist_removed_from_library, artist), Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, view -> {
                        AsyncTask.execute(() -> {
                            mSongDao.unhideSongsByArtist(artist);
                        });
                    });
            snackbar.show();

        });
    }

    private void hideSongsByAlbum(String album) {
        AsyncTask.execute(() -> {
            mSongDao.hideSongsByAlbum(album);
            Snackbar snackbar = Snackbar
                    .make(findViewById(android.R.id.content), getString(R.string.album_removed_from_library, album), Snackbar.LENGTH_SHORT)
                    .setAction(R.string.undo, view -> {
                        AsyncTask.execute(() -> {
                            mSongDao.unhideSongsByAlbum(album);
                        });
                    });
            snackbar.show();

        });

    }

    private void unhideSongsByAlbum(String album) {
        AsyncTask.execute(() -> {
            mSongDao.unhideSongsByAlbum(album);
        });
    }

    private void unhideSongsByArtist(String artist) {
        AsyncTask.execute(() -> {
            mSongDao.unhideSongsByArtist(artist);
        });
    }

    protected abstract void navigateToPlaylist(int playlistId);

    protected abstract void navigateToRadioStation(Long radioStationId);

    protected abstract void navigateToAlbum(String album);

    protected abstract void navigateToArtist(String artist);

    protected abstract void navigateToFilterDetails(AudioService.Filter filter);

    @Value
    @Builder
    public static class ContextMenuItem {
        ContextMenuFragment.ItemConfig mItemConfig;
        int mPosition;
    }
}
