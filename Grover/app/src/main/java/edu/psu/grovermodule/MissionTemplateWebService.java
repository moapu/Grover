package edu.psu.grovermodule;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import edu.psu.grovermodule.fragment.FragmentDiagnostics;
import edu.psu.grovermodule.fragment.FragmentMission;
import edu.psu.grovermodule.pojo.Climate;

/*
Project: PSA Grover Vehicle
Feature: Mission Templates GET Request Webservice
Course: IST 440w Section 1 Fall 2019
Date Developed: 3/12/19
Last Date Changed: 3/24/19
Rev: 5
*/

public class MissionTemplateWebService extends AsyncTask<String, Void, JSONObject> {

    /**
     * This constructor allows activity to be passed into the web service so that the UI can be
     * updated with the data obtained from the GET request that the web service makes.
     *
     * @param activity The Activity from Fragment Mission so the UI can be updated
     */
    public MissionTemplateWebService(Activity activity) {
        this.activity.set(activity);
    }

    private final AtomicReference<Activity> activity = new AtomicReference<Activity>();

    //Shared Preference Keys used to store retrieved mission template data
    public static String climate_type = "";
    private static final String PREF_TEMP_MIN = "TemperatureMin";
    private static final String PREF_TEMP_MAX = "TemperatureMax";
    private static final String PREF_BARO_MIN = "BarometerMin";
    private static final String PREF_BARO_MAX = "BarometerMax";
    private static final String PREF_HUMID_MIN = "HumidityMin";
    private static final String PREF_HUMID_MAX = "HumidityMax";

    private String user = "CloudIntegration";
    private String pwd = "IST440spring2019";
    private String urlString = "https://emplkasperpsu2.service-now" +
            ".com/api/now/table/u_climatetemplate_test/";
    private JSONObject missionTemplate;

