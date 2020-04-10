package ch.zhaw.engineering.tbdappname.ui.song.list;

import android.util.Log;

import ch.zhaw.engineering.tbdappname.ui.AppViewModel;

public class AllSongsListFragment extends SongListFragment {

    private static final String TAG = "AllSongsList";

    public static SongListFragment newInstance() {
        SongListFragment fragment = new AllSongsListFragment();
        return fragment;
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        appViewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
            Log.i(TAG, "Updating songs for song fragment");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    mAdapter = new SongRecyclerViewAdapter(songs, mListener);
                    mRecyclerView.setAdapter(mAdapter);
                });
            }
        });
    }
}
