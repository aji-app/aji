package ch.zhaw.engineering.tbdappname.ui.song;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.ui.TbdListFragment;

/**
 * A fragment representing a list of Songs.
 * <p/>
 * Activities containing this fragment MUST implement the {@link SongListFragmentListener}
 * interface.
 */
public class SongListFragment extends TbdListFragment implements SongRecyclerViewAdapter.OnTouchCallbacks {
    private static final String TAG = "SongListFragment";
    private static final String ARG_PLAYLIST_ID = "playlist-id";
    private SongListFragmentListener mListener;
    private Integer mPlaylistId;
    private ItemTouchHelper mItemTouchHelper;
    private SongRecyclerViewAdapter mAdapter;

    @SuppressWarnings("unused")
    public static SongListFragment newInstance() {
        return newInstance(null);
    }

    /**
     * Creates a new instance of the {@link SongListFragment}
     *
     * @param playlistId The songs of this playlist will be displayed
     * @return An configured instance of {@link SongListFragment}
     */
    @SuppressWarnings("unused")
    public static SongListFragment newInstance(@Nullable Integer playlistId) {
        SongListFragment fragment = new SongListFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song_list, container, false);

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
            final SongViewModel songViewModel = new ViewModelProvider(getActivity()).get(SongViewModel.class);
            if (mPlaylistId == null) {
                songViewModel.getSongsAndPlaylists().observe(getViewLifecycleOwner(), songsAndPlaylists -> {
                    Log.i(TAG, "Updating songs for song fragment");
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            mAdapter = new SongRecyclerViewAdapter(songsAndPlaylists.getSongs(), mListener, getActivity(), songsAndPlaylists.getPlaylists());
                            mRecyclerView.setAdapter(mAdapter);
                        });
                    }
                });
            } else {
                songViewModel.getSongsForPlaylist(mPlaylistId).observe(getViewLifecycleOwner(), songs -> {
                    if (getActivity() != null) {
                        Log.i(TAG, "Got Songs for Playlist Song View " + songs.size());
                        getActivity().runOnUiThread(() -> {
                            mAdapter = new SongRecyclerViewAdapter(songs, mListener, getActivity(), mPlaylistId, this);
                            ItemTouchHelper.Callback callback =
                                    new SongRecyclerViewAdapter.SimpleItemTouchHelperCallback(mAdapter, getActivity());
                            mItemTouchHelper = new ItemTouchHelper(callback);
                            mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                            mRecyclerView.setAdapter(mAdapter);
                        });
                    }
                });
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SongListFragmentListener) {
            mListener = (SongListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongListFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mListener != null) {
            Pair<Integer, List<Long>> data = mAdapter.getModifiedPlaylist();
            if (data.first != null && data.second != null) {
                Log.i(TAG, "save playlist with songs: " + data.second.size());
                mListener.onPlaylistModified(data.first, data.second);
            }
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public interface SongListFragmentListener {
        void onSongSelected(long songId);

        void onSongPlay(long songId);

        void onSongQueue(long songId);

        void onSongEdit(long songId);

        void onSongAddToPlaylist(long songId, int playlistId);

        void onSongDelete(long songId);

        void onCreatePlaylist();

        void onToggleFavorite(long songId);

        void onPlaylistModified(int playlistId, List<Long> songIds);
    }
}
