package edu.psu.grovermodule.handler;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;

import edu.psu.grovermodule.pojo.GPS;

import static android.content.Context.LOCATION_SERVICE;

/*
Project: PSA Grover Vehicle
Feature: Location Handler
Course: IST 440w Section 1 Fall 2019
Date Developed: 3/14/19
Last Date Changed: 4/20/2019
Rev: 1
*/

public class LocationHandler {
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private GPS gps;
    private Context mContext;
    private Fragment mFragment;

    /**
     * Pass in the context and fragment
     *
     * @param context  context
     * @param fragment fragment
     */
    public LocationHandler(Context context, Fragment fragment) {
        mContext = context;
        mFragment = fragment;
    }

    /**
     * @return gps
     */
    public GPS getGps() {
        return gps;
    }

    /**
     * remove location resource
     */
    public void removeUpdates() {
        mLocationManager.removeUpdates(mLocationListener);
        mContext = null;
        mFragment = null;
        log("removeUpdates called");
    }

    /**
     * initializes GPS services and sets it to GPS POJO
     */
    public void initGPS() {
        try {
            mLocationManager =
                    (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    log("onLocationChanged called");
                    gps = new GPS();
                    gps.setLongitude(location.getLongitude());
                    gps.setLatitude(location.getLatitude());
                    gps.setAltitude(location.getAltitude());
                    gps.setSpeed(location.getSpeed());
                    gps.setBearing(location.getBearing());
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };

            requestLocationPermission();

        } catch (SecurityException e) {
            log(e.getMessage());
        }
    }

    /**
     * request for location
     */
    public void requestLocation() {
        try {
            log("requestLocation called");
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    2000,
                    0,
                    mLocationListener);
        } catch (SecurityException ex) {
            log(ex.getMessage());
        }
    }

    /**
     * request for location permission
     */
    public void requestLocationPermission() {
        log("requestLocationPermission called");
        if ((ActivityCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
                &&
                (ActivityCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mFragment.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.INTERNET},
                        10);
            }
        }
    }

    /**
     * Logger
     *
     * @param s string
     */
    private void log(String s) {
        Log.i(getClass().getSimpleName(), s);
    }


}
