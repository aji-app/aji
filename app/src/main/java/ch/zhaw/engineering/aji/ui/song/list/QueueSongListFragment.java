package ch.zhaw.engineering.aji.ui.song.list;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import lombok.experimental.Delegate;

public class QueueSongListFragment extends SongListFragment {
    private static final String TAG = "QueueSongListFragment";
    private ItemTouchHelper mItemTouchHelper;
    private QueueListFragmentListener mQueueListener;

    public static SongListFragment newInstance() {
        return new QueueSongListFragment();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        super.onStartDrag(viewHolder);
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemDismiss(int position) {
        getAdapter().dismissWithSnackbar(position, R.string.song_removed_from_queue, song -> {
            mQueueListener.removeSongFromQueue(song.getSongId());
        });
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof QueueListFragmentListener) {
            mQueueListener = (QueueListFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement QueueListFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mQueueListener = null;
    }


    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        if (getActivity() != null) {
            mQueueListener.getCurrentQueue().observe(getViewLifecycleOwner(), songs -> {
                if (getAdapter() != null) {
                    getAdapter().setSongs(songs);
                    if (mRecyclerView.getAdapter() == null) {
                        mRecyclerView.setAdapter(getAdapter());
                    }
                } else {
                    setAdapter(new SongRecyclerViewAdapter(songs, new CustomListener(mListener, mQueueListener), this));
                    ItemTouchHelper.Callback callback =
                            new SongRecyclerViewAdapter.SimpleItemTouchHelperCallback(getAdapter(), getActivity(), 0, ItemTouchHelper.START);
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                    mRecyclerView.setAdapter(getAdapter());
                }
            });
        }
    }


    private static class CustomListener implements SongListFragmentListener {

        @Delegate(excludes = CustomListener.CustomDelegates.class)
        private final SongListFragmentListener mListener;
        private final QueueListFragmentListener mQueueListener;

        private CustomListener(SongListFragmentListener listener, QueueListFragmentListener queueListener) {
            mListener = listener;
            mQueueListener = queueListener;
        }

        @Override
        public void onSongSelected(long songId) {
            mQueueListener.onSkipToSong(songId);
        }

        private interface CustomDelegates {
            void onSongSelected(long songId);
        }
    }

    public interface QueueListFragmentListener {
        void removeSongFromQueue(long songId);

        void onSkipToSong(long songId);

        LiveData<List<Song>> getCurrentQueue();
    }
}
