package ch.zhaw.engineering.tbdappname;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.ui.AppViewModel;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistDetailsFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongListFragment;

public abstract class FragmentInteractionActivity extends AppCompatActivity implements SongListFragment.SongListFragmentListener, SongFragment.SongFragmentListener,
        PlaylistListFragment.PlaylistFragmentListener, PlaylistFragment.PlaylistFragmentListener, PlaylistDetailsFragment.PlaylistDetailsFragmentListener {

    private SongDao mSongDao;
    private PlaylistDao mPlaylistDao;
    protected AppViewModel mAppViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppViewModel = new ViewModelProvider(this).get(AppViewModel.class);
        mSongDao = SongDao.getInstance(this);
        mPlaylistDao = PlaylistDao.getInstance(this);
    }

    @Override
    public void onSongSelected(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongSelected: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        });
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
    public void onSongMenu(long songId) {
        AsyncTask.execute(() -> {
            Song song = mSongDao.getSongById(songId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongEdit: " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        });
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

    protected abstract void navigateToPlaylist(int playlistId);
}
