package com.example.caro.Activity;

import static com.example.caro.Activity.MenuGameActivity.mBluetoothService;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.caro.Adapter.ListRoomAdapter;
import com.example.caro.R;

import java.util.ArrayList;
import java.util.List;

public class ListRoomActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";
    ImageView back, reloading;
    private RecyclerView mListRoom;
    ListRoomAdapter mListRommAdapter;
    ProgressBar loading;
    List<BluetoothDevice> mListDevice;
    public static Handler mHandler;
    public static final int GAME_CREATING = 1;
    public static final int FALSE_CREATING = 0;

    @Override
    @SuppressLint("HandlerLeak")
    protected void onCreate(Bundle savedInstanceState) {

        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case GAME_CREATING: {
                        Intent intent = new Intent(ListRoomActivity.this, GameBluetoothActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case FALSE_CREATING: {
                        Toast.makeText(ListRoomActivity.this, "Thất bại!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            }
        };
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_room);
        back = findViewById(R.id.back_listRoom);
        reloading = findViewById(R.id.reload);
        loading = findViewById(R.id.loading);
        mListRoom = findViewById(R.id.listRoom);
        mListDevice = new ArrayList<>();
        mListRommAdapter = new ListRoomAdapter(mListDevice, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mListRoom.setLayoutManager(linearLayoutManager);
        mListRoom.setAdapter(mListRommAdapter);
        reloading.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (mBluetoothService.mBluetoothAdapter.isEnabled()) {
                    loading.setVisibility(View.VISIBLE);
                    mListRoom.setVisibility(View.GONE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loading.setVisibility(View.GONE);
                            mListRoom.setVisibility(View.VISIBLE);
                        }
                    }, 3000);
                    loadingRoom();
                } else {
                    Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(enableBT);
                }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
    }


    @SuppressLint("MissingPermission")
    void loadingRoom() {
        if (mBluetoothService.mBluetoothAdapter.isDiscovering()) {
            mBluetoothService.mBluetoothAdapter.cancelDiscovery();
            mListDevice.clear();
            mListRommAdapter.notifyDataSetChanged();
            Log.d(TAG, "btnDiscover: Canceling discovery.");
        }
        if (!mBluetoothService.mBluetoothAdapter.isDiscovering()) {
            Log.d(TAG, "btnDiscover: Enabled discovery.");
        }
        boolean temp = mBluetoothService.mBluetoothAdapter.startDiscovery();
        Log.d(TAG, "startDiscovery" + String.valueOf(temp));
    }

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @SuppressLint({"MissingPermission", "NotifyDataSetChanged"})
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String temp = device.getName();
                if (temp != null && !mListDevice.contains(device)) {
                    mListDevice.add(device);
                    mListRommAdapter.notifyDataSetChanged();
                    Log.d(TAG, "onReceive: " + device.getName());
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver3);
        super.onDestroy();
    }
}