package com.example.caro.Util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caro.R;

public class ToastCustom {
    public  static  void show(String text, Context context)
    {
        Toast toast =new Toast(context);
        LayoutInflater inflater= ((Activity)context).getLayoutInflater();
        View view =inflater.inflate(R.layout.toast_customing,(ViewGroup) ((Activity)context).findViewById(R.id.layout_custom_toast));
        TextView textView=view.findViewById(R.id.text_customToast);
        textView.setText(text);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER,0,50);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}