    /**
     * Calls the online database to obtain the mission parameters for the climate type that was
     * selected by the user.
     *
     * @param params
     * @return The JSONObject received from the web database
     */
    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            //Constructing the URL to query the mission template table, based off the user
            // selection.
            urlString = urlString + params[0];
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("GET");
            String USER_AGENT = "Mozilla/5.0";
            con.setRequestProperty("User-Agent", USER_AGENT);

            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pwd.toCharArray());
                }
            });

            int responseCode = con.getResponseCode();
            System.out.println("Sending GET request: " + url);
            System.out.println("Response code: " + responseCode);

            //Post Mission JSON Data
            String postMissionJsonData =
                    "{" + "\"u_climate_type\":\"" + "," + FragmentMission.mission_climate_type +
                            "\"u_altitude\":\"" + "," + FragmentDiagnostics.mission_altitude +
                            "\"u_latitude\":\"" + "," + FragmentDiagnostics.mission_latitude +
                            "\"u_longitude\":\"" + "," + FragmentDiagnostics.mission_longitude +
                            "\"u_date_and_time\":\"" + "," + FragmentMission.dateAndTime +
                            "\"u_photo\":\"" + "," + FragmentMission.photoData +
                            "\"u_mission_commander\":\"" + "," + FragmentMission.missionCommander + "}";

            // Reading response from input Stream
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();

            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();
            con.disconnect();

            String regex_quotes = "^\"|\"$";

            System.out.println("Response (MTWS): " + response);

            // Parse Climate Type from JSON
            String u_climate_type = "\"u_climatename\"";
            int climate_type_start = response.indexOf(u_climate_type);
            int climate_type_end = climate_type_start + u_climate_type.length();
            String climate_type_substring = response.toString().substring(climate_type_end);
            String[] climate_type_split = climate_type_substring.split(",", 0);
            String[] climate_type_array = climate_type_split[0].split(":", 1);
            climate_type = climate_type_array[0].replaceAll(":",
                    "").replaceAll(regex_quotes, "");
            System.out.println("Climate Type: " + climate_type);


            String json = response.toString();
            missionTemplate = new JSONObject(json);
        } catch (Exception e) {
            Log.e("MissionTemplateGET", e.getMessage(), e);
        }
        return missionTemplate;
    }

    /**
     * This method sets the temperature, barometer, and humidity defined in the online mission
     * template database. It then updates the Fragment Mission UI to display the mission parameters
     * obtained from the online database.
     *
     * @param jsonObject The JSON Object produced from doInBackground
     */
    @Override
    protected void onPostExecute(JSONObject jsonObject) {

        super.onPostExecute(jsonObject);
        try {
            JSONArray missionParam = jsonObject.getJSONArray("result");
            Climate climate = new Climate();
            String MAX_TEMP = "u_fmaxtemp";
            climate.set_tempMax(getKey(missionParam, MAX_TEMP));
            String MIN_TEMP = "u_fmintemp";
            climate.set_tempMin(getKey(missionParam, MIN_TEMP));
            String MAX_BARO = "u_maxbarometricpressure";
            climate.set_baroMax(getKey(missionParam, MAX_BARO));
            String MIN_BARO = "u_minbarometricpressure";
            climate.set_baroMin(getKey(missionParam, MIN_BARO));
            String MAX_HUMID = "u_maxhumidity";
            climate.set_humidMax(getKey(missionParam, MAX_HUMID));
            String MIN_HUMID = "u_minhumidity";
            climate.set_humidMin(getKey(missionParam, MIN_HUMID));

            //Change the UI text views
            TextView climate_baro_low = activity.get().findViewById(R.id.mission_baro_low);
            climate_baro_low.setText(activity.get().getResources().getString(R.string.climate_baro_low, climate.get_baroMin()));
            TextView climate_baro_high = activity.get().findViewById(R.id.mission_baro_high);
            climate_baro_high.setText(activity.get().getResources().getString(R.string.climate_baro_high, climate.get_baroMax()));
            TextView climate_temp_high = activity.get().findViewById(R.id.mission_temp_high);
            climate_temp_high.setText(activity.get().getResources().getString(R.string.climate_temp_high, climate.get_tempMax()));
            TextView climate_temp_low = activity.get().findViewById(R.id.mission_temp_low);
            climate_temp_low.setText(activity.get().getResources().getString(R.string.climate_temp_low, climate.get_tempMin()));
            TextView climate_humid_high = activity.get().findViewById(R.id.mission_humid_high);
            climate_humid_high.setText(activity.get().getResources().getString(R.string.climate_humid_high, climate.get_humidMax()));
            TextView climate_humid_low = activity.get().findViewById(R.id.mission_humid_low);
            climate_humid_low.setText(activity.get().getResources().getString(R.string.climate_humid_low, climate.get_humidMin()));

            //Add mission parameters to shared preferences
            if (climate.get_tempMax() != 0) {
                SharedPreferences pref =
                        PreferenceManager.getDefaultSharedPreferences(activity.get().getApplicationContext());
                SharedPreferences.Editor edit = pref.edit();
                edit.putFloat(PREF_BARO_MIN, climate.get_baroMin());
                edit.putFloat(PREF_BARO_MAX, climate.get_baroMax());
                edit.putFloat(PREF_TEMP_MIN, climate.get_tempMin());
                edit.putFloat(PREF_TEMP_MAX, climate.get_tempMax());
                edit.putFloat(PREF_HUMID_MIN, climate.get_humidMin());
                edit.putFloat(PREF_HUMID_MAX, climate.get_humidMax());
                edit.apply();
            }
        } catch (Exception e) {
            Log.e("Mission Template W.S.", "onPostExecute method", e);
        }
    }

    /**
     * This method loops through the passed in JSONArray to find the particular climate parameter
     * that is they key, then returns the float value that is associated with that key.
     *
     * @param array The JSONArray obtained from the JSONObject received from the online database
     * @param key   The particular parameter (temperature etc.) that is to be found in the JSONArray
     * @return Returns the climate value associated with the key
     */
    private float getKey(JSONArray array, String key) {
        float keyval = 0;
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsn = array.getJSONObject(i);
                if (jsn.has(key)) {
                    keyval = (float) jsn.getDouble(key);
                    return keyval;
                }
            }

        } catch (Exception e) {
            Log.e("Mission Template W.S.", "getKey method", e);
        }
        return keyval;
    }
}
