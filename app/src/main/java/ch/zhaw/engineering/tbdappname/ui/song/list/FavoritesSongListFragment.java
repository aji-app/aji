package ch.zhaw.engineering.tbdappname.ui.song.list;

import android.util.Log;

import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;

public class FavoritesSongListFragment extends SongListFragment {
    private static final String TAG = "FavoritesListFragment";

    public static SongListFragment newInstance() {
        return new FavoritesSongListFragment();
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        appViewModel.getFavorites().observe(getViewLifecycleOwner(), songs -> {
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
