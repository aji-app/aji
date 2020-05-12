package ch.zhaw.engineering.aji.ui.song.list;

import android.util.Log;

import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class AllSongsListFragment extends SongListFragment {

    private static final String TAG = "AllSongsList";

    public static AllSongsListFragment newInstance() {
        return new AllSongsListFragment();
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        if (getActivity() != null) {
            appViewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
                Log.i(TAG, "Updating songs for song fragment");
                mSongs = songs;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (mShowFirst) {
                            mListener.onSongSelected(mSongs.get(0).getSongId());
                        }
                        if (getAdapter() != null) {
                            getAdapter().setSongs(songs);
                            if (mRecyclerView.getAdapter() == null) {
                                mRecyclerView.setAdapter(getAdapter());
                            }
                        } else {
                            setAdapter(new SongRecyclerViewAdapter(songs, mListener));
                            mRecyclerView.setAdapter(getAdapter());
                        }
                    });
                }
            });
        }
    }
}
