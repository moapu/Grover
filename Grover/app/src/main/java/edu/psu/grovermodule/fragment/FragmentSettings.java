package edu.psu.grovermodule.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.psu.grovermodule.R;
import edu.psu.grovermodule.pojo.Email;
import edu.psu.grovermodule.pojo.SMS;


/*
Project: PSA Grover Vehicle
Feature: Bluetooth Handler
Course: IST 440w Section 1 Fall 2019
Date Developed: 2/1/19
Last Date Changed: 3/11/2019
Rev: 2
*/

public class FragmentSettings extends Fragment {

    Email email = new Email();
    String emailTo = "";
    String emailSub;
    String emailBody;

    SMS smsMessage = new SMS();
    private static final int REQUEST_SMS = 0;
    String phoneNum = "";
    String phoneTxt;

    // Fields for the timer Shared Preferences
    private static final String PREF_SPINNER_POSITION_TIMER = "TimerSpinnerPosition";
    private static final String PREF_TIMER = "Timer";
    private String timer_selection;
    private int spinnerPos = 0;

    // Fields for the email address and SMS phone number Shared Preferences settings
    private static final String PREF_EMAIL = "EmailAddress";
    private static final String PREF_SMS = "SmsNumber";

    EditText emailRecipientInputBox;
    EditText smsNumberInputBox;

    private OnFragmentInteractionListener mListener;
    private Button sendEmailBtn;
    private Button sendSMSBtn;

    public FragmentSettings() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
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

