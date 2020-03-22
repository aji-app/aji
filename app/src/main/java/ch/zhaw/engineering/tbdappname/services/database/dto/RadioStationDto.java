package ch.zhaw.engineering.tbdappname.services.database.dto;

import android.text.TextUtils;

import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import lombok.Data;

@Data
public class RadioStationDto {
    private Long id;
    private String name;
    private String url;
    private List<String> genres;

    public RadioStation toRadioStation() {
        RadioStation.RadioStationBuilder builder = RadioStation.builder()
                .name(name)
                .url(url)
                .genres(TextUtils.join(",", genres));
        if (id != null) {
            builder.id(id);
        }
        return builder.build();
    }
}
