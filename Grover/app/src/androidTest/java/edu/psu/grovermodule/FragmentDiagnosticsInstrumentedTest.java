package edu.psu.grovermodule;

import android.os.RemoteException;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FragmentDiagnosticsInstrumentedTest {

    //This starts the app before running any test
    @Rule
    public ActivityTestRule<MainActivity> activityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    //Use UIAutomator here to switch to different activities in ui then use esspresso to check
    //values, text boxes, etc.

    //Test to see if textview is displaying
    @Test
    public void checkingUITextDashboard(){
        onView(withId(R.id.navigation)).perform(click());
        onView(withId(R.id.accel_label)).check(matches(isDisplayed()));

    }

}