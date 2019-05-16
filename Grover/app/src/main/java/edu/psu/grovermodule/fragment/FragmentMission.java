package edu.psu.grovermodule.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import edu.psu.grovermodule.MissionTemplateWebService;
import edu.psu.grovermodule.MissionWebService;
import edu.psu.grovermodule.R;
import edu.psu.grovermodule.pojo.Climate;
import edu.psu.grovermodule.pojo.Mission;

import static android.app.Activity.RESULT_OK;

/*
Project: PSA Grover Vehicle
Feature: Mission Fragment for viewing and setting the mission template
Course: IST 440w Section 1 Fall 2019
Date Developed: 2/18/19
Last Date Changed: 4/14/19
Rev: 11
*/

public class FragmentMission extends Fragment {

    //Shared Preference Keys used to store retrieved mission template data
    private static final String PREF_TEMP_MIN = "TemperatureMin";
    private static final String PREF_TEMP_MAX = "TemperatureMax";
    private static final String PREF_BARO_MIN = "BarometerMin";
    private static final String PREF_BARO_MAX = "BarometerMax";
    private static final String PREF_HUMID_MIN = "HumidityMin";
    private static final String PREF_HUMID_MAX = "HumidityMax";
    private static final String PREF_CLIM_TYPE = "ClimateType";
    private static final String PREF_CLIMATE_SPIN_POS = "SpinnerPosition";

    // Fields for the mission commander Shared Preference settings
    private static final String PREF_COMMANDER_NAME = "MissionCommanderName";

    SharedPreferences missionPref;
    SharedPreferences.Editor edit;

    //Climate Type
    public static String mission_climate_type = "";

    //Date and Time
    public static String dateAndTime = "";

    //Mission Commander
    public static String missionCommander = "";

    //Climate spinner position
    private int spinnerPos = 0;

    //Image capture and permission codes
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;

    Spinner climate_spin;
    Climate climate;

    Mission mission;
    Button retrieve_mission_data;

    Button mCaptureBtn;
    ImageView mImageView;

    Uri image_uri;

    public static String photoData = "";
    public File photoFile;

    public static String JSONMission = "";

    private OnFragmentInteractionListener mListener;

    /**
     * Instantiates a new Fragment mission.
     */
    public FragmentMission() {
        // Required empty public constructor
    }

