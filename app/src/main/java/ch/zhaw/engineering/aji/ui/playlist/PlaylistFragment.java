package ch.zhaw.engineering.aji.ui.playlist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentPlaylistBinding;
import ch.zhaw.engineering.aji.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.SortResource;
import ch.zhaw.engineering.aji.ui.TabletAwareFragment;
import ch.zhaw.engineering.aji.ui.menu.MenuHelper;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class PlaylistFragment extends TabletAwareFragment {
    private PlaylistFragmentListener mListener;
    private PlaylistWithSongCount mTopPlaylist;
    private Song mTopSong;

    public static PlaylistFragment newInstance() {
        PlaylistFragment fragment = new PlaylistFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void showDetails() {
        if (mTopPlaylist != null) {
            if (mTopSong != null) {
                mListener.onSongSelected(mTopSong.getSongId(), 0);
            } else {
                mAppViewModel.setPlaceholderText(R.string.empty_playlist_prompt);
                mListener.showEmptyDetails();
            }
        } else {
            mListener.showEmptyDetails();
        }
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
            configureFab();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistFragmentListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        configureFab();
        setPlaceholderText();
    }


    private void setPlaceholderText() {
        if (mAppViewModel == null) {
            return;
        }
        String searchText = mAppViewModel.getSearchString(SortResource.PLAYLISTS);
        if (searchText != null && !searchText.equals("")) {
            mAppViewModel.setPlaceholderText(R.string.search_no_result);
        } else {
            mAppViewModel.setPlaceholderText(R.string.empty_playlists_prompt);
        }
    }

    private void configureFab() {
        if (mListener != null) {
            mListener.configureFab(v -> {
                if (mListener != null) {
                    mListener.onCreatePlaylist(null);
                }
            }, R.drawable.ic_add);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentPlaylistBinding binding = FragmentPlaylistBinding.inflate(inflater, container, false);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.playlist_container, PlaylistListFragment.newInstance())
                    .commitNow();
        }

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel.getAllPlaylists().observe(getViewLifecycleOwner(), playlists -> {
                setPlaceholderText();
                if (playlists.size() > 0) {
                    mTopPlaylist = playlists.get(0);
                    AsyncTask.execute(() -> {
                        mTopSong = mAppViewModel.getFirstSongOfPlaylist(mTopPlaylist.getPlaylistId());
                        triggerTabletLogic();
                    });
                } else {
                    mTopPlaylist = null;
                    mTopSong = null;
                }
                triggerTabletLogic();
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.filter_list_menu, menu);
        MenuHelper.setupSearchView(SortResource.PLAYLISTS, mAppViewModel, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (!MenuHelper.onOptionsItemSelected(SortResource.PLAYLISTS, mAppViewModel, item)) {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public interface PlaylistFragmentListener extends FabCallbackListener {
        void onCreatePlaylist(Long songToAdd);

        void onPlaylistSelected(int playlist);

        void showEmptyDetails();

        public void onSongSelected(long songId, int position);
    }
}
