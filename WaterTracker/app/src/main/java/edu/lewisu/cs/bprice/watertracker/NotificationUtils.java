package edu.lewisu.cs.bprice.watertracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationUtils {
    private static final int TODO_REMINDER_NOTIFICATION_ID = 1234;
    private static final String REMINDER_NOTIFICATION_CHANNEL = "reminder_notification_channel";
    private static final int TODO_REMINDER_PENDING_INTENT = 4321;
    private static final int IGNORE_PENDING_INTENT = 12;

    public static void remindUser(Context context){
        Intent startActivity = new Intent(context, MainActivity.class);
        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(
                context,
                TODO_REMINDER_PENDING_INTENT,
                startActivity,
                PendingIntent.FLAG_UPDATE_CURRENT
            );
        Intent ignoreReminderIntent = new Intent(context, NotificationAlertReciever.class);
        ignoreReminderIntent.setAction(NotificationAlertReciever.ACTION_DISMISS_NOTIFICATION);
        PendingIntent ignoreReminderPendingIntent = PendingIntent.getBroadcast(context, IGNORE_PENDING_INTENT, ignoreReminderIntent,0);

        NotificationManager notMan = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    REMINDER_NOTIFICATION_CHANNEL,
                    context.getString(R.string.channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            notMan.createNotificationChannel(channel);
        }

        NotificationCompat.Builder notBuilder = new NotificationCompat.Builder(context, REMINDER_NOTIFICATION_CHANNEL)
                .setColor(ContextCompat.getColor(context,R.color.purple_500))
                .setSmallIcon(R.drawable.outline_water_drop_black_24dp)
                .setContentTitle(context.getString(R.string.reminder_title))
                .setContentText(context.getString(R.string.reminder_text))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.reminder_text)))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(startActivityPendingIntent)
                .addAction(R.drawable.ic_cancel_black_24dp, "Not Now", ignoreReminderPendingIntent)
                .setAutoCancel(true);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            notBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        }
        notMan.notify(TODO_REMINDER_NOTIFICATION_ID, notBuilder.build());
    }


    public  static void clearAllNotifications(Context context){
        NotificationManager notMan = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notMan.cancel(TODO_REMINDER_NOTIFICATION_ID);
    }
}
