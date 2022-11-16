package com.example.caro.Adapter;



import  static  com.example.caro.Activity.MenuGameActivity.mBluetoothService;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caro.R;

import java.util.List;

public class ListRoomAdapter extends RecyclerView.Adapter<ListRoomAdapter.ListRoomViewHolder> {

    List<BluetoothDevice> mListDevice;
    Context context;
    public static String TAG = "MainActivity";

    public ListRoomAdapter(List<BluetoothDevice> mListDevice, Context context) {
        this.mListDevice = mListDevice;
        this.context = context;
    }

    @NonNull
    @Override
    public ListRoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item, parent, false);
        return new ListRoomViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull ListRoomViewHolder holder, int position) {
        BluetoothDevice device = mListDevice.get(position);
        if (device == null) {
            return;
        }
        holder.NameDevice.setText(device.getName().toString());
        holder.enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_custom);
                Window window = dialog.getWindow();
                if (window == null) {
                    return;
                }
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                WindowManager.LayoutParams windowAttributes = window.getAttributes();
                windowAttributes.gravity = Gravity.CENTER;
                window.setAttributes(windowAttributes);
                dialog.setCancelable(false);
                TextView button1;
                TextView button2;
                button1 = dialog.findViewById(R.id.accept_diaglog);
                button2 = dialog.findViewById(R.id.cancel_diaglog);
                button2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                button1.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Connect" + device.getName().toString());
                        mBluetoothService.startClient(device);

                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });


    }

    @Override
    public int getItemCount() {
        if (mListDevice == null) {
            return 0;
        } else {
            return mListDevice.size();
        }
    }

    public class ListRoomViewHolder extends RecyclerView.ViewHolder {
        TextView NameDevice;
        ImageView enter;

        public ListRoomViewHolder(@NonNull View itemView) {
            super(itemView);
            NameDevice = itemView.findViewById(R.id.nameDevice);
            enter = itemView.findViewById(R.id.enter_room);
        }
    }

}
