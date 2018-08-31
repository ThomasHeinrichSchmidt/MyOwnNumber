/*
 * Created by Thomas H. Schmidt, Linden on 10.07.15 09:02
 * Copyright (c) 2018 . No rights reserved - have fun!
 * Last change: 26.10.17 20:48
 *
 */

package de.thschmidt.myownnumber;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.*;
import static java.lang.Integer.signum;


public class MainActivity extends AppCompatActivity{

    // http://stackoverflow.com/questions/17371470/changing-ic-launcher-png-in-android-studio

    private static String ownPhoneNumber = "";
    private static String TAG = MainActivity.class.getSimpleName();
    private static boolean suggestSendingSMStoCallee;
    private static boolean requiredPermissionREAD_PHONE_STATEwasGranted;
    private static boolean requiredPermissionPROCESS_OUTGOING_CALLSwasGranted;

    public static boolean isSuggestSendingSMStoCallee() {
        return MainActivity.suggestSendingSMStoCallee;
    }
    public static void setSuggestSendingSMStoCallee(boolean suggestSendingSMStoCallee) {
        MainActivity.suggestSendingSMStoCallee = suggestSendingSMStoCallee;
    }
    public static boolean isRequiredPermissionREAD_PHONE_STATEgranted() {
        Log.d(TAG, "isRequiredPermissionREAD_PHONE_STATEgranted() = " + requiredPermissionREAD_PHONE_STATEwasGranted);
        return MainActivity.requiredPermissionREAD_PHONE_STATEwasGranted;
    }
    public static void setRequiredPermissionREAD_PHONE_STATEgranted(boolean requiredPermissionREAD_PHONE_STATEwasGranted) {
        Log.d(TAG, "setRequiredPermissionREAD_PHONE_STATEgranted() = " + requiredPermissionREAD_PHONE_STATEwasGranted);
        MainActivity.requiredPermissionREAD_PHONE_STATEwasGranted = requiredPermissionREAD_PHONE_STATEwasGranted;
    }
    public static boolean isRequiredPermissionPROCESS_OUTGOING_CALLgranted() {
        Log.d("PROCESS_OUTGOING_CALL()", " = " + requiredPermissionPROCESS_OUTGOING_CALLSwasGranted);
        return MainActivity.requiredPermissionPROCESS_OUTGOING_CALLSwasGranted;
    }
    public static void setRequiredPermissionPROCESS_OUTGOING_CALLSwasGranted(boolean requiredPermissionPROCESS_OUTGOING_CALLSwasGranted) {
        TAG = "setRequiredPermissionPROCESS_OUTGOING_CALLSwasGranted()";
        Log.d(TAG, " = " + requiredPermissionPROCESS_OUTGOING_CALLSwasGranted);
        MainActivity.requiredPermissionPROCESS_OUTGOING_CALLSwasGranted = requiredPermissionPROCESS_OUTGOING_CALLSwasGranted;
    }

    public static final int READ_PHONE_STATE_ID = 8372;
    public static final int PROCESS_OUTGOING_CALLS_ID = 1471;

    // TODO: conditionally remove LOG.d() in release build by using private static final boolean Debug

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TAG = this.toString();
        Log.d(TAG, "onCreate(): enter");
        super.onCreate(savedInstanceState);
        // TODO: do not show activity if called from notification, just send SMS
        setContentView(R.layout.activity_main);

        checkAndSetRequiredPermissions();

