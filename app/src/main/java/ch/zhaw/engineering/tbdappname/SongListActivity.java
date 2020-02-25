package ch.zhaw.engineering.tbdappname;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import ch.zhaw.engineering.tbdappname.services.database.repository.SongRepository;
import ch.zhaw.engineering.tbdappname.ui.songs.SongAdapter;
import ch.zhaw.engineering.tbdappname.ui.songs.SongViewModel;

public class SongListActivity extends AudioInterfaceActivity {
    private SongViewModel mSongViewModel;
    private boolean mSortAsc = true;

    private SongAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        RecyclerView recyclerView = findViewById(R.id.song_list);
        mSongViewModel = new SongViewModel(this.getApplication());

        TextView noData = findViewById(R.id.no_songs);

        mSongViewModel.getAllSongs().observe(this, songs -> {
            if (songs.isEmpty()) {
                noData.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                noData.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            runOnUiThread(() -> {
                mAdapter = new SongAdapter(songs, Collections.emptyList(),false, new SongAdapter.SongListInteractionListener() {
                    @Override
                    public void onSongClick(Song song) {
                        SongListActivity.this.playMusic(song, false);
                    }

                    @Override
                    public void onSongLongClick(Song song) {
                        SongListActivity.this.playMusic(song, true);
                    }

                    @Override
                    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                    }
                });
                recyclerView.setAdapter(mAdapter);
            });

        });

        ImageButton sortDirection = findViewById(R.id.sort_direction);
        sortDirection.setOnClickListener(v -> {
            mSortAsc = !mSortAsc;
            if (mSortAsc) {
                sortDirection.setImageResource(R.drawable.ic_up);
            } else {
                sortDirection.setImageResource(R.drawable.ic_down);
            }
            mSongViewModel.changeSortOrder(mSortAsc);
        });

        Button sortByName = findViewById(R.id.sort_by_name);
        sortByName.setOnClickListener(v -> mSongViewModel.changeSortType(SongRepository.SortType.TITLE));
        Button sortByArtist = findViewById(R.id.sort_by_artist);
        sortByArtist.setOnClickListener(v -> mSongViewModel.changeSortType(SongRepository.SortType.ARTIST));
        Button sortByAlbum = findViewById(R.id.sort_by_album);
        sortByAlbum.setOnClickListener(v -> mSongViewModel.changeSortType(SongRepository.SortType.ALBUM));

        SearchView searchView = findViewById(R.id.filter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                mSongViewModel.changeSearchText(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mSongViewModel.changeSearchText(newText);
                return true;
            }
        });

    }
}
