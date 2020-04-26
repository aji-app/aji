package ch.zhaw.engineering.aji.ui.preferences;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import ch.zhaw.engineering.aji.R;

public class PreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.root_preferences);
    }
}
