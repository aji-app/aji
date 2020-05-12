package ch.zhaw.engineering.aji.ui.song.list;

import android.util.Log;

import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class AllSongsListFragment extends SongListFragment {

    private static final String TAG = "AllSongsList";

    public static SongListFragment newInstance() {
        return new AllSongsListFragment();
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        if (getActivity() != null) {
            appViewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
                Log.i(TAG, "Updating songs for song fragment");
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (appViewModel.isTwoPane() && songs != null && songs.size() > 0) {
                            if (appViewModel.isOpenFirstInList()) {
                                mListener.onSongSelected(songs.get(0).getSongId());
                            } else {
                                appViewModel.resetOpenFirstInList();
                            }
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