    /**
     * This interface must be implemented by activities that contain this fragment to allow an
     * interaction in this fragment to be communicated to the activity and potentially other
     * fragments contained in that activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner timer_spinner = view.findViewById(R.id.timer_spinner);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor edit = sharedPreferences.edit();

        timer_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                timer_selection =
                        timer_spinner.getSelectedItem().toString();
                System.out.println(timer_selection);
                spinnerPos = timer_spinner.getSelectedItemPosition();
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                timer_selection =
                        timer_spinner.getSelectedItem().toString();
                spinnerPos = timer_spinner.getSelectedItemPosition();
            }
        });

        final CheckBox checkBox = view.findViewById(R.id.notifications);
        final LinearLayout notifications_wanted_options_layout =
                view.findViewById(R.id.notifications_desired_layout);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    notifications_wanted_options_layout.setVisibility(View.VISIBLE);
                } else {
                    notifications_wanted_options_layout.setVisibility(View.GONE);
                }
            }
        });

        // Timer interval dropdown widget listener for the user's selection
        timer_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get selected item for storage in SharedPreferences that we can later call to
                // set the proper int time interval. "10 seconds", "30 seconds", or "60 seconds".
                // The final selection will be added to SharedPreferences on press of the Save
                // button
                timer_selection =
                        timer_spinner.getSelectedItem().toString();
                // Print out the selection for testing purposes
                System.out.println(timer_selection);
                // Get selected item position for storage in SharedPreferences so we can set the
                // spinner with the same item again in the onResume method so that the user's
                // last selected preference is saved and displayed.
                // The final selection will be added to SharedPreferences on press of the Save
                // button
                spinnerPos = timer_spinner.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // See comments in onItemSelected
                timer_selection =
                        timer_spinner.getSelectedItem().toString();
                spinnerPos = timer_spinner.getSelectedItemPosition();
            }
        });

        // The EditText views for the email address and SMS phone number input boxes
        emailRecipientInputBox = view.findViewById(R.id.notification_email_input);
        smsNumberInputBox = view.findViewById(R.id.notification_sms_input);

        // Assigning resource values to the Button fields for the Send Email and Send SMS buttons
        // We will call these in the onClick listener of the Save button to enable or disable
        // buttons depending on the validity of the saved input.
        sendEmailBtn = getActivity().findViewById(R.id.sendEmailButton);
        sendSMSBtn = getActivity().findViewById(R.id.sendSMSButton);

        // SAVE button onClick listener
        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view1 -> {

            Toast.makeText(getContext(), "Timer setting saved!", Toast.LENGTH_SHORT).show();

            // The contents of the input box for the email address
            String emailEntry = emailRecipientInputBox.getEditableText().toString();

            // If the input box is empty
            if (emailEntry.isEmpty()) {
                Toast.makeText(getContext(), "No Email Address Entered",
                        Toast.LENGTH_SHORT).show();
                // Do not let the user hit the "Send Email" button due to an invalid entry;
                // This is disabled by default but this needs to be here in case the user
                // formerly entered and saved a valid entry; otherwise, the button will continue
                // to be enabled from before while attempting to save an empty entry.
                sendEmailBtn.setEnabled(false);

                // If the input box is NOT empty
            } else {
                // A pattern and matcher call for the email address format for input validation
                Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2," +
                        "4}");
                Matcher mat = pattern.matcher(emailEntry);
                if (mat.matches()) {
                    // Since the entry is the correct format, set the Email POJO address field
                    // to the entered value and other appropriate message fields
                    email.setRecipient(emailEntry);
                    email.setSubject("Mission Report");
                    email.setBody("Mission Status: Complete");
                    // Allow the user to hit the "Send SMS" button since the number is valid
                    sendEmailBtn.setEnabled(true);

                    // Assign the fields to the getter values of the Email POJO for use in the
                    // sendEmail method
                    emailTo = email.getRecipient();
                    emailSub = email.getSubject();
                    emailBody = email.getBody();

                    // Tell user that the valid email address was saved
                    Toast.makeText(getContext(), "Email address saved!",
                            Toast.LENGTH_SHORT).show();

                    // Save the valid email address to SharedPreferences for later use
                    edit.putString(PREF_EMAIL, emailTo);
                } else {
                    // The entry was not of valid email format; tell the user this
                    Toast.makeText(getContext(), "Invalid email entry",
                            Toast.LENGTH_SHORT).show();
                    // Do not let the user hit the "Send SMS" button due to an invalid entry
                    sendEmailBtn.setEnabled(false);
                }
            }

            // The contents of the input box for the SMS phone number
            String phoneEntry = smsNumberInputBox.getEditableText().toString();

            // If the phone number input box is empty
            if (phoneEntry.isEmpty()) {
                // There is no input; tell the user this
                Toast.makeText(getContext(), "No Phone Number Entered",
                        Toast.LENGTH_SHORT).show();
                // Do not let the user hit the "Send SMS" button due to an invalid entry;
                // This is disabled by default but this needs to be here in case the user
                // formerly entered and saved a valid entry; otherwise, the button will continue
                // to be enabled from before while attempting to save an empty entry.
                sendSMSBtn.setEnabled(false);

            } else {
                // See if the entry is 10 digits in length
                if (phoneEntry.length() == 10) {
                    try {
                        // See if the entry is numeric. Has to be long data type due to max value of
                        // integer data type not being high enough for potential phone number entry
                        Long.parseLong(phoneEntry);

                        // Since the entry is the correct length and numeric, set the SMS POJO phone
                        // number field with the entered value and other appropriate message fields
                        smsMessage.set_phoneNum(phoneEntry);
                        smsMessage.set_messageTxt("Mission Status: Complete");

                        // Allow the user to hit the "Send SMS" button since the number is valid
                        sendSMSBtn.setEnabled(true);

                        // Assign the fields to the getter values of the SMS POJO
                        phoneNum = smsMessage.get_phoneNum();
                        phoneTxt = smsMessage.get_messageTxt();

                        // Tell user that the valid phone number was saved
                        Toast.makeText(getContext(), "Phone number saved!",
                                Toast.LENGTH_SHORT).show();

                        // Save the valid number to SharedPreferences for later use
                        edit.putString(PREF_SMS, phoneNum);
                    } catch (NumberFormatException e) {
                        // The entry contained non-numeric characters; tell them this
                        Toast.makeText(getContext(), "Invalid phone entry." + e.toString(),
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // The entry was not long enough; tell them this
                    Toast.makeText(getContext(), "SMS Number entry must be 10 digits in length",
                            Toast.LENGTH_SHORT).show();
                    // Do not let the user hit the "Send SMS" button due to an invalid entry
                    sendSMSBtn.setEnabled(false);
                }
            }

            // Print out the saved timer selection for testing purposes
            System.out.println(timer_selection);

            // Add the timer selection string and spinner position values to the
            // SharedPreferences "Timer" settings
            edit.putString(PREF_TIMER, timer_selection);
            edit.putInt(PREF_SPINNER_POSITION_TIMER, spinnerPos);
            // Apply the updates to the "Timer" SharedPreferences
            edit.apply();

            // If both the Send Email and Send SMS buttons are enabled (indicating that the saved
            // input is valid), tell the user that the settings were successfully saved.
            if (sendSMSBtn.isEnabled() && sendEmailBtn.isEnabled()) {
                Toast.makeText(getContext(), "Settings saved!", Toast.LENGTH_LONG).show();
            }
        });

        sendEmailBtn = view.findViewById(R.id.sendEmailButton);

        sendEmailBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendEmail();
            }
        });

        sendSMSBtn = view.findViewById(R.id.sendSMSButton);

        sendSMSBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sendSMS();
            }
        });
    }

    /**
     * Takes in the email address from the EditText field and opens the Gmail app installed on the
     * phone. Auto-populates the data so user can send the report
     */
    public void sendEmail() {
        Intent sendReport = new Intent(Intent.ACTION_SENDTO);
        sendReport.setType("text/plain");
        sendReport.setData(Uri.parse("mailto:" + emailTo));
        sendReport.putExtra(Intent.EXTRA_SUBJECT, emailSub);
        sendReport.putExtra(Intent.EXTRA_TEXT, emailBody);
        sendReport.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(sendReport);
    }

