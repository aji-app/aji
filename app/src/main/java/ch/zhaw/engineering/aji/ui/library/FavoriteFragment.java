package ch.zhaw.engineering.aji.ui.library;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentFavoriteBinding;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.song.list.AllSongsListFragment;
import ch.zhaw.engineering.aji.ui.song.list.FavoritesSongListFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class FavoriteFragment extends Fragment {

    private FavoritesSongListFragment mListFragment;
    private AppViewModel mAppViewModel;
    private FavoritesFragmentListener mListener;
    private boolean mConfigureAtStart = false;

    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAppViewModel.isTwoPane()) {
            mListFragment.showFirst();
        }
        mAppViewModel.setPlaceholderText(R.string.no_favorites_prompt);
        configureFab();
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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FavoritesFragmentListener) {
            mListener = (FavoritesFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FavoritesFragmentListener");
        }
    }

    private void configureFab() {
        if (mListener != null) {
            mListener.configureFab(view -> {
                mListener.onPlayFavorites();
            }, R.drawable.ic_play);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface FavoritesFragmentListener extends FabCallbackListener {
        void onPlayFavorites();
    }
}
