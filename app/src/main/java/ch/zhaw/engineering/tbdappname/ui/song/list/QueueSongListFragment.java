package ch.zhaw.engineering.tbdappname.ui.song.list;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import ch.zhaw.engineering.tbdappname.AudioInterfaceActivity;
import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;

public class QueueSongListFragment extends SongListFragment {
    private ItemTouchHelper mItemTouchHelper;

    public static SongListFragment newInstance() {
        return new QueueSongListFragment();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        super.onStartDrag(viewHolder);
        mItemTouchHelper.startDrag(viewHolder);
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
                    mAdapter = new SongRecyclerViewAdapter(songs, mListener);
                    ItemTouchHelper.Callback callback =
                            new SongRecyclerViewAdapter.SimpleItemTouchHelperCallback(mAdapter, getActivity());
                    mItemTouchHelper = new ItemTouchHelper(callback);
                    mItemTouchHelper.attachToRecyclerView(mRecyclerView);
                    mRecyclerView.setAdapter(mAdapter);
                }
            });
        }
    }
}
