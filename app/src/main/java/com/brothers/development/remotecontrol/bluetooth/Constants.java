package com.brothers.development.remotecontrol.bluetooth;

/**
 * Defines several constants used between {@link BluetoothService} and the UI.
 */
public interface Constants {

    // Message types sent from the BluetoothService Handler
    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_WRITE = 3;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;
    int MESSAGE_READ_TRANSMISSION = 6;
    int MESSAGE_NO_READ_TRANSMISSION = 7;

    // Intent request codes
    int REQUEST_CONNECT_DEVICE = 0;
    int REQUEST_ENABLE_BT = 1;
    int REQUEST_CONFIGURATION_DEVICE = 2;

    // Key names received from the BluetoothService Handler
    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    // Constants that indicate the current connection state
    int STATE_NONE = 0;       // we're doing nothing
    int STATE_LISTEN = 1;     // now listening for incoming connections
    int STATE_CONNECTING = 2; // now initiating an outgoing connection
    int STATE_CONNECTED = 3;  // now connected to a remote device

    // The constants that indicate the status of the received message
    int MESSAGE_OK = 0;
    int MESSAGE_NOK = 1;

    // Name for the SDP record when creating server socket
    String NAME_SERVER = "BluetoothService";
    // Using the well-known SPP UUID
    String UUID_STRING_WELL_KNOWN_SPP = "8ce255c0-200a-11e0-ac64-0800200c9a66";

}
