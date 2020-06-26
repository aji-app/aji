package ch.zhaw.engineering.aji.services.database.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArtistWithAlbumCountDto extends ArtistDto {
    private int albumCount;
}
