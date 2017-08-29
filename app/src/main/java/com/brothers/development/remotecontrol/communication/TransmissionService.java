package com.brothers.development.remotecontrol.communication;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.brothers.development.remotecontrol.bluetooth.BluetoothService;
import com.brothers.development.remotecontrol.bluetooth.Constants;
import com.brothers.development.remotecontrol.message.Request;
import com.brothers.development.remotecontrol.message.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by iv4nlop3z on 06/12/15.
 *
 * This thread runs during a connection with a remote device.
 * It handles all incoming and outgoing transmissions.
 */
public class TransmissionService {

    // Debugging
    private static final String TAG = "TransmissionService";

    // The local server socket, input and output stream
    private final BluetoothSocket mmSocket;
    private final Handler mHandler;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    /**
     * @param socket type BluetoothSocket
     * @param handler type Handler
     */
    public TransmissionService(BluetoothSocket socket, Handler handler) {
        Log.d(TAG, "create TransmissionService");
        mmSocket = socket;
        mHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the BluetoothSocket input and output streams
        Log.d(TAG, "mmSocket Output-Input Stream");
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created: ", e);
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    /**
     * Read to the connected InputStream.
     */
    public void read() {
        Log.d(TAG, "read()");
        Thread thread = new Thread(new Runnable() {
            byte[] buffer = new byte[1024];
            int bytes;

            public void run() {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Response InputStream
                    Response response = new Response(buffer, bytes);
                    // switch for type message
                    switch (response.getTypeMessage()) {
                        case TIMEOUT:
                        case MOTION:
                        case CHANGE_NAME_DEVICE:
                            // Share the obtained bytes to the UI Activity
                            mHandler.obtainMessage(Constants.MESSAGE_READ_TRANSMISSION,
                                    response.isMessageOk() ? Constants.MESSAGE_OK : Constants.MESSAGE_NOK,
                                    -1,
                                    response.getMessage()).sendToTarget();
                            break;
                        case CHANGE_PIN_DEVICE:
                            // Share the obtained bytes to the UI Activity
                            mHandler.obtainMessage(Constants.MESSAGE_READ_TRANSMISSION,
                                    response.isMessageOk() ? Constants.MESSAGE_OK : Constants.MESSAGE_NOK,
                                    -1,
                                    response.getMessage()).sendToTarget();
                            break;
                        default:
                            // Share the obtained bytes to the UI Activity
                            mHandler.obtainMessage(Constants.MESSAGE_NO_READ_TRANSMISSION, -1, -1, -1).sendToTarget();
                            break;
                    }

                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "Exception during read: ", e);
                    connectionLost();
                }
            }
        });

        thread.start();
        try {
            synchronized(thread) {
                thread.wait( 1000 );
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "Exception during read: ", e);
            connectionLost();
        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param request type Request
     */
    public void write(Request request) {
        try {
            Log.d(TAG, "write");
            // Write from the OutputStream
            mmOutStream.write(BuilderDatagram.buildDatagram(request));
            // Share the sent message back to the UI Activity
            mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, request.getMessage()).sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "Exception during write: ", e);
            connectionLost();
        }
    }

    /**
     *
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed: ", e);
        }
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        Log.d(TAG, "connectionLost()");
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        // Start the service over to restart listening mode
        new BluetoothService(null, mHandler).start();
    }

}
