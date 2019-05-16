package edu.psu.grovermodule;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import edu.psu.grovermodule.fragment.FragmentDiagnostics;
import edu.psu.grovermodule.fragment.FragmentMission;

/**
 * This class communicates and sends sensor data collected in the FragmentDiagnostics JSON payloads
 * to a ServiceNow Table
 */

public class WebService extends AsyncTask<String, Void, String> {

    private String user = "CloudIntegration";
    private String pwd = "IST440spring2019";

    @Override
    protected String doInBackground(String... params) {
        try {
            String urlString = "https://emplkasperpsu2.service-now" +
                    ".com/api/now/table/u_mission_test";
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

            con2.setRequestMethod("POST");
            String USER_AGENT = "Mozilla/5.0";
            con2.setRequestProperty("User-Agent", USER_AGENT);
            con2.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            con2.setRequestProperty("Content-Type", "application/json");

            String postJsonData = "{" + FragmentDiagnostics.payload_GPS +
                    FragmentDiagnostics.payload_Acce +
                    FragmentDiagnostics.payload_Ambi +
                    FragmentDiagnostics.payload_Baro +
                    FragmentDiagnostics.payload_Gyro +
                    FragmentDiagnostics.payload_Humi +
                    FragmentDiagnostics.payload_Magn +
                    "," + "\"u_datetime\":" + "\"" + FragmentMission.dateAndTime + "\"" +
                    MissionWebService.jsonOutput + "}";

            // Send post request
            con2.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con2.getOutputStream());
            wr.writeBytes(postJsonData);
            wr.flush();
            wr.close();


            in = new BufferedReader(new InputStreamReader(con2.getInputStream()));
            response = new StringBuffer();
            responseCode = con2.getResponseCode();
            System.out.println("Sending 'POST' request to URL: " + url);
            System.out.println("Post Data: " + postJsonData);
            System.out.println("Response Code: " + responseCode);

            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();
            con2.disconnect();

            //printing result from response
            System.out.println(response.toString());

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
