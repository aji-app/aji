package ch.zhaw.engineering.tbdappname.ui.artist;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentArtistDetailsBinding;
import ch.zhaw.engineering.tbdappname.ui.song.list.AlbumSongListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.list.ArtistSongListFragment;

public class ArtistDetailsFragment extends Fragment {
    private static final String ARG_ARTIST = "artist";

    private String mArtist;
    private FragmentArtistDetailsBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mArtist = getArguments().getString(ARG_ARTIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentArtistDetailsBinding.inflate(inflater, container, false);

        mBinding.artistName.setText(mArtist);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.artist_songlist_container, ArtistSongListFragment.newInstance(mArtist))
                .commit();
        return mBinding.getRoot();
    }
}
