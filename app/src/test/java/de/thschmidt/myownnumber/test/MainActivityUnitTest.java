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

    @Before
    public void setUp() throws Exception {
        String prefixNumber = "*31#";
    }
    @Test
    public void emailValidator_CorrectEmailSimple_ReturnsTrue() {

        assertEquals("+49 1521234 777", de.thschmidt.myownnumber.MainActivity.getMemoPhoneNumber("+49 1521234777") );
        assertEquals("0049 18343246", de.thschmidt.myownnumber.MainActivity.getMemoPhoneNumber("0049 18343246"));
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