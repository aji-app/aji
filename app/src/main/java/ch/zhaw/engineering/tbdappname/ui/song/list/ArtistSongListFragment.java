package ch.zhaw.engineering.tbdappname.ui.song.list;

import android.os.Bundle;
import android.util.Log;

import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;
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
                    if (mAdapter != null) {
                        mAdapter.setSongs(songs);
                        if (mRecyclerView.getAdapter() == null) {
                            mRecyclerView.setAdapter(mAdapter);
                        }
                    } else {
                        mAdapter = new SongRecyclerViewAdapter(songs, new CustomListener(mListener));
                        mRecyclerView.setAdapter(mAdapter);
                    }
                });
            }
        });
    }

    private static class CustomListener implements SongListFragmentListener {

        @Delegate(excludes=CustomDelegates.class)
        private final SongListFragmentListener mListener;

        private CustomListener(SongListFragmentListener listener) {
            mListener = listener;
        }

        @Override
        public void onSongSelected(long songId) {
            mListener.onSongSelected(songId);
        }

        @Override
        public void onSongMenu(long songId) {
            mListener.onSongMenu(songId);
        }

        private interface CustomDelegates {
            void onSongSelected(long songId);
            void onSongMenu(long songId);
        }
    }
}
