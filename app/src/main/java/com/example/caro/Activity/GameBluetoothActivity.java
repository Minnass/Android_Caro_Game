package com.example.caro.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caro.Adapter.GridViewAdapter;
import com.example.caro.BlueToothService.BluetoothService;
import com.example.caro.Caro.Board;
import com.example.caro.Caro.Field;
import com.example.caro.Caro.Position;
import com.example.caro.R;
import com.example.caro.Util.ImageFromInternal;

import static com.example.caro.Activity.MenuGameActivity.mBluetoothService;
import static com.example.caro.Activity.MenuGameActivity.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class GameBluetoothActivity extends AppCompatActivity {

    public static final int MESSAGE_POSTION = 1;
    public static final int MESSAGE_IMAGE_AVATAR = 2;
    public static final int MESSAGE_IMAGE_CHAT = 3;
    public static final int MESSAGE_STRING_CHAT = 4;
    public static final int MESSAGE_STRING_NAME = 5;
    public static final int MESSAGE_BLUETOOTH = 6;
    public static final int MESSAGE_YOU_LOSE = 7;
    public static final int MESSAGE_EXIT = 8;
    public static final int MESSAGE_AGAIN = 9;
    public static final int MESSAGE_ACCEPT=10;
    public static final int success = 1;
//    public static final int fail = 0;

    public static Handler mGameHandler;
    private Board board;
    private GridView mBoadGame;
    private GridViewAdapter mGridViewAdapter;
    private TextView competitorName, yourName;
    private ImageView competitorImg, yourImg;

    private boolean yourTurn = false;
    private boolean startGame = false;
    private Position lastMove;
    Button sendbtn, sendImage;
    EditText message;
    ImageView imageView;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(GameBluetoothActivity.this);
        Boolean isSerVer = getIntent().getBooleanExtra("connected", false);
        if (isSerVer) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.diaglog_loading);
            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);
            dialog.setCancelable(false);
            TextView button1;
            button1 = dialog.findViewById(R.id.btn_loadingdiaglog);
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBluetoothService.Disconnect();
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else {
            startGame = true;
            mBluetoothService.sendString(user.getName(), BluetoothService.SEND_STRING_NAME);
        }
        mGameHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MESSAGE_POSTION: {
                        int position = (int) msg.obj;
                        Position receivedPosition = new Position(position % board.getDimensionX(), position / board.getDimensionY());
                        board.fillPostion(receivedPosition, Field.OPPONENT);
                        mGridViewAdapter.notifyDataSetChanged();
                        yourTurn = true;
                        break;
                    }
                    case MESSAGE_STRING_CHAT: {
                        //Log.d("MainActivity", (String) msg.obj);
                        Toast.makeText(GameBluetoothActivity.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case MESSAGE_STRING_NAME: {
                        Log.d("Main", (String) msg.obj);
                        competitorName.setText((String) msg.obj);
                        if (isSerVer) {
                            mBluetoothService.sendString(user.getName(), BluetoothService.SEND_STRING_NAME);
                        }
                        if (!isSerVer) {
                            sendAvatar();
                        }
                        break;
                    }
                    case MESSAGE_IMAGE_AVATAR: {
                        byte[] readbuff = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(readbuff, 0, msg.arg1);
                        competitorImg.setImageBitmap(bitmap);
                        if (isSerVer) {
                            sendAvatar();
                        }
                        break;
                    }
                    case MESSAGE_IMAGE_CHAT: {
                        byte[] readbuff = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(readbuff, 0, msg.arg1);
                        imageView.setImageBitmap(bitmap);
                        break;
                    }
                    case MESSAGE_YOU_LOSE:
                    {
                        showDiaglogLoser();
                        break;
                    }
                    case MESSAGE_AGAIN:
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameBluetoothActivity.this)
                                .setTitle("Thông báo")
                                .setMessage("Đối thủ muốn chơi lại");
                        alertDialogBuilder.setPositiveButton("Đồng ý",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                      mBluetoothService.sendString(BluetoothService.SEND_PLAY_AGAIN,BluetoothService.SEND_PLAY_AGAIN);
                                    }
                                });
                        alertDialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBluetoothService.sendString(BluetoothService.SEND_EXIT,BluetoothService.SEND_EXIT);
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        break;
                    }
                    case MESSAGE_ACCEPT:
                    {
                        if(startGame)
                        {
                            board.reset();
                            mGridViewAdapter.notifyDataSetChanged();
                            //dialog_.dismiss();
                        }
                        break;
                    }
                    case MESSAGE_EXIT:
                    {
                        break;
                    }
                    case MESSAGE_BLUETOOTH: {
                        if (msg.arg1 == 1 && msg.arg2 == success) {
                            yourTurn = true;
                            startGame = true;
                            dialog.dismiss();
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        };
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_bluetooth);
        mappingID();
        boardInit();
        if (!user.getName().equals("")) {
            yourName.setText(user.getName());
            String imagePath = user.getPathImage();
            File savedAvatar = new File(imagePath, "avatar.jpg");
            try {
                Bitmap savedBitmap = BitmapFactory.decodeStream(new FileInputStream(savedAvatar));
                yourImg.setImageBitmap(savedBitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothService.sendString(message.getText().toString(), BluetoothService.SEND_STRING_CHAT);
            }
        });
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filePath = user.getPathImage() + "/avatar.jpg";
                byte[] bufferImage = ImageFromInternal.readImageFromInternal(filePath);
                if (bufferImage != null) {
                    Toast.makeText(GameBluetoothActivity.this, String.valueOf(bufferImage.length), Toast.LENGTH_SHORT).show();
                }
