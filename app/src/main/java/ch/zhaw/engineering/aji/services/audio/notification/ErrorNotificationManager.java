package ch.zhaw.engineering.aji.services.audio.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ch.zhaw.engineering.aji.MainActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.entity.RadioStation;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import lombok.Setter;

public class ErrorNotificationManager {
    private static final String TAG = "ErrorNotification";
    public static final String EXTRA_NOTIFICATION_ID = "notification-id";
    public static final String EXTRA_RADIOSTATION_ID = "radiostation-id";
    public static final String EXTRA_SONG_ID = "song-id";
    private final Context mContext;
    private final NotificationManagerCompat mNotificationManager;
    private static final int NOTIFICATION_ID_SONG_OFFSET = 10000000;
    private static final int NOTIFICATION_ID_RADIO_OFFSET = 10000;

    @Setter
    private AlertErrorHandler mErrorHandler;

    public ErrorNotificationManager(Context context) {
        mContext = context;
        mNotificationManager = NotificationManagerCompat.from(mContext);
    }

    public void notifyError(@Nullable Song song) {
        // API 25 or lower
        if (song != null) {
            String text = mContext.getString(R.string.playback_error_song_text, song.getTitle(), song.getArtist());
            String title = mContext.getString(R.string.playback_error_song_title);
            int notificationId = (int) (NOTIFICATION_ID_SONG_OFFSET + song.getSongId());
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 && mErrorHandler != null) {
                mErrorHandler.showSongError(text, title, song.getSongId(), notificationId);
            }
            mNotificationManager.notify(notificationId, createNotification(song, notificationId, text, title));
        }
    }

    public void notifyError(@Nullable RadioStation station) {
        Log.i(TAG, "Error playing radiostation ");
        if (station != null) {
            String text = mContext.getString(R.string.playback_error_radio_text, station.getName());
            String title = mContext.getString(R.string.plaback_error_radio_title);
            int notificationId = (int) station.getId() + NOTIFICATION_ID_RADIO_OFFSET;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 && mErrorHandler != null) {
                mErrorHandler.showRadioError(text, title, station.getId(), notificationId);
            }
            mNotificationManager.notify(notificationId, createNotification(station, notificationId, text, title));
        }
    }

    private String getNotificationChannelId() {
        String channelId = "aji-error-channelId";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Aji Error Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            mNotificationManager.createNotificationChannel(channel);
        }
        return channelId;
    }

    private Notification createNotification(Song song, int notificationId, String text, String title) {
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_details, mContext.getString(R.string.go_to_songdetails), getSongDetailsIntent(song.getSongId(), notificationId));
        return createNotification(text, title, action, notificationId);
    }

    private Notification createNotification(RadioStation station, int notificationId, String text, String title) {
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_details, mContext.getString(R.string.go_to_radiodetails), getRadioDetailsIntent(station.getId(), notificationId));
        return createNotification(text, title, action, notificationId);
    }

    private Notification createNotification(String text, String title, NotificationCompat.Action action, int notificationId) {
        String channelId = getNotificationChannelId();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);
        Intent notificationIntent = new Intent(mContext, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openAppIntent = PendingIntent.getActivity(mContext, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return builder
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_notification)
                .setShowWhen(true)
                .setContentIntent(openAppIntent)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setContentText(text)
                .addAction(action)
                .setAutoCancel(true)
                .build();
    }

    private PendingIntent getSongDetailsIntent(long songId, int notificationId) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_SONG_ID, songId);
        args.putInt(EXTRA_NOTIFICATION_ID, notificationId);
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtras(args);
        intent.setAction("song-details " + songId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mContext, (int) songId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getRadioDetailsIntent(long radioId, int notificationId) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_RADIOSTATION_ID, radioId);
        args.putInt(EXTRA_NOTIFICATION_ID, notificationId);
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtras(args);
        intent.setAction("radio-details " + radioId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(mContext, (int) radioId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public interface AlertErrorHandler {
        void showRadioError(String text, String title, long id, int notificationId);

        void showSongError(String text, String title, long id, int notificationId);
    }
}
