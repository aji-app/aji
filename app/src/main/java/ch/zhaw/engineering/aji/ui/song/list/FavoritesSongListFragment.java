package ch.zhaw.engineering.aji.ui.song.list;

import android.util.Log;

import java.util.List;

import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class FavoritesSongListFragment extends SongListFragment {
    private static final String TAG = "FavoritesListFragment";

    public static FavoritesSongListFragment newInstance() {
        return new FavoritesSongListFragment();
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        appViewModel.getFavorites().observe(getViewLifecycleOwner(), songs -> {
            Log.i(TAG, "Updating songs for song fragment");
            mSongs = songs;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    setAdapter(new SongRecyclerViewAdapter(songs, mListener));
                    mRecyclerView.setAdapter(getAdapter());
                });
            }
        });
    }
}
