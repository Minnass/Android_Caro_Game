package com.example.caro.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.caro.BlueToothService.BluetoothService;
import com.example.caro.Model.User;
import com.example.caro.R;
import com.example.caro.Util.MySharedPerferences;
import com.example.caro.Util.PermissionAlertDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MenuGameActivity extends AppCompatActivity {
    TextView btn_findRoom, btn_createRoom, btn_setting, btn_quit, btn_localPlay;
    private static final String TAG = "MenuGameActivity";
    public static BluetoothService mBluetoothService;
    private final int PERMISSION_SCAN = 1;
    private final int PERMISSION_ADVERTISE = 2;
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 4;
    private final int PERMISSION_ACCESS_FINE_LOCATION = 5;
    public static User user;
    public static ArrayList<Integer> mListStick = null;


    @SuppressLint("HandlerLeak")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_game);
        Mapping();
        initUser();
        mListStick = initListSticker();
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveStickerIntoInterStorage();
            }
        }).start();
        mBluetoothService = new BluetoothService(this);
        btn_createRoom.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SupportAnnotationUsage")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                btnEnableDisable_Discoverable();
            }
        });
        btn_findRoom.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SupportAnnotationUsage")
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                btn_Discover();
            }
        });
        btn_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuGameActivity.this, EditInformationActivity.class);
                startActivity(intent);
            }
        });
        btn_quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_localPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO Intent Game 2 nguoi choi
                String[] option = {
                        "With Bot",
                        "With Human"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuGameActivity.this);
                builder.setTitle("Which mode?")
                        .setItems(option, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    Intent i = new Intent(MenuGameActivity.this, GameWithBotActivity.class);
                                    startActivity(i);
                                } else if (which == 1) {
                                    Intent i = new Intent(MenuGameActivity.this, GameActivity.class);
                                    startActivity(i);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    void Mapping() {
        btn_createRoom = findViewById(R.id.btn_createRoom);
        btn_findRoom = findViewById(R.id.btn_findRoom);
        btn_setting = findViewById(R.id.btn_setting);
        btn_quit = findViewById(R.id.btn_quit);
        btn_localPlay = findViewById(R.id.btn_localPlay);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void btnEnableDisable_Discoverable() {

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE}, PERMISSION_ADVERTISE);
            }
        } else {

            Bundle bundle = new Bundle();
            bundle.putBoolean("connected", true);
            Intent intent1 = new Intent(MenuGameActivity.this, GameBluetoothActivity.class);
            intent1.putExtras(bundle);
            startActivity(intent1);
            new Thread(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoverableIntent);
                }
            }).start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void btn_Discover() {
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
        if (checkSelfPermission(
                Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_SCAN);
            }

        } else {
            Intent intent = new Intent(MenuGameActivity.this, ListRoomActivity.class);
            startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ADVERTISE: {
                if ( grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_ADVERTISE)) {
                        PermissionAlertDialog.requestPermissionAgain(MenuGameActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE,Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_ADVERTISE);
                    } else {
                        PermissionAlertDialog.showAlerDialogWarning(MenuGameActivity.this);
                    }
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("connected", true);
                    Intent intent1 = new Intent(MenuGameActivity.this, GameBluetoothActivity.class);
                    intent1.putExtras(bundle);
                    startActivity(intent1);
                    new Thread(new Runnable() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void run() {
                            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                            startActivity(discoverableIntent);
                        }
                    }).start();
                }
                break;
            }
            case PERMISSION_SCAN: {
                if(grantResults.length==4)
                {
                    if(grantResults[0]!=PackageManager.PERMISSION_GRANTED&&grantResults[2]!=PackageManager.PERMISSION_GRANTED)
                    {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            PermissionAlertDialog.requestPermissionAgain(MenuGameActivity.this,new  String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_SCAN);
                        } else {
                            PermissionAlertDialog.showAlerDialogWarning(MenuGameActivity.this);
                        }
                    }
                    else if(grantResults[0]!=PackageManager.PERMISSION_GRANTED)
                    {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            PermissionAlertDialog.requestPermissionAgain(MenuGameActivity.this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_SCAN);
                        } else {
                            PermissionAlertDialog.showAlerDialogWarning(MenuGameActivity.this);
                        }
                    }
                    else if(grantResults[2]!=PackageManager.PERMISSION_GRANTED)
                    {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {
                            PermissionAlertDialog.requestPermissionAgain(MenuGameActivity.this,new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_SCAN);
                        } else {
                            PermissionAlertDialog.showAlerDialogWarning(MenuGameActivity.this);
                        }
                    }
                    else
                    {
                        Intent intent = new Intent(MenuGameActivity.this, ListRoomActivity.class);
                        startActivity(intent);
                    }
                }
                else
                {
                    if ( grantResults[0] != PackageManager.PERMISSION_GRANTED&&permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                    {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            PermissionAlertDialog.requestPermissionAgain(MenuGameActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_SCAN);
                        } else {
                            PermissionAlertDialog.showAlerDialogWarning(MenuGameActivity.this);
                        }
                    }
                    else if(grantResults[0] != PackageManager.PERMISSION_GRANTED&&permissions[0].equals(Manifest.permission.BLUETOOTH_SCAN))
                    {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {
                            PermissionAlertDialog.requestPermissionAgain(MenuGameActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_SCAN);
                        } else {
                            PermissionAlertDialog.showAlerDialogWarning(MenuGameActivity.this);
                        }
                    }
                    else
                    {
                        Intent intent = new Intent(MenuGameActivity.this, ListRoomActivity.class);
                        startActivity(intent);
                    }
                }
                break;
            }
