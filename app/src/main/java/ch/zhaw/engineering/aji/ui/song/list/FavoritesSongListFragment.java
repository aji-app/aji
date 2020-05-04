package ch.zhaw.engineering.aji.ui.song.list;

import android.util.Log;

import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

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
                    setAdapter(new SongRecyclerViewAdapter(songs, mListener));
                    mRecyclerView.setAdapter(getAdapter());
                });
            }
        });
    }
}
