package ch.zhaw.engineering.aji.ui.filter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentFilterBinding;
import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.TabletAwareFragment;
import ch.zhaw.engineering.aji.util.PreferenceHelper;

import static ch.zhaw.engineering.aji.services.audio.AudioService.Filter.EchoFilter;

public class FilterFragment extends TabletAwareFragment {
    private FilterFragmentListener mListener;
    private FragmentFilterBinding mBinding;
    private PreferenceHelper mPreferenceHelper;

    @Override
    protected void showDetails() {

        mAppViewModel.setPlaceholderText(R.string.no_filters);
        mListener.onFilterSelected(EchoFilter);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentFilterBinding.inflate(inflater, container, false);

        mBinding.echoFilter.setOnClickListener(v -> {
            mListener.onFilterSelected(EchoFilter);
        });
        // Inflate the layout for this fragment
        return mBinding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FilterFragmentListener) {
            mListener = (FilterFragmentListener) context;
            mListener.disableFab();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FilterFragmentListener");
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        if (mListener != null) {
            mListener.disableFab();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface FilterFragmentListener  extends FabCallbackListener {
        void showEmptyDetails();
        void onFilterSelected(AudioService.Filter filter);
    }
}
