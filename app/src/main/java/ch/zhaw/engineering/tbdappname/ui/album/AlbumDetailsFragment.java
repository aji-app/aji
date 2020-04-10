package ch.zhaw.engineering.tbdappname.ui.album;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentAlbumDetailsBinding;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentAlbumDetailsBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }
}
