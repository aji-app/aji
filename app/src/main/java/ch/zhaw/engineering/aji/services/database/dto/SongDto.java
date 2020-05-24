package ch.zhaw.engineering.aji.services.database.dto;

import android.graphics.Bitmap;

import ch.zhaw.engineering.aji.services.database.entity.Song;
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

    public SongDto clear() {
        id = null;
        mediaStoreSongId = null;
        filepath = null;
        title = null;
        album = null;
        artist = null;
        trackNumber = null;
        duration = 0;
        albumArt = null;
        return this;
    }

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
