package ch.zhaw.engineering.aji.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.engineering.aji.services.audio.AudioService;
import ch.zhaw.engineering.aji.services.audio.backend.AudioBackend;

public class PreferenceHelper {
    private static final String PREF_MEDIA_STORE = "mediastore_sync";
    private final Context mContext;
    private final SharedPreferences mSharedPreferences;
    private static List<AudioBackend.Callback<Boolean>> callbacks = new ArrayList<>();
    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = (prefs, key) -> {
        if (PREF_MEDIA_STORE.equals(key)) {
            boolean shouldUseMediaStore = prefs.getBoolean(PREF_MEDIA_STORE, true);
            for (AudioBackend.Callback<Boolean> callback : callbacks) {
                callback.receiveValue(shouldUseMediaStore);
            }
        }
    };

    public PreferenceHelper(@NonNull Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    public void observeMediaStoreSetting(AudioBackend.Callback<Boolean> callback) {
        callbacks.add(callback);
    }

    public boolean isMediaStoreEnabled() {
        return mSharedPreferences.getBoolean(PREF_MEDIA_STORE, true);
    }

    public boolean isFilterEnbaled(AudioService.Filter filter) {
        return mSharedPreferences.getBoolean(getFilterKey(filter), false);
    }

    public void setFilterEnabled(AudioService.Filter filter, boolean enabled) {
        mSharedPreferences.edit().putBoolean(getFilterKey(filter), enabled).apply();
    }


    private String getFilterKey(AudioService.Filter filter) {
        return "filter_" + filter.name();
    }
}
