package ch.zhaw.engineering.tbdappname.ui.expandedcontrols;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.databinding.FragmentExpandedControlsBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExpandedControlsFragment extends Fragment {

    private FragmentExpandedControlsBinding mBinding;

    public ExpandedControlsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentExpandedControlsBinding.inflate(inflater);
        return mBinding.getRoot();
    }
}
