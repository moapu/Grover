package edu.psu.grovermodule.handler;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.android.BtleService;

import bolts.Continuation;

/*
Project: PSA Grover Vehicle
Feature: Bluetooth Handler
Course: IST 440w Section 1 Fall 2019
Date Developed: 3/11/19
Last Date Changed: 3/11/2019
Rev: 1
*/

public class BluetoothHandler implements ServiceConnection {
    private static final String MW_MAC_ADDRESS = "E2:BA:AD:56:4E:72";

    // Checks for metawear mac address
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceHardwareAddress = device.getAddress();
                if (deviceHardwareAddress.equals(MW_MAC_ADDRESS)) {
                    log("METAWEAR FOUND");
                }
            }
        }
    };

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mBluetoothExists = false;
    private BtleService.LocalBinder mServiceBinder;
    private MetaWearBoard mBoard;
    private Context mContext;
    private boolean mbientSensorConnected = false;

    /**
     * @param context fragment context
     */
    public BluetoothHandler(Context context) {
        this.mContext = context;
    }

    /**
     * @return mBluetoothExists
     */
    public boolean isBluetoothExists() {
        return mBluetoothExists;
    }

    /**
     * @return mbientSensorConnected
     */
    public boolean isMbientSensorConnected() {
        return mbientSensorConnected;
    }

    /**
     * @return mServiceBinder
     */
    public BtleService.LocalBinder getServiceBinder() {
        return mServiceBinder;
    }

    /**
     * @return mBoard
     */
    public MetaWearBoard getMWboard() {
        return mBoard;
    }

    /**
     * Unregisters bluetooth receiver
     */
    public void unregister() {
        mContext.unregisterReceiver(this.mReceiver);
        mContext.unbindService(this);
        mBoard.tearDown();
        mContext = null;
        log("unregister unregistered");
    }

    /**
     * Requests bluetooth permission
     *
     * @param fragment fragment
     */
    public void requestBTPermissions(Fragment fragment) {
        int permissionCheck = mContext.checkSelfPermission("Manifest" +
                ".permission" +
                ".ACCESS_FINE_LOCATION");
        permissionCheck += mContext.checkSelfPermission("Manifest.permission" +
                ".ACCESS_COARSE_LOCATION");

        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null) {
                log("Bluetooth Exists");

                mBluetoothExists = true;

                if (!mBluetoothAdapter.isEnabled()) {
                    log("Requesting to enable bluetooth");
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    fragment.startActivityForResult(enableBtIntent, 9001);
                }

                if (permissionCheck != 0) {
                    log("Requesting for mLocation");
                    fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 9001);
                }
            }
        } catch (NullPointerException e) {
            log("Bluetooth adapter does not exist");
        }
    }

    /**
     * Binds bluetooth service
     */
    public void bindBluetoothService() {
        mContext.bindService(new Intent(mContext,
                        BtleService.class),
                this, Context.BIND_AUTO_CREATE);
        log("bindService Called");
    }

    /**
     * Logger
     *
     * @param s string
     */
    private void log(String s) {
        Log.i(getClass().getSimpleName(), s);
    }

    /**
     * Retrieves the metawear board
     */
    private void retrieveBoard() {
        log("binding MAC address to mBoard");
        BluetoothManager btManager =
                (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        if (isBluetoothExists()) {
            BluetoothDevice remoteDevice =
                    btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

            // Create a MetaWear mBoard object for the Bluetooth Device
            mBoard = mServiceBinder.getMetaWearBoard(remoteDevice);
            connectToMW();
        }
    }

    /**
     * Connects to metawear board through async
     */
    private void connectToMW() {
        // when connects to the mBoard
        mBoard.connectAsync().continueWith((Continuation<Void, Void>) task ->
        {
            if (task.isFaulted()) {
                log("Failed to connect");
                startDiscoveringDevices();
                connectToMW();  // recursion
                // TODO: stop this recursion after certain amount of time
            } else {
                log("Connected: " + mBoard.getModelString() + " | " + mBoard.getMacAddress());
                mbientSensorConnected = true;
            }
            return null;
        });
    }

    /**
     * Scans for bluetooth devices
     */
    private void startDiscoveringDevices() {
        log("Looking for unpaired devices");
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            log("Canceling discovery");
            mBluetoothAdapter.startDiscovery();
            log("Restarting discovery");
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            mContext.getApplicationContext().registerReceiver(mReceiver, filter);
        } else if (!mBluetoothAdapter.isDiscovering()) {
            log("Starting discovery");
            mBluetoothAdapter.startDiscovery();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            mContext.getApplicationContext().registerReceiver(mReceiver, filter);
        }
    }

    /**
     * Called when bluetooth service is connected
     *
     * @param componentName componentName
     * @param service       service
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        mServiceBinder = (BtleService.LocalBinder) service;
        log("onServiceConnected called");
        retrieveBoard();
    }

    /**
     * Unused method, needed for service connection implementation
     *
     * @param componentName componentName
     */
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        log("onServiceDisconnected called");
    }
}
