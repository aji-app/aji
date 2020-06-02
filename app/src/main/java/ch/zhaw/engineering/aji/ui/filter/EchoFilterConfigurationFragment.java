package ch.zhaw.engineering.aji.ui.filter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.databinding.FragmentEchoFilterConfigurationBinding;
import ch.zhaw.engineering.aji.util.PreferenceHelper;

import static ch.zhaw.engineering.aji.services.audio.AudioService.Filter.EchoFilter;

public class EchoFilterConfigurationFragment extends Fragment {

    public static final String DELAY_KEY = "delay";
    public static final String STRENGTH_KEY = "strength";

    public static final double DELAY_DEFAULT = 1.0;
    public static final double STRENGTH_DEFAULT = 0.4;

    private static final double DELAY_MAX = 2.0;
    private static final double STRENGTH_MAX = 1.0;

    private FragmentEchoFilterConfigurationBinding mBinding;
    private PreferenceHelper mPreferenceHelper;
    private EchoFilterDetailsListener mListener;

    private double mDelay;
    private double mStrength;
    private boolean mEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentEchoFilterConfigurationBinding.inflate(inflater, container, false);
        mBinding.enabled.setOnCheckedChangeListener((v, checked) -> {
            mEnabled = checked;
            mPreferenceHelper.setFilterEnabled(EchoFilter, checked);
            updateFilter();
        });

        mBinding.delaySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    double delay = progress / (seekBar.getMax() / DELAY_MAX);
                    setDelay(delay);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mBinding.strengthSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    double strength = progress / (seekBar.getMax() / STRENGTH_MAX);
                    setStrength(strength);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        // Inflate the layout for this fragment
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            mPreferenceHelper = new PreferenceHelper(getActivity());
            mBinding.enabled.setChecked(mPreferenceHelper.isFilterEnbaled(EchoFilter));

            double delay = mPreferenceHelper.getFilterValue(EchoFilter, DELAY_KEY, DELAY_DEFAULT);
            mBinding.delaySeekbar.setProgress((int) (delay * mBinding.delaySeekbar.getMax() / DELAY_MAX));
            setDelay(delay);

            double strength = mPreferenceHelper.getFilterValue(EchoFilter, STRENGTH_KEY, STRENGTH_DEFAULT);
            mBinding.strengthSeekbar.setProgress((int) (strength * mBinding.strengthSeekbar.getMax() / STRENGTH_MAX));
            setStrength(strength);
        }
    }

    private void setDelay(double value) {
        if (getActivity() != null) {
            mDelay = value;
            String delayValue = getActivity().getString(R.string.filter_slider_seconds, value);
            mBinding.delayValue.setText(delayValue);
            mPreferenceHelper.setFilterValue(EchoFilter, DELAY_KEY, value);
            updateFilter();
        }
    }

    private void setStrength(double value) {
        if (getActivity() != null) {
            mStrength = value;
            String strengthValue = getActivity().getString(R.string.filter_slider_percent, ((int) (value * 100)));
            mBinding.strengthValue.setText(strengthValue);
            mPreferenceHelper.setFilterValue(EchoFilter, STRENGTH_KEY, value);
            updateFilter();
        }
    }

    private void updateFilter() {
        mListener.modifyEchoFilter(mEnabled, mStrength, mDelay);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof EchoFilterDetailsListener) {
            mListener = (EchoFilterDetailsListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SongDetailsFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface EchoFilterDetailsListener {
        void modifyEchoFilter(boolean enabled, double strength, double delay);
    }
}
