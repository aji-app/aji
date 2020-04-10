package ch.zhaw.engineering.tbdappname.ui.song.list;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;

import ch.zhaw.engineering.tbdappname.ui.TbdListFragment;

/**
 * A fragment representing a list of Songs.
 * <p/>
 * Activities containing this fragment MUST implement the {@link SongListFragmentListener}
 * interface.
 */
public abstract class SongListFragment extends TbdListFragment implements SongRecyclerViewAdapter.OnTouchCallbacks {
    private static final String TAG = "SongListFragment";
    private static final String ARG_SHOW_FAVORITES = "show-favorites";

    protected SongListFragmentListener mListener;
    protected SongRecyclerViewAdapter mAdapter;
    private boolean mEditMode;


    public void setEditMode(boolean editMode) {
        mEditMode = editMode;
        mAdapter.setEditMode(mEditMode);
        if (!mEditMode) {
            notifyListenerPlaylistUpdated();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    protected abstract void initializeRecyclerView(AppViewModel appViewModel);

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            final AppViewModel appViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            initializeRecyclerView(appViewModel);
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
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        notifyListenerPlaylistUpdated();
    }

    private void notifyListenerPlaylistUpdated() {
        if (mListener != null && mAdapter != null) {
            Pair<Integer, List<Long>> data = mAdapter.getModifiedPlaylist();
            if (data.first != null && data.second != null) {
                Log.i(TAG, "save playlist with songs: " + data.second.size());
                mListener.onPlaylistModified(data.first, data.second);
            }
        }
    }

    public interface SongListFragmentListener {
        void onSongSelected(long songId, SongSelectionOrigin origin);

        void onSongPlay(long songId);

        void onSongQueue(long songId);

        void onSongMenu(long songId, SongListFragment.SongSelectionOrigin origin);

        void onSongAddToPlaylist(long songId, int playlistId);

        void onSongDelete(long songId);

        void onCreatePlaylist();

        void onToggleFavorite(long songId);

        void onPlaylistModified(int playlistId, List<Long> songIds);
    }

    public enum SongSelectionOrigin {
        ALBUM, SONG, ARTIST, PLAYLIST, EXPANDED_CONTROLS
    }
}