    /**
     * Takes in the phone number from the EditText field, auto-populates the text, and sends the
     * text message.
     */

    public void sendSMS() {
        int permissionCheck =
                ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                        Manifest.permission.SEND_SMS);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.SEND_SMS}, REQUEST_SMS);
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNum, null, phoneTxt, null, null);
            Toast.makeText(getContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendSMS();
                } else {
                    Toast.makeText(getContext(),
                            "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //Try and change the spinner and text views to match what is in SharedPreferences
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        Spinner timer_spinner =
                Objects.requireNonNull(getActivity()).findViewById(R.id.timer_spinner);
        spinnerPos = sharedPreferences.getInt(PREF_SPINNER_POSITION_TIMER, 0);
        timer_spinner.setSelection(spinnerPos);

        // Try to fill the email address and SMS phone number EditText input boxes with the data
        // saved in SharedPreferences
        EditText emailRecipientInputBox = getActivity().findViewById(R.id.notification_email_input);
        EditText smsNumberInputBox = getActivity().findViewById(R.id.notification_sms_input);

        String savedEmailAddress = sharedPreferences.getString(PREF_EMAIL, "");
        String savedSmsNumber = sharedPreferences.getString(PREF_SMS, "");

        // Only fill the input box with that value if it is exists, and enable the Send Email button
        if (!savedEmailAddress.isEmpty()) {
            emailRecipientInputBox.setText(savedEmailAddress);
            sendEmailBtn = getActivity().findViewById(R.id.sendEmailButton);
            sendEmailBtn.setEnabled(true);
        }
        // Only fill the input box with that value if it is exists, and enable the Send SMS button
        if (!savedSmsNumber.isEmpty()) {
            smsNumberInputBox.setText(savedSmsNumber);
            sendSMSBtn = getActivity().findViewById(R.id.sendSMSButton);
            sendSMSBtn.setEnabled(true);
        }

    }

}
