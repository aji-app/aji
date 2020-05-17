package ch.zhaw.engineering.aji.ui.artist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentArtistDetailsBinding;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.album.AlbumDetailsFragment;
import ch.zhaw.engineering.aji.ui.song.list.ArtistSongListFragment;

public class ArtistDetailsFragment extends Fragment {
    private static final String ARG_ARTIST = "artist";
    private String mArtist;
    private FragmentArtistDetailsBinding mBinding;
    private AlbumDetailsFragment.AlbumDetailsListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArtist = getArguments().getString(ARG_ARTIST);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof AlbumDetailsFragment.AlbumDetailsListener) {
            mListener = (AlbumDetailsFragment.AlbumDetailsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AlbumDetailsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener.disableFab();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentArtistDetailsBinding.inflate(inflater, container, false);

        mBinding.artistName.setText(mArtist);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.artist_songlist_container, ArtistSongListFragment.newInstance(mArtist))
                .commit();
        return mBinding.getRoot();
    }

    public interface ArtistDetailsListener extends FabCallbackListener {
    }
}
