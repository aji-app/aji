package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentPlaylistBinding;
import ch.zhaw.engineering.tbdappname.ui.SortingListener;

public class PlaylistFragment extends Fragment {
    private PlaylistFragmentListener mListener;

    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PlaylistFragmentListener) {
            mListener = (PlaylistFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistFragmentListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentPlaylistBinding binding = FragmentPlaylistBinding.inflate(inflater, container, false);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.playlist_container, PlaylistListFragment.newInstance())
                    .commitNow();
        }

        binding.fabAddPlaylist.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCreatePlaylist();
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlist_list_menu, menu);
        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setMaxWidth((Resources.getSystem().getDisplayMetrics().widthPixels / 2));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mListener.onSearchTextChanged(SortingListener.SortResource.PLAYLISTS, newText);
                return true;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.playlist_direction_asc:
                mListener.onSortDirectionChanged(SortingListener.SortResource.PLAYLISTS, true);
                return true;
            case R.id.playlist_direction_desc:
                mListener.onSortDirectionChanged(SortingListener.SortResource.PLAYLISTS, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface PlaylistFragmentListener extends SortingListener {
        void onCreatePlaylist();
    }
}
