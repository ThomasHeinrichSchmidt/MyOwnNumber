package de.thschmidt.myownnumber;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity {

    // http://stackoverflow.com/questions/17371470/changing-ic-launcher-png-in-android-studio

    private static String ownPhoneNumber = "";
    private static final String TAG = MainActivity.class.getSimpleName();

    // TODO: conditionally remove LOG.d() in release build by using private static final boolean Debug

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate(): enter");
        Log.d(TAG, "onCreate(): ownPhoneNumber = '" + getOwnPhoneNumber(getApplicationContext()) + "'");
        super.onCreate(savedInstanceState);
        // TODO: do not show activity if called from notification, just send SMS
        setContentView(R.layout.activity_main);

        // retrieve calling number, if activity was started by Notification, i.e. a calling number has been set
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d(TAG, "onCreate(): found extras!");
            String mCallingNumber = getString(R.string.UnknownPhoneNumber);
            if (android.os.Build.VERSION.SDK_INT < 21) {
                mCallingNumber = "";
                mCallingNumber = (String) extras.get("CallingNumber");
            }
            else {
                mCallingNumber = extras.getString("CallingNumber", "");
            }
            if (!mCallingNumber.equals("")) {
                Log.d(TAG, "onCreate(): found calling number = " + mCallingNumber);
                Log.d(TAG, "onCreate(): now text/SMS " + getOwnPhoneNumber(getApplicationContext()) + " to " + mCallingNumber);
                //End if
                final boolean sendSMS = false;
                if (sendSMS) {
                    // needs <uses-permission android:name="android.permission.SEND_SMS"/> in AndroidManifest
                    SmsManager sms = SmsManager.getDefault();
                    PendingIntent sentIntent = null;
                    PendingIntent deliveryIntent = null;
                    sms.sendTextMessage(mCallingNumber, "", getOwnPhoneNumber(getApplicationContext()), sentIntent, deliveryIntent );   // http://developer.android.com/reference/android/telephony/SmsManager.html
                    String info = "\u2709 '" + getOwnPhoneNumber(getApplicationContext()) + "' \u27A0  \u260F" + mCallingNumber;  // ? ? ?  (U+2709 U+27A0 U260F)
                    Toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();
                }
                else {
                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setType("vnd.android-dir/mms-sms");
                    smsIntent.putExtra("address", mCallingNumber);
                    smsIntent.putExtra("sms_body", getOwnPhoneNumber(getApplicationContext()));
                    startActivity(smsIntent);
                    // TODO: check if works for all API levels  "Be aware, this will not work for android 4.4 and probably up... "vnd.android-dir/mms-sms" is not longer supported, Max Ch Jan 9 '14 at 18:32 - http://stackoverflow.com/questions/2372248/launch-sms-application-with-an-intent"
                    // see below: sendSms()
                    /**
                        To start the SMS app with number populated use action ACTION_SENDTO:
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("smsto:" + Uri.encode(phoneNumber)));
                            startActivity(intent);
                        This will work on Android 4.4. It should also work on earlier versions of Android however as the APIs were never public the behavior might vary.
                        If you didn't have issues with your prior method I would probably just stick to that pre-4.4 and use ACTION_SENDTO for 4.4+.
                        http://stackoverflow.com/questions/19853220/android4-4-can-not-handle-sms-intent-with-vnd-android-dir-mms-sms
                    */
                }
                Log.d(TAG, "onCreate(): cancel Notification " + DisplayNotification.NOTIFICATION_ID);
                cancelNotification(getApplicationContext(), DisplayNotification.NOTIFICATION_ID);
                Log.d(TAG, "onCreate(): removeExtra('CallingNumber')");
                getIntent().removeExtra("CallingNumber");
            }
        }

        final EditText myTextBox = (EditText) findViewById(R.id.MyNumber);
        myTextBox.setText(getOwnPhoneNumber(getApplicationContext()));
        // http://stackoverflow.com/questions/22679700/android-how-to-get-phone-number-from-the-dual-sim-phone

        myTextBox.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "addTextChangedListener(): enter beforeTextChanged()");
            }
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "addTextChangedListener(): enter afterTextChanged()");
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TextView myOutputBox = (TextView) findViewById(R.id.myOutputBox);
                // myOutputBox.setText(s);
                Log.d(TAG, "addTextChangedListener(): enter onTextChanged()");
            }
        });
        Log.d(TAG, "onCreate(): leave");
    }

    public static String getOwnPhoneNumber(Context context) {
        if (ownPhoneNumber.equals("")) {
            // http://www.mysamplecode.com/2012/06/android-edittext-text-change-listener.html
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            setOwnPhoneNumber(tMgr.getLine1Number());
            if ((null != ownPhoneNumber) && (ownPhoneNumber.length() > 2)) {
                // ownPhoneNumber = ownPhoneNumber.substring(2);
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {  // LOLLIPOP = 21
                    setOwnPhoneNumber(PhoneNumberUtils.formatNumber(ownPhoneNumber));
                } else {
                    String formattedNumber = PhoneNumberUtils.formatNumber(ownPhoneNumber, getUserCountry(context.getApplicationContext()));
                    if (formattedNumber != null) setOwnPhoneNumber(formattedNumber);
                }
            } else {
            /*
            http://stackoverflow.com/questions/2480288/programmatically-obtain-the-phone-number-of-the-android-phone
            Query all the INBOX folder SMS by sms provider and get the "TO" numbers or the SENT folder - "FROM" numbers.
            Extra benefits of this trick: 1. you can get all the line numbers if there is multi sim in the device.
            You will get all the sim numbers ever used in the device, check time frame (sms received or sent only today) etc.
             */
                setOwnPhoneNumber(context.getString(R.string.UnknownPhoneNumber));
            }
        }
        return ownPhoneNumber;
    }

    private static void setOwnPhoneNumber(String ownPhoneNumber) {
        MainActivity.ownPhoneNumber = ownPhoneNumber;
    }

    /**
     * http://stackoverflow.com/questions/3659809/where-am-i-get-country
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    private static String getUserCountry(Context context) {
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) { // SIM country code is available
                return simCountry.toUpperCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA) { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) { // network country code is available
                    return networkCountry.toUpperCase(Locale.US);
                }
            }
        }
        catch (Exception e) {
            Log.d(TAG, "getUserCountry(): exception caught " + e.toString());
        }
        return null;
    }

    // http://stackoverflow.com/questions/20079047/android-kitkat-4-4-hangouts-cannot-handle-sending-sms-intent
    public static boolean sendSms(Context context, String text, String number) {
        return sendSms(context, text, Collections.singletonList(number));
    }
    public static boolean sendSms(Context context, String text, List<String> numbers) {

        String numbersStr = TextUtils.join(",", numbers);

        Uri uri = Uri.parse("sms:" + numbersStr);

        Intent intent = new Intent();
        intent.setData(uri);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.putExtra("sms_body", text);
        intent.putExtra("address", numbersStr);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_SENDTO);
            String defaultSmsPackageName = Telephony.Sms.getDefaultSmsPackage(context);
            if(defaultSmsPackageName != null) {
                intent.setPackage(defaultSmsPackageName);
            }
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setType("vnd.android-dir/mms-sms");
        }

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void cancelNotification(Context context, int notifyId) {
        String notificationService = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(notificationService);
        notificationManager.cancel(notifyId);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

