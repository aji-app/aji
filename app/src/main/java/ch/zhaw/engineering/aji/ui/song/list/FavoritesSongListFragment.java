package ch.zhaw.engineering.aji.ui.song.list;

import android.util.Log;
import android.view.View;

import java.util.List;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class FavoritesSongListFragment extends SongListFragment {
    private static final String TAG = "FavoritesListFragment";

    public static FavoritesSongListFragment newInstance() {
        return new FavoritesSongListFragment();
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        mBinding.songPrompt.setText(R.string.no_favorites_prompt);
        appViewModel.getFavorites().observe(getViewLifecycleOwner(), songs -> {
            Log.i(TAG, "Updating songs for song fragment");
            mSongs = songs;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    mBinding.songPrompt.setVisibility(!songs.isEmpty() || appViewModel.isTwoPane() ? View.GONE : View.VISIBLE);
                    setAdapter(new SongRecyclerViewAdapter(songs, mListener));
                    mRecyclerView.setAdapter(getAdapter());

                });
            }
        });
    }
}
