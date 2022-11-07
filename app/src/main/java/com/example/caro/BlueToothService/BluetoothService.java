

package com.example.caro.BlueToothService;



import  static  com.example.caro.Activity.GameActivity.mGameHandler;
import  static com.example.caro.Activity.GameActivity.success;
import  static  com.example.caro.Activity.ListRoomActivity.mHandler;
import  static  com.example.caro.Activity.GameActivity.MESSAGE_BLUETOOTH;
import  static  com.example.caro.Activity.GameActivity.MESSAGE_GAME;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.example.caro.Activity.ListRoomActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothService {

    private static final String TAG = "MainActivity";

    private static final String appName = "MYAPP";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public final BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public BluetoothService(Context context) {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        @SuppressLint("MissingPermission")
        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            }catch (IOException e){
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage() );
            }

            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread Running.");
            BluetoothSocket socket = null;
            while (true) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    Log.d(TAG, "run: RFCOM server socket start.....");

                    socket = mmServerSocket.accept();

                    Log.d(TAG, "run: RFCOM server socket accepted connection.");

                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
                    break;
                }

                //talk about this is in the 3rd
                if (socket != null) {
                    mGameHandler.obtainMessage(MESSAGE_BLUETOOTH,1,success).sendToTarget();
                    connected(socket);
                    try {
                        mmServerSocket.close();
                        Log.d(TAG,"Close succesfully");
                    } catch (IOException e) {
                        Log.d(TAG,"cath close thread_server");
                        e.printStackTrace();
                    }
                    break;
                }
                Log.i(TAG, "END mAcceptThread ");
            }
        }

        public void cancel() {
            Log.d(TAG, "cancel: Canceling AcceptThread.");
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage() );
            }
        }

    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
            if(mmSocket==null)
            {
                mHandler.obtainMessage(ListRoomActivity.FALSE_CREATING);
            }
        }

        @SuppressLint("MissingPermission")
        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            mHandler.obtainMessage(ListRoomActivity.GAME_CREATING).sendToTarget();
            connected(mmSocket);

        }

        public void cancel() {
            try {
                mmSocket.close();
                Log.d(TAG,"client close thread");
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    public synchronized void start() {
        Log.d(TAG, " server start");
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
        }
        mInsecureAcceptThread = new AcceptThread();
        mInsecureAcceptThread.start();
    }

    public void startClient(BluetoothDevice device){
        Log.d(TAG, "startClient: Started.");
        if (mConnectThread != null) {
            mConnectThread.cancel();
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes=0; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {

                    mmOutStream.write(buffer);
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    mGameHandler.obtainMessage(MESSAGE_GAME,Integer.valueOf(incomingMessage)).sendToTarget();
                    //MainActivity.handler.obtainMessage(3,incomingMessage).sendToTarget();
                    //Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage() );
                    break;
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage() );
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }
    public void write(byte[] out) {
        mConnectedThread.write(out);
    }
    public void Disconnect()
    {
        if(mConnectedThread!=null)
        {
            mConnectedThread.cancel();
        }
        if(mInsecureAcceptThread!=null)
        {
            mInsecureAcceptThread.cancel();
        }
    }
}