    /**
     * On button pressed.
     *
     * @param uri the uri
     */
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_mission, container, false);
        missionPref = PreferenceManager.getDefaultSharedPreferences(getContext());


        climate_spin = myView.findViewById(R.id.climate_spinner);
        climate_spin.setEnabled(false);

        mImageView = myView.findViewById(R.id.image_view);
        mCaptureBtn = myView.findViewById(R.id.capture_image_btn);

        // Instantiate the Mission class
        mission = new Mission();
        // Button for retrieving Mission Data
        retrieve_mission_data = myView.findViewById(R.id.retrieve_mission_data);

        missionCommander = MissionWebService.mission_commander;
        mission.set_missionCommander(missionCommander);

        // Find out mission parameters from the climate chosen
        String retrieved_climate = "";
        if (!MissionWebService.climate_id.isEmpty()) {
            retrieved_climate = MissionWebService.climate_id;
        }
        mission_climate_type = retrieved_climate;

        climate = new Climate();
        climate.set_climateId(mission_climate_type);

        climate.getMissionTemplate(climate.get_climateId(), getActivity());

        //climate type
        mission.set_climateType(MissionTemplateWebService.climate_type);

        //GPS location
        mission.set_altitude(FragmentDiagnostics.mission_altitude);
        mission.set_longitude(FragmentDiagnostics.mission_longitude);
        mission.set_latitude(FragmentDiagnostics.mission_latitude);

        //date
        Date thisDate = new Date();
        SimpleDateFormat dateForm = new SimpleDateFormat("MM/dd/Y HH:mm");
        String myString = dateForm.format(thisDate);
        dateAndTime = myString;
        System.out.println(myString);
        mission.set_dateAndTime(dateAndTime);

        //Mission Commander
        System.out.println(missionCommander);

        /*
          request for permission to use camera and write to external storage
                   */
        //button click to capture image
        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                        Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_DENIED ||
                        ContextCompat.checkSelfPermission(getActivity(),
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_DENIED) {

                    //requesting permission to write to storage
                    String[] permission = {Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE};

                    //display message to request permissions
                    requestPermissions(permission, PERMISSION_CODE);
                } else {
                    //permission already granted
                    openCamera();
                }
            }

        });

        climate_spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            /**
             * Overloaded the onItemSelected method for the selection spinner, to both get mission
             * parameters from the online database, and then set the initial mission parameters as
             * text views on the Mission Fragment UI
             * @param parent the AdapterView
             * @param view the view
             * @param position the selected item position
             * @param id the ID of the item
             *              */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Find and establish the textviews for temperature, barometric pressure, and
                // humidity
                TextView climate_temp_low =
                        Objects.requireNonNull(Objects.requireNonNull(getActivity()).findViewById(R.id.mission_temp_low));
                TextView climate_temp_high =
                        Objects.requireNonNull(getActivity().findViewById(R.id.mission_temp_high));
                TextView climate_baro_high =
                        Objects.requireNonNull(getActivity().findViewById(R.id.mission_baro_high));
                TextView climate_baro_low =
                        Objects.requireNonNull(getActivity().findViewById(R.id.mission_baro_low));
                TextView climate_humid_high =
                        Objects.requireNonNull(getActivity().findViewById(R.id.mission_humid_high));
                TextView climate_humid_low =
                        Objects.requireNonNull(getActivity().findViewById(R.id.mission_humid_low));

                //If the saved spinner position is the same as the climate spinner position,
                //enter the pre-saved climate data and do nothing else
                if (spinnerPos == position) {
                    climate_temp_low.setText(getString(R.string.climate_temp_low,
                            missionPref.getFloat(PREF_TEMP_MIN, 0)));
                    climate_temp_high.setText(getString(R.string.climate_temp_high,
                            missionPref.getFloat(PREF_TEMP_MAX, 0)));
                    climate_baro_low.setText(getString(R.string.climate_baro_low,
                            missionPref.getFloat(PREF_BARO_MIN, 0)));
                    climate_baro_high.setText(getString(R.string.climate_baro_high,
                            missionPref.getFloat(PREF_BARO_MAX, 0)));
                    climate_humid_low.setText(getString(R.string.climate_humid_low,
                            missionPref.getFloat(PREF_HUMID_MIN, 0)));
                    climate_humid_high.setText(getString(R.string.climate_humid_high,
                            missionPref.getFloat(PREF_HUMID_MAX, 0)));
                    return;
                }

                //Try and change the text views
                try {
                    System.out.println("low temp: " + climate.get_tempMin());
                    climate_temp_low.setText(getString(R.string.climate_temp_low,
                            climate.get_tempMin()));
                    climate_temp_high.setText(getString(R.string.climate_temp_high,
                            climate.get_tempMax()));
                    climate_baro_high.setText(getString(R.string.climate_baro_high,
                            climate.get_baroMax()));
                    climate_baro_low.setText(getString(R.string.climate_baro_low,
                            climate.get_baroMin()));
                    climate_humid_high.setText(getString(R.string.climate_humid_high,
                            climate.get_humidMax()));
                    climate_humid_low.setText(getString(R.string.climate_humid_low,
                            climate.get_humidMin()));
                } catch (Exception e) {
                    Log.e("FragmentMission", e.toString());
                }
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

        return myView;
    }

    //private method of your class
    private int getIndex(Spinner spinner, String myString) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                return i;
            }
        }

        return 0;
    }


    /**
     * Opening camera
     */
    private void openCamera() {
        File pictureDirectory = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureName = getPictureName();
        File imageFile = new File(pictureDirectory, pictureName);
        photoFile = imageFile;
        image_uri = Uri.fromFile(imageFile);

        //Camera intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    /**
     * request for permission to write settings
     */
    //handling permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //this method is called, when a user selects Allow or Deny from Permission Request Popup
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //permission is granted
                    openCamera();
                } else {
                    //permission is denied
                    Toast.makeText(getContext(), "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //called when image is captured from camera

        if (resultCode == RESULT_OK) {
            //set the image captured to the ImageView
            mImageView.setImageURI(image_uri);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] byteFormat = stream.toByteArray();
            photoData = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
            mission.set_photo(photoData);

        }
    }

    /**
     * Unique name for Captured Photo
     */
    private String getPictureName() {
        SimpleDateFormat setDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = setDate.format(new Date());
        return "Mission_Photo_Capture_" + timestamp + ".jpg";
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Try and change the spinner and text views to match what is in SharedPreferences
        spinnerPos = missionPref.getInt(PREF_CLIMATE_SPIN_POS, 0);
        climate_spin.setSelection(spinnerPos);

        String savedCommanderName = missionPref.getString(PREF_COMMANDER_NAME, "");

        TextView missionCommanderNameInputBox =
                getActivity().findViewById(R.id.missionCommanderName);
        missionCommanderNameInputBox.setText(savedCommanderName);

        climate.getMissionTemplate(climate.get_climateId(), getActivity());


    }

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        climate.set_climateType(mission_climate_type);
        climate.getMissionTemplate(climate.get_climateId(), getActivity());
        Button retrieve_mission_data = getActivity().findViewById(R.id.retrieve_mission_data);
        edit = missionPref.edit();

        /*
          Submit the Mission Data
                   */
        retrieve_mission_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mission_climate_type = MissionTemplateWebService.climate_type;
                climate.set_climateType(mission_climate_type);

                System.out.println("Mission Commander: " + mission.get_missionCommander());
                TextView username = getActivity().findViewById(R.id.missionCommanderName);
                username.setText(mission.get_missionCommander());

                SimpleDateFormat setDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String timestamp = setDate.format(new Date());


                String postMissionJsonData =
                        "{" + "\"u_missionid\":\"" + mission.get_climateType() + timestamp +
                                "\"," + "\"u_altitude\":\"" + mission.get_altitude() + "\"," +
                                "\"u_latitude" +
                                "\":\"" + mission.get_latitude() +
                                "\"," + "\"u_longitude\":\"" + mission.get_longitude() + "\"," +
                                "\"u_datetime\":\"" + mission.get_dateAndTime() + "\"," +
                                "\"u_photo\":\"" + mission.get_photo() + "\"," +
                                "\"u_mission_commander" +
                                "\":\"" + mission.get_missionCommander() + "\"}";

                JSONMission = postMissionJsonData;
                System.out.println("Post Mission JSON Data: " + postMissionJsonData);
                System.out.println("Image URI: " + image_uri);
                System.out.println("Photo Data: " + photoData);
                String prompt;
                if (missionCommander.isEmpty()) {
                    prompt = "Welcome!";
                } else {
                    prompt = "Welcome, Mission Commander " + missionCommander + "!";
                }
                Toast.makeText(getContext(), prompt, Toast.LENGTH_SHORT).show();

                edit.putString(PREF_COMMANDER_NAME, missionCommander);
                edit.apply();
                new MissionWebService().execute();

                System.out.println("Mission climate type: " + MissionTemplateWebService.climate_type);

                if (!mission.get_climateType().isEmpty()) {
                    climate_spin.setSelection(getIndex(climate_spin, mission.get_climateType()));
                } else {
                    climate_spin.setSelection(missionPref.getInt(PREF_CLIMATE_SPIN_POS, 0));
                }

                climate.getMissionTemplate(climate.get_climateId(), getActivity());

            }
        });

        System.out.println("Mission climate type: " + mission.get_climateType());

        if (!mission.get_climateType().isEmpty()) {
            climate_spin.setSelection(getIndex(climate_spin, mission.get_climateType()));
        } else {
            climate_spin.setSelection(missionPref.getInt(PREF_CLIMATE_SPIN_POS, 0));
        }

        //Save the Climate spinner position to reenter the data on return to the fragment
        //And not make another GET request for data already obtained
        spinnerPos = climate_spin.getSelectedItemPosition();
        edit.putInt(PREF_CLIMATE_SPIN_POS, spinnerPos);

        //Add climate type to shared preferences so it can be used to compare data.
        edit.putString(PREF_CLIM_TYPE, climate.get_climateType());
        edit.apply();

    }

}
