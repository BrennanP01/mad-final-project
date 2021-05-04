package edu.lewisu.cs.bprice.watertracker;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationAlertReciever extends BroadcastReceiver {
    public static final String ACTION_DISMISS_NOTIFICATION = "dismiss_notification";
    public static final String ACTION_REVIEW_REMINDER = "review_reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if(action.equals(ACTION_REVIEW_REMINDER)){
        NotificationUtils.remindUser(context);
    }else if(action.equals(ACTION_DISMISS_NOTIFICATION)){
        NotificationUtils.clearAllNotifications(context);
    }
    }
}
