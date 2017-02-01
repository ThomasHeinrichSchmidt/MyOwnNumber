/*
 * Copyright (c) 2017 by Thomas H. Schmidt, Linden - have fun!
 */

package de.thschmidt.myownnumber.test;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

// https://developer.android.com/training/testing/unit-testing/local-unit-tests.html
public class MainActivityUnitTest {

    String testnumber[][] = {
            { "44", "44"},
            { "+46", "+46"},
            {"+49 152 1234 777", "+49 1521234777"},
            {"+48 152 1234 777", "+48 152 1234777"},
            {"0047 183 43246", "0047 183 43246"},
            {"0171 6826 4567", "0171 68264567"},
            {"0046 845 45678 87", "0046 8454567887"},
            {"+45 121 6666 193", "+45 1216666193"},
            {"+4401768 345678", "+4401768345678"},
            {"07293 22 6666", "07293 226666"},            // 07### ######   UK mobile number format
            {"+44 7911 11 3456", "+44 7911 113456"},
            {"+1 302 1234567", "+1 302 1234567"},
            {"+39 211 5432", "+39 211 5432"},
            // https://libphonenumber.appspot.com/       samples see http://wikitravel.org/en/Wikitravel:Phone_numbers
            {"+414466 1111 0", "+41446611110"},
            {"8 (840) 123-45-67", "8 (840) 123-45-67"}   // Phone Number entered: +7 840 123-45-67  , defaultCountry entered: RU,  Language entered: ru-RU

            // list of formatted telephone numbers from different countries
    };

    @Before
    public void setUp() throws Exception {
        String prefixNumber = "*31#";
    }
    @Test
    public void emailValidator_CorrectEmailSimple_ReturnsTrue() {
        // format numbers, country codes GB, DE    https://www.iso.org/obp/ui/#search/code/
        // http://libphonenumber.appspot.com/
        //
        assertEquals(null, de.thschmidt.myownnumber.MainActivity.getMemoPhoneNumber(null) );
        assertEquals("", de.thschmidt.myownnumber.MainActivity.getMemoPhoneNumber("") );

        for (int i=0; i < testnumber.length; i++) {
            assertEquals(testnumber[i][0], de.thschmidt.myownnumber.MainActivity.getMemoPhoneNumber(testnumber[i][1]) );
        }
    }
    // ...

    /*
    @Test
    public void settersWorkFineOnTheJvm() throws Exception {
        assertEquals("Ted", testUser.getName());
        testUser.setName("Tom");
        assertEquals("Tom", testUser.getName());
    }
    */
}