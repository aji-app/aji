package ch.zhaw.engineering.aji.ui.playlist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import ch.zhaw.engineering.aji.ui.ListFragment;
import ch.zhaw.engineering.aji.util.SwipeToDeleteCallback;

public class PlaylistListFragment extends ListFragment {
    private static final String TAG = "PlaylistListFragment";
    private PlaylistFragmentListener mListener;
    private PlaylistRecyclerViewAdapter mAdapter;
    private AppViewModel mAppViewModel;

    @SuppressWarnings("unused")
    public static PlaylistListFragment newInstance() {
        PlaylistListFragment fragment = new PlaylistListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlist_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            mRecyclerView = (RecyclerView) view;
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            mRecyclerView.setLayoutManager(layoutManager);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListener != null) {
            mListener.showEmptyDetails();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            mAppViewModel.getAllPlaylists().observe(getViewLifecycleOwner(), this::onPlaylistsChanged);
            mAppViewModel.setPlaceholderText(R.string.empty_playlists_prompt);
        }
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void onPlaylistsChanged(List<PlaylistWithSongCount> playlists) {
        Log.i(TAG, "Updating playlists for playlist list fragment");
        if (getActivity() != null) {
            if (playlists.size() > 0) {
                AsyncTask.execute(() -> {
                    Song song = mAppViewModel.getFirstSongOfPlaylist(playlists.get(0).getPlaylistId());
                    if (song != null && mAppViewModel.isTwoPane()) {
                        getActivity().runOnUiThread(() -> {
                            mListener.onSongSelected(song.getSongId(), 0);
                        });
                    } else {
                        mAppViewModel.setPlaceholderText(R.string.empty_playlist_prompt);
                    }
                });
            } else {
                mAppViewModel.setPlaceholderText(R.string.empty_playlists_prompt);
            }
            getActivity().runOnUiThread(() -> {
                if (mAdapter == null) {
                    mAdapter = new PlaylistRecyclerViewAdapter(playlists, mListener, getActivity());
                } else {
                    mAdapter.updateItems(playlists);
                }
                ItemTouchHelper.Callback callback =
                        new SwipeToDeleteCallback(getActivity()) {
                            @Override
                            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                mAdapter.onDismiss(viewHolder.getAdapterPosition());
                            }
                        };
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(mRecyclerView);
                mRecyclerView.setAdapter(mAdapter);
            });
        }
    }

    public interface PlaylistFragmentListener {
        void onPlaylistSelected(int playlist);

        void onPlaylistMenu(int playlist, FragmentInteractionActivity.ContextMenuItem... additionalItems);

        void onPlaylistPlay(int playlist);

        void onPlaylistQueue(int playlist);

        void onPlaylistDelete(int playlist);

        public void onSongSelected(long songId, int position);

        void showEmptyDetails();
    }
}
