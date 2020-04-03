package ch.zhaw.engineering.tbdappname.ui.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import ch.zhaw.engineering.tbdappname.R;

public class PreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.root_preferences);
    }
}
