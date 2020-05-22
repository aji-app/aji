package ch.zhaw.engineering.aji.ui.album;

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

public class AlbumFragment extends TabletAwareFragment {
    private AlbumFragmentListener mListener;
    private Song mTopSong;

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.album_list_container, AlbumArtistListFragment.newAlbumInstance())
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
        mAppViewModel.getAlbums().observe(getViewLifecycleOwner(), albums -> {
            AsyncTask.execute(() -> {
                String searchText = mAppViewModel.getSearchString(SortResource.ALBUMS);
                if (mAppViewModel.showHiddenSongs()) {
                    mAppViewModel.setPlaceholderText(R.string.no_hidden);
                } else if (searchText!= null && !searchText.equals("")) {
                    mAppViewModel.setPlaceholderText(R.string.search_no_result);
                } else {
                    mAppViewModel.setPlaceholderText(R.string.no_songs_prompt);
                }
                if (albums.size() > 0) {
                    mTopSong = mAppViewModel.getFirstSongOfAlbum(albums.get(0));
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
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AlbumFragmentListener) {
            mListener = (AlbumFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AlbumFragmentListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        configureFab();
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

    public interface AlbumFragmentListener extends FabCallbackListener {
        void showEmptyDetails();

        void onSongSelected(long songId, int position);
    }
}
