package ch.zhaw.engineering.tbdappname.services.database.dto;

import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlaylistWithSongCount extends Playlist {
    int songCount;
}
