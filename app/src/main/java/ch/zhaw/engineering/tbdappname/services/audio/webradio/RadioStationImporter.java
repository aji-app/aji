package ch.zhaw.engineering.tbdappname.services.audio.webradio;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import androidx.preference.PreferenceManager;

import java.util.List;

import ch.zhaw.engineering.tbdappname.R;
import ch.zhaw.engineering.tbdappname.services.database.AppDatabase;
import ch.zhaw.engineering.tbdappname.services.database.dao.RadioStationDao;
import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.database.repository.RadioStationRepository;
import ch.zhaw.engineering.tbdappname.services.files.CsvHelper;

public class RadioStationImporter {
    private static final String PREF_KEY_RADIOSTATION_IMPORT = "radiostation_default_import";

    private RadioStationImporter() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void loadDefaultRadioStations(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean radioStationsImported = preferences.getBoolean(PREF_KEY_RADIOSTATION_IMPORT, false);
        if (radioStationsImported) {
            return;
        }
        AsyncTask.execute(() -> {
            List<RadioStationDto> radios = CsvHelper.readRadioStations(context, R.raw.radio_stations);
            RadioStationDao dao = AppDatabase.getInstance(context).radioStationDao();

            dao.insertAll(radios);

            preferences.edit().putBoolean(PREF_KEY_RADIOSTATION_IMPORT, true).apply();
        });
    }
}
