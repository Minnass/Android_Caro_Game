package com.example.caro.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.caro.BlueToothService.BluetoothService;
import com.example.caro.Model.User;
import com.example.caro.R;
import com.example.caro.Util.MySharedPerferences;

import java.util.ArrayList;
import java.util.List;

public class MenuGameActivity extends AppCompatActivity {
   TextView findRoom, createRoom,setting,exit,twoPlayer;
    private static final String TAG = "MenuGameActivity";
    public static BluetoothService mBluetoothService;
    private final int PERMISSION_SCAN = 1;
    private final int PERMISSION_ADVERTISE = 2;
    public static User user;
    @SuppressLint("HandlerLeak")


    @RequiresApi(api = Build.VERSION_CODES.M)
    void initPermission() {
        @SuppressLint("InlinedApi")
        String[] permission = new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE
        };
        requestPermissions(permission, 6);
    }

    void initUser()
    {
        user=new User("","","");
        if(MySharedPerferences.isSavedBefore(MenuGameActivity.this))
        {
            user.setSex(MySharedPerferences.getValue(MenuGameActivity.this,"sex"));
            user.setName(MySharedPerferences.getValue(MenuGameActivity.this,"name"));
            user.setPathImage(MySharedPerferences.getValue(MenuGameActivity.this,"imagePath"));
        }
    }
    @SuppressLint("HandlerLeak")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //CheckhoiTao
        initPermission();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_game);
        Mapping();
        initUser();
        mBluetoothService = new BluetoothService(this);
        createRoom.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SupportAnnotationUsage")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                btnEnableDisable_Discoverable();
            }
        });
        findRoom.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SupportAnnotationUsage")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                btn_Discover();
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MenuGameActivity.this,EditInformationActivity.class);
                startActivity(intent);
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        twoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent Game 2 nguoi choi
            }
        });
    }
    void Mapping() {
        findRoom = findViewById(R.id.btn_match);
        createRoom = findViewById(R.id.btn_createRoom);
        setting=findViewById(R.id.btn_modify);
        exit=findViewById(R.id.btn_quit);
        twoPlayer=findViewById(R.id.btn_play);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnEnableDisable_Discoverable() {
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            @SuppressLint("InlinedApi")
            String[] permission = new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
            };
            requestPermissions(permission, PERMISSION_ADVERTISE);
        } else {
            new Thread(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                }
            }).start();
            Bundle bundle = new Bundle();
            bundle.putBoolean("connected", true);
            Intent intent1 = new Intent(MenuGameActivity.this, GameActivity.class);
            intent1.putExtras(bundle);
            startActivity(intent1);
            mBluetoothService.start();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    void btn_Discover () {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            @SuppressLint("InlinedApi")
            String[] permission = new String[]{Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
            };
            requestPermissions(permission, PERMISSION_SCAN);
        } else {
            if (!mBluetoothService.mBluetoothAdapter.isEnabled()) {
                new Thread(new Runnable() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableBT);
                    }
                }).start();
            }
            Intent intent = new Intent(MenuGameActivity.this, ListRoomActivity.class);
            startActivity(intent);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){
        switch (requestCode) {
            case PERMISSION_ADVERTISE: {
                List<String> temp = new ArrayList<>();
                int count = 0;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        temp.add(permissions[i]);
                        count++;
                    }
                }
                if (count > 0) {
                    String[] strArr = new String[temp.size()];
                    temp.toArray(strArr);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("Cảnh báo!");
                    alertDialog.setIcon(R.drawable.warning);
                    alertDialog.setMessage("Để chơi được game bạn cần phải cấp quyền!");
                    alertDialog.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(strArr, PERMISSION_SCAN);
                        }
                    });
                    alertDialog.setNegativeButton("Từ chối", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                } else {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                    mBluetoothService.start();
                }
            }
            case PERMISSION_SCAN: {
                List<String> temp = new ArrayList<>();
                int count = 0;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        temp.add(permissions[i]);
                        count++;
                    }
                }
                if (count > 0) {
                    String[] strArr = new String[temp.size()];
                    temp.toArray(strArr);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("Cảnh báo!");
                    alertDialog.setIcon(R.drawable.warning);
                    alertDialog.setMessage("Để chơi được game bạn cần phải cấp quyền!");
                    alertDialog.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(strArr, PERMISSION_SCAN);
                        }
                    });
                    alertDialog.setNegativeButton("Từ chối", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                } else {
                    if (!mBluetoothService.mBluetoothAdapter.isEnabled()) {
                        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableBT);
                    }
                    Intent intent = new Intent(this, ListRoomActivity.class);
                    startActivity(intent);
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}