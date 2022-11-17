package com.example.caro.Util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.example.caro.Activity.EditInformationActivity;

public class PermissionAlertDialog {
    public static void requestPermissionAgain(Context context, String[] permissions, int RequestCode) {
        new AlertDialog.Builder(context)
                .setTitle("Cảnh báo")
                .setMessage("Quyền này bắt buộc để tiếp tục chức năng")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context, permissions, RequestCode);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    public static void showAlerDialogWarning(Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
                .setTitle("Warning")
                .setMessage(" \"Don't show again\" đã được đặt là mặc định, bạn phải đến AppSetting để cấp quyền.");
        alertDialogBuilder.setPositiveButton("Đồng ý",
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", ((Activity)context).getPackageName(), null);
                        intent.setData(uri);
                        ((Activity)context).startActivity(intent);
                    }
                });
        alertDialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}

