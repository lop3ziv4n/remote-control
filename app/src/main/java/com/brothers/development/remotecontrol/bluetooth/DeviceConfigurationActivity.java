package com.brothers.development.remotecontrol.bluetooth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.brothers.development.remotecontrol.R;

/**
 * Created by iv4nlop3z on 16/11/15.
 */
public class DeviceConfigurationActivity extends Activity {

    // Debugging
    private static final String TAG = "DeviceConfigActivity";

    // Return Intent extra
    public static String EXTRA_DEVICE_NAME = "device_name";
    public static String EXTRA_DEVICE_PIN = "device_pin";
    public static int RESULT_ERROR = 99;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // Component view activity
    private TextView txtDeviceName;
    private TextView txtDevicePin;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_configuration);
        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
        // Get device name the parameter extra from MainActivity
        Bundle bundle = getIntent().getExtras();
        mConnectedDeviceName = bundle.getString(EXTRA_DEVICE_NAME);
        Log.d(TAG, "mConnectedDeviceName: " + mConnectedDeviceName);
        // Device name is null, return error
        if (mConnectedDeviceName == null) {
            setResult(DeviceConfigurationActivity.RESULT_ERROR);
            finish();
        }
        // Initializing component
        txtDevicePin = (TextView) findViewById(R.id.text_device_pin);
        txtDeviceName = (TextView) findViewById(R.id.text_device_name);
        txtDeviceName.setText(mConnectedDeviceName);
    }

    /**
     * @param view Button Accept
     */
    public void onAcceptChange(View view) {
        String name = txtDeviceName.getText().toString();
        String pin = txtDevicePin.getText().toString();

        Log.d(TAG, "onAcceptChange --> name: " + name);
        Log.d(TAG, "onAcceptChange --> pin: " + pin);

        // Create the result Intent and include the pin and name device
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_PIN, pin);
        intent.putExtra(EXTRA_DEVICE_NAME, name);

        setResult(Activity.RESULT_OK, intent);

        finish();
    }

    /**
     * @param view Button Cancel
     */
    public void onCancelChange(View view) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    /**
     * @param view CheckBox
     */
    public void onCheckClicked(View view) {
        // Is the view now checked?
        if (((CheckBox) view).isChecked()) {
            // Enabled TextView device pin
            findViewById(R.id.text_device_pin).setEnabled(true);
        } else {
            // Disabled TextView device pin
            findViewById(R.id.text_device_pin).setEnabled(false);
        }
    }
}
