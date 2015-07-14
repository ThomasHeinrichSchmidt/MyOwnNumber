package de.thschmidt.myownnumber;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by  Created by --thomas in June 2015.
 * http://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html
 * see 5. Exercise: Define receiver for phone changes
 *        Tip   Remember that your receiver is only called if the user started it once. This requires an activity.
 *  provides calling number and displays a Notification once a call is received
 */
public class MyPhoneReceiver extends BroadcastReceiver {
    private static final String TAG = MyPhoneReceiver.class.getSimpleName();
    private static String previousState = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String state = extras.getString(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "onReceive(): state is " + state + ", extras is " + extras);

            // get calling number for external call and toast it
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                String phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG, "onReceive(): STATE_RINGING - calling phone number is " + phoneNumber);
                if (phoneNumber != null) {
                    poposeToast(context);
                }
                previousState = state; // STATE_RINGING
                Log.d(TAG, "onReceive(): previousState = " + previousState);
            }

            // get called number for own call and propose SMS/text message to callee
            else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE) && previousState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                String phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG, "onReceive(): STATE_IDLE - calling phone number is " + phoneNumber);
                if (phoneNumber != null) {
                    Context appContext = poposeToast(context);
                    // http://www.laurivan.com/android-display-a-notification/
                    Handler mHandler = new Handler();
                    mHandler.post(new DisplayNotification(appContext, phoneNumber));
                    previousState = "";
                    Log.d(TAG, "onReceive(): posted DisplayNotification(), clear previousState");
                }
            }

            // remember previous call state
            // own call: OFFHOOK > IDLE  (accepted)
            // own call:                 (not accepted)
            // call:     RINGING > OFFHOOK > IDLE   (accepted)
            // call:     RINGING > IDLE             (not accepted)

            else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                if (! previousState.equals(TelephonyManager.EXTRA_STATE_RINGING) ) {
                    // OFFHOOK to signal own call ( = IDLE after OFFHOOK)
                    // OFFHOOK after RINGING signals call accepted
                    previousState = state; // STATE_OFFHOOK
                }
                Log.d(TAG, "onReceive(): previousState = " + previousState);
            }
        }
    }

    private Context poposeToast(Context context) {
        Context appContext = context.getApplicationContext();
        // Log.d(TAG, "onReceive(): getting context " + appContext.toString());
        // Log.d(TAG, "onReceive(): MainActivity.ownPhoneNumber is '" + MainActivity.getOwnPhoneNumber(context) + "'");
        String info = "My Own Number\n\u260F" + MainActivity.getOwnPhoneNumber(context); // white telephone
        // show own number to improve user memory
        Log.d(TAG, "onReceive(): Toast " + info);
        Toast.makeText(context, info, Toast.LENGTH_LONG).show();
        return appContext;
    }
}