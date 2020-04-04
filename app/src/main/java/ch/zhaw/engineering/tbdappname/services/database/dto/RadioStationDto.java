package ch.zhaw.engineering.tbdappname.services.database.dto;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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

    public static RadioStationDto fromRadioStation(RadioStation radioStation) {
        return RadioStationDto.builder()
                .name(radioStation.getName())
                .url(radioStation.getUrl())
                .genres(Arrays.asList(radioStation.getGenres().split(",")))
                .id(radioStation.getId())
                .build();
    }
}
