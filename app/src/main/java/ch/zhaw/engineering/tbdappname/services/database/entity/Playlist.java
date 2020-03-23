package ch.zhaw.engineering.tbdappname.services.database.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(indices = { @Index(value = "name", unique = true)})
public class Playlist {
    @PrimaryKey(autoGenerate = true)
    private int playlistId;

    private String name;
}

