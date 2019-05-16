package edu.psu.grovermodule.handler;


import android.os.AsyncTask;
import android.util.Log;

import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.module.BarometerBosch;

import bolts.Continuation;
import edu.psu.grovermodule.pojo.Temp;

/*
Project: PSA Grover Vehicle
Feature: mbient sensor (Temperature)
Course: IST 440W Section 1 Fall 2019
Date Developed: 3/1/19
Date Last Changed: 3/12/2019
Rev: 1
*/
public class MbientSensorHandler extends AsyncTask<MetaWearBoard, String, Float> {
    /**
     * Runs Async to get the mbient sensor temperature
     *
     * @param metaWearBoards Meta wear board instance
     */
    @Override
    protected Float doInBackground(MetaWearBoard... metaWearBoards) {
        float value = 0f;

        if (metaWearBoards != null) {
            try {
                MbientSensor ms = new MbientSensor();
                ms.temperature(metaWearBoards[0]);
                Thread.sleep(250);
                value = (float) celsiusToFahrenheit(ms.getTemp());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    private double celsiusToFahrenheit(float celsius) {
        return (9.0 / 5.0) * celsius + 32;
    }

    class MbientSensor {
        private final Temp temp = new Temp();

        /**
         * @return returns the temp in POJO
         */
        float getTemp() {
            return temp.getTemperature();
        }

        /**
         * Mbient sensor API call for temperature
         *
         * @param board meta wear instance
         */
        void temperature(MetaWearBoard board) {
            com.mbientlab.metawear.module.Temperature temperature =
                    board.getModule(com.mbientlab.metawear.module.Temperature.class);
            com.mbientlab.metawear.module.Temperature.Sensor tempSensor =
                    temperature.findSensors(com.mbientlab.metawear.module.Temperature.SensorType.PRESET_THERMISTOR)[0];

            ((com.mbientlab.metawear.module.Temperature.ExternalThermistor) temperature.findSensors(com.mbientlab.metawear.module.Temperature.SensorType
                    .EXT_THERMISTOR)[0])
                    .configure((byte) 0, (byte) 6, false);

            board.getModule(BarometerBosch.class).start();
            temperature.findSensors(com.mbientlab.metawear.module.Temperature.SensorType.BOSCH_ENV)[0].read();

            tempSensor.addRouteAsync(source ->
                    source.stream((Subscriber) (data, env) ->
                    {
                        temp.setTemperature(data.value(Float.class));
                        Log.i("MbientSensor", "Temp data " + data.value(Float.class));
                    }))
                    .continueWith((Continuation<Route, Void>) task ->
                    {
                        tempSensor.read();
                        return null;
                    });
        }
    }
}

