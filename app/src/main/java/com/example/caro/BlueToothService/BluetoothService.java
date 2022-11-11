

package com.example.caro.BlueToothService;


import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_CHAT;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_IMAGE;
import static com.example.caro.Activity.GameBluetoothActivity.mGameHandler;
import static com.example.caro.Activity.GameBluetoothActivity.success;
import static com.example.caro.Activity.ListRoomActivity.mHandler;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_BLUETOOTH;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_GAME;
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
    public static  final String SEND_FEEDBACK="DANHAN";
    public static final String SEND_STRING = "STRING";
    public static final String SEND_INT = "INTTTT";
    public static final String SEND_IMAGE = "IMAGEE";
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

        // Khi gửi đi 1 message thì đợi cho đến khi có thông điệp gửi lại để tiếp tục gửi lần tiếp theo
        private boolean isSent = true;

        synchronized boolean get() {
            return isSent;
        }

        synchronized void set(boolean l) {
            isSent = l;
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
            String type="";
            while (true) {
                if (flag) {
                    try {
                        byte[] temp = new byte[512];
                        int bytes = mmInStream.read(temp);
                        if (bytes > 0) {
                            byte[]  typeMessage=Arrays.copyOfRange(temp,0,6);
                            String TypeOfMessage=new String(typeMessage);
                            String Content=new String(temp,0,bytes).substring(6);

                            if (TypeOfMessage.equals(SEND_FEEDBACK)) {
                                //    Log.d(TAG, "Nhan phan hoi thanh cong");
                                //Đặt lại trạng thái có thể gửi thông điệp tiếp theo
                                set(true);
                                continue;
                            }
                            else if(TypeOfMessage.equals(SEND_INT))
                            {
                                mGameHandler.obtainMessage(MESSAGE_IMAGE,Integer.valueOf(Content)).sendToTarget();
                            }
                            else  if(TypeOfMessage.equals(SEND_STRING))
                            {
                                mGameHandler.obtainMessage(MESSAGE_CHAT,Content).sendToTarget();
                            }

                            else if(TypeOfMessage.equals(SEND_IMAGE)) {
                                numberOfBytes = Integer.parseInt(Content);
                                Log.d(TAG, "" + numberOfBytes);
                                buffer = new byte[numberOfBytes];
                                flag=false;
                          //      Log.d(TAG, "nhan ne");

                            }
                            mConnectedThread.write("ok".getBytes(StandardCharsets.UTF_8));
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

    public void sendImage(byte[] buffer) {
        int lengthBuffer=buffer.length;
        int length1=SEND_IMAGE.getBytes(StandardCharsets.UTF_8).length;
        int length2=String.valueOf(lengthBuffer).getBytes(StandardCharsets.UTF_8).length;
        byte[] temp=new byte[length1+length2];
        System.arraycopy(SEND_IMAGE.getBytes(StandardCharsets.UTF_8),0,temp,0,length1);
        System.arraycopy(String.valueOf(lengthBuffer).getBytes(StandardCharsets.UTF_8),0,temp,length1,length2);
        while (!mConnectedThread.get()) ;
        mConnectedThread.write(temp);
        mConnectedThread.set(false);
        Log.d(TAG,buffer.length+"");
        int subArraySize = 800;
        for(int i=0;i<buffer.length;i+=subArraySize){
            while(!mConnectedThread.get());
            Log.d(TAG,i+"");
            byte[] tempArray;
            tempArray=Arrays.copyOfRange(buffer,i,Math.min(buffer.length,i+subArraySize));
            mConnectedThread.write(tempArray);
            mConnectedThread.set(false);
        }
    }

    public void sendString(String string) {
        byte[] buffer;
        int length1=SEND_STRING.getBytes(StandardCharsets.UTF_8).length;
        int length2=string.getBytes(StandardCharsets.UTF_8).length;
        buffer=new byte[length1+length2];
        System.arraycopy(SEND_STRING.getBytes(StandardCharsets.UTF_8),0,buffer,0,length1);
        System.arraycopy(string.getBytes(StandardCharsets.UTF_8),0,buffer,length1,length2);
        while (!mConnectedThread.get()) ;
        mConnectedThread.write(buffer);
    }

    public void sendInt(int number) {
        byte[] buffer;
        int length1=SEND_STRING.getBytes(StandardCharsets.UTF_8).length;
        int length2=String.valueOf(number).getBytes(StandardCharsets.UTF_8).length;
        buffer=new byte[length1+length2];
        System.arraycopy(SEND_STRING.getBytes(StandardCharsets.UTF_8),0,buffer,0,length1);
        System.arraycopy(String.valueOf(number).getBytes(StandardCharsets.UTF_8),0,buffer,length1,length2);
        while (!mConnectedThread.get()) ;
        mConnectedThread.write(buffer);
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





