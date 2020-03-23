package ch.zhaw.engineering.tbdappname;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
                    .replace(R.id.container, SongFragment.newInstance())
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
        Toast.makeText(this, "onCreatePlaylist", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, AddOrEditPlaylistActivity.class);
        startActivity(intent);
    }

    @Override
    public void onToggleFavorite(Song song) {
        Toast.makeText(this, "onToggleFavorite: " + song.getTitle(), Toast.LENGTH_SHORT).show();
        AsyncTask.execute(() -> {
            mSongRepository.toggleFavorite(song);
        });
    }

    @Override
    public void onPlaylistSelected(PlaylistWithSongCount item) {
        Toast.makeText(this, "onPlaylistSelected: " + item.getName(), Toast.LENGTH_SHORT).show();
    }
}
