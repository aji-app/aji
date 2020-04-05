package ch.zhaw.engineering.tbdappname.services.files;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;

public class WebRadioPlsParser {
    private static final String TAG = "WebRadioPlsParser";

    public static RadioStationDto parseSingleRadioStationFromPlsFile(String path) {
        File file = new File(path);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            RadioStationDto station = new RadioStationDto();
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("File")) {
                    String[] parts = line.split("=");
                    if (parts.length > 1) {
                        String[] pathParts = new String[parts.length - 1];
                        System.arraycopy(parts, 1, pathParts, 0, parts.length - 1);
                        station.setUrl(TextUtils.join("", pathParts));
                    }
                }
                if (line.startsWith("Title")) {
                    String[] parts = line.split("=");
                    if (parts.length > 1) {
                        String[] titleParts = new String[parts.length - 1];
                        System.arraycopy(parts, 1, titleParts, 0, parts.length - 1);
                        station.setName(TextUtils.join("", titleParts));
                    }
                    return station;
                }
            }
            Log.i(TAG, "File is not a pls file with entries");
            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
