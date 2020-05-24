package ch.zhaw.engineering.aji.services.database.dto;

import lombok.Data;

@Data
public class SongWithOnlyAlbumAndIds {
    private long songId;
    private Long mediaStoreSongId;
    private String album;
    private String albumArtPath;
}
