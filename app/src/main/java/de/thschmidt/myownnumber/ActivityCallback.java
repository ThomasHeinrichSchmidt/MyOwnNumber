package de.thschmidt.myownnumber;

/**
 * Created by Thomas H. Schmidt on 03.07.2015.
 */

// declare an interface where you could have an other class MyPhoneReceiver to communicate to Activity, calling it ActivityCallback
// http://stackoverflow.com/questions/10996479/how-to-update-a-textview-of-an-activity-from-another-class

public interface ActivityCallback {
        // Declaration of the template functions for the interface
        public void UpdateOwnNumber(String number);
        public String GetOwnNumber();
}

