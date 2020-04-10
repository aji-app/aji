package ch.zhaw.engineering.tbdappname;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.files.WebRadioPlsParser;
import ch.zhaw.engineering.tbdappname.ui.AppViewModel;
import ch.zhaw.engineering.tbdappname.ui.expandedcontrols.ExpandedControlsFragment;
import ch.zhaw.engineering.tbdappname.ui.library.AlbumArtistListFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistDetailsFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistListFragment;
import ch.zhaw.engineering.tbdappname.ui.radiostation.RadioStationDetailsFragment;
import ch.zhaw.engineering.tbdappname.ui.radiostation.RadioStationFragmentInteractionListener;
import ch.zhaw.engineering.tbdappname.ui.song.SongDetailsFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongFragment;
import ch.zhaw.engineering.tbdappname.ui.song.list.SongListFragment;

import static ch.zhaw.engineering.tbdappname.DirectorySelectionActivity.EXTRA_FILE;

public abstract class FragmentInteractionActivity extends AppCompatActivity implements SongListFragment.SongListFragmentListener, SongFragment.SongFragmentListener,
        PlaylistListFragment.PlaylistFragmentListener, PlaylistFragment.PlaylistFragmentListener, PlaylistDetailsFragment.PlaylistDetailsFragmentListener,
        RadioStationFragmentInteractionListener, RadioStationDetailsFragment.RadioStationDetailsFragmentListener, ExpandedControlsFragment.ExpandedControlsFragmentListener,
        SongDetailsFragment.SongDetailsFragmentListener, AlbumArtistListFragment.AlbumArtistListFragmentListener {

    private static final int REQUEST_CODE_PLS_SELECT = 2;
    private SongDao mSongDao;
    private PlaylistDao mPlaylistDao;
    protected AppViewModel mAppViewModel;
    private RadioStationDao mRadioStationDao;

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
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongSelected: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
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
                // TODO
                break;
        }
    }

    @Override
    public void onSongSortTypeChanged(SongDao.SortType sortType) {
        Toast.makeText(this, "onSortTypeChanged: " + sortType, Toast.LENGTH_SHORT).show();
        mAppViewModel.changeSongSortType(sortType);
    }

    @Override
    public void onSongSearchTextChanged(String searchText) {
        Toast.makeText(this, "onSongSearchTextChanged: " + searchText, Toast.LENGTH_SHORT).show();
        mAppViewModel.changeSongSearchText(searchText);
    }


    @Override
    public void onSongSortDirectionChanged(boolean ascending) {
        Toast.makeText(this, "onSortDirectionChanged: " + (ascending ? "ascending" : "descending"), Toast.LENGTH_SHORT).show();
        mAppViewModel.changeSongSortOrder(ascending);
    }

    @Override
    public void onSongPlay(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongPlay: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onSongQueue(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongQueue: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onSongMenu(long songId, SongListFragment.SongSelectionOrigin origin) {
        AsyncTask.execute(() -> {
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

            Song song = mSongDao.getSongById(songId);
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongAddToPlaylist: " + song.getTitle() + ", " + playlist.getName(), Toast.LENGTH_SHORT).show();
            });
        });

    }

    @Override
    public void onSongDelete(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongDelete: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
            mSongDao.deleteSongById(songId);
        });
    }

    @Override
    public void onCreatePlaylist() {
        showCreatePlaylistDialog();
    }

    @Override
    public void onToggleFavorite(long songId) {
        AsyncTask.execute(() -> {
            mSongDao.toggleFavorite(songId);

            Song song = mSongDao.getSongById(songId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onToggleFavorite: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onPlaylistModified(int playlistId, List<Long> songIds) {
        AsyncTask.execute(() -> {
            mPlaylistDao.modifyPlaylist(songIds, playlistId);
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistModified: " + playlist.getName() + " ...", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onPlaylistSelected(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistSelected: " + playlist.getName(), Toast.LENGTH_SHORT).show();

                navigateToPlaylist(playlistId);
            });
        });
    }

    @Override
    public void onPlaylistEdit(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistEdit: " + playlist.getName(), Toast.LENGTH_SHORT).show();
                navigateToPlaylist(playlistId);
            });
        });

    }

    @Override
    public void onPlaylistPlay(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistPlay: " + playlist.getName(), Toast.LENGTH_SHORT).show();
            });
        });

    }

    @Override
    public void onPlaylistQueue(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistQueue: " + playlist.getName(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onPlaylistDelete(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistDelete: " + playlist.getName(), Toast.LENGTH_SHORT).show();
            });

            mPlaylistDao.deletePlaylist(playlistId);
        });
    }


    @Override
    public void onPlaylistNameChanged(int playlistId, String newName) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            playlist.setName(newName);
            mPlaylistDao.update(playlist);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistNameChanged: " + playlist.getName(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onPlaylistSearchTextChanged(String text) {
        mAppViewModel.changePlaylistSearchText(text);
    }

    @Override
    public void onPlaylistSortDirectionChanged(boolean ascending) {
        mAppViewModel.changePlaylistSortOrder(ascending);
    }

    @Override
    public void onRadioStationSelected(long radioStationId) {
        AsyncTask.execute(() -> {
            RadioStation radio = mRadioStationDao.getRadioStation(radioStationId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onRadioStationSelected: " + radio.getName(), Toast.LENGTH_SHORT).show();
                navigateToRadioStation(radioStationId);
            });
        });
    }

    @Override
    public void onRadioStationPlay(long radioStationId) {
        AsyncTask.execute(() -> {
            RadioStation radio = mRadioStationDao.getRadioStation(radioStationId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onRadioStationPlay: " + radio.getName(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onRadioStationEdit(long radioStationId) {
        AsyncTask.execute(() -> {
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
                runOnUiThread(() -> {
                    Toast.makeText(this, "onRadioStationDelete: " + radio.getName(), Toast.LENGTH_SHORT).show();
                });
            }
            mRadioStationDao.deleteRadioStationById(radioStationId);
        });
    }

    @Override
    public void onCreateRadioStation() {
        AsyncTask.execute(() -> {
            runOnUiThread(() -> {
                Toast.makeText(this, "onCreateRadioStation", Toast.LENGTH_SHORT).show();
                navigateToRadioStation(null);
            });
        });
    }

    @Override
    public void onRadioStationSearchTextChanged(String searchText) {
        mAppViewModel.changeRadioSearchText(searchText);
    }

    @Override
    public void onRadioStationSortDirectionChanged(boolean ascending) {
        mAppViewModel.changeRadioSortOrder(ascending);
    }

    @Override
    public void onRadioStationEdit(RadioStationDto updatedRadioStation) {
        AsyncTask.execute(() -> {
            if (updatedRadioStation.getId() != null) {
                mRadioStationDao.updateRadioStation(updatedRadioStation);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "onRadioStationEdit: " + updatedRadioStation.getName(), Toast.LENGTH_SHORT).show();
            });
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
            runOnUiThread(() -> {
                Toast.makeText(this, "onRadioStationSaved: " + updatedRadioStation.getName(), Toast.LENGTH_SHORT).show();
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
        runOnUiThread(() -> {
            Toast.makeText(this, "onPlayPause", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onNext() {
        runOnUiThread(() -> {
            Toast.makeText(this, "onNext", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onPrevious() {
        runOnUiThread(() -> {
            Toast.makeText(this, "onPrevious", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onToggleShuffle() {
        runOnUiThread(() -> {
            Toast.makeText(this, "onToggleShuffle", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onChangeRepeatMode() {
        runOnUiThread(() -> {
            Toast.makeText(this, "onChangeRepeatMode", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onToggleAutoQueue() {
        runOnUiThread(() -> {
            Toast.makeText(this, "onToggleAutoQueue", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onAlbumPlay(String album) {
        runOnUiThread(() -> {
            Toast.makeText(this, "onAlbumPlay: " + album, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onAlbumMenu(String album) {
        runOnUiThread(() -> {
            Toast.makeText(this, "onAlbumMenu: " + album, Toast.LENGTH_SHORT).show();
            navigateToAlbum(album);
        });
    }

    @Override
    public void onAlbumSelected(String album) {
        runOnUiThread(() -> {
            navigateToAlbum(album);
            Toast.makeText(this, "onAlbumSelected: " + album, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onArtistPlay(String artist) {
        runOnUiThread(() -> {
            Toast.makeText(this, "onArtistPlay: " + artist, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onArtistMenu(String artist) {
        runOnUiThread(() -> {
            navigateToArtist(artist);
            Toast.makeText(this, "onArtistMenu: " + artist, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onArtistSelected(String artist) {
        runOnUiThread(() -> {
            navigateToArtist(artist);
            Toast.makeText(this, "onArtistSelected: " + artist, Toast.LENGTH_SHORT).show();
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
