package ch.zhaw.engineering.aji.ui.song.list;

import android.os.Bundle;
import android.util.Log;

import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import lombok.experimental.Delegate;

public class ArtistSongListFragment extends SongListFragment {
    private static final String TAG = "ArtistSongsFragment";
    private static final String ARG_ARTIST = "artist";
    private static final String ARG_TWO_PANE = "two-pane";
    private boolean mTwoPane = false;
    private String mArtist;

    public static SongListFragment newInstance(String album, boolean twoPane) {
        SongListFragment fragment = new ArtistSongListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST, album);
        args.putBoolean(ARG_TWO_PANE, twoPane);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ARTIST)) {
            mArtist = getArguments().getString(ARG_ARTIST);
            mTwoPane = getArguments().getBoolean(ARG_TWO_PANE);
        }
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        appViewModel.getSongsForArtist(mArtist).observe(getViewLifecycleOwner(), songs -> {
            Log.i(TAG, "Updating songs for song fragment");
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (mTwoPane && songs != null && songs.size() > 0) {
                        mListener.onSongSelected(songs.get(0).getSongId());
                    }
                    if (getAdapter() != null) {
                        getAdapter().setSongs(songs);
                        if (mRecyclerView.getAdapter() == null) {
                            mRecyclerView.setAdapter(getAdapter());
                        }
                    } else {
                        setAdapter(new SongRecyclerViewAdapter(songs, new CustomListener(mListener)));
                        mRecyclerView.setAdapter(getAdapter());
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
        public void onSongMenu(long songId, FragmentInteractionActivity.ContextMenuItem... additionalItems) {
            mListener.onSongMenu(songId);
        }

        private interface CustomDelegates {
            void onSongSelected(long songId);
            void onSongMenu(long songId, FragmentInteractionActivity.ContextMenuItem... additionalItems);
        }
    }
}
