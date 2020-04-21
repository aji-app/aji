package ch.zhaw.engineering.tbdappname.ui.song.list;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ch.zhaw.engineering.tbdappname.AudioInterfaceActivity;
import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;

public class QueueSongListFragment extends SongListFragment {
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
        mAdapter.dismissWithSnackbar(position, R.string.song_removed_from_queue, song -> {
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
            AudioInterfaceActivity activity = (AudioInterfaceActivity) getActivity();
            activity.getCurrentQueue().observe(getViewLifecycleOwner(), songs -> {
                if (mAdapter != null) {
                    mAdapter.setSongs(songs);
                    if (mRecyclerView.getAdapter() == null) {
                        mRecyclerView.setAdapter(mAdapter);
                    }
                } else {
                    mAdapter = new SongRecyclerViewAdapter(songs, mListener, this);
                    ItemTouchHelper.Callback callback =
                            new SongRecyclerViewAdapter.SimpleItemTouchHelperCallback(mAdapter, getActivity(), 0, ItemTouchHelper.START);
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                    mRecyclerView.setAdapter(mAdapter);
                }
            });
        }
    }

    public interface QueueListFragmentListener {
        void removeSongFromQueue(long songId);
    }
}
