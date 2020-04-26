package ch.zhaw.engineering.aji.services.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder()
@NoArgsConstructor
@AllArgsConstructor
@Entity(indices = { @Index(value = "filepath", unique = true), @Index(value="songId")})
public class Song {
    @PrimaryKey(autoGenerate = true)
    private long songId;
    private Long mediaStoreSongId;

    private String filepath;
    private String title;
    private String album;
    private String artist;
    private String trackNumber;
    private long duration;
    private Integer rating;
    private boolean favorite;
    private boolean deleted;
    private String albumArtPath;

    @Override
    @NonNull
    public String toString() {
        return title + " by " + artist;
    }
}
