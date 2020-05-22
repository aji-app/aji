package ch.zhaw.engineering.aji.ui.song;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentSongBinding;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.SortResource;
import ch.zhaw.engineering.aji.ui.TabletAwareFragment;
import ch.zhaw.engineering.aji.ui.song.list.AllSongsListFragment;
import ch.zhaw.engineering.aji.util.PreferenceHelper;

public class SongFragment extends TabletAwareFragment {
    private SongFragmentListener mListener;
    private boolean mShowFirst = true;
    private Song mTopSong;
    private FragmentSongBinding mBinding;

    @SuppressWarnings("unused")
    public static SongFragment newInstance() {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void showDetails() {
        if (!mShowFirst) {
            return;
        }
        if (mTopSong != null) {
            mListener.onSongSelected(mTopSong.getSongId(), 0);
        } else {

            mListener.showEmptyDetails();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AllSongsListFragment listFragment = AllSongsListFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.bottom_container, listFragment)
                .commitNow();

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {

            mAppViewModel.getSongs().observe(getViewLifecycleOwner(), songs -> {
                String searchText = mAppViewModel.getSearchString(SortResource.SONGS);
                if (mAppViewModel.showHiddenSongs()) {
                    mAppViewModel.setPlaceholderText(R.string.no_hidden);
                } else if (searchText != null && !searchText.equals("")) {
                    mAppViewModel.setPlaceholderText(R.string.search_no_result);
                } else {
                    mAppViewModel.setPlaceholderText(R.string.no_songs_prompt);
                }
                if (songs.isEmpty()) {
                    mTopSong = null;
                } else {
                    mTopSong = songs.get(0);
                }
                triggerTabletLogic();
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mShowFirst = true;
        configureFab();
    }

    private void configureFab() {
        if (getActivity() != null) {
            PreferenceHelper helper = new PreferenceHelper(getActivity());
            configureFab(!helper.isMediaStoreEnabled());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSongBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    private void configureFab(boolean enabled) {
        if (mListener != null) {
            if (enabled) {
                mListener.configureFab(v -> {
                    mShowFirst = false;
                    mListener.onAddSongsButtonClick();
                }, R.drawable.ic_add);
            } else {
                mListener.disableFab();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SongFragmentListener) {
            mListener = (SongFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface SongFragmentListener extends FabCallbackListener {
        void onAddSongsButtonClick();

        void onSongSelected(long songId, int position);

        void showEmptyDetails();
    }
}
