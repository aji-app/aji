package ch.zhaw.engineering.tbdappname.ui.album;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentAlbumDetailsBinding;
import ch.zhaw.engineering.tbdappname.ui.song.list.AlbumSongListFragment;

public class AlbumDetailsFragment extends Fragment {
    private static final String ARG_ALBUM = "album";
    private String mAlbum;
    private FragmentAlbumDetailsBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlbum = getArguments().getString(ARG_ALBUM);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentAlbumDetailsBinding.inflate(inflater, container, false);
        mBinding.albumName.setText(mAlbum);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.album_songlist_container, AlbumSongListFragment.newInstance(mAlbum))
                .commit();

        return mBinding.getRoot();
    }
}
