package com.example.caro.Util;

import static com.example.caro.Activity.MenuGameActivity.user;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageFromInternal {
    public static void readImageFromInternal(String filePath, byte[] buffer) {
        File file;
        FileInputStream in = null;
        try {
            file = new File(filePath);
            in = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        buffer = new byte[(int) file.length()];
        int beginPosition = 0;
        int bytes = 0;
        int subLength = 800;
        while (true) {
            byte[] dataToSend = new byte[subLength];
            try {
                bytes = in.read(dataToSend, 0, subLength);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bytes == -1) {
                break;
            }
            System.arraycopy(dataToSend, 0, buffer, beginPosition, bytes);
            beginPosition += bytes;
        }
    }
}
