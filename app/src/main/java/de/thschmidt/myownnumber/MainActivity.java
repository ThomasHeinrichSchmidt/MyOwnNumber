package de.thschmidt.myownnumber;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import java.util.Locale;


// Activity implements ActivityCallback, and hence it's function definitions, see below.
// http://stackoverflow.com/questions/10996479/how-to-update-a-textview-of-an-activity-from-another-class

public class MainActivity extends ActionBarActivity implements ActivityCallback {

    // http://stackoverflow.com/questions/17371470/changing-ic-launcher-png-in-android-studio

    static String mPhoneNumber = "+?? ???-???-????";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // http://www.mysamplecode.com/2012/06/android-edittext-text-change-listener.html
        final EditText myTextBox = (EditText) findViewById(R.id.MyNumber);
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mPhoneNumber = tMgr.getLine1Number();
        Locale mDefaultLocale = Locale.getDefault();
        if (mPhoneNumber != null && mPhoneNumber.length() > 2) {
            // mPhoneNumber = mPhoneNumber.substring(2);
            if (android.os.Build.VERSION.SDK_INT < 21) {
                mPhoneNumber = PhoneNumberUtils.formatNumber(mPhoneNumber);
            }
            else {
                String formattedNumber = PhoneNumberUtils.formatNumber(mPhoneNumber, getUserCountry(getApplicationContext()));
                if (formattedNumber != null) mPhoneNumber = formattedNumber;
            }
        }
        else {
            /*
            http://stackoverflow.com/questions/2480288/programmatically-obtain-the-phone-number-of-the-android-phone
            Query all the INBOX folder SMS by sms provider and get the "TO" numbers or the SENT folder - "FROM" numbers.
            Extra benefits of this trick: 1. you can get all the line numbers if there is multi sim in the device.
            You will get all the sim numbers ever used in the device, check time frame (sms received or sent only today) etc.
             */
            mPhoneNumber = "+?? ???-???-????";
        }
        myTextBox.setText(mPhoneNumber);
        // http://stackoverflow.com/questions/22679700/android-how-to-get-phone-number-from-the-dual-sim-phone

        myTextBox.addTextChangedListener(new TextWatcher() {
             public void afterTextChanged(Editable s) {
                 Log.v("", "inside afterTextChanged");
             }
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                 Log.v("", "inside beforeTextChanged");
             }
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 // TextView myOutputBox = (TextView) findViewById(R.id.myOutputBox);
                 // myOutputBox.setText(s);
                 Log.v("", "inside onTextChanged");
             }
        });
    }

    /**
     * http://stackoverflow.com/questions/3659809/where-am-i-get-country
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public static String getUserCountry(Context context) {
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
        catch (Exception e) { }
        return null;
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


    @Override
    public void UpdateOwnNumber(String number)
    {
        final EditText myTextBox = (EditText) findViewById(R.id.MyNumber);
        myTextBox.setText(number);
    }

    @Override
    public String GetOwnNumber()
    {
        final EditText myTextBox = (EditText) findViewById(R.id.MyNumber);
        return myTextBox.getText().toString();
    }

}

