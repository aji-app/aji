package ch.zhaw.engineering.tbdappname.services.database.dto;

import android.graphics.Bitmap;

import ch.zhaw.engineering.tbdappname.services.database.entity.Song;
import lombok.Data;

@Data
public class SongDto {
    private Long id;
    private Long mediaStoreSongId;

    private String filepath;
    private String title;
    private String album;
    private String artist;
    private String trackNumber;
    private long duration;

    public Bitmap albumArt;

    public Song toSong(String albumArtPath) {
        Song.SongBuilder songBuilder = Song.builder()
                .mediaStoreSongId(mediaStoreSongId)
                .filepath(filepath)
                .title(title)
                .album(album)
                .artist(artist)
                .trackNumber(trackNumber)
                .duration(duration)
                .rating(null)
                .favorite(false)
                .albumArtPath(albumArtPath);
        if (id != null) {
            songBuilder.songId(id);
        }

        return songBuilder.build();
    }
}