//            case PERMISSION_ACCESS_COARSE_LOCATION: {
//                if (grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                        PermissionAlertDialog.requestPermissionAgain(MenuGameActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_ACCESS_COARSE_LOCATION);
//                    } else {
//                        PermissionAlertDialog.showAlerDialogWarning(MenuGameActivity.this);
//                    }
//                }
//                break;
//            }
//            case PERMISSION_ACCESS_FINE_LOCATION:
//            {
//                if(grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
//                {
//                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        PermissionAlertDialog.requestPermissionAgain(MenuGameActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ACCESS_FINE_LOCATION);
//                    }
//                    else
//                    {
//                        PermissionAlertDialog.showAlerDialogWarning(MenuGameActivity.this);
//                    }
//                }
//                break;
//            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    //Prepare stickẻ by saving into Internal storage for send stick in play game
    ArrayList<Integer> initListSticker() {
        ArrayList<Integer> mListStick = new ArrayList<>();
        mListStick.add(R.drawable.after_boom_sticker);
        mListStick.add(R.drawable.beat_brick_sticker);
        mListStick.add(R.drawable.boss_sticker);
        mListStick.add(R.drawable.big_smile_sticker);
        mListStick.add(R.drawable.hell_boy_sticker);
        mListStick.add(R.drawable.dribble_sticker);
        return mListStick;

    }

    public void saveStickerIntoInterStorage() {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("caroPlayPath", Context.MODE_PRIVATE);
        FileOutputStream out = null;

        for (int i = 0; i < mListStick.size(); i++) {
            try {
                File mypath = new File(directory, String.valueOf(mListStick.get(i)) + ".jpg");
                out = new FileOutputStream(mypath);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), mListStick.get(i));
                bitmap.compress(Bitmap.CompressFormat.WEBP, 75, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                assert out != null;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MySharedPerferences.setValue(MenuGameActivity.this, "imagePath", directory.getAbsolutePath());
    }

    void initUser() {
        user = new User("", "", "");
        if (MySharedPerferences.isSavedBefore(MenuGameActivity.this)) {
            user.setSex(MySharedPerferences.getValue(MenuGameActivity.this, "sex"));
            user.setName(MySharedPerferences.getValue(MenuGameActivity.this, "name"));
            user.setPathImage(MySharedPerferences.getValue(MenuGameActivity.this, "imagePath"));
        }
    }
}