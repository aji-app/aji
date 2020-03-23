package ch.zhaw.engineering.tbdappname;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.database.repository.PlaylistRepository;
import ch.zhaw.engineering.tbdappname.services.database.repository.SongRepository;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistFragment;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongMetaFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongViewModel;

import static ch.zhaw.engineering.tbdappname.AddOrEditPlaylistActivity.EXTRA_PLAYLIST_ID;

public class TestActivity extends AppCompatActivity implements SongListFragment.SongListFragmentListener, SongMetaFragment.SongMetaFragmentListener, PlaylistListFragment.PlaylistFragmentListener, PlaylistFragment.PlaylistFragmentListener {
    private static final String TAG = "TestActivity";
    private SongViewModel mSongViewModel;
    private PlaylistRepository mPlaylistRepository;
    private SongRepository mSongRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, PlaylistFragment.newInstance())
                    .commitNow();
        }

        mSongViewModel = new ViewModelProvider(this).get(SongViewModel.class);
        mPlaylistRepository = PlaylistRepository.getInstance(this);
        mSongRepository = SongRepository.getInstance(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.song_list:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, SongFragment.newInstance())
                        .commitNow();
                return true;
            case R.id.playlist_list:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, PlaylistFragment.newInstance())
                        .commitNow();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.test_options_menu, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public void onSongSelected(Song song) {
        Toast.makeText(this, "onSongSelected: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSortTypeChanged(SongRepository.SortType sortType) {
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
    public void onSongPlay(Song song) {
        Toast.makeText(this, "onSongPlay: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSongQueue(Song song) {
        Toast.makeText(this, "onSongQueue: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSongEdit(Song song) {
        Toast.makeText(this, "onSongEdit: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSongAddToPlaylist(Song song, Playlist playlist) {
        Toast.makeText(this, "onSongAddToPlaylist: " + song.getTitle() + ", " + playlist.getName(), Toast.LENGTH_SHORT).show();
        AsyncTask.execute(() -> {
            mPlaylistRepository.addSongToPlaylist(song, playlist);
        });
    }

    @Override
    public void onSongDelete(Song song) {
        Toast.makeText(this, "onSongDelete: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreatePlaylist() {
        showPlaylistNameDialog(null);
//        Toast.makeText(this, "onCreatePlaylist", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(this, AddOrEditPlaylistActivity.class);
//        startActivity(intent);
    }

    @Override
    public void onToggleFavorite(Song song) {
        Toast.makeText(this, "onToggleFavorite: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        AsyncTask.execute(() -> {
            mSongRepository.toggleFavorite(song);
        });
    }

    @Override
    public void onPlaylistSelected(PlaylistWithSongCount playlist) {
        Toast.makeText(this, "onPlaylistSelected: " + playlist.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlaylistEdit(PlaylistWithSongCount playlist) {
        Toast.makeText(this, "onPlaylistEdit: " + playlist.getName(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, AddOrEditPlaylistActivity.class);
        intent.putExtra(EXTRA_PLAYLIST_ID, playlist.getPlaylistId());
        startActivity(intent);
    }

    @Override
    public void onPlaylistPlay(PlaylistWithSongCount playlist) {
        Toast.makeText(this, "onPlaylistPlay: " + playlist.getName(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPlaylistQueue(PlaylistWithSongCount playlist) {
        Toast.makeText(this, "onPlaylistQueue: " + playlist.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlaylistDelete(PlaylistWithSongCount playlist) {
        Toast.makeText(this, "onPlaylistDelete: " + playlist.getName(), Toast.LENGTH_SHORT).show();
        AsyncTask.execute(() -> {
            mPlaylistRepository.deletePlaylistById(playlist.getPlaylistId());
        });
    }

    private void showPlaylistNameDialog(PlaylistWithSongCount playlist) {


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
                    mPlaylistRepository.insert(newPlaylist);
                });
                alertDialog.dismiss();
            });
        });

        alertDialog.show();
        editText.requestFocus();
    }
}
