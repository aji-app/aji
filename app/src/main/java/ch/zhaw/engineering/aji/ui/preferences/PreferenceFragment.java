package ch.zhaw.engineering.aji.ui.preferences;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import ch.zhaw.engineering.aji.R;

public class PreferenceFragment extends PreferenceFragmentCompat {
    private PreferenceListener mListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.root_preferences);

        findPreference("licenses").setOnPreferenceClickListener(preference -> {
            mListener.onShowOpenSourceLicenses();
            return true;
        });
        findPreference("about").setOnPreferenceClickListener(preference -> {
            mListener.onOpenAbout();
            return true;
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PreferenceListener) {
            mListener = (PreferenceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PreferenceListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface PreferenceListener {
        void onOpenAbout();
        void onShowOpenSourceLicenses();
    }
}
