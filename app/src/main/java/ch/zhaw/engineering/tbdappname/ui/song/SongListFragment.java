package ch.zhaw.engineering.tbdappname.ui.song;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
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
    private SongViewModel mSongViewModel;
    private Integer mPlaylistId;
    private ItemTouchHelper mItemTouchHelper;
    private SongRecyclerViewAdapter mAdapter;

    @SuppressWarnings("unused")
    public static SongListFragment newInstance() {
        return newInstance(null);
    }

    @SuppressWarnings("unused")
    /**
     * @param playlistId The songs of this playlist will be displayed
     */
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
        mSongViewModel = new ViewModelProvider(getActivity()).get(SongViewModel.class);
        if (mPlaylistId == null) {
            mSongViewModel.getSongsAndPlaylists().observe(getViewLifecycleOwner(), songsAndPlaylists -> {
                Log.i(TAG, "Updating songs for song fragment");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        mAdapter = new SongRecyclerViewAdapter(songsAndPlaylists.getSongs(), mListener, getActivity(), songsAndPlaylists.getPlaylists());
                        mRecyclerView.setAdapter(mAdapter);
                    });
                }
            });
        } else {

            mSongViewModel.getSongsForPlaylist(mPlaylistId).observe(getViewLifecycleOwner(), songs -> {
                if (getActivity() != null) {
                    Log.i(TAG, "Got Songs for Playlist Song View " + songs.size());
                    getActivity().runOnUiThread(() -> {
                        mAdapter = new SongRecyclerViewAdapter(songs, mListener, getActivity(), mPlaylistId, this);
                        ItemTouchHelper.Callback callback =
                                new SongRecyclerViewAdapter.SimpleItemTouchHelperCallback(mAdapter);
                        mItemTouchHelper = new ItemTouchHelper(callback);
                        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                        mRecyclerView.setAdapter(mAdapter);
                    });
                }
            });
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SongListFragmentListener) {
            mListener = (SongListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

        void onSongRemovedFromPlaylist(long songId, int playlistId);

        void onSongDelete(long songId);

        void onCreatePlaylist();

        void onToggleFavorite(long songId);

        void onSongsReordered(List<Long> songIds, int playlistId);
    }
}
