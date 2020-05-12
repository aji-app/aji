package ch.zhaw.engineering.aji.services.audio.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.lifecycle.LifecycleService;
import androidx.navigation.NavController;
import androidx.navigation.NavDeepLinkBuilder;

import ch.zhaw.engineering.aji.MainActivity;
import ch.zhaw.engineering.aji.R;
import ch.zhaw.engineering.aji.services.database.entity.RadioStation;
import ch.zhaw.engineering.aji.services.database.entity.Song;
import ch.zhaw.engineering.aji.ui.library.LibraryFragmentDirections;
import ch.zhaw.engineering.aji.ui.radiostation.RadioStationFragmentDirections;

public class ErrorNotificationManager {
    private static final String TAG = "ErrorNotificationManager";
    public static final String EXTRA_NOTIFICATION_ID = "notification-id";
    public static final String EXTRA_RADIOSTATION_ID = "radiostation-id";
    public static final String EXTRA_SONG_ID = "song-id";
    private final LifecycleService mContext;
    private final NotificationManagerCompat mNotificationManager;
    private static final int NOTIFICATION_ID_SONG_OFFSET = 10000000;

    public ErrorNotificationManager(LifecycleService context) {
        mContext = context;
        mNotificationManager = NotificationManagerCompat.from(mContext);
    }

    public void notifyError(@Nullable Song song) {
        if (song != null) {
            int id = (int) (NOTIFICATION_ID_SONG_OFFSET + song.getSongId());
            mNotificationManager.notify(id, createNotification(song, id));
        }
    }

    public void notifyError(@Nullable RadioStation station) {
        if (station != null) {
            int id = (int) station.getId();
            mNotificationManager.notify(id, createNotification(station, id));
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

    private Notification createNotification(Song song, int notificationId) {
        String text = mContext.getString(R.string.playback_error_song_text, song.getTitle(), song.getArtist());
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_details, mContext.getString(R.string.go_to_songdetails), getSongDetailsIntent(song.getSongId(), notificationId));
        String title = mContext.getString(R.string.playback_error_song_title);
        return createNotification(text, title, action, notificationId);
    }

    private Notification createNotification(RadioStation station, int notificationId) {
        String text = mContext.getString(R.string.playback_error_radio_text, station.getName());
        NotificationCompat.Action action = new NotificationCompat.Action(R.drawable.ic_details, mContext.getString(R.string.go_to_radiodetails), getRadioDetailsIntent(station.getId(), notificationId));
        String title = mContext.getString(R.string.plaback_error_radio_title);
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
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(mContext, (int)songId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        return new NavDeepLinkBuilder(mContext)
//                .setGraph(R.navigation.mobile_navigation)
//                .setDestination(R.id.nav_song_details)
//                .setArguments(args)
//                .createPendingIntent();
    }

    private PendingIntent getRadioDetailsIntent(long radioId, int notificationId) {
        Bundle args = new Bundle();
        args.putLong(EXTRA_RADIOSTATION_ID, radioId);
        args.putInt(EXTRA_NOTIFICATION_ID, notificationId);
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.putExtras(args);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(mContext, (int)radioId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

//        Bundle args = RadioStationFragmentDirections.actionNavRadiostationsToRadiostationDetails(radioId).getArguments();
//        args.putInt(EXTRA_NOTIFICATION_ID, notificationId);
//
//        return new NavDeepLinkBuilder(mContext)
//                .setGraph(R.navigation.mobile_navigation)
//                .setDestination(R.id.nav_radiostation_details)
//                .setArguments(args)
//                .createPendingIntent();
    }
}
