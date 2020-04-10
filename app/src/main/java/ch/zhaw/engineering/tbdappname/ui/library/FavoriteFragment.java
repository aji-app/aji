package ch.zhaw.engineering.tbdappname.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentFavoriteBinding;
import ch.zhaw.engineering.tbdappname.ui.song.list.FavoritesSongListFragment;

public class FavoriteFragment extends Fragment {

    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
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
