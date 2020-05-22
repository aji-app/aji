package ch.zhaw.engineering.aji.ui.song.list;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.ui.contextmenu.ContextMenuFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import lombok.experimental.Delegate;

public class ArtistSongListFragment extends SongListFragment {
    private static final String TAG = "ArtistSongsFragment";
    private static final String ARG_ARTIST = "artist";
    private String mArtist;

    public static SongListFragment newInstance(String album) {
        SongListFragment fragment = new ArtistSongListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST, album);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ARTIST)) {
            mArtist = getArguments().getString(ARG_ARTIST);
        }
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        appViewModel.getSongsForArtist(mArtist).observe(getViewLifecycleOwner(), songs -> {
            Log.i(TAG, "Updating songs for song fragment");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    mBinding.songPrompt.setVisibility(!songs.isEmpty() || appViewModel.isTwoPane() ? View.GONE : View.VISIBLE);
                    if (appViewModel.isTwoPane() && songs != null && songs.size() > 0) {
                        mListener.onSongSelected(songs.get(0).getSongId(), 0);
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
