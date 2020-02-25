package ch.zhaw.engineering.tbdappname.services.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.Data;

@Data
@Entity(indices = { @Index(value = "name", unique = true)})
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    private int playlistId;

    private String name;
}

