package ch.zhaw.engineering.tbdappname;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dao.PlaylistDao;
import ch.zhaw.engineering.tbdappname.services.database.dao.SongDao;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistDetailsFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongMetaFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongViewModel;

public class TestActivity extends AppCompatActivity implements SongListFragment.SongListFragmentListener, SongMetaFragment.SongMetaFragmentListener,
        PlaylistListFragment.PlaylistFragmentListener, PlaylistFragment.PlaylistFragmentListener, PlaylistDetailsFragment.PlaylistDetailsFragmentListener {
    private static final String TAG = "TestActivity";
    private SongViewModel mSongViewModel;
    private SongDao mSongDao;
    private PlaylistDao mPlaylistDao;

    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        if (savedInstanceState == null) {
            mCurrentFragment = PlaylistFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, mCurrentFragment)
                    .commitNow();
        }

        mSongViewModel = new ViewModelProvider(this).get(SongViewModel.class);
        mSongDao = SongDao.getInstance(this);
        mPlaylistDao = PlaylistDao.getInstance(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.song_list:
                if (mCurrentFragment instanceof SongFragment) {
                    return false;
                }
                replaceFragment(SongFragment.newInstance());
                return true;
            case R.id.playlist_list:
                if (mCurrentFragment instanceof PlaylistFragment) {
                    return false;
                }
                replaceFragment(PlaylistFragment.newInstance());
                return true;
            default:
                return false;
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, android.R.anim.slide_in_left, android.R.anim.slide_out_right )
//                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
        mCurrentFragment = fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_options_menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public void onBackPressed() {
        // Catch back action and pops from backstack
        // (if you called previously to addToBackStack() in your transaction)
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        }
        // Default action on back pressed
        else super.onBackPressed();
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
    public void onSortTypeChanged(SongDao.SortType sortType) {
        Toast.makeText(this, "onSortTypeChanged: " + sortType, Toast.LENGTH_SHORT).show();
        mSongViewModel.changeSortType(sortType);
    }

    @Override
    public void onSearchTextChanged(String text) {
        Toast.makeText(this, "onSearchTextChanged: " + text, Toast.LENGTH_SHORT).show();
        mSongViewModel.changeSearchText(text);
    }

    @Override
    public void onSortDirectionChanged(boolean ascending) {
        Toast.makeText(this, "onSortDirectionChanged: " + (ascending ? "ascending" : "descending"), Toast.LENGTH_SHORT).show();
        mSongViewModel.changeSortOrder(ascending);
    }

    @Override
    public void onSongPlay(long songId) {
        Toast.makeText(this, "onSongPlay: " + mSongDao.getSongById(songId).getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSongQueue(long songId) {
        Toast.makeText(this, "onSongQueue: " + mSongDao.getSongById(songId).getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSongEdit(long songId) {
        Toast.makeText(this, "onSongEdit: " + mSongDao.getSongById(songId).getTitle(), Toast.LENGTH_SHORT).show();
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
        showPlaylistNameDialog(null);
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
    public void onSongsReordered(List<Long> songIds, int playlistId) {
        AsyncTask.execute(() -> {
            mPlaylistDao.reorderSongsInPlaylist(songIds, playlistId);
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongsReordered: " + playlist.getName() + " ...", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onPlaylistSelected(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistSelected: " + playlist.getName(), Toast.LENGTH_SHORT).show();

                replaceFragment(PlaylistDetailsFragment.newInstance(playlistId));
            });
        });
    }

    @Override
    public void onPlaylistEdit(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistEdit: " + playlist.getName(), Toast.LENGTH_SHORT).show();
                replaceFragment(PlaylistDetailsFragment.newInstance(playlistId));
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
            mPlaylistDao.deletePlaylist(playlistId);

            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistDelete: " + playlist.getName(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showPlaylistNameDialog(Playlist playlist) {


// ...Irrelevant code for customizing the buttons and title
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
                .setTitle(playlist == null ? R.string.create_playlist : R.string.edit_playlist)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        if (playlist != null) {
            editText.setText(playlist.getName());
        }

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
    public void onPlaylistSave(int playlistId) {
        AsyncTask.execute(() -> {
            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onPlaylistSave: " + playlist.getName(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onSongRemovedFromPlaylist(long songId, int playlistId) {
        AsyncTask.execute(() -> {
            mPlaylistDao.deleteSongFromPlaylist(songId, playlistId);

            Playlist playlist = mPlaylistDao.getPlaylistById(playlistId);
            Song song = mSongDao.getSongById(songId);
            runOnUiThread(() -> {
                Toast.makeText(this, "onSongRemovedFromPlaylist: " + playlist.getName() + " - " + song.getTitle(), Toast.LENGTH_SHORT).show();
            });
        });
    }
}
