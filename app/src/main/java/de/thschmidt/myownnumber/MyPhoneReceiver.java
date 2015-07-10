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
 * Created by Thomas H. Schmidt on 02.07.2015.
 */
// http://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html
// see 5. Exercise: Define receiver for phone changes
//  Tip   Remember that your receiver is only called if the user started it once. This requires an activity.
public class MyPhoneReceiver extends BroadcastReceiver {
    private static final String TAG = MyPhoneReceiver.class.getSimpleName();
    ActivityCallback mCallBack = null;

    /*
    // declare MyPhoneReceiver that takes in ActivityCallback (i.e. your Activity class object that is also a ActivityCallback).
    public MyPhoneReceiver(ActivityCallback callBack){
        this.mCallBack=callBack;
    }
    */

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String state = extras.getString(TelephonyManager.EXTRA_STATE);
            Log.d(TAG, "onReceive(): state is " + state);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                String phoneNumber = extras.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG, "onReceive(): calling phonumber is " + phoneNumber);
                Context appContext = context.getApplicationContext();
                Log.d(TAG, "onReceive(): getting context " + appContext.toString());
                mCallBack = MainActivity.class.UpdateOwnNumber();
                Log.d(TAG, "onReceive(): getting mCallBack " + mCallBack.toString());
                context.findViewById(R.id.MyNumber);
                // String info = "\u260F " + phoneNumber + "\n" + this.mCallBack.GetOwnNumber();; // white telephone
                String info = "My Own Number\n\u260F" + this.mCallBack.GetOwnNumber();; // white telephone
                Toast.makeText(context, info, Toast.LENGTH_LONG).show();
                // http://www.laurivan.com/android-display-a-notification/
                Handler mHandler = new Handler();
                mHandler.post(new DisplayNotification(appContext, mCallBack));
                Log.d(TAG, "onReceive(): posted DisplayNotification()");
            }
        }
    }
}
