package ch.zhaw.engineering.tbdappname.services.database.dto;

import ch.zhaw.engineering.tbdappname.services.database.entity.Playlist;
import lombok.Data;

@Data
public class PlaylistWithSongCount {
    long playlistId;
    String name;
    int songCount;
}
