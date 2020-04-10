package ch.zhaw.engineering.tbdappname.ui.library;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentFavoriteBinding;
import ch.zhaw.engineering.tbdappname.ui.song.list.FavoritesSongListFragment;
import ch.zhaw.engineering.tbdappname.ui.song.list.SongListFragment;

public class FavoriteFragment extends Fragment {

    public static FavoriteFragment newInstance() {
        FavoriteFragment fragment = new FavoriteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentFavoriteBinding binding = FragmentFavoriteBinding.inflate(inflater, container, false);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.favorite_container, FavoritesSongListFragment.newInstance())
                .commit();

        return binding.getRoot();
    }
}
