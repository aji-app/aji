package ch.zhaw.engineering.aji.ui.song.list;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import ch.zhaw.engineering.aji.FragmentInteractionActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.AppDatabase;
import ch.zhaw.engineering.aji.services.database.dao.SongDao;
import ch.zhaw.engineering.aji.ui.contextmenu.ContextMenuFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import lombok.experimental.Delegate;

public class AllSongsListFragment extends SongListFragment {

    private static final String TAG = "AllSongsList";

    public static AllSongsListFragment newInstance() {
        return new AllSongsListFragment();
    }

    @Override
    protected void initializeRecyclerView(AppViewModel appViewModel) {
        if (getActivity() != null) {
            SongDao dao = AppDatabase.getInstance(getActivity()).songDao();
            appViewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
                Log.i(TAG, "Updating songs for song fragment");
                mSongs = songs;
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        mBinding.songPrompt.setVisibility(!songs.isEmpty() || appViewModel.isTwoPane() ? View.GONE : View.VISIBLE);
                        if (mShowFirst && mSongs.size() > 0) {
                            mListener.onSongSelected(mSongs.get(0).getSongId(), 0);
                        }
                        if (getAdapter() != null) {
                            getAdapter().setSongs(songs);
                            getAdapter().setShowFavoriteButton(!appViewModel.showHiddenSongs());
                            if (mRecyclerView.getAdapter() == null) {
                                mRecyclerView.setAdapter(getAdapter());
                            }
                        } else {
                            setAdapter(new SongRecyclerViewAdapter(songs, new CustomListener(mListener, appViewModel, dao)));
                            mRecyclerView.setAdapter(getAdapter());
                        }
                    });
                }
            });
        }
    }

    private static class CustomListener implements SongListFragmentListener {

        @Delegate(excludes = CustomDelegates.class)
        private final SongListFragmentListener mListener;
        private AppViewModel mAppViewModel;
        private SongDao mSongDao;

        private CustomListener(SongListFragmentListener listener, AppViewModel appViewModel, SongDao songDao) {
            mListener = listener;
            mAppViewModel = appViewModel;
            mSongDao = songDao;
        }

        @Override
        public void onSongMenu(long songId, Integer position, FragmentInteractionActivity.ContextMenuItem... additionalItems) {
            if (mAppViewModel.showHiddenSongs()) {
                mListener.onSongMenu(songId, position, FragmentInteractionActivity.ContextMenuItem.builder()
                        .position(0)
                        .itemConfig(ContextMenuFragment.ItemConfig.builder()
                                .imageId(R.drawable.ic_show)
                                .textId(R.string.show_in_library)
                                .exclusive(true)
                                .callback($ -> {
                                    AsyncTask.execute(() -> {
                                        mSongDao.unhideSong(songId);
                                    });
                                })
                                .build())
                        .build());
            } else {
                mListener.onSongMenu(songId, position);
            }

        }

        @Override
        public void onSongSelected(long songId, int position) {
            if (!mAppViewModel.showHiddenSongs()) {
                mListener.onSongSelected(songId, position);
            } else {
                onSongMenu(songId, position);
            }
        }

        private interface CustomDelegates {
            void onSongMenu(long songId, Integer position, FragmentInteractionActivity.ContextMenuItem... additionalItems);

            void onSongSelected(long songId, int position);
        }
    }
}
