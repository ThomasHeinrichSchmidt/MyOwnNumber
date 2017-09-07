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
    private static String outgoingCallNumber = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive(): enter");
        Bundle extras = intent.getExtras();

        Log.d(TAG, "onReceive():    intent.getAction() is " + intent.getAction());
        // MainActivity.checkAndSetRequiredPermissions();
        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            outgoingCallNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            toastOwnNumberToImproveUserMemory(context);
            Log.d(TAG, "onReceive():    OUTGOING_CALL number is " + outgoingCallNumber);
        }
        if (extras != null) {
            String state = extras.getString(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "onReceive():    state is " + state + ", extras is " + extras);

            // remember previous call states due to
            // own call: OFFHOOK > IDLE  (accepted by callee)
            // own call: OFFHOOK > IDLE  (canceled by callee)
            // own call: OFFHOOK > IDLE  (canceled by myself)
            // call:     RINGING > OFFHOOK > IDLE   (accepted by myself)
            // call:     RINGING > IDLE             (not accepted by myself)
            // call:     RINGING > OFFHOOK > IDLE   (accepted by myself, number suppressed)
            // call:     RINGING > IDLE             (not accepted by myself, number suppressed)

            // get calling number for external call and toast it
            if (state != null && state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                String phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG, "onReceive():    STATE_RINGING - calling phone number is " + phoneNumber);
                toastOwnNumberToImproveUserMemory(context);
                previousState = state; // STATE_RINGING
                Log.d(TAG, "onReceive():    set previousState = " + previousState);
            }

            // get called number for own call and suggest SMS/text message to callee
            // - but not for a received call
            else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE) && previousState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                String phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                // API level 21 does also provide the outgoing number here (without the need for the extra Intent.ACTION_NEW_OUTGOING_CALL)
                if (phoneNumber == null) {
                    if (outgoingCallNumber != null) {
                        phoneNumber = outgoingCallNumber;
                    }
                }
                Log.d(TAG, "onReceive():    STATE_IDLE - calling phone number is " + phoneNumber);
                if (phoneNumber != null) {
                    // http://www.laurivan.com/android-display-a-notification/
                    Handler mHandler = new Handler();
                    Context appContext = context.getApplicationContext();
                    mHandler.post(new DisplayNotification(appContext, phoneNumber));
                    outgoingCallNumber = null;
                    Log.d(TAG, "onReceive():    posted DisplayNotification() for phone number " + phoneNumber);
                }
            } else if (state != null && state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                if (!previousState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    // OFFHOOK to signal own call ( = IDLE after OFFHOOK)
                    // OFFHOOK after RINGING signals incoming call accepted
                    previousState = state; // STATE_OFFHOOK
                    Log.d(TAG, "onReceive():    set previousState = " + previousState);
                }
            }

            if (state != null && state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                previousState = "";
                Log.d(TAG, "onReceive():    clear previousState");
            }
        }
        Log.d(TAG, "onReceive(): previousState is " + previousState);
        Log.d(TAG, "onReceive(): leave");
    }

    private void toastOwnNumberToImproveUserMemory(Context context) {
        // Context appContext = context.getApplicationContext();
        // Log.d(TAG, "onReceive(): getting context " + appContext.toString());
        // Log.d(TAG, "onReceive(): MainActivity.ownPhoneNumber is '" + MainActivity.getOwnPhoneNumber(context) + "'");
        String info = "My Own Number\n\u260F" + MainActivity.getOwnPhoneNumber(context); // white telephone
        // show own number to improve user memory
        Log.d(TAG, "onReceive():    Toast " + info.replace('\n',' '));
        Toast.makeText(context, info, Toast.LENGTH_LONG).show();
    }
}