package ch.zhaw.engineering.aji.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentFavoriteBinding;
import ch.zhaw.engineering.aji.ui.song.list.FavoritesSongListFragment;
import ch.zhaw.engineering.aji.ui.song.list.SongListFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class FavoriteFragment extends Fragment {

    private FavoritesSongListFragment mListFragment;
    private AppViewModel mAppViewModel;

    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onShown() {
        if (mAppViewModel != null && mAppViewModel.isTwoPane()) {
            mListFragment.showFirst();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentFavoriteBinding binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        mListFragment = FavoritesSongListFragment.newInstance();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.favorite_container, mListFragment)
                .commit();

        return binding.getRoot();
    }
}
