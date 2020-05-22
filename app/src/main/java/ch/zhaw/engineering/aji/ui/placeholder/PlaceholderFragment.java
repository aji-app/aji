package ch.zhaw.engineering.aji.ui.placeholder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ch.zhaw.engineering.aji.databinding.FragmentPlaceholderBinding;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class PlaceholderFragment extends Fragment {

    FragmentPlaceholderBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentPlaceholderBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            new ViewModelProvider(getActivity()).get(AppViewModel.class).getPlaceholderText().observe(getViewLifecycleOwner(), text -> {
                getActivity().runOnUiThread(() -> mBinding.placeholderText.setText(text));
            });
        }
    }
}
