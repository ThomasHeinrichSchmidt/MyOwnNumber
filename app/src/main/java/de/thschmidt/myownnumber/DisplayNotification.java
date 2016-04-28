package de.thschmidt.myownnumber;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
        if (MainActivity.isSuggestSendingSMStoCallee()) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("CallingNumber", mCallingNumber);
            // TODO: check if mCallingNumber is "null" - may happen if caller suppresses number
            PendingIntent pendingIntent = PendingIntent.getActivity(context,
                    NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String msg = "\u2709  \u00ab" + MainActivity.getOwnPhoneNumber(context) + "\u00bb  \u27A0  \u260F " + mCallingNumber;   //  letter arrow phone (U+2709 U+27A0 U260F)
            // http://stackoverflow.com/questions/13717492/notifications-builder-in-api-10
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setContentTitle(context.getString(R.string.MyOwnNumber))
                    // .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))  // needed? does not seem to make a difference   http://stackoverflow.com/questions/28387602/notification-bar-icon-turns-white-in-android-5-lollipop
                    .setContentText(msg)
                    .setContentIntent(pendingIntent)
                    .setColor(Color.rgb(132, 165, 39))  // SMS green, #84a527 (https://www.colorcodehex.com/84a527/)
                    .setSmallIcon(getNotificationIcon())
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher));
            Notification n;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                n = builder.build();
            } else {
                //noinspection deprecation
                n = builder.getNotification();
            }
            mNotificationManager.notify(NOTIFICATION_ID, n);
        }
    }

    private int getNotificationIcon() {
        // http://developer.android.com/design/patterns/notifications.html#guidelines
        // Notification icons should only be a white-on-transparent background image.
        // Used IrfanView to build it:
        // clean original picture, Image / Decrease Color Depth to 2, Image / Negative, Image / Resize/Resample (Size Method "Resize", otherwise picture is increased to 24 Bit again)
        // Android seems to be using the drawable-xxhdpi picture resolution only
        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return whiteIcon ? R.drawable.ic_notification : R.drawable.ic_launcher;
    }
}

