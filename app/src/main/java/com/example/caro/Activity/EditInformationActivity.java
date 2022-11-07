package com.example.caro.Activity;



import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caro.R;
import com.example.caro.Util.HideSoftKeyBoard;
import com.example.caro.Util.MySharedPerferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import  static  com.example.caro.Activity.MenuGameActivity.user;

public class EditInformationActivity extends AppCompatActivity {

    private final int CAMERA_PEMISSION = 1;
    ImageView avatar;
    TextView camera, library, save, exit;
    EditText name;
    RadioButton male, female;
    RadioGroup sex;
    ActivityResultLauncher<String> galleryLauncher;
    ActivityResultLauncher<Intent> cameraLauncher;
    LinearLayout viewGroup;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_information);
        mappingID();
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                avatar.setImageURI(result);
            }
        });
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmap = (Bitmap) bundle.get("data");
                    avatar.setImageBitmap(bitmap);
                }
            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name.getText().toString().equals("")) {
                    Toast.makeText(EditInformationActivity.this, "Nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                } else {
                    saveInformation();
                    Toast.makeText(EditInformationActivity.this, "Đã lưu thành công", Toast.LENGTH_SHORT).show();
                }
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });
        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryLauncher.launch("image/*");

            }
        });
        sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.male: {
                      MenuGameActivity.user.setSex("Nam");
                        break;
                    }
                    case R.id.female: {
                     MenuGameActivity.user.setSex("Nữ");
                        break;
                    }
                }
            }
        });
        //Lấy dữ liệu đã lưu trước đó
        initUI();
        //set Disable Edittext when touch outside
        HideSoftKeyBoard hideSoftKeyBoard = new HideSoftKeyBoard(EditInformationActivity.this);
        hideSoftKeyBoard.setupUI(viewGroup);
    }

    void mappingID() {
        viewGroup = findViewById(R.id.viewGroup);
        avatar = findViewById(R.id.avatar_edit);
        //To check either Image changed or not
        name = findViewById(R.id.name_edit);
        male = findViewById(R.id.male);
        female = findViewById(R.id.female);
        library = findViewById(R.id.library);
        camera = findViewById(R.id.camera);
        save = findViewById(R.id.save);
        exit = findViewById(R.id.exit_edit);
        sex = findViewById(R.id.sex);
        //default radio group is male
        sex.check(male.getId());
        MenuGameActivity.user.setSex("Nam");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PEMISSION: {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        cameraLauncher.launch(intent);
                    }

                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        requestPermissionAgain();
                    } else {
                        showAlerDialogWarning();
                    }
                }
                break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkPermission() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] permission = new String[]{Manifest.permission.CAMERA
            };
            requestPermissions(permission, CAMERA_PEMISSION);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(intent);
            }
        }
    }

    public void requestPermissionAgain() {
        new AlertDialog.Builder(this)
                .setTitle("Cảnh báo")
                .setMessage("Quyền này bắt buộc để tiếp tục chức năng")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(EditInformationActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PEMISSION);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    public void showAlerDialogWarning() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage(" \"Don't show again\" đã được đặt là mặc định, bạn phải đến AppSetting để cấp quyền.");
        alertDialogBuilder.setPositiveButton("Đồng ý",
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
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

    void initUI() {
        if (!MySharedPerferences.isSavedBefore(EditInformationActivity.this)) {
            return;
        }
        name.setText(MySharedPerferences.getValue(EditInformationActivity.this, "name"));
        String temp = MySharedPerferences.getValue(EditInformationActivity.this, "sex");
        if (temp == "Nam") sex.check(R.id.male);
        else sex.check(R.id.female);
        String imagePath=MySharedPerferences.getValue(EditInformationActivity.this,"imagePath");
        File savedAvatar =  new File(imagePath, "avatar.jpg");
        try {
            Bitmap savedBitmap = BitmapFactory.decodeStream(new FileInputStream(savedAvatar));
            avatar.setImageBitmap(savedBitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void saveInformation() {
        //luu ten va gioi tinh

        MySharedPerferences.deleteBefore(EditInformationActivity.this);
        MySharedPerferences.setValue(EditInformationActivity.this, "name", MenuGameActivity.user.getName());
        MySharedPerferences.setValue(EditInformationActivity.this, "sex", MenuGameActivity.user.getSex());

        //luu hinh anh

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("caroPlayPath", Context.MODE_PRIVATE);
        File mypath = new File(directory, "avatar.jpg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mypath);
            Bitmap bitmap = ((BitmapDrawable) avatar.getDrawable()).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.WEBP, 75, out);
            // avatarUser1.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert out != null;
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MySharedPerferences.setValue(EditInformationActivity.this, "imagePath", directory.getAbsolutePath());
        //update user fromGameActivity
      MenuGameActivity.user.setPathImage(directory.getAbsolutePath());
      MenuGameActivity.user.setName(name.getText().toString());
        //Cập nhật lại lần đầu setting nhân vật =fasle;
        MySharedPerferences.setSavedBefore(EditInformationActivity.this);
    }
}