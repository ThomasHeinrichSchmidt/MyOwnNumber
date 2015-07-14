package de.thschmidt.myownnumber;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

/**
 * Created by  Created by --thomas. in June 2015.
 * used to display a notification about incoming calls and
 * offer to text/SMS the own number to the caller
 */
class DisplayNotification implements Runnable {

    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private String mCallingNumber = "";
    /**
     * NOTIFICATION_ID is a numeric value that identifies the notification that we'll be sending.
     * This value needs to be unique within this app, but it doesn't need to be unique system-wide.
     */
    public static final int NOTIFICATION_ID = 718477;

    public DisplayNotification(Context mContext, String callingNumber) {
        this.mContext = mContext;
        this.mCallingNumber = callingNumber;
        mNotificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Override
    public void run() {
        makeNotification(mContext);
    }

    private void makeNotification(Context context) {
        // register MainActivity to handle click on Notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("CallingNumber", mCallingNumber);
        // TODO: check if mCallingNumber is "null" - may happen if caller suppresses number
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // http://stackoverflow.com/questions/13717492/notifications-builder-in-api-10
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.MyOwnNumber))
                .setContentText("\u2709 '" + MainActivity.getOwnPhoneNumber(context) + "' \u27A0  \u260F " + mCallingNumber)  // ? ? ?  (U+2709 U+27A0 U260F)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher) // .ic_action_picture)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                ;
        Notification n;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            n = builder.build();
        } else {
            n = builder.getNotification();
        }
        mNotificationManager.notify(NOTIFICATION_ID, n);
    }
}

