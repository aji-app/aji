package ch.zhaw.engineering.aji.ui.song.list;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.database.entity.Song;
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
    protected List<Song> mSongs;
    protected boolean mShowFirst = false;
    private final boolean mHighlightCurrentSongOnlyWithCorrectPosition;

    SongListFragmentListener mListener;

    @Getter
    private SongRecyclerViewAdapter mAdapter;
    private Long mPlayingSongId;
    private Integer mPlayingPosition;

    public SongListFragment() {
        this(true, false);
    }

    public SongListFragment(boolean highlightCurrentSong, boolean respectPosition) {
        super();
        mHighlightCurrentSong = highlightCurrentSong;
        mHighlightCurrentSongOnlyWithCorrectPosition = respectPosition;
    }

    public void showFirst() {
        if (mSongs != null && mSongs.size() > 0) {
            mListener.onSongSelected(mSongs.get(0).getSongId(), 0);
        } else {
            mShowFirst = true;
        }
    }

    public void setAdapter(SongRecyclerViewAdapter adapter) {
        mAdapter = adapter;
        if (mPlayingSongId != null) {
            mAdapter.setHighlighted(mPlayingSongId, mPlayingPosition);
            mPlayingSongId = null;
            mPlayingPosition = null;
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
            AppViewModel appViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            initializeRecyclerView(appViewModel);

            if (mHighlightCurrentSong) {
                mListener.getCurrentSong().observe(getViewLifecycleOwner(), song -> {
                    if (song == null) {
                        mPlayingSongId = null;
                        if (getAdapter() != null) {
                            getAdapter().setHighlighted(null, null);
                        }
                    } else if (!song.isRadio()) {
                        if (getAdapter() != null) {
                            if (mHighlightCurrentSongOnlyWithCorrectPosition) {
                                int position = song.getPositionInQueue();
                                getAdapter().setHighlighted(song.getId(), position);
                                mRecyclerView.scrollToPosition(position);
                            } else {
                                getAdapter().setHighlighted(song.getId(), null);
                            }
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
        void onSongSelected(long songId, int position);

        void onSongPlay(long songId);

        void onSongQueue(long songId);

        void onSongMenu(long songId, Integer position, FragmentInteractionActivity.ContextMenuItem... additionalItems);

        void onSongAddToPlaylist(long songId, int playlistId);

        void onSongDelete(long songId);

        void onCreatePlaylist(Long songToAdd);

        void onToggleFavorite(long songId);

        void onPlaylistModified(int playlistId, List<Long> songIds);

        LiveData<AudioService.SongInformation> getCurrentSong();
    }
}
