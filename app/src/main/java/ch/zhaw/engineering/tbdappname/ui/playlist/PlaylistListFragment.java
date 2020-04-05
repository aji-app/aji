package ch.zhaw.engineering.tbdappname.ui.playlist;

import android.content.Context;
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

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.dto.PlaylistWithSongCount;
import ch.zhaw.engineering.tbdappname.ui.TbdListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.SongRecyclerViewAdapter;
import ch.zhaw.engineering.tbdappname.util.SwipeToDeleteCallback;

public class PlaylistListFragment extends TbdListFragment {
    private static final String TAG = "PlaylistListFragment";
    private PlaylistFragmentListener mListener;
    private PlaylistRecyclerViewAdapter mAdapter;

    @SuppressWarnings("unused")
    public static PlaylistListFragment newInstance() {
        PlaylistListFragment fragment = new PlaylistListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            PlaylistListViewModel viewModel = new ViewModelProvider(getActivity()).get(PlaylistListViewModel.class);
            viewModel.getAllPlaylists().observe(getViewLifecycleOwner(), this::onPlaylistsChanged);
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

        void onPlaylistEdit(int playlist);

        void onPlaylistPlay(int playlist);

        void onPlaylistQueue(int playlist);

        void onPlaylistDelete(int playlist);

    }
}
