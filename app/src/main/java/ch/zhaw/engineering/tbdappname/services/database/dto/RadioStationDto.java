package ch.zhaw.engineering.tbdappname.services.database.dto;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.zhaw.engineering.tbdappname.services.database.entity.RadioStation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadioStationDto {
    private Long id;
    @Builder.Default
    private String name = "";
    @Builder.Default
    private String url = "";
    @Builder.Default
    private List<String> genres = new ArrayList<>();

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
