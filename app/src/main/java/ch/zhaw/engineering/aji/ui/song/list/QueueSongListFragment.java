package ch.zhaw.engineering.aji.ui.song.list;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.contextmenu.ContextMenuFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import lombok.experimental.Delegate;

public class QueueSongListFragment extends SongListFragment {
    private static final String TAG = "QueueSongListFragment";
    private ItemTouchHelper mItemTouchHelper;
    private QueueListFragmentListener mQueueListener;

    public static SongListFragment newInstance() {
        return new QueueSongListFragment();
    }

    public QueueSongListFragment() {
        super(true, true);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        super.onStartDrag(viewHolder);
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemDismiss(int position) {
        getAdapter().dismissWithoutSnackbar(position, song -> {
            mQueueListener.removeSongFromQueue(song.getSongId(), position);
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
                mBinding.songPrompt.setVisibility(View.GONE);
                if (getAdapter() != null) {
                    getAdapter().setSongs(songs);
                    if (mRecyclerView.getAdapter() == null) {
                        mRecyclerView.setAdapter(getAdapter());
                    }
                } else {
                    setAdapter(new SongRecyclerViewAdapter(songs, new CustomListener(mListener, mQueueListener, this), this));
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
        private QueueSongListFragment mFragment;

        private CustomListener(SongListFragmentListener listener, QueueListFragmentListener queueListener, QueueSongListFragment fragment) {
            mListener = listener;
            mQueueListener = queueListener;
            mFragment = fragment;
        }

        @Override
        public void onSongSelected(long songId, int position) {
            mQueueListener.onSkipToSong(position);
        }

        @Override
        public void onSongMenu(long songId, Integer position, FragmentInteractionActivity.ContextMenuItem... additionalItems) {
            mListener.onSongMenu(songId, position, FragmentInteractionActivity.ContextMenuItem.builder()
                    .position(0)
                    .itemConfig(ContextMenuFragment.ItemConfig.builder()
                            .imageId(R.drawable.ic_remove_from_queue)
                            .textId(R.string.remove_from_queue)
                            .callback($ -> {
                                if (position != null) {
                                    mFragment.getAdapter().dismissWithoutSnackbar(position, song -> {
                                        mQueueListener.removeSongFromQueue(song.getSongId(), position);
                                    });
                                }
                            }).build()
                    )
                    .build());
        }

        private interface CustomDelegates {
            void onSongSelected(long songId, int position);

            void onSongMenu(long songId, Integer position, FragmentInteractionActivity.ContextMenuItem... additionalItems);
        }
    }


    public interface QueueListFragmentListener {
        void removeSongFromQueue(long songId, Integer position);

        void onSkipToSong(int position);

        LiveData<List<Song>> getCurrentQueue();
    }
}
