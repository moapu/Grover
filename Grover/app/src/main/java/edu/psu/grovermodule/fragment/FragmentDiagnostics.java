package edu.psu.grovermodule.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import edu.psu.grovermodule.R;
import edu.psu.grovermodule.WebService;
import edu.psu.grovermodule.database.DataSource;
import edu.psu.grovermodule.handler.BluetoothHandler;
import edu.psu.grovermodule.handler.LocationHandler;
import edu.psu.grovermodule.handler.MbientSensorHandler;
import edu.psu.grovermodule.handler.SensorManagerHandler;
import edu.psu.grovermodule.pojo.Accelerometer;
import edu.psu.grovermodule.pojo.AmbientLight;
import edu.psu.grovermodule.pojo.Barometer;
import edu.psu.grovermodule.pojo.GPS;
import edu.psu.grovermodule.pojo.Gyroscope;
import edu.psu.grovermodule.pojo.Humidity;
import edu.psu.grovermodule.pojo.Magnetometer;
import edu.psu.grovermodule.pojo.Temp;

import static android.content.Context.CAMERA_SERVICE;

/*
Project: PSA Grover Vehicle
Feature: Diagnostics Fragment for starting and stopping data capture, and viewing sensory data
Course: IST 440w Section 1 Fall 2019
Date Developed: 2/1/19
Last Date Changed: 4/14/19
Rev: 11
*/

public class FragmentDiagnostics extends Fragment implements SensorEventListener {
    private static final String PREF_TIMER = "Timer";

    // Static JSON payload fields
    public static String payload_Acce = "";
    public static String payload_Ambi = "";
    public static String payload_GPS = "";
    public static String payload_Baro = "";
    public static String payload_Clim = "";
    public static String payload_Gyro = "";
    public static String payload_Humi = "";
    public static String payload_Magn = "";
    public static String payload_Temp = "";

    //Shared Preference Keys used to store retrieved mission template data
    private static final String PREF_TEMP_MIN = "TemperatureMin";
    private static final String PREF_TEMP_MAX = "TemperatureMax";
    private static final String PREF_BARO_MIN = "BarometerMin";
    private static final String PREF_BARO_MAX = "BarometerMax";
    private static final String PREF_HUMID_MIN = "HumidityMin";
    private static final String PREF_HUMID_MAX = "HumidityMax";
    private static final String PREF_CLIM_TYPE = "ClimateType";

    //GPS information for mission
    public static double mission_altitude = 0;
    public static double mission_longitude = 0;
    public static double mission_latitude = 0;

    private final String TAG = "Diagnostics";
    private static final int CAMERA_REQUEST = 50;
    private ToggleButton mToggleButton;
    private BluetoothHandler mBluetoothHandler;
    private SensorManagerHandler mSensorManagerHandler;
    private LocationHandler mLocationHandler;
    private DataSource mDataSource;
    private OnFragmentInteractionListener mOnFragmentListener;

    boolean baroCheck = false;
    boolean lightCheck = false;
    boolean accChec = false;
    boolean gyroCheck = false;
    boolean magCheck = false;
    boolean humidCheck = false;

    boolean isChecked = false;

