package com.brothers.development.remotecontrol.communication;


import android.util.Log;

import com.brothers.development.remotecontrol.bluetooth.BluetoothService;
import com.brothers.development.remotecontrol.bluetooth.Constants;
import com.brothers.development.remotecontrol.message.Timeout;

/**
 * Created by iv4nlop3z on 20/12/15.
 */
public class PingStatusDeviceService {

    // Debugging
    private static final String TAG = "PingStatusDeviceService";

    // Bluetooth services
    private BluetoothService bluetoothService = null;
    // Timeout message
    private Timeout timeout = null;

    /**
     * @param bluetoothService type BluetoothService
     */
    public PingStatusDeviceService(BluetoothService bluetoothService) {
        this.bluetoothService = bluetoothService;
    }

    /**
     * Start send message timeout.
     */
    public void start() {
        Log.d(TAG, "start()");
        Thread thread = new Thread(new Runnable() {

            public void run() {
                // Check that we're actually connected before trying anything
                if (bluetoothService.getState() == Constants.STATE_CONNECTED) {
                    timeout = new Timeout();
                    // Get the message request and tell the BluetoothService to write
                    bluetoothService.write(timeout);
                    // Get reply the BluetoothService
                    bluetoothService.read();
                }
            }
        });

        while (true) {
            thread.start();
            try {
                synchronized (thread) {
                    thread.wait(5000);
                }

            } catch (InterruptedException e) {
                Log.e(TAG, "Exception during read: ", e);
            }
        }
    }
}
