package ch.zhaw.engineering.aji.ui.song.list;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import ch.zhaw.engineering.aji.services.audio.AudioService;
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
                        if (mAdapter != null) {
                            mAdapter.setSongs(songs);
                            if (mRecyclerView.getAdapter() == null) {
                                mRecyclerView.setAdapter(mAdapter);
                            }
                        } else {
                            mAdapter = new SongRecyclerViewAdapter(songs, mListener);
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    });
                }
            });
        }
    }
}
