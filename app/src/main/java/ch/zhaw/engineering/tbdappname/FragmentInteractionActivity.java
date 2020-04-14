package ch.zhaw.engineering.tbdappname;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.audio.AudioService;
import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.files.WebRadioPlsParser;
import ch.zhaw.engineering.tbdappname.ui.SortResource;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistSelectionFragment;
import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;
import ch.zhaw.engineering.tbdappname.ui.expandedcontrols.ExpandedControlsFragment;
import ch.zhaw.engineering.tbdappname.ui.library.AlbumArtistListFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistDetailsFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistListFragment;
import ch.zhaw.engineering.tbdappname.ui.radiostation.RadioStationDetailsFragment;
import ch.zhaw.engineering.tbdappname.ui.radiostation.RadioStationFragmentInteractionListener;
import ch.zhaw.engineering.tbdappname.ui.song.SongDetailsFragment;
import ch.zhaw.engineering.tbdappname.ui.song.list.SongListFragment;

import static ch.zhaw.engineering.tbdappname.DirectorySelectionActivity.EXTRA_FILE;

public abstract class FragmentInteractionActivity extends AudioInterfaceActivity implements SongListFragment.SongListFragmentListener,
        PlaylistListFragment.PlaylistFragmentListener, PlaylistFragment.PlaylistFragmentListener, PlaylistDetailsFragment.PlaylistDetailsFragmentListener,
        RadioStationFragmentInteractionListener, RadioStationDetailsFragment.RadioStationDetailsFragmentListener, ExpandedControlsFragment.ExpandedControlsFragmentListener,
        SongDetailsFragment.SongDetailsFragmentListener, AlbumArtistListFragment.AlbumArtistListFragmentListener, PlaylistSelectionFragment.PlaylistSelectionListener {

    private static final String TAG = "FragmentInteractions";
    private static final int REQUEST_CODE_PLS_SELECT = 2;
    private SongDao mSongDao;
    private PlaylistDao mPlaylistDao;
    protected AppViewModel mAppViewModel;
    private RadioStationDao mRadioStationDao;
    private PlaylistSelectionFragment mAddToPlaylistSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppViewModel = new ViewModelProvider(this).get(AppViewModel.class);
        mSongDao = SongDao.getInstance(this);
        mPlaylistDao = PlaylistDao.getInstance(this);
        mRadioStationDao = RadioStationDao.getInstance(this);
    }

    @Override
    public void onSongSelected(long songId, SongListFragment.SongSelectionOrigin origin) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            Log.i(TAG, "onSongSelected: " + song.getTitle());
        });
        switch (origin) {

            case ALBUM:
                navigateToSongFromAlbum(songId);
                break;
            case SONG:
                navigateToSongFromLibrary(songId);
                break;
            case ARTIST:
                navigateToSongFromArtist(songId);
                break;
            case PLAYLIST:
                navigateToSongFromPlaylist(songId);
                break;
            case EXPANDED_CONTROLS:
                // TODO Navigate to song from persistent controls
                break;
        }
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
        });
        runOnUiThread(() -> {
            mAddToPlaylistSheet = PlaylistSelectionFragment.newInstance(songId);
            mAddToPlaylistSheet.show(getSupportFragmentManager(), PlaylistSelectionFragment.TAG);
        });
    }

    @Override
    public void onSongQueue(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            playMusic(song, true);
            Log.i(TAG, "onSongQueue: " + song.getTitle());
        });
    }

    @Override
    public void onSongMenu(long songId, SongListFragment.SongSelectionOrigin origin) {
        AsyncTask.execute(() -> {
            // TODO: Open menu bottom sheet for song
            Song song = mSongDao.getSongById(songId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongEdit: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        });
        navigateToSongFromLibrary(songId);
    }

    @Override
    public void onSongAddToPlaylist(long songId, int playlistId) {
        AsyncTask.execute(() -> {
            mPlaylistDao.addSongToPlaylist(songId, playlistId);

            if (mAddToPlaylistSheet != null) {
                mAddToPlaylistSheet.dismiss();
            }

            Song song = mSongDao.getSongById(songId);
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            Log.i(TAG, "onSongAddToPlaylist: " + song.getTitle() + ", " + playlist.getName());
        });

    }

    @Override
    public void onSongDelete(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            Log.i(TAG, "onSongDelete: " + song.getTitle());
            mSongDao.deleteSongById(songId);
        });
    }

    @Override
    public void onCreatePlaylist() {
        showCreatePlaylistDialog();
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
            mPlaylistDao.modifyPlaylist(songIds, playlistId);
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
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
    public void onPlaylistEdit(int playlistId) {
        AsyncTask.execute(() -> {
            // TODO: Open menu bottom sheet for playlist
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            Log.i(TAG, "onPlaylistEdit: " + playlist.getName());
            runOnUiThread(() -> {
                navigateToPlaylist(playlistId);
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
    public void onRadioStationEdit(long radioStationId) {
        AsyncTask.execute(() -> {
            // TODO: Open menu bottom sheet for radio
            RadioStation radio = mRadioStationDao.getRadioStation(radioStationId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onRadioStationEdit: " + radio.getName(), Toast.LENGTH_SHORT).show();
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
        AsyncTask.execute(() -> {
            if (updatedRadioStation.getId() != null) {
                mRadioStationDao.updateRadioStation(updatedRadioStation);
            }
            Log.i(TAG, "onRadioStationEdit: " + updatedRadioStation.getName());
        });
    }

    @Override
    public void onRadioStationSaved(RadioStationDto updatedRadioStation) {
        AsyncTask.execute(() -> {
            if (updatedRadioStation.getId() != null) {
                mRadioStationDao.updateRadioStation(updatedRadioStation);
            } else {
                mRadioStationDao.createRadioStation(updatedRadioStation);
            }
            Log.i(TAG, "onRadioStationSaved: " + updatedRadioStation.getName());
            runOnUiThread(() -> {
                onSupportNavigateUp();
            });
        });
    }

    @Override
    public void onRadioStationImport() {
        Intent intent = new Intent(this, PlsFileSelectionActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PLS_SELECT);
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
    public void onToggleShuffle() {
        AudioService.AudioServiceBinder service = mAudioService.getValue();
        if (service == null) {
            return;
        }
        service.toggleShuffle();
        // TODO UPdate shuffle button
//        if (service.isShuffleModeEnabled()) {
//            shuffle.setImageResource(R.drawable.ic_shuffle_on);
//        } else {
//            shuffle.setImageResource(R.drawable.ic_shuffle_off);
//        }
        Log.i(TAG, "onToggleShuffle" + service.isShuffleModeEnabled());
    }

    @Override
    public void onChangeRepeatMode() {
        AudioService.AudioServiceBinder service = mAudioService.getValue();
        if (service == null) {
            return;
        }
        service.toggleRepeatMode();
        Log.i(TAG, "onChangeRepeatMode");
    }

    @Override
    public void onToggleAutoQueue() {
        AudioService.AudioServiceBinder service = mAudioService.getValue();
        if (service == null) {
            return;
        }
        service.toggleAutoQueue();
        Log.i(TAG, "onToggleAutoQueue");
    }

    @Override
    public void onAlbumPlay(String album) {
        // TODO play album
        runOnUiThread(() -> {
            Toast.makeText(this, "onAlbumPlay: " + album, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onAlbumMenu(String album) {
        Log.i(TAG, "onAlbumMenu: " + album);
        // TODO Open menu bottom sheet for album
        runOnUiThread(() -> {
            navigateToAlbum(album);
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
        // TODO play songs from artist
        runOnUiThread(() -> {
            Toast.makeText(this, "onArtistPlay: " + artist, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onArtistMenu(String artist) {
        Log.i(TAG, "onArtistMenu: " + artist);
        runOnUiThread(() -> {
            // TODO: Open Menu Sheet for artist
            navigateToArtist(artist);
        });
    }

    @Override
    public void onArtistSelected(String artist) {
        Log.i(TAG, "onArtistSelected: " + artist);
        runOnUiThread(() -> {
            navigateToArtist(artist);
        });
    }

    private void showCreatePlaylistDialog() {

        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.alert_create_playlist, null);
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

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this, R.style.Theme_App_AlertDialog)
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
                    mPlaylistDao.insert(newPlaylist);
                });
                alertDialog.dismiss();
            });
        });

        alertDialog.show();
        editText.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PLS_SELECT) {
            if (data != null && data.hasExtra(EXTRA_FILE)) {
                String path = data.getStringExtra(EXTRA_FILE);
                AsyncTask.execute(() -> {
                    radioStationImported(WebRadioPlsParser.parseSingleRadioStationFromPlsFile(path));
                });
            }
        }
    }

    protected abstract void navigateToPlaylist(int playlistId);

    protected abstract void navigateToRadioStation(Long radioStationId);

    protected abstract void radioStationImported(RadioStationDto imported);

    protected abstract void navigateToAlbum(String album);

    protected abstract void navigateToArtist(String artist);

    protected abstract void navigateToSongFromLibrary(long songId);

    protected abstract void navigateToSongFromAlbum(long songId);

    protected abstract void navigateToSongFromArtist(long songId);

    protected abstract void navigateToSongFromPlaylist(long songId);
}
