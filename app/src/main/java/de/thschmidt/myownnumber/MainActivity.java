package de.thschmidt.myownnumber;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
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
import java.util.regex.*;


public class MainActivity extends ActionBarActivity {

    // http://stackoverflow.com/questions/17371470/changing-ic-launcher-png-in-android-studio

    private static String ownPhoneNumber = "";
    private static String TAG = MainActivity.class.getSimpleName();
    private static boolean suggestSendingSMStoCallee;

    public static boolean isSuggestSendingSMStoCallee() {
        return suggestSendingSMStoCallee;
    }
    public static void setSuggestSendingSMStoCallee(boolean suggestSendingSMStoCallee) {
        MainActivity.suggestSendingSMStoCallee = suggestSendingSMStoCallee;
    }

    // TODO: conditionally remove LOG.d() in release build by using private static final boolean Debug

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TAG = this.toString();
        Log.d(TAG, "onCreate(): enter");
        Log.d(TAG, "onCreate(): ownPhoneNumber = '" + getOwnPhoneNumber(getApplicationContext()) + "'");
        super.onCreate(savedInstanceState);
        // TODO: do not show activity if called from notification, just send SMS
        setContentView(R.layout.activity_main);

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

        // Re-Store preferences
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        setSuggestSendingSMStoCallee(settings.getBoolean("suggestSendingSMStoCallee", true));
        Log.d(TAG, "set suggestSendingSMStoCallee := " + isSuggestSendingSMStoCallee());


