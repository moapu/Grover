package edu.psu.grovermodule;

import android.app.Activity;
import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.psu.grovermodule.MainActivity;
import edu.psu.grovermodule.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class FragmentDashboardInstrumentedTest {

    //This starts the app before running any test
    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    //Test to see if textview is displaying
    @Test
    public void checkingUITextDashboard(){
        onView(ViewMatchers.withId(R.id.dashboard_header)).check(matches(isDisplayed()));
        onView(withId(R.id.batteryLevel)).check(matches(isDisplayed()));
        onView(withId(R.id.progressBar)).check(matches(isDisplayed()));
        onView(withId(R.id.wifiText)).check(matches(isDisplayed()));
        onView(withId(R.id.cellText)).check(matches(isDisplayed()));
        onView(withId(R.id.speedText)).check(matches(isDisplayed()));
    }

    //Standard UIQuickTest
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
