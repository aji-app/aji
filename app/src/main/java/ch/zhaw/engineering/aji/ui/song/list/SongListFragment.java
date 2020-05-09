package ch.zhaw.engineering.aji.ui.song.list;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

import ch.zhaw.engineering.aji.ui.ListFragment;
import lombok.Getter;

/**
 * A fragment representing a list of Songs.
 * <p/>
 * Activities containing this fragment MUST implement the {@link SongListFragmentListener}
 * interface.
 */
public abstract class SongListFragment extends ListFragment implements SongRecyclerViewAdapter.OnTouchCallbacks {
    private static final String TAG = "SongListFragment";
    private final boolean mHighlightCurrentSong;

    SongListFragmentListener mListener;

    @Getter
    private SongRecyclerViewAdapter mAdapter;
    private Long mPlayingSongId;

    public SongListFragment() {
        this(true);
    }

    public SongListFragment(boolean highlightCurrentSong) {
        super();
        mHighlightCurrentSong = highlightCurrentSong;
    }

    public void setAdapter(SongRecyclerViewAdapter adapter) {
        mAdapter = adapter;
        if (mPlayingSongId != null) {
            mAdapter.setHighlighted(mPlayingSongId);
            mPlayingSongId = null;
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
            if (mHighlightCurrentSong) {
                mListener.getCurrentSong().observe(getViewLifecycleOwner(), song -> {
                    if (song == null) {
                        mPlayingSongId = null;
                        if (getAdapter() != null) {
                            getAdapter().setHighlighted(null);
                        }
                    } else if (!song.isRadio()) {
                        if (getAdapter() != null) {
                            getAdapter().setHighlighted(song.getId());
                        } else {
                            mPlayingSongId = song.getId();
                        }

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
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
    }


    @Override
    public void onItemDismiss(int position) {
    }

    public interface SongListFragmentListener {
        void onSongSelected(long songId);

        void onSongPlay(long songId);

        void onSongQueue(long songId);

        void onSongMenu(long songId, FragmentInteractionActivity.ContextMenuItem... additionalItems);

        void onSongAddToPlaylist(long songId, int playlistId);

        void onSongDelete(long songId);

        void onCreatePlaylist();

        void onToggleFavorite(long songId);

        void onPlaylistModified(int playlistId, List<Long> songIds);

        LiveData<AudioService.SongInformation> getCurrentSong();
    }
}