        Log.d(TAG, "onCreate(): ownPhoneNumber = '" + getOwnPhoneNumber(getApplicationContext()) + "'");
        // retrieve calling number, if activity was started by Notification, i.e. a calling number has been set
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d(TAG, "onCreate(): found extras!");
            String mCallingNumber;
            if (android.os.Build.VERSION.SDK_INT < 21) {
                mCallingNumber = (String) extras.get("CallingNumber");
            }
            else {
                mCallingNumber = extras.getString("CallingNumber", "");
            }
            if (mCallingNumber != null && !mCallingNumber.equals("")) {
                Log.d(TAG, "onCreate(): found calling number = " + mCallingNumber);
                Log.d(TAG, "onCreate(): now text/SMS " + getOwnPhoneNumber(getApplicationContext()) + " to " + mCallingNumber);
                //End if
                final boolean sendSMSautomatically = false;
                //noinspection ConstantConditions
                if (sendSMSautomatically) {
                    // needs <uses-permission android:name="android.permission.SEND_SMS"/> in AndroidManifest
                    SmsManager sms = SmsManager.getDefault();
                    PendingIntent sentIntent = null;
                    PendingIntent deliveryIntent = null;
                    sms.sendTextMessage(mCallingNumber, "", getOwnPhoneNumber(getApplicationContext()), sentIntent, deliveryIntent );   // http://developer.android.com/reference/android/telephony/SmsManager.html
                    String info = "\u2709 '" + getOwnPhoneNumber(getApplicationContext()) + "' \u27A0  \u260F" + mCallingNumber;  // ? ? ?  (U+2709 U+27A0 U260F)
                    Toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();
                }
                else if (isSuggestSendingSMStoCallee()) {
                    Log.d(TAG, "onCreate(): text/SMS suggested due to menu setting suggestSendingSMStoCallee = " + isSuggestSendingSMStoCallee());
                    Intent smsIntent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // https://stackoverflow.com/questions/37111922/how-to-open-sms-app-via-implicit-intent
                        smsIntent = new Intent(Intent.ACTION_SENDTO);
                        smsIntent.setData(Uri.parse("smsto:" + Uri.encode(mCallingNumber)));
                        smsIntent.putExtra("sms_body", getOwnPhoneNumber(getApplicationContext()));
                    }
                    else {
                        smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        smsIntent.putExtra("address", mCallingNumber);
                        smsIntent.putExtra("sms_body", getOwnPhoneNumber(getApplicationContext()));
                    }
                    try {
                        if (smsIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(smsIntent);
                        }
                        Log.d(TAG, "onCreate(): Finished sending SMS...");
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(getApplicationContext(), "Text/SMS failed.",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Log.d(TAG, "onCreate(): did not text/SMS due to menu setting suggestSendingSMStoCallee" + isSuggestSendingSMStoCallee());
                }
                Log.d(TAG, "onCreate(): cancel Notification " + DisplayNotification.NOTIFICATION_ID);
                cancelNotification(getApplicationContext(), DisplayNotification.NOTIFICATION_ID);
                Log.d(TAG, "onCreate(): removeExtra('CallingNumber')");
                getIntent().removeExtra("CallingNumber");
            }
        }

