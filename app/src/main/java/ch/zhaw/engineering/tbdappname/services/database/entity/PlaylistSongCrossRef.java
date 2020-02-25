package ch.zhaw.engineering.tbdappname.services.database.entity;

import androidx.room.Entity;
import androidx.room.Index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity(primaryKeys = {"playlistId", "songId"}, indices = {@Index(value = "songId")})
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistSongCrossRef {
    private long playlistId;
    private long songId;

    private int order;
}
