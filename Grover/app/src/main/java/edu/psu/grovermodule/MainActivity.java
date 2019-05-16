package edu.psu.grovermodule;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.Toast;

import edu.psu.grovermodule.fragment.FragmentDashboard;
import edu.psu.grovermodule.fragment.FragmentDiagnostics;
import edu.psu.grovermodule.fragment.FragmentMission;
import edu.psu.grovermodule.fragment.FragmentSettings;

/**
 * Project: PSA Grover Vehicle
 * Feature: switches fragments
 * Course: IST 440w Section 1 Fall 2019
 * Date Developed: 3/11/19
 * Last Date Changed: 3/17/2019
 * Rev: 2
 */
public class MainActivity extends AppCompatActivity implements
        FragmentDashboard.OnFragmentInteractionListener,
        FragmentDiagnostics.OnFragmentInteractionListener,
        FragmentMission.OnFragmentInteractionListener,
        FragmentSettings.OnFragmentInteractionListener {

    private static final int SCREEN_OFF_TIMEOUT_IN_MILL = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpNavigationView();

        // set the home page as dashboard view
        replaceFragment(new FragmentDashboard());

        if (canWriteToSystemPermission()) {
            systemScreenOffTimeout();
        } else {
            notifyUserToModifySystemSettings();
            openSystemSettingsToModify();
            notifyUserPermissionEnabled();
        }
    }

    private void setUpNavigationView() {
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelected);
    }

    /**
     * replace with a new Fragment
     *
     * @param fragment passes a fragment
     */
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.fragmentLayout, fragment)
                .commit();
    }

    private boolean canWriteToSystemPermission() {
        return Settings.System.canWrite(getApplicationContext());
    }

    private void systemScreenOffTimeout() {
        Settings.System.getInt(getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, SCREEN_OFF_TIMEOUT_IN_MILL);
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,
                SCREEN_OFF_TIMEOUT_IN_MILL);
    }

    private void notifyUserToModifySystemSettings() {
        Toast toast = Toast.makeText(this,
                "This app requires permission to write system settings. Please enable.",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void openSystemSettingsToModify() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        startActivity(intent);
    }

    private void notifyUserPermissionEnabled() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("This app now has permission to write system settings.");
        alertDialog.show();
    }

    // replace fragment when clicked on different navigation item
    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelected
            = item ->
    {
        switch (item.getItemId()) {
            case R.id.navigation_dashboard:
                replaceFragment(new FragmentDashboard());
                return true;
            case R.id.navigation_diagnostics:
                replaceFragment(new FragmentDiagnostics());
                return true;
            case R.id.navigation_mission:
                replaceFragment(new FragmentMission());
                return true;
            case R.id.navigation_settings:
                replaceFragment(new FragmentSettings());
                return true;
        }
        return false;
    };

    @Override
    public void onFragmentInteraction(Uri uri) {}
}
