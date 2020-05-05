package ch.zhaw.engineering.aji.services.audio.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    public static void cancelNotification(@NonNull Context context, int id) {
        NotificationManagerCompat.from(context).cancel(id);
    }
}