    public FragmentDiagnostics() {
        // Required empty public constructor
        //Instantiates a new Fragment diagnostics.
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new WebService().execute();

        // start bluetooth
        mBluetoothHandler = new BluetoothHandler(getContext());
        mBluetoothHandler.requestBTPermissions(this);
        if (mBluetoothHandler.isBluetoothExists()) {
            mBluetoothHandler.bindBluetoothService();
        }

        // init GPS
        mLocationHandler = new LocationHandler(getContext(), this);
        mLocationHandler.initGPS();

        // SQLite
        mDataSource = DataSource.getInstance(getContext());

        log("onCreate Called");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_diagnostics, container, false);
    }

    /**
     * displays the temperature if the START button is checked
     */
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize the start button
        mToggleButton = Objects.requireNonNull(getActivity()).findViewById(R.id.start_button);
        mToggleButton.setOnCheckedChangeListener((CompoundButton buttonView, boolean checked) ->
        {
            isChecked = checked;
            if (isChecked) {
                if (mBluetoothHandler.isBluetoothExists() && mBluetoothHandler.isMbientSensorConnected()) {
                    showTemperature();
                }
            }

        });

        String timerSettingMsg = getString(R.string.timer_set_to_save_every_x_seconds,
                checkTimerSetting());

        Toast toast =
                Toast.makeText(getContext(), timerSettingMsg, Toast.LENGTH_LONG);
        ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                .setGravity(Gravity.CENTER);
        toast.show();

    }

    /**
     * unregister phone sensor (SensorManager)
     */
    private void unregister() {
        if (mSensorManagerHandler != null) {
            mSensorManagerHandler.getSensorManager().unregisterListener(this);
        }
        if (mLocationHandler != null) {
            mLocationHandler.removeUpdates();
        }
    }

    /**
     * On button pressed.
     *
     * @param uri the uri
     */
    public void onButtonPressed(Uri uri) {
        if (mOnFragmentListener != null) {
            mOnFragmentListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mOnFragmentListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnFragmentListener = null;
    }

    /**
     * @param s string
     */
    private void log(String s) {
        Log.i(getClass().getSimpleName(), s);
    }

    /**
     * register phone sensors, initialize GPS, and turn on Bluetooth
     */
    @Override
    public void onStart() {
        super.onStart();
        // call the webservice
        Timer sensorTimer = new Timer();
        TimerTask sensorCheck = new TimerTask() {
            @Override
            public void run() {
                if (isChecked) {
                    new WebService().execute();
                }
            }
        };

        int timerLength = checkTimerSetting() * 1000;
        sensorTimer.schedule(sensorCheck, 0, timerLength);

        // register the phone sensors
        mSensorManagerHandler = new SensorManagerHandler();
        mSensorManagerHandler.register(getContext(), this);
        log("onStart called");
    }

    /**
     * request for location onResume
     */
    @Override
    public void onResume() {
        super.onResume();
        mLocationHandler.requestLocation();
        mDataSource.open();
        log("onResume Called");
    }

    /**
     * stop updates for GPS onPause
     */
    @Override
    public void onPause() {
        super.onPause();
        mLocationHandler.removeUpdates();
        mDataSource.close();
        log("onPause Called");
    }

    /**
     * unregister the phone sensors onStop
     */
    @Override
    public void onStop() {
        super.onStop();
        unregister();
        log("onStop called");
        new WebService().execute();
    }

    /**
     * get rid of resources (bluetooth, mbient)
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mBluetoothHandler.unregister();
        log("onDestroy called");
    }

    /**
     * Shows the temperature from the Mbient sensor
     */
    private void showTemperature() {
        MbientSensorHandler task = new MbientSensorHandler();
        task.execute(mBluetoothHandler.getMWboard());
        Temp temp = new Temp();

        try {
            Log.i("bluetooth", String.valueOf(task.get()));
            temp.setTemperature(task.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TextView tv = Objects.requireNonNull(getActivity()).findViewById(R.id.temp_value);
        if (temp.getTemperature() != 0f) {
            tv.setText(getString(R.string.temperature_value, temp.getTemperature()));
            mDataSource.insertToSQL(temp);
            log("temp inserted to SQL");
        }
    }

    /**
     * @param sensorEvent the sensor event
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Gson gson = new Gson();

        try {
            if (isChecked) {
                // The toggle is enabled, so set its background color to red to show
                // the user that it is running and they can now stop it
                mToggleButton.setBackgroundResource(R.color.red);

                if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
                    ambientLightEvent(sensorEvent.values[0], gson);
                }
                if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    accelerometerEvent(sensorEvent, gson);
                }
                if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                    gyroscopeEvent(sensorEvent, gson);
                }
                if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    magnetometerEvent(sensorEvent, gson);
                }
                if (sensorEvent.sensor.getType() == Sensor.TYPE_PRESSURE) {
                    barometerEvent(sensorEvent.values[0], gson);
                }
                if (sensorEvent.sensor.getType() == Sensor
                        .TYPE_RELATIVE_HUMIDITY) {
                    humidityEvent(sensorEvent.values[0], gson);
                }

                updateGPSTextViews(mLocationHandler, this);
            } else {
                mToggleButton.setBackgroundResource(R.color.colorPrimary);
                flashlight(false);
            }

        } catch (Exception e) {
            Log.e("FragmentDiagnostics", e.getMessage(), e);
        }
    }


    /**
     * @param value the float of the sensor event array value
     * @param gson  the GSON object to turn into a JSON
     */
    private void ambientLightEvent(float value, Gson gson) {
        AmbientLight ambientLight = new AmbientLight();
        ambientLight.setLux(value);
        String json = gson.toJson(ambientLight);
        System.out.println(json);

        TextView light =
                Objects.requireNonNull(getActivity()).findViewById(R.id.amb_light_value);
        light.setText(getString(R.string.amb_light_value, ambientLight.getLux()));
        TextView flash =
                Objects.requireNonNull(getActivity()).findViewById(R.id.flash_value);
        float lux = ambientLight.getLux();

        if (ambientLight.getLux() != 0) {
            lightCheck = true;
        }

        //post Json Data AmbientLight
        payload_Ambi = "\"u_ambientlight\":\"" + ambientLight.getLux() + "\",";

        System.out.println("Ambient light JSON Payload:\n" + payload_Ambi);

        try {
            if (lux <= 50) {
                log("It's pretty dark right now.");
                flash.setText(R.string.flashlight_on);
                flashlight(true);
            } else {
                flash.setText(R.string.flashlight_off);
                flashlight(false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot access camera or does not have flash unit", e);
        }

        // SQL stuff here
        mDataSource.insertToSQL(ambientLight);
        log("ambient light inserted to SQL");
    }

    private void humidityEvent(float value, Gson gson) {
        Humidity humidity = new Humidity();
        humidity.setRelHumidity(value);
        String json = gson.toJson(humidity);
        System.out.println(json);

        TextView humidity_text = Objects.requireNonNull(getActivity())
                .findViewById(R.id.humidity_value);
        humidity_text.setText(getString(R.string.humidity_value, humidity.getRelHumidity()));

        if (humidity.getRelHumidity() != 0) {
            humidCheck = true;
        }

        // SQL stuff here
        mDataSource.insertToSQL(humidity);
        log("humidity inserted to SQL");

        //post Json Data Humidity
        payload_Humi = "\"u_humidity\":\"" + humidity.getRelHumidity() + "\",";

        System.out.println("Humidity JSON Payload:\n" + payload_Humi);
    }

    private void barometerEvent(float value, Gson gson) {
        Barometer barometer = new Barometer();
        barometer.setPressure(value);
        String json = gson.toJson(barometer);
        System.out.println(json);

        TextView baro = Objects.requireNonNull(getActivity()).findViewById(R.id.barometer_value);
        baro.setText(getString(R.string.barometer_value, barometer.getPressure()));

        if (barometer.getPressure() != 0) {
            baroCheck = true;
        }

        // SQL stuff here
        mDataSource.insertToSQL(barometer);
        log("barometer inserted to SQL");

        //post Json Data Barometer
        payload_Baro = "\"u_barometricpressure\":\"" + barometer.getPressure() + "\",";

        System.out.println("Barometer JSON Payload:\n" + payload_Baro);
    }

    private void magnetometerEvent(SensorEvent sensorEvent, Gson gson) {
        Magnetometer magnetometer = new Magnetometer();
        magnetometer.setX(sensorEvent.values[0]);
        magnetometer.setY(sensorEvent.values[1]);
        magnetometer.setZ(sensorEvent.values[2]);
        String json = gson.toJson(magnetometer);
        System.out.println(json);

        TextView mag =
                Objects.requireNonNull(getActivity()).findViewById(R.id.mag_value);
        mag.setText(getString(R.string.magn_value, magnetometer.getX(), magnetometer.getY()
                , magnetometer.getZ()));

        if (magnetometer.getX() != 0) {
            magCheck = true;
        }

        // SQL stuff here
        mDataSource.insertToSQL(magnetometer);
        log("magnetometer inserted to SQL");

        //post Json Data Magnetometer
        payload_Magn = "\"u_magnetometerx\":\"" + magnetometer.getX()
                + "\",\"u_magnetometery\":\"" +
                magnetometer.getY() + "\",\"u_magnetometerz\":\"" +
                magnetometer.getZ() + "\"";

        System.out.println("Magnetometer JSON Payload:\n" + payload_Magn);
    }

    private void gyroscopeEvent(SensorEvent sensorEvent, Gson gson) {
        Gyroscope gyroscope = new Gyroscope();
        gyroscope.setX(sensorEvent.values[0]);
        gyroscope.setY(sensorEvent.values[1]);
        gyroscope.setZ(sensorEvent.values[2]);
        String json = gson.toJson(gyroscope);
        System.out.println(json);

        TextView gyro =
                Objects.requireNonNull(Objects.requireNonNull(getActivity()).findViewById(R.id
                        .gyro_value));
        gyro.setText(getString(R.string.gyro_values, gyroscope.getX(), gyroscope.getY()
                , gyroscope.getZ()));

        if (gyroscope.getX() != 0) {
            gyroCheck = true;
        }

        // SQL stuff here
        mDataSource.insertToSQL(gyroscope);
        log("gyroscope inserted to SQL");

        //post Json Data Gyroscope
        payload_Gyro = "\"u_gyroscopex\":\"" + gyroscope.getX()
                + "\",\"u_gyroscopey\":\"" +
                gyroscope.getY() + "\",\"u_gyroscopez\":\"" +
                gyroscope.getZ() + "\",";

        System.out.println("Gyroscope JSON Payload:\n" + payload_Gyro);
    }

    private void accelerometerEvent(SensorEvent sensorEvent, Gson gson) {
        Accelerometer acc = new Accelerometer();
        acc.setX(sensorEvent.values[0]);
        acc.setY(sensorEvent.values[1]);
        acc.setZ(sensorEvent.values[2]);
        String json = gson.toJson(acc);

        System.out.println(json);

        TextView accel = Objects.requireNonNull(getActivity()).findViewById(R.id.accel_value);
        accel.setText(getString(R.string.accel_values, acc.getX(), acc.getY()
                , acc.getZ()));

        if (acc.getX() != 0) {
            accChec = true;
        }

        // SQL here
        mDataSource.insertToSQL(acc);
        log("accelerometer inserted to SQL");

        //post Json Data Accelerometer
        payload_Acce = "\"u_accelerometerx\":\"" + acc.getX()
                + "\",\"u_accelerometery\":\"" +
                acc.getY() + "\",\"u_accelerometerz\":\"" +
                acc.getZ() + "\",";

        System.out.println("Accelerometer JSON Payload:\n" + payload_Acce);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Activates or deactivates the flashlight according to the Boolean value it is passed and
     * whether the device has the flashlight capability
     *
     * @param isOn Boolean of whether the flashlight is on
     */
    private void flashlight(boolean isOn) {
        CameraManager cameraManager =
                (CameraManager) Objects.requireNonNull(getContext()).getSystemService(CAMERA_SERVICE);
        TextView flashStatus = Objects.requireNonNull(getActivity()).findViewById(R.id.flash_value);
        boolean hasCameraFlash =
                getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        try {
            if (cameraManager != null && hasCameraFlash) {
                String cameraId = cameraManager.getCameraIdList()[0];
                if (isOn) {
                    cameraManager.setTorchMode(cameraId, true);
                    flashStatus.setText(R.string.flashlight_on);
                } else {
                    cameraManager.setTorchMode(cameraId, false);
                    flashStatus.setText(R.string.flashlight_off);
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access failed", e);
        }
    }

    /**
     * Update the UI components with any available GPS data
     *
     * @param mLocationHandler the location handler
     * @param fragment         the context fragment
     */
    public void updateGPSTextViews(LocationHandler mLocationHandler, Fragment fragment) {
        GPS gps = new GPS();
        gps.setAltitude(mLocationHandler.getGps().getAltitude());
        gps.setBearing(mLocationHandler.getGps().getBearing());
        gps.setLatitude(mLocationHandler.getGps().getLatitude());
        gps.setLongitude(mLocationHandler.getGps().getLongitude());

        // Convert speed from knots kilometers per hour
        float speed_kmph = mLocationHandler.getGps().getSpeed();
        // Speed is now in kmph
        speed_kmph *= 1.852;
        gps.setSpeed(speed_kmph);

        if (mLocationHandler.getGps() != null) {
            TextView gpsTextView =
                    Objects.requireNonNull(getActivity()).findViewById(R.id.gps_value);
            TextView speedTextView =
                    Objects.requireNonNull(getActivity()).findViewById(R.id.speed_value);
            TextView altitudeTextView = getActivity().findViewById(R.id.altitude_value);
            TextView bearingTextView = getActivity().findViewById(R.id.bearing_value);

            gpsTextView.setText(getString(R.string.gps_values,
                    gps.getLatitude(), gps.getLongitude()));
            speedTextView.setText(getString(R.string.speed_value,
                    gps.getSpeed()));
            altitudeTextView.setText(getString(R.string.altitude_value,
                    gps.getAltitude()));
            bearingTextView.setText(getString(R.string.bearing_value,
                    gps.getBearing()));

            payload_GPS = "\"u_bearing\":\"" + gps.getBearing()
                    + "\",\"u_kmphspeed\":\"" +
                    gps.getSpeed() + "\",\"u_latitude\":\"" +
                    gps.getLatitude() + "\",\"u_altitude\":\"" +
                    gps.getAltitude() + "\",\"u_longitude\":\"" +
                    gps.getLongitude() + "\",";

            System.out.println("GPS JSON Payload:\n" + payload_GPS);
        }
    }

    /**
     * Request permission to access the camera or GPS, depending on the request code passed in
     *
     * @param requestCode  the request code
     * @param permissions  the permissions
     * @param grantResults the result granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager
                        .PERMISSION_GRANTED)) {
                    return;
                } else {
                    Toast.makeText(getContext(), "Permission Denied for the Camera", Toast
                            .LENGTH_SHORT).show();
                }
                break;
            case 10:
                mLocationHandler.requestLocationPermission();
                break;
            default:
                break;
        }
    }

    /**
     * Turn the selected timer value from the Settings timer spinner stored in SharedPreferences
     * into the proper integer value for use in setting the interval for the web service data
     * sending
     *
     * @return the integer timer value
     */
    public int checkTimerSetting() {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        String timerSelection = sharedPreferences.getString(PREF_TIMER, "");
        final String ten = "10 seconds";
        final String thirty = "30 seconds";
        final String sixty = "60 seconds";

        int timerLength;

        switch (timerSelection) {
            case ten:
                timerLength = 10;
                break;
            case thirty:
                timerLength = 30;
                break;
            case sixty:
                timerLength = 60;
                break;
            default:
                timerLength = 10;
                break;
        }

        return timerLength;
    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface OnFragmentInteractionListener {
        /**
         * On fragment interaction.
         *
         * @param uri the uri
         */
        void onFragmentInteraction(Uri uri);
    }
}
