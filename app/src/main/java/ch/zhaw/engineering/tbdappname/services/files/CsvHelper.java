package ch.zhaw.engineering.tbdappname.services.files;

import android.content.Context;

import androidx.annotation.RawRes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.dto.RadioStationDto;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

public final class CsvHelper {
    private static final String TAG = "CsvHelper";

    private CsvHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static List<RadioStationDto> readRadioStations(Context context, @RawRes int resourceId) {
        InputStream fileStream = context.getResources().openRawResource(resourceId);

        CsvReader reader = new CsvReader();
        reader.setContainsHeader(true);
        try {
            CsvContainer container = reader.read(new InputStreamReader(fileStream, StandardCharsets.UTF_8));
            List<RadioStationDto> radioStations = new ArrayList<>(container.getRowCount());
            for (CsvRow row : container.getRows()) {
                RadioStationDto.RadioStationDtoBuilder builder = RadioStationDto.builder();
                builder.name(row.getField(0));

                String[] csvGenres = row.getField(1).split(";");
                builder.genres(Arrays.asList(csvGenres));

                builder.url(row.getField(2));

                radioStations.add(builder.build());
            }
            return radioStations;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}
