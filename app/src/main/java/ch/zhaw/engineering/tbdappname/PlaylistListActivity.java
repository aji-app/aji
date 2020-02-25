package ch.zhaw.engineering.tbdappname;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ch.zhaw.engineering.tbdappname.services.database.entity.PlaylistWithSongs;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistAdapter;
import ch.zhaw.engineering.tbdappname.ui.playlist.PlaylistViewModel;

import static ch.zhaw.engineering.tbdappname.AddOrEditPlaylistActivity.EXTRA_PLAYLIST_ID;

public class PlaylistListActivity extends AudioInterfaceActivity {

    private PlaylistViewModel mPlaylistViewModel;
    private boolean mSortAsc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_list);

        RecyclerView recyclerView = findViewById(R.id.playlist_list);
        mPlaylistViewModel = new PlaylistViewModel(this.getApplication());

        TextView noData = findViewById(R.id.no_playlists);

        mPlaylistViewModel.getAllPlaylists().observe(this, playlists -> {
            if (playlists.isEmpty()) {
                noData.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                noData.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            runOnUiThread(() -> {
                PlaylistAdapter adapter = new PlaylistAdapter(playlists, new PlaylistAdapter.PlaylistListInteractionListener() {
                    @Override
                    public void onPlaylistClick(PlaylistWithSongs playlist) {
                        playMusic(playlist.playlist);
                    }

                    @Override
                    public void onPlaylistLongClick(PlaylistWithSongs playlist) {
                        Intent intent = new Intent(PlaylistListActivity.this, AddOrEditPlaylistActivity.class);
                        intent.putExtra(EXTRA_PLAYLIST_ID, playlist.playlist.getPlaylistId());
                        startActivity(intent);
                    }
                }, this);
                recyclerView.setAdapter(adapter);
            });

        });

        ImageButton sortDirection = findViewById(R.id.playlist_sort_direction);
        sortDirection.setOnClickListener(v -> {
            mSortAsc = !mSortAsc;
            if (mSortAsc) {
                sortDirection.setImageResource(R.drawable.ic_up);
            } else {
                sortDirection.setImageResource(R.drawable.ic_down);
            }
            mPlaylistViewModel.changeSortOrder(mSortAsc);
        });

        SearchView searchView = findViewById(R.id.filter);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                mPlaylistViewModel.changeSearchText(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mPlaylistViewModel.changeSearchText(newText);
                return true;
            }
        });

        FloatingActionButton fab = findViewById(R.id.add_playlist);

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddOrEditPlaylistActivity.class);
            startActivity(intent);
        });
    }
}