//                        Log.d("Main", bufferImage.length + "");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothService.sendImage(bufferImage, BluetoothService.SEND_IMAGE_CHAT);
                    }
                }).start();
            }
        });
    }

    void mappingID() {

        competitorImg = findViewById(R.id.avatar_competitor);
        yourImg = findViewById(R.id.avatar_me);
        competitorName = findViewById(R.id.name_competitor);
        yourName = findViewById(R.id.name_me);
        sendbtn = findViewById(R.id.sendText);
        message = findViewById(R.id.message);
        imageView = findViewById(R.id.image_game);
        sendImage = findViewById(R.id.sendImage);
    }

    private void boardInit() {
        mBoadGame = findViewById(R.id.board_bluetooth_mode);
        // TODO: player should choose board dimension
        mBoadGame.setNumColumns(20);
        board = new Board(20, 20);
        mGridViewAdapter = new GridViewAdapter(this, board);
        mBoadGame.setAdapter(mGridViewAdapter);
        mBoadGame.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (!yourTurn || !startGame) {
                    Toast.makeText(GameBluetoothActivity.this, "Chưa đến lượt!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (board.getField(position) != Field.EMPTY) {
                    return;
                }
                lastMove = new Position(position % board.getDimensionX(), position / board.getDimensionY());
                boolean a = board.fillPostion(lastMove, Field.PLAYER);
                mGridViewAdapter.notifyDataSetChanged();
                yourTurn = false;
                //Gui vi tri cho doi phuong
                mBluetoothService.sendInt(position);
                if (board.findWinner(lastMove) == Field.PLAYER) {
                    mBluetoothService.sendString(BluetoothService.YOU_LOSE, BluetoothService.YOU_LOSE);
//                    showDiaglogWinner();
                }
            }

        });
    }

    void sendAvatar() {
        String filePath = user.getPathImage() + "/avatar.jpg";
        byte[] bufferImage = ImageFromInternal.readImageFromInternal(filePath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mBluetoothService.sendImage(bufferImage, BluetoothService.SEND_IMAGE_AVATAR);
            }
        }).start();

    }

    void showDiaglogWinner() {
        final Dialog winnerDiaglog = new Dialog(this);
        winnerDiaglog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        winnerDiaglog.setContentView(R.layout.dialog_game_winner);
        Window window = winnerDiaglog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);
        winnerDiaglog.setCancelable(false);
        TextView button1, button2;
        button1 = winnerDiaglog.findViewById(R.id.again);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                final Dialog dialog_=new Dialog(GameBluetoothActivity.this);
//                    dialog_.requestWindowFeature(Window.FEATURE_NO_TITLE);
//                    dialog_.setContentView(R.layout.diaglog_loading);
//                    Window window = dialog_.getWindow();
//                    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//                    WindowManager.LayoutParams windowAttributes = window.getAttributes();
//                    windowAttributes.gravity = Gravity.CENTER;
//                    window.setAttributes(windowAttributes);
//                    dialog_.setCancelable(false);
//                    TextView button1;
//                    button1 = dialog_.findViewById(R.id.cancel_again);
//                    button1.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
////                            mBluetoothService.Disconnect();
//                            startGame=false;
//                            dialog_.dismiss();
//                        }
//                    });
//                    dialog_.show();
            }
        });
        button2 = winnerDiaglog.findViewById(R.id.exit);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothService.Disconnect();
                finish();
            }
        });
        winnerDiaglog.show();
    }

    void showDiaglogLoser() {
        final Dialog loserDiaglog = new Dialog(this);
        loserDiaglog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loserDiaglog.setContentView(R.layout.dialog_game_loser);
        Window window = loserDiaglog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);
        loserDiaglog.setCancelable(false);
        TextView button1, button2;
        button1 = loserDiaglog.findViewById(R.id.again_loser);
        button2 = loserDiaglog.findViewById(R.id.exit_loser);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothService.Disconnect();
                finish();
            }
        });
        loserDiaglog.show();
    }
}