        Log.d(TAG, "onCreate(): leave");
    }

    @Override
    protected void onStop(){
        TAG = this.toString();
        super.onStop();
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("suggestSendingSMStoCallee", isSuggestSendingSMStoCallee());
        // Commit the edits!
        editor.commit();
        Log.d(TAG, "onStop(): commit changes to suggestSendingSMStoCallee := " + isSuggestSendingSMStoCallee());
    }


    public static String getOwnPhoneNumber(Context context) {
        if (ownPhoneNumber.equals("")) {
            // http://www.mysamplecode.com/2012/06/android-edittext-text-change-listener.html
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            setOwnPhoneNumber(tMgr.getLine1Number());
            if ((null != ownPhoneNumber) && (ownPhoneNumber.length() > 2)) {
                // ownPhoneNumber = ownPhoneNumber.substring(2);
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {  // LOLLIPOP = 21
                    //noinspection deprecation
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
        // remove *31# or (better) use number starting from + or 00 or 0
        Pattern p = Pattern.compile("\\+?[- 0-9]{3,}$");  // rightmost string of at least 3 digits or blanks optionally preceded by a +
        Matcher m = p.matcher(ownPhoneNumber);
        if (m.find()) {
            try {
                ownPhoneNumber = m.group();
            }
            catch (Exception e) { }
        }
        ownPhoneNumber = getMemoPhoneNumber(ownPhoneNumber);
        MainActivity.ownPhoneNumber = ownPhoneNumber;
    }

    @NonNull
    public static String getMemoPhoneNumber1(String ownPhoneNumber) {
        // group number to enhance memorization (equal digits, inc/dec digits)
        String memoPhoneNumber = "";
        int equalDigitCount = 0;
        int incrementDigitCount = 0;
        int decrementDigitCount = 0;
        int minimalRunCount = 3;
        int startOfRun = 0;
        boolean runOver = false;
        char lastc = ownPhoneNumber.charAt(0);
        memoPhoneNumber += lastc;
        for (int i = 1; i < ownPhoneNumber.length(); i++){
            char c = ownPhoneNumber.charAt(i);
            if (lastc == c) {
                if (equalDigitCount == 0) startOfRun = 0;
                equalDigitCount++;
                incrementDigitCount = 0;
                decrementDigitCount = 0;
            }
            if (1 + (int) lastc == (int) c) {
                if (incrementDigitCount == 0) startOfRun = 0;
                incrementDigitCount++;
                equalDigitCount = 0;
                decrementDigitCount = 0;
            }
            if (-1 + (int) lastc == (int) c ) {
                if (decrementDigitCount == 0) startOfRun = 0;
                decrementDigitCount++;
                equalDigitCount = 0;
                incrementDigitCount = 0;
            }
            if (equalDigitCount > 0 || incrementDigitCount > 0 || decrementDigitCount > 0) {
                if (startOfRun == 0) startOfRun = memoPhoneNumber.length() - 1;
                runOver = false;
            }
            else {
                if (equalDigitCount >= minimalRunCount || incrementDigitCount >= minimalRunCount || decrementDigitCount >= minimalRunCount) {
                    if (i < ownPhoneNumber.length() -1) memoPhoneNumber += " ";
                    if (memoPhoneNumber.charAt(startOfRun-1) != ' ') memoPhoneNumber = memoPhoneNumber.substring(0, startOfRun) + " " + memoPhoneNumber.substring(startOfRun, memoPhoneNumber.length());
                }
                runOver = true;
            }
            if (runOver) {
                equalDigitCount = 0;
                incrementDigitCount = 0;
                decrementDigitCount = 0;
                startOfRun = 0;
            }
            memoPhoneNumber += c;
            lastc = c;
        }
        if (equalDigitCount >= minimalRunCount || incrementDigitCount >= minimalRunCount || decrementDigitCount >= minimalRunCount) {
            if (memoPhoneNumber.charAt(startOfRun-1) != ' ') memoPhoneNumber = memoPhoneNumber.substring(0, startOfRun) + " " + memoPhoneNumber.substring(startOfRun, memoPhoneNumber.length());
        }
        ownPhoneNumber = memoPhoneNumber;
        return ownPhoneNumber;
    }

    enum Symbol { NONE, EQUAL, LESS, GREATER, JUMP }
    enum State { START, STARTOFRUN, REPEATING, DESCENDING, ASCENDING, ENDOFRUN, JUMPING, FINAL }
    static State transition[][] = {
            //  NONE         EQUAL            LESS              GREATER          JUMP
    {
    // START
            State.FINAL,  State.REPEATING, State.DESCENDING, State.ASCENDING, State.JUMPING
    }, {
    // STARTOFRUN
            State.FINAL,  State.REPEATING, State.DESCENDING, State.ASCENDING, State.JUMPING
    }, {
    // REPEATING
           State.FINAL, State.REPEATING, State.ENDOFRUN, State.ENDOFRUN, State.JUMPING
    }, {
    // DESCENDING
            State.FINAL, State.ENDOFRUN, State.DESCENDING, State.ENDOFRUN, State.JUMPING
    }, {
    // ASCENDING
            State.FINAL, State.ENDOFRUN, State.ENDOFRUN, State.ASCENDING, State.JUMPING
    }, {
    // ENDOFRUN
            State.FINAL,  State.REPEATING, State.DESCENDING, State.ASCENDING, State.JUMPING
    }, {
    // JUMPING
            State.FINAL,  State.STARTOFRUN, State.STARTOFRUN, State.STARTOFRUN, State.JUMPING
    }, {
    // FINAL
           State.FINAL, State.FINAL, State.FINAL, State.FINAL, State.FINAL
    }
    };


    private static int index = 0;
    private static char c = 0, lastc = 0;
    private static final int  minimalRunCount = 3;

    @NonNull
    public static String getMemoPhoneNumber(String ownPhoneNumber) {
        // group number to enhance memorization (equal digits, inc/dec digits)
        TAG = "static";
        if (ownPhoneNumber == null) return ownPhoneNumber;
        if (ownPhoneNumber.length() < 3) return ownPhoneNumber;

        String memoPhoneNumber = "";
        Symbol symbol = Symbol.NONE;
        State state = State.START;
        StringBuilder buffer = new StringBuilder ();
        int e  = 0, d = 0, a = 0;
        index = 0; c = 0; lastc = 0;

        while (state != State.FINAL) {
            switch(state) {
                case START:
                    // Log.d(TAG, "getMemoPhoneNumber: START ");
                    break;
                case STARTOFRUN:
                    // push back last char from result
                    buffer.append(memoPhoneNumber.charAt(memoPhoneNumber.length()-1));
                    memoPhoneNumber = memoPhoneNumber.substring(0,memoPhoneNumber.length()-2);
                    break;
                case REPEATING:
                    e = buffer.length();
                    break;
                case DESCENDING:
                    d = buffer.length();
                    break;
                case ASCENDING:
                    a = buffer.length();
                    break;
                case ENDOFRUN:
                    //
                    memoPhoneNumber += buffer.charAt(0);
                    buffer.deleteCharAt(0);
                    break;
                case JUMPING:
                    if (e >= minimalRunCount || d >= minimalRunCount || a >= minimalRunCount) {
                        memoPhoneNumber += " " + buffer + " ";
                    }
                    else {
                        memoPhoneNumber += buffer;
                    }
                    e = d = a = 0;
                    buffer.setLength(0);
                    break;
                case FINAL:
                    // done
                    if (BuildConfig.DEBUG) throw new AssertionError("getMemoPhoneNumber() 'FINAL:' can never happen ");
                    break;
                default:
                    if (BuildConfig.DEBUG) throw new AssertionError("getMemoPhoneNumber() 'default:' can never happen ");
                    break;
            }
            symbol = nextSymbol(ownPhoneNumber, buffer);
            state = transition[state.ordinal()][symbol.ordinal()];
        }
        // get remaining numbers from buffer
        memoPhoneNumber += buffer;
        // trim space: left, right, and multiple
        return memoPhoneNumber.trim().replaceAll(" +", " ");
    }

    private static Symbol nextSymbol(String ownPhoneNumber, StringBuilder buffer) {
        if (BuildConfig.DEBUG && ownPhoneNumber.length() < 3) throw new AssertionError("ownPhoneNumber is too short");
        Symbol symbol;
        if (lastc == 0) {
            c = ownPhoneNumber.charAt(index++);
            buffer.append(c);
        }
        lastc = c;
        if (ownPhoneNumber.length() > index) {
            c = ownPhoneNumber.charAt(index++);
            buffer.append(c);
        } else {
            symbol = Symbol.NONE;
            return symbol;
        }
        if (lastc == c) symbol = Symbol.EQUAL;
        else if (+1 + (int) lastc == (int) c) symbol = Symbol.GREATER;
        else if (-1 + (int) lastc == (int) c) symbol = Symbol.LESS;
        else symbol = Symbol.JUMP;
        return symbol;
    }


    /**
     * http://stackoverflow.com/questions/3659809/where-am-i-get-country
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    private static String getUserCountry(Context context) {
        TAG = "static";
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
        TAG = this.toString();

        switch (item.getItemId()) {
            case  R.id.action_copy:
                Log.d(TAG, "onOptionsItemSelected(" + id + "): ownPhoneNumber = '" + getOwnPhoneNumber(getApplicationContext()) + "'");
                MyClipboardManager clipboard = new MyClipboardManager();
                clipboard.copyToClipboard(getApplicationContext(), getOwnPhoneNumber(getApplicationContext()));
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
                Log.d(TAG, "onOptionsItemSelected(" + id + "): suggestSendingSMStoCallee := '" + isSuggestSendingSMStoCallee() + "'");
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

