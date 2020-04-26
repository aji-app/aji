package ch.zhaw.engineering.aji.services.database.entity;

import androidx.room.Entity;
import androidx.room.Index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(primaryKeys = {"playlistId", "songId"}, indices = {@Index(value = "songId"), @Index(value = "playlistId")})
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistSongCrossRef {
    private long playlistId;
    private long songId;

    private int order;
}
