package ch.zhaw.engineering.tbdappname.services.audio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleService;
import androidx.lifecycle.LiveData;

import ch.zhaw.engineering.tbdappname.MainActivityOld;
import ch.zhaw.engineering.tbdappname.R;

import static android.content.Context.NOTIFICATION_SERVICE;
import static androidx.media.app.NotificationCompat.MediaStyle;
import static ch.zhaw.engineering.tbdappname.services.audio.AudioService.EXTRAS_COMMAND;
import static ch.zhaw.engineering.tbdappname.util.Duration.getPositionDurationString;

public class NotificationManager {
    private static final String TAG = "NotificationManager";
    public static final String SHUTDOWN_INTENT = "ch.zhaw.engineering.tbdapppname.services.audio.shutdown";
    private static final int NOTIFICATION_ID = 1;

    private final LifecycleService mContext;
    private final LiveData<AudioService.PlayState> mCurrentState;
    private final android.app.NotificationManager mNotificationManager;
    private final LiveData<AudioService.SongInformation> mCurrentSongInformation;
    private final LiveData<Long> mCurrentPosition;

    public NotificationManager(LifecycleService context, LiveData<AudioService.SongInformation> currentSongInformation, LiveData<Long> currentPosition, LiveData<AudioService.PlayState> currentState) {
        mContext = context;
        mCurrentState = currentState;
        mNotificationManager = (android.app.NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        mCurrentSongInformation = currentSongInformation;
        mCurrentPosition = currentPosition;

        mCurrentSongInformation.observe(mContext, song -> update());

        mCurrentPosition.observe(mContext, position -> update());

        mCurrentState.observe(mContext, state -> {
            update();
            if (state == AudioService.PlayState.STOPPED || state == AudioService.PlayState.PAUSED || state == AudioService.PlayState.INITIAL) {
                stop();
            }
            if (state == AudioService.PlayState.PLAYING) {
                start();
            }
        });

    }

    public void cancel() {
        Log.i(TAG, "Cancel Notification");
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public void start() {
        Log.i(TAG, "Start foreground");
        mContext.startForeground(NOTIFICATION_ID, createCurrentNotification());
    }

    private void stop() {
        Log.i(TAG, "Stop foreground");
        mContext.stopForeground(false);
    }

    private void update() {
        mNotificationManager.notify(NOTIFICATION_ID, createCurrentNotification());
    }

    private String getNotificationChannelId() {
        String channelId = "tbd-appname-channelId";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title", android.app.NotificationManager.IMPORTANCE_LOW);
            channel.enableLights(false);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }
        return channelId;
    }

    private Notification createCurrentNotification() {
        String channelId = getNotificationChannelId();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);
        Intent notificationIntent = new Intent(mContext, MainActivityOld.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent openAppIntent = PendingIntent.getActivity(mContext, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AudioService.SongInformation currentSongInformation = mCurrentSongInformation.getValue();
        AudioService.PlayState currentState = mCurrentState.getValue();

        boolean shouldShowPosition = currentState != AudioService.PlayState.STOPPED;
        long currentPosition = mCurrentPosition.getValue() == null ? 0 : mCurrentPosition.getValue();
        String currentPlaylistName = currentSongInformation == null || currentSongInformation.getPlaylistName() == null ? "" : (" - " + currentSongInformation.getPlaylistName());

        return builder
                // Show controls on lock screen even when user hides sensitive content.
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_app_icon)
                // Add media control buttons that invoke intents in your media service
                .addAction(R.drawable.ic_prev, "Previous", getControlIntent(AudioService.AudioServiceCommand.PREVIOUS)) // #0
                .addAction(AudioService.PlayState.PLAYING == currentState ? R.drawable.ic_pause : R.drawable.ic_play, "Pause", getControlIntent(AudioService.AudioServiceCommand.PLAYPAUSE))  // #1
                .addAction(R.drawable.ic_next, "Next", getControlIntent(AudioService.AudioServiceCommand.NEXT))     // #2
                .addAction(R.drawable.ic_stop, "Next", getControlIntent(AudioService.AudioServiceCommand.STOP))     // #2
                // TODO: Improve time and playlist string generation
                .setSubText(shouldShowPosition ? getPositionDurationString(currentSongInformation, currentPosition) + currentPlaylistName : "")
//                .setContentText(currentSong == null ? "Stopped" : currentSong.toString())
                .setStyle(new MediaStyle()
                                .setShowActionsInCompactView(0, 1, 2)
                                .setShowCancelButton(true)
                                .setCancelButtonIntent(getShutdownIntent())
//                        .setMediaSession()
                )
                .setShowWhen(false)
                .setContentIntent(openAppIntent)
                .setContentTitle(currentSongInformation == null ? "Not Playing" : currentSongInformation.getTitle())
                .setContentText(currentSongInformation == null ? "" : currentSongInformation.getArtist())
                .setOnlyAlertOnce(true)
                .setDeleteIntent(getShutdownIntent())
//                .setLargeIcon(albumArtBitmap)
                .build();


    }


    private PendingIntent getControlIntent(AudioService.AudioServiceCommand command) {
        Intent pauseIntent = new Intent(mContext, AudioService.class);
        pauseIntent.putExtra(EXTRAS_COMMAND, command);
        return PendingIntent.getService(mContext, command.ordinal(), pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent getShutdownIntent() {
        Intent intent = new Intent();
        intent.setAction(SHUTDOWN_INTENT);
        return PendingIntent.getBroadcast(mContext, 6, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
