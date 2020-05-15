package ch.zhaw.engineering.aji.ui.preferences;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceFragmentCompat;

import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.ui.viewmodel.AppViewModel;

public class PreferenceFragment extends PreferenceFragmentCompat {
    private PreferenceListener mListener;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.root_preferences);

        findPreference("remove_all_songs").setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity(), R.style.Theme_App_AlertDialog_PurpleLime)
                    .setTitle(R.string.remove_all_songs)
                    .setMessage(R.string.remove_all_songs_and_playlists)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        mListener.cleanupDatabase();
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

            dialogBuilder.show();
            return true;
        });

        // TODO: Add remove all songs button when media store is disabled
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

        void cleanupDatabase();
    }
}
