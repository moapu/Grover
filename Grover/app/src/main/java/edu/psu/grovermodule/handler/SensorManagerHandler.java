package edu.psu.grovermodule.handler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.Objects;

/*
Project: PSA Grover Vehicle
Feature: Sensor Manger
Course: IST 440W Section 1 Fall 2019
Date Developed: 3/11/19
Date Last Changed: 3/12/2019
Rev: 1
Author(s): Mostafa Apu
*/
public class SensorManagerHandler {
    private SensorManager sensorManager;

    /**
     * Register a single sensor
     *
     * @param sensor              the sensor ID int
     * @param context             the activity context
     * @param sensorEventListener the sensor listener
     */
    private void registerSensor(int sensor, Context context,
                                SensorEventListener sensorEventListener) {
        sensorManager =
                (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensorName = Objects.requireNonNull(sensorManager).getDefaultSensor(sensor);
        sensorManager.registerListener(sensorEventListener, sensorName,
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    /**
     * Register the sensor event listeners
     *
     * @param context             the activity context
     * @param sensorEventListener the sensor listener
     */
    public void register(Context context, SensorEventListener sensorEventListener) {
        if (sensorManager == null) {
            // getDefaultSensor the sensors and register
            registerSensor(Sensor.TYPE_ACCELEROMETER, context, sensorEventListener);
            registerSensor(Sensor.TYPE_GYROSCOPE, context, sensorEventListener);
            registerSensor(Sensor.TYPE_MAGNETIC_FIELD, context, sensorEventListener);
            registerSensor(Sensor.TYPE_PRESSURE, context, sensorEventListener);
            registerSensor(Sensor.TYPE_LIGHT, context, sensorEventListener);
            registerSensor(Sensor.TYPE_RELATIVE_HUMIDITY, context, sensorEventListener);

            // log
            log("registered phone sensors");
        }
    }

    /**
     * Gets sensor manager.
     *
     * @return the sensor manager instance
     */
    public SensorManager getSensorManager() {
        return sensorManager;
    }

    /**
     * logger for this class.
     */
    private void log(String s) {
        Log.i(getClass().getSimpleName(), s);
    }
}
