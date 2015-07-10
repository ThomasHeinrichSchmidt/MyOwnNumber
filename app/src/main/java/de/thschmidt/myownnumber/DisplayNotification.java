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
 * Created by Thomas H. Schmidt on 03.07.2015.
 */
public class DisplayNotification implements Runnable {

    Context mContext;
    ActivityCallback mCallBack = null;
    NotificationManager mNotificationManager;
    /**
     * A numeric value that identifies the notification that we'll be sending.
     * This value needs to be unique within this app, but it doesn't need to be
     * unique system-wide.
     */
    public static final int NOTIFICATION_ID = 1;

    // declare DisplayNotification that takes in ActivityCallback (i.e. your Activity class object that is also an ActivityCallback).
    public DisplayNotification(Context mContext, ActivityCallback callBack) {
        this.mContext = mContext;
        mNotificationManager = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        this.mCallBack=callBack;
    }

    @Override
    public void run() {
        makeNotification(mContext);
    }

    private void makeNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // http://stackoverflow.com/questions/13717492/notifications-builder-in-api-10
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("MyOwnNumber")
                .setContentText(MainActivity.mPhoneNumber)
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

