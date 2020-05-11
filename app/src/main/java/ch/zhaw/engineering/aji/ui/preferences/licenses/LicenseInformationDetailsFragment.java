package ch.zhaw.engineering.aji.ui.preferences.licenses;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import ch.zhaw.engineering.aji.databinding.FragmentLicenseInformationDetailsBinding;

public class LicenseInformationDetailsFragment extends Fragment {
    public static final String ARG_LICENSE_TEXT_ID = "license-text-id";
    FragmentLicenseInformationDetailsBinding mBinding;
    @StringRes
    private int mLicenseTextId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLicenseTextId = getArguments().getInt(ARG_LICENSE_TEXT_ID);
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentLicenseInformationDetailsBinding.inflate(inflater, container, false);
        mBinding.licenseContent.setText(mLicenseTextId);
        return mBinding.getRoot();
    }
}
