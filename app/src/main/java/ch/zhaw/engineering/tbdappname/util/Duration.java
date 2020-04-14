package ch.zhaw.engineering.tbdappname.util;

import java.util.Locale;

import ch.zhaw.engineering.tbdappname.services.audio.AudioService;

public class Duration {

    public static String getPositionDurationString(AudioService.SongInformation songInfo, long currentPosition) {
        if (songInfo != null) {
            if (songInfo.getDuration() > 0) {
                return String.format(Locale.ENGLISH, "%s / %s", getMillisAsTime(currentPosition), getMillisAsTime(songInfo.getDuration()));
            } else {
                return getMillisAsTime(currentPosition);
            }
        }
        return "";
    }

    public static String getMillisAsTime(long time) {
        long minutes = time / (60 * 1000);
        long seconds = (time / 1000) % 60;
        return String.format(Locale.ENGLISH, "%d:%02d", minutes, seconds);
    }
}
