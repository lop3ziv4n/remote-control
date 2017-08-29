package com.brothers.development.remotecontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.brothers.development.remotecontrol.bluetooth.BluetoothService;
import com.brothers.development.remotecontrol.bluetooth.Constants;
import com.brothers.development.remotecontrol.bluetooth.DeviceConfigurationActivity;
import com.brothers.development.remotecontrol.bluetooth.DeviceListActivity;
import com.brothers.development.remotecontrol.joystick.JoystickMovedListener;
import com.brothers.development.remotecontrol.joystick.JoystickView;
import com.brothers.development.remotecontrol.message.Motion;
import com.brothers.development.remotecontrol.message.NameDevice;
import com.brothers.development.remotecontrol.message.PinDevice;
import com.brothers.development.remotecontrol.message.Request;
import com.brothers.development.remotecontrol.message.TypeMessage;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    // Debugging
    private static final String TAG = "MainActivity";

    // Bluetooth adapter
    private BluetoothAdapter bluetoothAdapter = null;

    // Bluetooth services
    private BluetoothService bluetoothService = null;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // Joystick
    private TextView txtViewX, txtViewY;

    /**
     * @param savedInstanceState Bundle savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Get Text View
        txtViewX = (TextView)findViewById(R.id.TextViewX);
        txtViewY = (TextView)findViewById(R.id.TextViewY);
        // Init JoystickView
        JoystickView joystickView = (JoystickView)findViewById(R.id.joystickView);
        joystickView.setOnJostickMovedListener(_listener);
        // Get local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), R.string.not_available_bluetooth, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     *
     */
    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupService() will then be called during onActivityResult
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (bluetoothService == null) {
            setupService();
        }
    }

    /**
     *
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    /**
     *
     */
    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (bluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetoothService.getState() == Constants.STATE_NONE) {
                // Start the Bluetooth chat services
                bluetoothService.start();
            }
        }
    }

    /**
     * Set up the UI and background operations.
     */
    private void setupService() {
        Log.d(TAG, "setupService()");
        // Initialize the BluetoothService to perform bluetooth connections
        bluetoothService = new BluetoothService(getApplicationContext(), mHandler);
    }

    /**
     * Sends a message.
     *
     * @param request A Request to send.
     */
    private void sendMessage(Request request) {
        Log.d(TAG, "sendMessage: " + request.getMessage());
        // Check that we're actually connected before trying anything
        if (bluetoothService.getState() != Constants.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        // Check that there's actually something to send
        if (!request.getMessage().isEmpty()) {
            // Get the message request and tell the BluetoothService to write
            bluetoothService.write(request);
            // Get reply the BluetoothService
            if(!request.getTypeMessage().equals(TypeMessage.MOTION)){
                bluetoothService.read();
            }
        }
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        Log.d(TAG, "setStatus subTitle: " + subTitle);
        final ActionBar actionBar =  getSupportActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: " + msg.what);
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case Constants.STATE_CONNECTING:
                            setStatus(getString(R.string.title_connecting));
                            break;
                        case Constants.STATE_LISTEN:
                        case Constants.STATE_NONE:
                            setStatus(getString(R.string.title_not_connected));
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE: " + msg.obj);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    Log.d(TAG, "MESSAGE_READ: " + readMessage);;
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case Constants.MESSAGE_READ_TRANSMISSION:
                    Log.d(TAG, "MESSAGE_READ_TRANSMISSION" + msg.arg1);
                    switch (msg.arg1) {
                        case Constants.MESSAGE_OK:
                            Toast.makeText(getApplicationContext(), R.string.applied_changes, Toast.LENGTH_SHORT).show();
                            break;
                        case Constants.MESSAGE_NOK:
                            Toast.makeText(getApplicationContext(), R.string.not_applied_changes, Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case Constants.MESSAGE_NO_READ_TRANSMISSION:
                    // Error Connection Device
                    Toast.makeText(getApplicationContext(), R.string.connection_lost, Toast.LENGTH_SHORT).show();
                    // Close Connection!!!
                    break;

            }
        }
    };

    /**
     * Return DeviceListActivity
     *
     * @param requestCode int requestCode
     * @param resultCode int resultCode
     * @param data Intent data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        switch (requestCode) {
            case Constants.REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Start connection device
                    connectDevice(data);
                }
                break;
            case Constants.REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled
                    setupService();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case Constants.REQUEST_CONFIGURATION_DEVICE:
                // When DeviceConfigurationActivity returns with a name and pin device for change
                if (resultCode == Activity.RESULT_OK) {
                    // Do changes in the device
                    configurationDevice(data);
                }
                if (resultCode == DeviceConfigurationActivity.RESULT_ERROR){
                    // Error occurred
                    Log.d(TAG, "Not connected to the device");
                    Toast.makeText(getApplicationContext(), R.string.not_connected_device, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Change configuration device
     *
     * @param data    An {@link Intent} with {@link DeviceConfigurationActivity#EXTRA_DEVICE_NAME,
     *                                              DeviceConfigurationActivity#EXTRA_DEVICE_PIN} extra.
     */
    private void configurationDevice(Intent data) {
        // Get the device name
        String name = data.getExtras().getString(DeviceConfigurationActivity.EXTRA_DEVICE_NAME);
        // Get the device pin
        String pin = data.getExtras().getString(DeviceConfigurationActivity.EXTRA_DEVICE_PIN);

        Log.d(TAG, "configurationDevice --> name: " + name);
        Log.d(TAG, "configurationDevice --> pin: " + pin);

        if (!name.equals(mConnectedDeviceName)) {
            NameDevice nameDevice = new NameDevice(name);
            sendMessage(nameDevice);
        }

        if (!pin.isEmpty()) {
            PinDevice pinDevice = new PinDevice(pin);
            sendMessage(pinDevice);
        }
    }

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     */
    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        Log.d(TAG, "connectDevice: " + address);
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        bluetoothService.connect(device);
    }

    /**
     * @param menu Menu menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Start Menu");
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * @param item MenuItem item
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent;
        switch (item.getItemId()) {
            case R.id.connect_scan:
                // Launch the DeviceListActivity to see devices and do scan
                serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, Constants.REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.configuration_device:
                // Launch the DeviceConfigurationActivity to see configuration device
                serverIntent = new Intent(this, DeviceConfigurationActivity.class);
                serverIntent.putExtra(DeviceConfigurationActivity.EXTRA_DEVICE_NAME, mConnectedDeviceName);
                startActivityForResult(serverIntent, Constants.REQUEST_CONFIGURATION_DEVICE);
                return true;
            default: return super.onOptionsItemSelected(item);

        }
    }

    /**
     *
     */
    private JoystickMovedListener _listener = new JoystickMovedListener() {

        /**
         * @param pan int pan
         * @param tilt int tilt
         */
        @Override
        public void OnMoved(int pan, int tilt) {
            txtViewX.setText(Integer.toString(pan));
            txtViewY.setText(Integer.toString(tilt));
        }

        /**
         *
         */
        @Override
        public void OnReleased() {
            txtViewX.setText("released");
            txtViewY.setText("released");
        }

        /**
         *
         */
        public void OnReturnedToCenter() {
            txtViewX.setText("stopped");
            txtViewY.setText("stopped");
        };
    };
}
