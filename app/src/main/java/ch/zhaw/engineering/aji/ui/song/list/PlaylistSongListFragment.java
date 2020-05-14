package ch.zhaw.engineering.aji.ui.song.list;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import lombok.experimental.Delegate;

public class PlaylistSongListFragment extends SongListFragment {
    private static final String ARG_PLAYLIST_ID = "playlist-id";
    private static final String TAG = "PlaylistSongsFragment";
    private Integer mPlaylistId;
    private ItemTouchHelper mItemTouchHelper;
    private boolean mPlaylistDeleted;

    public static PlaylistSongListFragment newInstance(@Nullable Integer playlistId) {
        PlaylistSongListFragment fragment = new PlaylistSongListFragment();
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
    public void onItemDismiss(int position) {
        getAdapter().dismissWithSnackbar(position, R.string.song_removed_playlist, null);
    }

    public void setEditMode(boolean editMode) {
        getAdapter().setEditMode(editMode);
        if (!editMode && !mPlaylistDeleted) {
            notifyListenerPlaylistUpdated();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!mPlaylistDeleted) {
            notifyListenerPlaylistUpdated();
        }
    }

    private void notifyListenerPlaylistUpdated() {
        if (mListener != null && getAdapter() != null) {
            Pair<Integer, List<Long>> data = getAdapter().getModifiedPlaylist();
            if (data.first != null && data.second != null) {
                Log.i(TAG, "save playlist with songs: " + data.second.size());
                mListener.onPlaylistModified(data.first, data.second);
            }
        }
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        appViewModel.getSongsForPlaylist(mPlaylistId).observe(getViewLifecycleOwner(), songs -> {
            if (getActivity() != null) {
                Log.i(TAG, "Got Songs for Playlist Song View " + songs.size());
                getActivity().runOnUiThread(() -> {
                    if (appViewModel.isTwoPane() && songs != null && songs.size() > 0) {
                        mListener.onSongSelected(songs.get(0).getSongId(), 0);
                    }

                    setAdapter(new SongRecyclerViewAdapter(songs, new CustomListener(mListener), mPlaylistId, this));
                    ItemTouchHelper.Callback callback =
                            new SongRecyclerViewAdapter.SimpleItemTouchHelperCallback(getAdapter(), getActivity());
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                    mRecyclerView.setAdapter(getAdapter());
                });
            }
        });
    }

    public void setPlaylistDeleted(boolean playlistDeleted) {
        mPlaylistDeleted = true;
    }

    private static class CustomListener implements SongListFragmentListener {

        @Delegate(excludes = CustomDelegates.class)
        private final SongListFragmentListener mListener;

        private CustomListener(SongListFragmentListener listener) {
            mListener = listener;
        }

        @Override
        public void onSongSelected(long songId, int position) {
            mListener.onSongSelected(songId, position);
        }

        @Override
        public void onSongMenu(long songId, Integer position, FragmentInteractionActivity.ContextMenuItem... additionalItems) {
            // TODO: Ist das nötig?
            mListener.onSongMenu(songId, position);
        }

        private interface CustomDelegates {
            void onSongSelected(long songId, int position);

            void onSongMenu(long songId, Integer position, FragmentInteractionActivity.ContextMenuItem... additionalItems);
        }
    }
}
