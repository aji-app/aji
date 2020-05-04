package ch.zhaw.engineering.aji.ui.song.list;

import android.os.Bundle;
import android.util.Log;

import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
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
