package ch.zhaw.engineering.aji.ui.preferences;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.ui.FabCallbackListener;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;
import ch.zhaw.engineering.aji.util.Themes;

public class PreferenceFragment extends PreferenceFragmentCompat {
    public static final String KEY_REMOVE_ALL_SONGS = "remove_all_songs";
    public static final String KEY_LICENSES = "licenses";
    public static final String KEY_ABOUT = "about";

    private PreferenceListener mListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.root_preferences);

        Preference removeAllSongs = findPreference(KEY_REMOVE_ALL_SONGS);
        if (removeAllSongs != null) {
            removeAllSongs.setOnPreferenceClickListener(preference -> {
                if (getActivity() != null) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.remove_all_songs)
                            .setMessage(R.string.remove_all_songs_and_playlists)
                            .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                mListener.cleanupDatabase();
                            })
                            .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

                    dialogBuilder.show();
                }
                return true;
            });
        }

        Preference themesPreference = findPreference(Themes.KEY_THEME);
        if (themesPreference != null) {
            themesPreference.setOnPreferenceChangeListener((pref, newValue) -> {
                mListener.themeChanged();
                return true;
            });
        }

        Preference licensePreference = findPreference(KEY_LICENSES);
        if (licensePreference != null) {
            licensePreference.setOnPreferenceClickListener(preference -> {
                mListener.onShowOpenSourceLicenses();
                return true;
            });
        }

        Preference aboutPreference = findPreference(KEY_ABOUT);
        if (aboutPreference != null) {
            aboutPreference.setOnPreferenceClickListener(preference -> {
                mListener.onOpenAbout();
                return true;
            });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            final AppViewModel appViewModel = new ViewModelProvider(getActivity()).get(AppViewModel.class);
            if (appViewModel.isTwoPane()) {
                mListener.onOpenAbout();
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof PreferenceListener) {
            mListener = (PreferenceListener) context;
            mListener.disableFab();
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PreferenceListener");
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


    public interface PreferenceListener extends FabCallbackListener {
        void onOpenAbout();

        void onShowOpenSourceLicenses();

        void cleanupDatabase();

        void themeChanged();
    }
}
