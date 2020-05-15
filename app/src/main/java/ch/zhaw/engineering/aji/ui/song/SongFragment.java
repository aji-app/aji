package ch.zhaw.engineering.aji.ui.song;

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
import ch.zhaw.engineering.aji.databinding.FragmentSongBinding;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.song.list.AllSongsListFragment;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import ch.zhaw.engineering.aji.util.PreferenceHelper;

public class SongFragment extends Fragment {

    private FragmentSongBinding mBinding;
    private SongFragmentListener mListener;
    private AllSongsListFragment mListFragment;
    private AppViewModel mAppViewModel;

    @SuppressWarnings("unused")
    public static SongFragment newInstance() {
        SongFragment fragment = new SongFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mListFragment = AllSongsListFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.bottom_container, mListFragment)
                    .commitNow();
        }
    }

    public void onShown() {
        if (mAppViewModel != null && mAppViewModel.isTwoPane()) {
            mListFragment.showFirst();
        }
        configureFab();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private void configureFab() {
        if (getActivity()  != null) {
            mAppViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            PreferenceHelper helper = new PreferenceHelper(getActivity());
            configureFab(!helper.isMediaStoreEnabled());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentSongBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    private void configureFab(boolean enabled) {
        if (mListener != null) {
            if (enabled) {
                mListener.configureFab(v -> mListener.onAddSongsButtonClick(), R.drawable.ic_add);
            } else {
                mListener.disableFab();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SongFragmentListener) {
            mListener = (SongFragmentListener) context;
            configureFab();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongFragmentListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        configureFab();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface SongFragmentListener extends FabCallbackListener {
        void onAddSongsButtonClick();
    }
}
