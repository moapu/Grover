package edu.psu.grovermodule.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import edu.psu.grovermodule.R;
import edu.psu.grovermodule.handler.BluetoothHandler;
import edu.psu.grovermodule.handler.SensorManagerHandler;

/*
Project: PSA Grover Vehicle
Feature: Dashboard fragment for showing the system check statuses
Course: IST 440w Section 1 Fall 2019
Author(s): Joseph Sliwka, Abu Chowdhury, Jennifer A'Harrah
Date Developed: 2/1/19
Last Date Changed: 4/14/19
Rev: 11
*/
public class FragmentDashboard extends Fragment {
    private BluetoothHandler mBluetoothHandler;
    private TextView batteryText;
    private ProgressBar progressBar;

    private OnFragmentInteractionListener mListener;
    private SensorManagerHandler mSensorManagerHandler;


    // Fields for the mission commander Shared Preference settings
    private static final String PREF_COMMANDER_NAME = "MissionCommanderName";

    /**
     * Instantiates a new Fragment dashboard.
     */
    public FragmentDashboard() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothHandler = new BluetoothHandler(getContext());

        mBluetoothHandler.requestBTPermissions(this);
        if (mBluetoothHandler.isBluetoothExists()) {
            mBluetoothHandler.bindBluetoothService();
        }
        mSensorManagerHandler = new SensorManagerHandler();

    }

    /**
     * Is connected boolean.
     *
     * @param activeNetwork the active network
     * @return the boolean
     */
    public boolean isConnected(NetworkInfo activeNetwork) {
        return activeNetwork != null;
    }


    /**
     * Is wifi string.
     *
     * @param activeNetwork the active network
     * @param cm            the cm
     * @return the string
     */
    public String isWifi(Network activeNetwork, ConnectivityManager cm) {
        //boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
        boolean isWiFi = false;

        if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {

            isWiFi = true;

        }

        return Boolean.toString(isWiFi);
    }

    /**
     * Is a fast connection
     *
     * @param activeNetwork the active network
     * @param cm            the connectivity manager
     * @param Network       the network
     * @return the string describing the network speed
     */
    public String isConnectionFast(NetworkInfo activeNetwork, ConnectivityManager cm,
                                   Network Network) {

        int subType = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        if (activeNetwork != null) {
            subType = activeNetwork.getSubtype();
        }

        switch (subType) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "50-100 kbps";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "13-64 kbps";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "50-100 kbps";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "400-1000 kbps";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "600-1400 kbps";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "100 kbps";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "2-14 Mbps";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "700-1700 kbps";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "1-23 Mbps";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "400-7000 kbps";

            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "1-2 Mbps";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "5 Mbps";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "10-20 Mbps";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "25 kbps";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "10+ Mbps";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(Network);
                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return "High Link Speed";
                } else {
                    return "No Connection";
                }
        }
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
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
    public void onDestroy() {
        super.onDestroy();

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

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        String savedCommanderName = sharedPreferences.getString(PREF_COMMANDER_NAME, "");

        if (!savedCommanderName.isEmpty()) {
            Toast.makeText(getContext(), "Welcome, " + savedCommanderName + "!",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Welcome!", Toast.LENGTH_SHORT).show();
        }

        ConnectivityManager cm =
                (ConnectivityManager) Objects.requireNonNull(getActivity()).getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetwork = Objects.requireNonNull(cm).getActiveNetworkInfo();
        Network network = cm.getActiveNetwork();

        TextView wifiTxt = Objects.requireNonNull(getActivity()).findViewById(R.id.wifiText);
        TextView mobileTxt = Objects.requireNonNull(getActivity()).findViewById(R.id.cellText);
        TextView speedTxt = Objects.requireNonNull(getActivity()).findViewById(R.id.speedText);
        TextView senTxt = Objects.requireNonNull(getActivity()).findViewById(R.id.bluetoothTxt);

        if (isWifi(network, cm).equals("true")) {
            wifiTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
        }
        if (isConnected(activeNetwork)) {
            mobileTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
        }
        speedTxt.setText(getString(R.string.speedText, isConnectionFast(activeNetwork, cm,
                network)));

        if (mBluetoothHandler.isBluetoothExists()) {
            senTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check, 0);
        }


        batteryText = getActivity().findViewById(R.id.batteryLevel);
        progressBar = getActivity().findViewById(R.id.progressBar);
        getActivity().registerReceiver(this.mBatInfoReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            batteryText.setText(getString(R.string.battery_level, level));
            progressBar.setProgress(level);
        }
    };
}





