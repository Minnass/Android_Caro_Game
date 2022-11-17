

package com.example.caro.BlueToothService;


import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_ACCEPT;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_AGAIN;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_EXIT;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_IMAGE_AVATAR;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_IMAGE_CHAT;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_POSTION;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_STRING_CHAT;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_STRING_NAME;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_YOU_LOSE;
import static com.example.caro.Activity.GameBluetoothActivity.mGameHandler;
import static com.example.caro.Activity.GameBluetoothActivity.success;
import static com.example.caro.Activity.ListRoomActivity.mHandler;
import static com.example.caro.Activity.GameBluetoothActivity.MESSAGE_BLUETOOTH;

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
import com.example.caro.Caro.Player;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothService {

    public static final String SEND_FEEDBACK = "@@@@@1";
    public static final String SEND_INT_POSTION = "@@@@@2";
    public static final String SEND_STRING_CHAT = "@@@@@3";
    public static final String SEND_STRING_NAME = "@@@@@4";
    public static final String SEND_IMAGE_CHAT = "@@@@@5";
    public static final String SEND_IMAGE_AVATAR = "@@@@@6";
    public static  final String SEND_PLAY_AGAIN="@@@@@7";
    public static  final String SEND_ACCEPT="@@@@@8";
    public static final String SEND_EXIT="@@@@10";
    public static final String SEND_YOU_LOSE= "@@@@11";

    private static final String appName = "MYAPP";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    public final BluetoothAdapter mBluetoothAdapter;
    Context mContext;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    public ConnectedThread mConnectedThread;

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

            } catch (IOException e) {

            }
            mmServerSocket = tmp;
        }

        public void run() {

            BluetoothSocket socket = null;
            while (true) {
                try {


                    socket = mmServerSocket.accept();


                } catch (IOException e) {

                    break;
                }

                //talk about this is in the 3rd
                if (socket != null) {
                    mGameHandler.obtainMessage(MESSAGE_BLUETOOTH, 1, success).sendToTarget();
                    connected(socket);
                    try {
                        mmServerSocket.close();

                    } catch (IOException e) {

                        e.printStackTrace();
                    }
                    break;
                }

            }
        }

        public void cancel() {

            try {
                mmServerSocket.close();
            } catch (IOException e) {

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

                }
                return;
            }
            mHandler.obtainMessage(ListRoomActivity.GAME_CREATING).sendToTarget();
            connected(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();

            } catch (IOException e) {

            }
        }
    }

    public synchronized void start() {
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
        }
        Log.d("Main","start server");
        mInsecureAcceptThread = new AcceptThread();
        mInsecureAcceptThread.start();
    }

    public void startClient(BluetoothDevice device) {
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
            String TypeOfMessage = "";
            while (true) {
                if (flag) {
                    try {
                        byte[] temp = new byte[512];
                        int bytes = mmInStream.read(temp);
                        if (bytes > 0) {
                            TypeOfMessage = new String(temp, 0, bytes).substring(0, 6);
                            String Content = new String(temp, 0, bytes).substring(6);
                            if (TypeOfMessage.equals(SEND_FEEDBACK)) {
                                set(true);
                                continue;
                            } else if (TypeOfMessage.equals(SEND_INT_POSTION)) {
                                mGameHandler.obtainMessage(MESSAGE_POSTION, Integer.valueOf(Content)).sendToTarget();
                            } else if (TypeOfMessage.equals(SEND_STRING_CHAT)) {
                                mGameHandler.obtainMessage(MESSAGE_STRING_CHAT, Content).sendToTarget();
                            } else if (TypeOfMessage.equals(SEND_STRING_NAME)) {
                                mGameHandler.obtainMessage(MESSAGE_STRING_NAME, Content).sendToTarget();
                            }
                            else if (TypeOfMessage.equals(SEND_YOU_LOSE))
                            {
                                mGameHandler.obtainMessage(MESSAGE_YOU_LOSE, Content).sendToTarget();
                            }
                            else if(TypeOfMessage.equals(SEND_PLAY_AGAIN))
                            {
                                mGameHandler.obtainMessage(MESSAGE_AGAIN).sendToTarget();
                            }
                            else if(TypeOfMessage.equals(SEND_ACCEPT))
                            {
                                mGameHandler.obtainMessage(MESSAGE_ACCEPT).sendToTarget();
                            }
                            else if(TypeOfMessage.equals(SEND_EXIT))
                            {
                                mGameHandler.obtainMessage(MESSAGE_EXIT).sendToTarget();
                            }
                            else {
                                numberOfBytes = Integer.parseInt(Content);
                                buffer = new byte[numberOfBytes];
                                flag = false;
                            }
                            mBluetoothService.sendString(SEND_FEEDBACK, SEND_FEEDBACK);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        byte[] data = new byte[800];
                        int numbers = mmInStream.read(data);
                        System.arraycopy(data, 0, buffer, index, numbers);
                        index = index + numbers;
                        if (index == numberOfBytes) {
                            if (TypeOfMessage.equals(SEND_IMAGE_AVATAR)) {
                                mGameHandler.obtainMessage(MESSAGE_IMAGE_AVATAR, numberOfBytes, -1, buffer).sendToTarget();
                            } else {
                                mGameHandler.obtainMessage(MESSAGE_IMAGE_CHAT, numberOfBytes, -1, buffer).sendToTarget();
                            }
                            index=0;
                            flag = true;
                        }
                        mBluetoothService.sendString(SEND_FEEDBACK, SEND_FEEDBACK);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //Call this from the main activity to send data to the remote device
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {

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

    //Khởi tạo luồng trao đổi thông điệp
    private void connected(BluetoothSocket mmSocket) {
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void sendImage(byte[] buffer, String type) {
        int lengthBuffer = buffer.length;
        int length1 = type.length();
        int length2 = String.valueOf(lengthBuffer).getBytes(StandardCharsets.UTF_8).length;
        byte[] temp = new byte[length1 + length2];
        if (type == SEND_IMAGE_CHAT) {
            System.arraycopy(SEND_IMAGE_CHAT.getBytes(StandardCharsets.UTF_8), 0, temp, 0, length1);
        } else {
            System.arraycopy(SEND_IMAGE_AVATAR.getBytes(StandardCharsets.UTF_8), 0, temp, 0, length1);
        }
        System.arraycopy(String.valueOf(lengthBuffer).getBytes(StandardCharsets.UTF_8), 0, temp, length1, length2);
        while (!mConnectedThread.get()) ;
        mConnectedThread.write(temp);
        mConnectedThread.set(false);
        int subArraySize = 800;
        try
        {
            for (int i = 0; i < buffer.length; i += subArraySize) {
                while (!mConnectedThread.get()) ;
                byte[] tempArray;
                tempArray = Arrays.copyOfRange(buffer, i, Math.min(buffer.length, i + subArraySize));
                mConnectedThread.write(tempArray);
            }
        }catch (Exception e)
        {
         Log.e("TAG","Send Image not successful");
        }
    }
    // Hàm gửi các thông điệp cho đối thủ thông qua Bluetooth Connection
    public void sendString(String string, String type) {
        byte[] buffer;
        int length1 = SEND_STRING_CHAT.getBytes(StandardCharsets.UTF_8).length;
        int length2 = string.getBytes(StandardCharsets.UTF_8).length;
        buffer = new byte[length1 + length2];
        if (type == SEND_STRING_CHAT) {
            System.arraycopy(SEND_STRING_CHAT.getBytes(StandardCharsets.UTF_8), 0, buffer, 0, length1);
        } else if (type == SEND_FEEDBACK) {
            System.arraycopy(SEND_FEEDBACK.getBytes(StandardCharsets.UTF_8), 0, buffer, 0, length1);

        }
        else if(type==SEND_YOU_LOSE)
        {
            System.arraycopy(SEND_YOU_LOSE.getBytes(StandardCharsets.UTF_8), 0, buffer, 0, length1);
        }
        else if(type==SEND_PLAY_AGAIN)
        {
            System.arraycopy(SEND_PLAY_AGAIN.getBytes(StandardCharsets.UTF_8), 0, buffer, 0, length1);
        }
        else if(type==SEND_ACCEPT)
        {
            System.arraycopy(SEND_ACCEPT.getBytes(StandardCharsets.UTF_8), 0, buffer, 0, length1);
        }
        else if(type==SEND_EXIT)
        {
            System.arraycopy(SEND_EXIT.getBytes(StandardCharsets.UTF_8), 0, buffer, 0, length1);
        }
        else {
            System.arraycopy(SEND_STRING_NAME.getBytes(StandardCharsets.UTF_8), 0, buffer, 0, length1);
        }

        System.arraycopy(string.getBytes(StandardCharsets.UTF_8), 0, buffer, length1, length2);
        while (!mConnectedThread.get()) ;
        mConnectedThread.write(buffer);
//
    }
    //Hàm gửi vị trí đã chọn  cho đối thủ thông qua Bluetooth Connection
    public void sendInt(int number) {
        byte[] buffer;
        int length1 = SEND_INT_POSTION.getBytes(StandardCharsets.UTF_8).length;
        int length2 = String.valueOf(number).getBytes(StandardCharsets.UTF_8).length;
        buffer = new byte[length1 + length2];
        System.arraycopy(SEND_INT_POSTION.getBytes(StandardCharsets.UTF_8), 0, buffer, 0, length1);
        System.arraycopy(String.valueOf(number).getBytes(StandardCharsets.UTF_8), 0, buffer, length1, length2);
        while (!mConnectedThread.get()) ;
        mConnectedThread.write(buffer);
        mConnectedThread.set(false);
    }
    //Hủy kết nối Socket
    public void Disconnect() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
        }
    }
}





