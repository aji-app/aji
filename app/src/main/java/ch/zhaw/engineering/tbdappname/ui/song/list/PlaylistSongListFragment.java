package ch.zhaw.engineering.tbdappname.ui.song.list;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;
import lombok.experimental.Delegate;

public class PlaylistSongListFragment extends SongListFragment {
    private static final String ARG_PLAYLIST_ID = "playlist-id";
    private static final String TAG = "PlaylistSongsFragment";
    private Integer mPlaylistId;
    private ItemTouchHelper mItemTouchHelper;

    public static SongListFragment newInstance(@Nullable Integer playlistId) {
        SongListFragment fragment = new PlaylistSongListFragment();
        if (playlistId != null) {
            Bundle args = new Bundle();
            args.putInt(ARG_PLAYLIST_ID, playlistId);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_PLAYLIST_ID)) {
            mPlaylistId = getArguments().getInt(ARG_PLAYLIST_ID);
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        super.onStartDrag(viewHolder);
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        appViewModel.getSongsForPlaylist(mPlaylistId).observe(getViewLifecycleOwner(), songs -> {
            if (getActivity() != null) {
                Log.i(TAG, "Got Songs for Playlist Song View " + songs.size());
                getActivity().runOnUiThread(() -> {
                    mAdapter = new SongRecyclerViewAdapter(songs, new CustomListener(mListener), mPlaylistId, this);
                    ItemTouchHelper.Callback callback =
                            new SongRecyclerViewAdapter.SimpleItemTouchHelperCallback(mAdapter, getActivity());
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                    mRecyclerView.setAdapter(mAdapter);
                });
            }
        });
    }

    private static class CustomListener implements SongListFragmentListener {

        @Delegate(excludes=CustomDelegates.class)
        private final SongListFragmentListener mListener;

        private CustomListener(SongListFragmentListener listener) {
            mListener = listener;
        }

        @Override
        public void onSongSelected(long songId) {
            mListener.onSongSelected(songId);
        }

        @Override
        public void onSongMenu(long songId) {
            mListener.onSongMenu(songId);
        }

        private interface CustomDelegates {
            void onSongSelected(long songId);
            void onSongMenu(long songId);
        }
    }
}
