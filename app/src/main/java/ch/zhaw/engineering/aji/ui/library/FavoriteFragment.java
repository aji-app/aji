package ch.zhaw.engineering.aji.ui.library;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentFavoriteBinding;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.TabletAwareFragment;
import ch.zhaw.engineering.aji.ui.song.list.FavoritesSongListFragment;

public class FavoriteFragment extends TabletAwareFragment {

    private FavoritesFragmentListener mListener;
    private Song mTopSong;

    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel.getFavorites().observe(getViewLifecycleOwner(), songs -> {
                setPlaceholderText();
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
    protected void showDetails() {
        if (mTopSong != null) {
            mListener.onSongSelected(mTopSong.getSongId(), 0);
        } else {
            mListener.showEmptyDetails();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        configureFab();
        setPlaceholderText();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentFavoriteBinding binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.favorite_container, FavoritesSongListFragment.newInstance())
                .commit();

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FavoritesFragmentListener) {
            mListener = (FavoritesFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FavoritesFragmentListener");
        }
    }

    private void setPlaceholderText() {
        if (mAppViewModel == null) {
            return;
        }
        mAppViewModel.setPlaceholderText(R.string.no_favorites_prompt);
    }

    private void configureFab() {
        if (mListener != null) {
            mListener.configureFab(view -> {
                mListener.onPlayFavorites();
            }, R.drawable.ic_play);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FavoritesFragmentListener extends FabCallbackListener {
        void onPlayFavorites();

        void showEmptyDetails();

        void onSongSelected(long songId, int position);
    }
}
