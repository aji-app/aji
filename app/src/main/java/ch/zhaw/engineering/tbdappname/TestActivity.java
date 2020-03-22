package ch.zhaw.engineering.tbdappname;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.Toast;

import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.database.repository.SongRepository;
import ch.zhaw.engineering.tbdappname.ui.song.SongFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongViewModel;
import ch.zhaw.engineering.tbdappname.ui.songMeta.SongMetaFragment;

public class TestActivity extends AppCompatActivity implements SongListFragment.SongListFragmentListener, SongMetaFragment.SongMetaFragmentListener {

    private SongViewModel mSongViewModel;

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
    }

    @Override
    public void onSongDelete(Song song) {
        Toast.makeText(this, "onSongDelete: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreatePlaylist() {
        Toast.makeText(this, "onCreatePlaylist", Toast.LENGTH_SHORT).show();
    }
}
