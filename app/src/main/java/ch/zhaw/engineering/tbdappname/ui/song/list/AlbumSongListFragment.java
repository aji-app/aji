package ch.zhaw.engineering.tbdappname.ui.song.list;

import android.os.Bundle;
import android.util.Log;

import ch.zhaw.engineering.tbdappname.ui.viewmodel.AppViewModel;
import lombok.experimental.Delegate;

public class AlbumSongListFragment extends SongListFragment {
    private static final String TAG = "AlbumSongsFragment";
    private static final String ARG_ALBUM = "album";
    private String mAlbum;

    public static SongListFragment newInstance(String album) {
        SongListFragment fragment = new AlbumSongListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALBUM, album);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(ARG_ALBUM)) {
            mAlbum = getArguments().getString(ARG_ALBUM);
        }
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        appViewModel.getSongsForAlbum(mAlbum).observe(getViewLifecycleOwner(), songs -> {
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
        public void onSongSelected(long songId, SongSelectionOrigin origin) {
            mListener.onSongSelected(songId, SongSelectionOrigin.ALBUM);
        }

        @Override
        public void onSongMenu(long songId, SongSelectionOrigin origin) {
            mListener.onSongMenu(songId, SongSelectionOrigin.ALBUM);
        }

        private interface CustomDelegates {
            void onSongSelected(long songId, SongSelectionOrigin origin);
            void onSongMenu(long songId, SongSelectionOrigin origin);
        }
    }
}
