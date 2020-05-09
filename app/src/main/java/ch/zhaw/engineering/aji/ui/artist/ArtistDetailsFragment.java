package ch.zhaw.engineering.aji.ui.artist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentArtistDetailsBinding;
import ch.zhaw.engineering.aji.ui.song.list.ArtistSongListFragment;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentArtistDetailsBinding.inflate(inflater, container, false);

        mBinding.artistName.setText(mArtist);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.artist_songlist_container, ArtistSongListFragment.newInstance(mArtist))
                .commit();
        return mBinding.getRoot();
    }
}
