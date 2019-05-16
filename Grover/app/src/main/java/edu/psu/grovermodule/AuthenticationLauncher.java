package edu.psu.grovermodule;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.multidots.fingerprintauth.AuthErrorCodes;
import com.multidots.fingerprintauth.FingerPrintAuthCallback;
import com.multidots.fingerprintauth.FingerPrintAuthHelper;

/**
 * Project: PSA Grover Vehicle
 * Feature: Fingerprint Handler containing methods for the fingerprint authentication
 * Course: IST 440w
 * Section 1 Fall 2019
 * Date Developed: 3/19/19
 * Last Date Changed: 3/19/19 Rev: 0
 */

public class AuthenticationLauncher extends AppCompatActivity implements FingerPrintAuthCallback {

    FingerPrintAuthHelper mFingerPrintAuthHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_layout);
        mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFingerPrintAuthHelper.startAuth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFingerPrintAuthHelper.stopAuth();
    }

    @Override
    public void onNoFingerPrintHardwareFound() {
        Toast.makeText(this, "No fingerprint sensor found", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNoFingerPrintRegistered() {

    }

    @Override
    public void onBelowMarshmallow() {
        Toast.makeText(this, "Device can not support fingerprint authentication",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        ImageView lockImage = findViewById(R.id.lockImageView);
        lockImage.setImageDrawable(getDrawable(R.drawable.ic_lock_open_black_24dp));
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAuthFailed(int errorCode, String errorMessage) {
        switch (errorCode) {    //Parse the error code for recoverable/non recoverable error.
            case AuthErrorCodes.CANNOT_RECOGNIZE_ERROR:
                Toast.makeText(this, "Cannot recognize fingerprint", Toast.LENGTH_SHORT).show();
                //Cannot recognize the fingerprint scanned.
                break;
            case AuthErrorCodes.NON_RECOVERABLE_ERROR:
                //This is not recoverable error. Try other options for user authentication. like
                // pin, password.
                break;
            case AuthErrorCodes.RECOVERABLE_ERROR:
                //Any recoverable error. Display message to the user.
                break;
        }
    }

    public void enterAppIfNoFingerprintSensor(View view) {
        ImageView lockImage = findViewById(R.id.lockImageView);
        lockImage.setImageDrawable(getDrawable(R.drawable.ic_lock_open_black_24dp));
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
