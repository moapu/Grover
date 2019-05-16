package edu.psu.grovermodule;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

/**
 * This class communicates and sends sensor data collected in the FragmentMission JSON payloads to a
 * ServiceNow Table
 */

public class MissionWebService extends AsyncTask<String, Void, String> {
    private String user = "CloudIntegration";
    private String pwd = "IST440spring2019";

    static String jsonOutput = "";
    public static String mission_commander = "";
    public static String commander_email = "";
    public static String commander_sms = "";
    public static String climate_id = "";

    @Override
    protected String doInBackground(String... params) {
        try {
            //String urlString = "https://emplkasperpsu2.service-now
            // .com/api/now/table/u_mission_test";
            String urlString = "https://emplkasperpsu2.service-now" +
                    ".com/api/now/table/u_missionconfig_test?sysparm_limit=1";
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();


            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pwd.toCharArray());
                }
            });

            int responseCode = con.getResponseCode();

            // Reading response from input Stream
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String output;
            StringBuffer response = new StringBuffer();

            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();
            con.disconnect();

            HttpURLConnection con2 = (HttpURLConnection) url.openConnection();

            con2.setRequestMethod("GET");
            String USER_AGENT = "Mozilla/5.0";
            con2.setRequestProperty("User-Agent", USER_AGENT);
            con2.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con2.setRequestProperty("Content-Type", "application/json");

            responseCode = con2.getResponseCode();
            System.out.println("Sending 'GET' request to URL: " + url);
            System.out.println("Response Code: " + responseCode);

            if (responseCode >= 400) {
                if (responseCode == 404 || responseCode == 410) {
                    throw new FileNotFoundException(url.toString());
                } else {
                    throw new java.io.IOException(
                            "Server returned HTTP"
                                    + " response code: " + responseCode
                                    + " for URL: " + url.toString());
                }
            }

            in = new BufferedReader(new InputStreamReader(con2.getInputStream()));
            response = new StringBuffer();

            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();
            con2.disconnect();

            System.out.println("Response: " + response);

            jsonOutput =
                    response.toString().substring(12, 136) + "\"" + response.toString().substring(221, 253) + "\""
                            + "," + response.toString().substring(299,
                            response.toString().length() - 3);

            String regex_quotes = "^\"|\"$";

            // Parse mission commander name from JSON
            String u_mission_commander = "\"u_missioncommandername\"";
            int name_string_start = response.indexOf(u_mission_commander);
            int name_string_end = name_string_start + u_mission_commander.length();
            String name_substring = response.toString().substring(name_string_end);
            String[] name_split = name_substring.split(",", 0);
            String[] names_array = name_split[0].split(":", 1);
            mission_commander = names_array[0].replaceAll(":",
                    "").replaceAll(regex_quotes, "");
            System.out.println("Mission Commander Name: " + mission_commander);

            // Parse Climate ID from JSON
            String value_json_tag = "\"value\":";
            int value_string_start = response.indexOf(value_json_tag) + value_json_tag.length();
            int value_string_end = response.indexOf(value_json_tag) + 42;
            String climate_parse =
                    response.toString().substring(value_string_start, value_string_end);
            climate_parse = climate_parse.replaceAll(regex_quotes, "");
            climate_id = climate_parse;
            System.out.println("Climate ID: " + climate_id);

            // Parse Commander Email Address from JSON
            String u_commanderemail = "\"u_commanderemail\"";
            int commander_email_start = response.indexOf(u_commanderemail);
            int commander_email_end = commander_email_start + u_commanderemail.length();
            String commander_email_substring = response.toString().substring(commander_email_end);
            String[] commander_email_split = commander_email_substring.split(",", 0);
            String[] commander_email_array = commander_email_split[0].split(":", 1);
            commander_email = commander_email_array[0].replaceAll(":",
                    "").replaceAll(regex_quotes, "");
            System.out.println("Commander Email: " + commander_email);

            // Parse Commander SMS Number from JSON
            String u_commander_phone = "\"u_commanderphonenum\"";
            int commander_phone_start = response.indexOf(u_commander_phone);
            int commander_phone_end = commander_phone_start + u_commander_phone.length();
            String commander_phone_substring = response.toString().substring(commander_phone_end);
            String[] commander_phone_split = commander_phone_substring.split(",", 0);
            String[] commander_phone_array = commander_phone_split[0].split(":", 1);
            commander_sms = commander_phone_array[0].replaceAll(":",
                    "").replaceAll(regex_quotes, "");
            System.out.println("Commander Phone Number: " + commander_sms);

            //printing result from response
            System.out.println("jsonOutput: " + jsonOutput);

        } catch (Exception e) {
            Log.e("MainActivity", e.getMessage(), e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        //update your ui here
    }

    @Override
    protected void onPreExecute() {
        //do any code before exec
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        //If you want to update a progress bar ..do it here
    }
}
