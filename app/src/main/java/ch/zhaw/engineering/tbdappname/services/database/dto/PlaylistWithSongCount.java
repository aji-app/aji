package ch.zhaw.engineering.tbdappname.services.database.dto;

import lombok.Data;

@Data
public class PlaylistWithSongCount {
    int playlistId;
    String name;
    int songCount;
}