        final EditText myTextBox = (EditText) findViewById(R.id.MyNumber);
        myTextBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TAG = this.toString();
                String number = String.valueOf(myTextBox.getText());
                Log.d(TAG, "onClick(): copy number = '" + number + "' to clipboard");
                MyClipboardManager clipboard = new MyClipboardManager();
                clipboard.copyToClipboard(getApplicationContext(), number);
                ToastToClipboard(number);
            }
        });
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
        restoreSettingSuggestSendingSMStoCallee();
        Log.d(TAG, "onCreate(): leave");
    }

    private void restoreSettingSuggestSendingSMStoCallee() {
        TAG = "restoreSettingSuggestSendingSMStoCallee(): ";
        // Restore preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        Map<String,?> keys = settings.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d(TAG + " pref value", entry.getKey() + " = " +  entry.getValue().toString());
        }
        setSuggestSendingSMStoCallee(settings.getBoolean("suggestSendingSMStoCallee", true));
        Log.d(TAG, "set suggestSendingSMStoCallee := " + isSuggestSendingSMStoCallee());
    }

    public void checkAndSetRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // https://stackoverflow.com/questions/38536970/getting-java-lang-securityexception-when-requesting-for-sim-info
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                setRequiredPermissionREAD_PHONE_STATEgranted(true);
            }
            else {
                Log.d(TAG, "onCreate(): permission.READ_PHONE_STATE not granted");
                setRequiredPermissionREAD_PHONE_STATEgranted(false);
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_ID);
            }
            if (checkSelfPermission(Manifest.permission.PROCESS_OUTGOING_CALLS)  == PackageManager.PERMISSION_GRANTED) {
                setRequiredPermissionPROCESS_OUTGOING_CALLSwasGranted(true);
            }
            else {
                Log.d(TAG, "onCreate(): permission.PROCESS_OUTGOING_CALLS not granted");
                setRequiredPermissionPROCESS_OUTGOING_CALLSwasGranted(false);
                requestPermissions(new String[]{Manifest.permission.PROCESS_OUTGOING_CALLS}, PROCESS_OUTGOING_CALLS_ID);
            }
        }
        else  {
            setRequiredPermissionREAD_PHONE_STATEgranted(true);
            setRequiredPermissionPROCESS_OUTGOING_CALLSwasGranted(true);
        }
        restoreSettingSuggestSendingSMStoCallee();
        SavePreferences();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult(requestCode=" + requestCode + ", permissions=" + permissions + ", grantResults=" + grantResults);
        if (READ_PHONE_STATE_ID == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "requestCode READ_PHONE_STATE_ID hit ==> requiredPermissionREAD_PHONE_STATEwasGranted := true");
                setRequiredPermissionREAD_PHONE_STATEgranted(true);
                final EditText myTextBox = (EditText) findViewById(R.id.MyNumber);
                myTextBox.setText(getOwnPhoneNumber(getApplicationContext()));
            } else {
                Log.d(TAG, "requestCode READ_PHONE_STATE_ID hit ==> requiredPermissionREAD_PHONE_STATEwasGranted := false");
                setRequiredPermissionREAD_PHONE_STATEgranted(false);
            }
        }
        if (PROCESS_OUTGOING_CALLS_ID == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "requestCode PROCESS_OUTGOING_CALLS_ID hit ==> requiredPermissionPROCESS_OUTGOING_CALLSwasGranted := true");
                setRequiredPermissionPROCESS_OUTGOING_CALLSwasGranted(true);
            } else {
                Log.d(TAG, "requestCode PROCESS_OUTGOING_CALLS_ID hit ==> requiredPermissionPROCESS_OUTGOING_CALLSwasGranted := false");
                setRequiredPermissionPROCESS_OUTGOING_CALLSwasGranted(false);
            }
        }
    }
    private void ToastToClipboard(String number) {
        String info = number + " \u27A0 \uD83D\uDCCB"; // ➠ clipboard   ↪ = \u21AA
        // show own number to improve user memory
        Log.d(TAG, "ToastToClipboard():    Toast " + info.replace('\n',' '));
        Toast.makeText(getApplicationContext(), info, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop(){
        SavePreferences();
        super.onStop();
        Log.d(TAG, "onStop(): commit changes to suggestSendingSMStoCallee := " + isSuggestSendingSMStoCallee());
    }

    private void SavePreferences() {
        // We need an Editor object to make preference changes.
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("suggestSendingSMStoCallee", isSuggestSendingSMStoCallee());
        editor.putBoolean("requiredPermissionREAD_PHONE_STATEwasGranted", isRequiredPermissionREAD_PHONE_STATEgranted());
        editor.putBoolean("requiredPermissionPROCESS_OUTGOING_CALLSwasGranted", isRequiredPermissionPROCESS_OUTGOING_CALLgranted());
        editor.commit();
        Map<String,?> keys = settings.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d(TAG, "SavePreferences(): pref value" + entry.getKey() + ": " +  entry.getValue().toString());
        }
        Log.d(TAG, "SavePreferences(): finished");
    }

    public static String getOwnPhoneNumber(Context context) {
        if (ownPhoneNumber.equals("") || ownPhoneNumber.equals(context.getString(R.string.UnknownPhoneNumber))) {
            // http://www.mysamplecode.com/2012/06/android-edittext-text-change-listener.html
            if (isRequiredPermissionREAD_PHONE_STATEgranted()) {
                TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                setOwnPhoneNumber(context, tMgr.getLine1Number());
            }
            if ((null != ownPhoneNumber) && (ownPhoneNumber.length() > 2) && !ownPhoneNumber.equals(context.getString(R.string.UnknownPhoneNumber))) {
                // ownPhoneNumber = ownPhoneNumber.substring(2);
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {  // LOLLIPOP = 21
                    //noinspection deprecation
                    setOwnPhoneNumber(context, PhoneNumberUtils.formatNumber(ownPhoneNumber));
                } else {
                    String formattedNumber = PhoneNumberUtils.formatNumber(ownPhoneNumber, getUserCountry(context.getApplicationContext()));
                    if (formattedNumber != null) setOwnPhoneNumber(context, formattedNumber);
                }
            } else {
            /*
            http://stackoverflow.com/questions/2480288/programmatically-obtain-the-phone-number-of-the-android-phone
            Query all the INBOX folder SMS by sms provider and get the "TO" numbers or the SENT folder - "FROM" numbers.
            Extra benefits of this trick: 1. you can get all the line numbers if there is multi sim in the device.
            You will get all the sim numbers ever used in the device, check time frame (sms received or sent only today) etc.
             */
                setOwnPhoneNumber(context, context.getString(R.string.UnknownPhoneNumber));
            }
        }
        return ownPhoneNumber;
    }

    private static void setOwnPhoneNumber(Context context, String ownPhoneNumber) {
        String _ownPhoneNumber = new String(ownPhoneNumber) ;
        Log.d("setOwnPhoneNumber(): ", "_ownPhoneNumber = " + _ownPhoneNumber);
        if (_ownPhoneNumber != null && !_ownPhoneNumber.equals("")) {
            // remove *31# or (better) use number starting from + or 00 or 0
            Pattern p = Pattern.compile("\\+?[- 0-9]{3,}$");  // rightmost string of at least 3 digits or blanks optionally preceded by a +
            Matcher m = p.matcher(_ownPhoneNumber);
            if (m.find()) {
                try {
                    _ownPhoneNumber = m.group();
                } catch (Exception e) {
                }
            }
            _ownPhoneNumber = getMemoPhoneNumber(_ownPhoneNumber);
        }
        else {
            _ownPhoneNumber =  context.getString(R.string.UnknownPhoneNumber);
        }
        MainActivity.ownPhoneNumber = _ownPhoneNumber;
    }

    @NonNull
    public static String getMemoPhoneNumber(String ownPhoneNumber) {
        // group number to enhance memorization (equal digits, inc/dec digits)
        if (ownPhoneNumber == null) return ownPhoneNumber;
        if (ownPhoneNumber.length() < 3) return ownPhoneNumber;

        StringBuilder memoPhoneNumber = new StringBuilder();
        int minimalRunCount = 3;
        int total = ownPhoneNumber.length();
        int index = 0;
        char nextc = 0;
        int emergencyExitCount = 0;
        while (index < total - 1) {
            if (emergencyExitCount++ > total) return ownPhoneNumber;
            char c = ownPhoneNumber.charAt(index);
            nextc = ownPhoneNumber.charAt(index+1);
            int equalDigitCount = (c == nextc) ? 1 : 0;
            int incrementDigitCount = (+1 + (int) c == (int) nextc) ? 1 : 0;
            int decrementDigitCount = (-1 + (int) c == (int) nextc) ? 1 : 0;
            if (BuildConfig.DEBUG && (equalDigitCount+incrementDigitCount+decrementDigitCount > 1)) throw new AssertionError("getMemoPhoneNumber() at most one Count must exist.");
            if (equalDigitCount+incrementDigitCount+decrementDigitCount == 0 || index + 2 >= total) {  // no run found or last 2 chars in number
                memoPhoneNumber.append(c);
                index++;
                continue;
            }
            for (int i = index + 2; i < total ; i++) {
                c = nextc;
                nextc = ownPhoneNumber.charAt(i);
                boolean runover = false;
                if (equalDigitCount > 0 && c == nextc) equalDigitCount++;
                else if (incrementDigitCount > 0 && (+1 + (int) c == (int) nextc)) incrementDigitCount++;
                else if (decrementDigitCount > 0 && (-1 + (int) c == (int) nextc)) decrementDigitCount++;
                else runover = true;
                if (BuildConfig.DEBUG && (signum(equalDigitCount)+signum(incrementDigitCount)+signum(decrementDigitCount) > 1)) throw new AssertionError("getMemoPhoneNumber() only one Count greater 0 must exist.");
                if (runover || i == total - 1) {
                    if (equalDigitCount + incrementDigitCount + decrementDigitCount + 1 >= minimalRunCount) {
                        // format run
                        memoPhoneNumber.append(" " + ownPhoneNumber.substring(index, i));
                        // add space if at least two chars left  OR   last char does not fit to run
                        if (i < total - 1 || (i == total - 1 && runover)) memoPhoneNumber.append(" ");
                        index = i;
                    }
                    else {
                        memoPhoneNumber.append(     ownPhoneNumber.substring(index, i - 1)       );
                        index = i - 1;
                    }
                    break;
                }
            }
        }
        memoPhoneNumber.append(nextc);
        // remove superfluous spaces
        memoPhoneNumber = new StringBuilder(memoPhoneNumber.toString().replaceAll(" -", "-"));
        memoPhoneNumber = new StringBuilder(memoPhoneNumber.toString().replaceAll("- ", "-"));
        // trim space: left, right, and multiple
        return memoPhoneNumber.toString().trim().replaceAll(" +", " ");
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
    @SuppressWarnings("unused")
    public static boolean sendSms(Context context, String text, String number) {
        return sendSms(context, text, Collections.singletonList(number));
    }
    private static boolean sendSms(Context context, String text, List<String> numbers) {

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

    @SuppressWarnings("SameParameterValue")
    private static void cancelNotification(Context context, int notifyId) {
        String notificationService = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(notificationService);
        notificationManager.cancel(notifyId);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // set suggest Sending SMS to Callee from SharedPreferences
        restoreSettingSuggestSendingSMStoCallee();
        MenuItem sms = menu.findItem(R.id.action_suggestSendingSMStoCallee);
        if (sms != null && sms.isCheckable()) {
            sms.setChecked(isSuggestSendingSMStoCallee());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case  R.id.action_copy:
                Log.d(TAG, "onOptionsItemSelected(" + id + "): ownPhoneNumber = '" + getOwnPhoneNumber(getApplicationContext()) + "'");
                MyClipboardManager clipboard = new MyClipboardManager();
                clipboard.copyToClipboard(getApplicationContext(), getOwnPhoneNumber(getApplicationContext()));
                ToastToClipboard(getOwnPhoneNumber(getApplicationContext()));
                return true;
            case R.id.action_suggestSendingSMStoCallee:
                if (item.isChecked()) {
                    setSuggestSendingSMStoCallee(false);  // erase pending notification
                    cancelNotification(getApplicationContext(), DisplayNotification.NOTIFICATION_ID);
                }
                else {
                    setSuggestSendingSMStoCallee(true);
                }
                item.setChecked(isSuggestSendingSMStoCallee());
                SavePreferences();
                Log.d(TAG, "onOptionsItemSelected(" + id + "): suggestSendingSMStoCallee := '" + isSuggestSendingSMStoCallee() + "'");
                return true;
            case R.id.action_link_policy:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.t-h-schmidt.de/myownnumber/privacy-policy/privacy-policy.html"));
                Log.d(TAG, "onOptionsItemSelected(" + id + "): link_policy --> privacy-policy.html");
                startActivity(browserIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

