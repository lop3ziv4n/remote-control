package com.brothers.development.remotecontrol.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.brothers.development.remotecontrol.communication.TransmissionService;
import com.brothers.development.remotecontrol.message.Request;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;


/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService {

    // Debugging
    private static final String TAG = "BluetoothService";

    // Unique UUID for this application
    private UUID MY_UUID;

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private TransmissionService mTransmissionService;
    private int mState;

    /**
     * Constructor. Prepares a new Bluetooth session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = Constants.STATE_NONE;
        mHandler = handler;
    }

    /**
     * Set the current state of the connection
     *
     * @param state An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        Log.d(TAG, "setState: " + mState + " -> " + state);
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start()");
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mTransmissionService != null) {
            mTransmissionService.cancel();
            mTransmissionService = null;
        }
        setState(Constants.STATE_LISTEN);
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect: " + device);
        // Cancel any thread attempting to make a connection
        if (mState == Constants.STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (mTransmissionService != null) {
            mTransmissionService.cancel();
            mTransmissionService = null;
        }
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(Constants.STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected socket and device");
        // Cancel the thread that completed the connection
        cancelConnect();
        // Start the thread to manage the connection and perform transmissions
        mTransmissionService = new TransmissionService(socket, mHandler);

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(Constants.STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop()");
        cancelConnect();
        setState(Constants.STATE_NONE);
    }

    /**
     *  Stop all threads
     */
    private void cancelConnect() {
        Log.d(TAG, "cancelConnect()");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mTransmissionService != null) {
            mTransmissionService.cancel();
            mTransmissionService = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
    }

    /**
     * Write to the TransmissionService in an unsynchronized manner
     *
     * @param request The Request to write
     * @see TransmissionService#write(Request)
     */
    public void write(Request request) {
        Log.d(TAG, "write: " + request.getMessage());
        // Create temporary object
        TransmissionService r;
        // Synchronize a copy of the TransmissionService
        synchronized (this) {
            if (mState != Constants.STATE_CONNECTED) return;
            r = mTransmissionService;
        }
        // Perform the write unsynchronized
        r.write(request);
    }

    /**
     * read to the TransmissionService in an unsynchronized manner
     */
    public void read() {
        Log.d(TAG, "read()");
        // Create temporary object
        TransmissionService r;
        // Synchronize a copy of the TransmissionService
        synchronized (this) {
            if (mState != Constants.STATE_CONNECTED) return;
            r = mTransmissionService;
        }
        // Perform the read unsynchronized
        r.read();
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        Log.d(TAG, "connectionFailed()");
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        // Start the service over to restart listening mode
        BluetoothService.this.start();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        /**
         *
         */
        public AcceptThread() {
            Log.d(TAG, "AcceptThread");
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                // Using the well-known SPP UUID
                MY_UUID = UUID.fromString(Constants.UUID_STRING_WELL_KNOWN_SPP);
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME_SERVER, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed: ", e);
            }
            mmServerSocket = tmp;
        }

        /**
         *
         */
        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread " + this);
            setName("AcceptThread");
            BluetoothSocket socket;
            // Listen to the server socket if we're not connected
            while (mState != Constants.STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed: ", e);
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case Constants.STATE_LISTEN:
                            case Constants.STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case Constants.STATE_NONE:
                            case Constants.STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket: ", e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        /**
         *
         */
        public void cancel() {
            Log.d(TAG, "ServerSocket " + mmServerSocket + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed: ", e);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        // The local server socket and device
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        // Fix Bug version android 4.2
        private BluetoothSocket mmSocketFall;
        private boolean mmUseFall = false;

        /**
         * @param device type BluetoothDevice
         */
        public ConnectThread(BluetoothDevice device) {
            Log.d(TAG, "ConnectThread");
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                // Using the well-known SPP UUID
                MY_UUID = UUID.fromString(Constants.UUID_STRING_WELL_KNOWN_SPP);
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed: ", e);
            }
            mmSocket = tmp;
        }

        /**
         *
         */
        public void run() {
            Log.d(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
                // Fix Bug version android 4.2
                mmUseFall = false;
            } catch (IOException e) {
                Log.e(TAG, "Connection failure: ", e);
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure: ", e2);
                }
                // Fix Bug version android 4.2
                Class<?> clazz = mmSocket.getRemoteDevice().getClass();
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                try {
                    Method m = clazz.getMethod("createRfcommSocket", paramTypes);
                    Object[] params = new Object[] {1};
                    mmSocketFall = (BluetoothSocket) m.invoke(mmSocket.getRemoteDevice(), params);
                    // This sleep for input pin
                    // BTTest pin 0290
                    Thread.sleep(500);
                    mmSocketFall.connect();
                    mmUseFall = true;
                } catch (Exception e2) {
                    Log.e(TAG, "Connection Fall failure: ", e2);
                    // Close the socket
                    try {
                        mmSocketFall.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "unable to close() socket during connection failure: ", e3);
                    }
                    connectionFailed();
                    return;
                }

                /*  TODO: Fix Bug version android 4.2
                    java.io.IOException: read failed, socket might closed or timeout, read ret: -1
                    at android.bluetooth.BluetoothSocket.readAll(BluetoothSocket.java:517)
                    at android.bluetooth.BluetoothSocket.readInt(BluetoothSocket.java:528)
                    at android.bluetooth.BluetoothSocket.connect(BluetoothSocket.java:320)
                    at com.brothers.development.remotecontrol.bluetooth.BluetoothService$ConnectThread.run(BluetoothService.java:255)
                */
            }
            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            // Fix Bug version android 4.2
            if (mmUseFall) {
                connected(mmSocketFall, mmDevice);
            } else {
                connected(mmSocket, mmDevice);
            }
        }

        /**
         *
         */
        public void cancel() {
            Log.d(TAG, "Socket " + mmSocket + "cancel " + this);
            try {
                // Fix Bug version android 4.2
                if (mmUseFall) {
                    mmSocketFall.close();
                } else {
                    mmSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed: ", e);
            }
        }
    }
}