package edu.psu.grovermodule;

import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.psu.grovermodule.MainActivity;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule = new
            ActivityTestRule<MainActivity>(MainActivity.class);

    @Test
    public void testUiDevice() throws RemoteException {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        if (device.isScreenOn()){
            device.setOrientationLeft();
            device.openNotification();
            device.setOrientationRight();
            device.openNotification();
            device.setOrientationNatural();
        }
    }
}

/*
    @Test
    public void testPressMenuButton(){
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressMenu();
    }

    @Test
    public void testPressBackButton(){
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack();
    }

    @Test
    public void testPressHomeButton(){
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressHome();
    }

*/
    /*
    Notes:
    click(x,y)
    getDisplayHeight() returns the height of the device display in pixels
    getDisplayWidth()  returns the width in pixels
    isNaturalOrientation() returns a bool indicating if the device is oriented in its natural orientation
    isScreenOn() returns if bool if on
    openNotifications() slide open the notification shade
    pressBack(), pressHome(), pressMenu()
    setOrientationLeft()/setOrientationRight() rotate the device left or right
    swipe(x1, y1, x2, y2, steps)
     */


