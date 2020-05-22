package ch.zhaw.engineering.aji.ui.artist;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.SortResource;
import ch.zhaw.engineering.aji.ui.TabletAwareFragment;
import ch.zhaw.engineering.aji.ui.library.AlbumArtistListFragment;

public class ArtistFragment extends TabletAwareFragment {

    private ArtistFragmentListener mListener;
    private Song mTopSong;

    public static ArtistFragment newInstance() {
        return new ArtistFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.artist_list_container, AlbumArtistListFragment.newArtistsInstance())
                .commitNow();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAppViewModel.getArtists().observe(getViewLifecycleOwner(), artists -> {
            setPlaceholderText();
            AsyncTask.execute(() -> {
                if (artists.size() > 0) {
                    mTopSong = mAppViewModel.getFirstSongOfArtist(artists.get(0));
                } else {
                    mTopSong = null;
                }
                triggerTabletLogic();
            });
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ArtistFragmentListener) {
            mListener = (ArtistFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongFragmentListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        configureFab();
        setPlaceholderText();
    }

    private void setPlaceholderText() {
        if (mAppViewModel == null) {
            return;
        }
        String searchText = mAppViewModel.getSearchString(SortResource.ALBUMS);
        if (mAppViewModel.showHiddenSongs()) {
            mAppViewModel.setPlaceholderText(R.string.no_hidden);
        } else if (searchText != null && !searchText.equals("")) {
            mAppViewModel.setPlaceholderText(R.string.search_no_result);
        } else {
            mAppViewModel.setPlaceholderText(R.string.no_songs_prompt);
        }
    }

    private void configureFab() {
        if (mListener != null) {
            mListener.disableFab();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface ArtistFragmentListener extends FabCallbackListener {
        void showEmptyDetails();

        void onSongSelected(long songId, int position);
    }
}
