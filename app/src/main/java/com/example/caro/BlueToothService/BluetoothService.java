

package com.example.caro.BlueToothService;


import static com.example.caro.Activity.GameActivity.MESSAGE_CHAT;
import static com.example.caro.Activity.GameActivity.MESSAGE_IMAGE;
import static com.example.caro.Activity.GameActivity.mGameHandler;
import static com.example.caro.Activity.GameActivity.success;
import static com.example.caro.Activity.ListRoomActivity.mHandler;
import static com.example.caro.Activity.GameActivity.MESSAGE_BLUETOOTH;
import static com.example.caro.Activity.GameActivity.MESSAGE_GAME;
import static com.example.caro.Activity.MenuGameActivity.mBluetoothService;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.caro.Activity.ListRoomActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothService {

    public static final String SEND_STRING = "STRING";
    public static final String SEND_INT = "INT";
    public static final String SEND_IMAGE = "IMAGE";

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
        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
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
                    mGameHandler.obtainMessage(MESSAGE_BLUETOOTH, 1, success).sendToTarget();
                    connected(socket);
                    try {
                        mmServerSocket.close();
                        //      Log.d(TAG,"Close succesfully");
                    } catch (IOException e) {
                        Log.d(TAG, "cath close thread_server");
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
                Log.e(TAG, "cancel: Close of AcceptThread ServerSocket failed. " + e.getMessage());
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
            if (mmSocket == null) {
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
                Log.d(TAG, "client close thread");
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

    public void startClient(BluetoothDevice device) {
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
        boolean isSuccesful = true;

        synchronized boolean get() {
            return isSuccesful;
        }

        synchronized void set(boolean l) {
            isSuccesful = l;
        }

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

        public void run() {
            byte[] buffer = null;
            int numberOfBytes = 0;
            int index = 0;
            boolean flag = true;

            while (true) {
                if (flag) {
                    try {
                        byte[] temp = new byte[512];
                        int k = mmInStream.read(temp);
                        if (k > 0) {
                            if (new String(temp, 0, k).equals("ok")) {
                            //    Log.d(TAG, "Nhan phan hoi thanh cong");
                                set(true);
                            } else {
                                numberOfBytes = Integer.parseInt(new String(temp, 0, k));
                                Log.d(TAG, "" + numberOfBytes);
                                buffer = new byte[numberOfBytes];
                                 flag=false;
                          //      Log.d(TAG, "nhan ne");
                                mConnectedThread.write("ok".getBytes(StandardCharsets.UTF_8));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG,"loi");
                    }
                } else {
                    try {
                        byte[] data = new byte[800];
                        int numbers = mmInStream.read(data);
                        System.arraycopy(data, 0, buffer, index, numbers);
                        index = index + numbers;
                        Log.d(TAG,index+"");
                        if (index == numberOfBytes) {
                            mGameHandler.obtainMessage(MESSAGE_IMAGE, numberOfBytes, -1, buffer).sendToTarget();
                            flag = true;
                        }
                        mConnectedThread.write("ok".getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG,"loi");
                    }
                }
            }
        }


//        public void run() {
//            byte[] buffer = new byte[1024];  // buffer store for the stream
//            byte[] bufferImage = null;
//            boolean flag = true;
//            int bytes = 0; // bytes returned from read()
//            String sendingType = "";
//
//            while (true) {
//                try {
                    //mmOutStream.write(buffer);
//                    bytes = mmInStream.read(buffer);
//                    Log.d(TAG, new String(buffer, 0, bytes));
//                    if (flag) {
//                        sendingType = new String(buffer, 0, bytes);
//                        flag = false;
//                    }
//                    if (sendingType.equals(SEND_INT)) {
//                        bytes = mmInStream.read(buffer);
//                        mGameHandler.obtainMessage(MESSAGE_GAME, Integer.valueOf(new String(buffer, 0, bytes))).sendToTarget();
//                        flag = true;
//                    } else if (sendingType.equals(SEND_STRING)) {
//                        bytes = mmInStream.read(buffer);
//                        mGameHandler.obtainMessage(MESSAGE_CHAT, new String(buffer, 0, bytes)).sendToTarget();
//                        flag = true;
//                    } else {
//                        byte[] temp = new byte[1024];
//                        int k = mmInStream.read(temp);
//                        int numberofBytes = Integer.parseInt(new String(temp, 0, k));
//                        Log.d(TAG, "" + numberofBytes);
//                        int index = 0;
//                        bufferImage = new byte[numberofBytes];
//                        while (true) {
//                            try {
//                                byte[] data = new byte[8192];
//                                int number = mmInStream.read(data);
//                                Log.d(TAG,number+"");
//                                System.arraycopy(data, 0, bufferImage, index, number);
//                                index += number;
//                            } catch (Exception e) {
//
//                                if (index == numberofBytes) {
//                                    Log.d(TAG, index + "loi");
//                                    mGameHandler.obtainMessage(MESSAGE_IMAGE, numberofBytes, -1, bufferImage);
//                                    break;
//                                }
//                            }
//                        }
//                        flag = true;
//                    }


                    //
                    //String incomingMessage =
                    //  mGameHandler.obtainMessage(MESSAGE_GAME,Integer.valueOf(incomingMessage)).sendToTarget();
                    //MainActivity.handler.obtainMessage(3,incomingMessage).sendToTarget();
                    //Log.d(TAG, "InputStream: " + incomingMessage);
//                } catch (IOException e) {
//                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
//                    break;
//                }
//
//            }




        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
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

    public void sendImage(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.WEBP, 10, stream);
        byte[] imageBytes = stream.toByteArray();
        Log.d(TAG, imageBytes.length + "");
        int subArraySize = 800;
       while (!mConnectedThread.get()) ;
        mConnectedThread.write(String.valueOf(imageBytes.length).getBytes(StandardCharsets.UTF_8));
        mConnectedThread.set(false);
        for(int i=0;i<imageBytes.length;i+=subArraySize){
            while(!mConnectedThread.get());
            Log.d(TAG,i+"");
            byte[] tempArray;
            tempArray=Arrays.copyOfRange(imageBytes,i,Math.min(imageBytes.length,i+subArraySize));
            mConnectedThread.write(tempArray);
            mConnectedThread.set(false);
        }
    }

//    public void sendImage(Bitmap bitmap) {
//        //send kieu image
//        mConnectedThread.write(SEND_IMAGE.getBytes(StandardCharsets.UTF_8));
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 40, stream);
//        byte[] imageBytes = stream.toByteArray();
//        int subArraySize = 8192;
//        mConnectedThread.write(String.valueOf(imageBytes.length).getBytes(StandardCharsets.UTF_8));
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        for (int i = 0; i < imageBytes.length; i += subArraySize) {
//            byte[] tempArray;
//            tempArray = Arrays.copyOfRange(imageBytes, i, Math.min(imageBytes.length, i + subArraySize));
//            mConnectedThread.write(tempArray);
//        }
//    }

    public void sendString(String string) {
        mConnectedThread.write(SEND_STRING.getBytes(StandardCharsets.UTF_8));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, string.getBytes(StandardCharsets.UTF_8).length + "");
        mBluetoothService.write(string.getBytes(StandardCharsets.UTF_8));
    }

    public void sendInt(int number) {
        mConnectedThread.write(SEND_INT.getBytes(StandardCharsets.UTF_8));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mConnectedThread.write(String.valueOf(number).getBytes(StandardCharsets.UTF_8));
    }

    public void Disconnect() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
        }
    }
}





