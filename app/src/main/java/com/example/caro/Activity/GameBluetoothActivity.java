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
import android.os.CountDownTimer;
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

import com.example.caro.Adapter.ChattingAdapter;
import com.example.caro.Adapter.GridViewAdapter;
import com.example.caro.BlueToothService.BluetoothService;
import com.example.caro.Caro.Board;
import com.example.caro.Caro.Field;
import com.example.caro.Caro.Position;
import com.example.caro.R;
import com.example.caro.Util.ImageFromInternal;
import com.example.caro.Util.MySharedPerferences;
import com.example.caro.Util.ToastCustom;

import static com.example.caro.Activity.MenuGameActivity.mBluetoothService;
import static com.example.caro.Activity.MenuGameActivity.mListStick;
import static com.example.caro.Activity.MenuGameActivity.user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class GameBluetoothActivity extends AppCompatActivity {

    //Danh sách các Flag lưu kiểu gửi messaga type cho handler
    public static final int MESSAGE_POSTION = 1;
    public static final int MESSAGE_IMAGE_AVATAR = 2;
    public static final int MESSAGE_IMAGE_CHAT = 3;
    public static final int MESSAGE_STRING_CHAT = 4;
    public static final int MESSAGE_STRING_NAME = 5;
    public static final int MESSAGE_BLUETOOTH = 6;
    public static final int MESSAGE_YOU_LOSE = 7;
    public static final int MESSAGE_EXIT = 8;
    public static final int MESSAGE_AGAIN = 9;
    public static final int MESSAGE_ACCEPT = 10;
    public static final int success = 1;

    public static Handler mGameHandler;         //Handler xử lí các message gửi đến UI thread
    private Board board;                        //Lưu dữ liệu sau mỗi lần đánh của bạn và đói phương
    private GridView mBoadGame;                 //Gridview tạo ván cờ
    private GridViewAdapter mGridViewAdapter;   //Adaoter của gridview
    private TextView competitorName, yourName;  //Tên của đối phương và bạn
    private ImageView competitorImg, yourImg;   //Avatar của  đối phương và bạn

    private boolean yourTurn = false;// cờ kiểm soát lượt đánh
    private boolean startGame = false; // cờ kiểm soát game có thể bắt đầu hay chưa
    private boolean isWinner = false; // cờ nếu đã có người chơi chiến thắng
    private Position lastMove;          //Vị trí mà bạn chọn trong ván cờ


    Dialog winnerDiaglog = null; // Dialog nếu bạn thắng
    Dialog loserDiaglog = null; // Dialog nếu bạn thua
    Dialog dialog = null;   // Dialog đợi đối thủ nếu bạn là chủ phòng
    Dialog acceptWatingDialog = null; // Dialog đợi sự phản hổi yêu cầu chơi lại
    Boolean isSerVer = false; // cờ dánh dáu bạn là Chủ phòng
    Boolean ExitedCompetitor = false;// Cờ đánh dấu đối thủ đã thoát hay chưa

    ImageView chatting, exitGame, receivedSticker, sentSticker;
    ;//Bao gồm icon chat để show BoxChat,icon để exit game, Hình chứa sticker được gửi từ đối phươn
    TextView receivedString, sentMessage; //TextView để chứa Message từ đối phương, và được gửi từ bạn
    Dialog chattingDialog;  //Box chat


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_bluetooth);
        mappingID();
        boardInit();
        initLoserDialog();
        initWinnerDialog();
        updateUI();
        createChattingDialog();
        exitGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothService.mConnectedThread != null) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameBluetoothActivity.this)
                            .setTitle("Thông báo")
                            .setMessage("Bạn chắc chắn muốn thoát");
                    alertDialogBuilder.setPositiveButton("Đồng ý",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    mBluetoothService.sendString(BluetoothService.SEND_EXIT, BluetoothService.SEND_EXIT);
                                    mBluetoothService.Disconnect();
                                    finish();
                                }
                            });
                    alertDialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing

                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    finish();
                }
            }
        });
        chatting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBluetoothService.mConnectedThread!=null)
                {
                    chattingDialog.show();
                }
            }
        });

        dialog = new Dialog(GameBluetoothActivity.this);
        isSerVer = getIntent().getBooleanExtra("connected", false);
        //Nếu là server thì show 1 dialog chờ đợi đối thủ, nếu là client thì sẽ send Tên người chơi
        if (isSerVer&&mBluetoothService.mBluetoothAdapter.isEnabled()) {
            mBluetoothService.start();
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
            ToastCustom.show("Chủ phòng đánh trước",GameBluetoothActivity.this);
            startGame = true;
            if (MySharedPerferences.isSavedBefore(GameBluetoothActivity.this)) {
                mBluetoothService.sendString(user.getName(), BluetoothService.SEND_STRING_NAME);
            }
        }
        //Phân loại các thông điệp gửi đến UI thread
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
                        receivedString.setText((String) msg.obj);
                        new CountDownTimer(3000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                receivedString.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onFinish() {
                                receivedString.setVisibility(View.GONE);
                            }
                        }.start();
                        break;
                    }
                    case MESSAGE_STRING_NAME: {

                        competitorName.setText((String) msg.obj);
                        if (isSerVer && MySharedPerferences.isSavedBefore(GameBluetoothActivity.this)) {
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
                        if (isSerVer && MySharedPerferences.isSavedBefore(GameBluetoothActivity.this)) {
                            sendAvatar();
                        }
                        break;
                    }
                    case MESSAGE_IMAGE_CHAT: {
                        byte[] readbuff = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(readbuff, 0, msg.arg1);
                        receivedSticker.setImageBitmap(bitmap);
                        new CountDownTimer(3000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                receivedSticker.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onFinish() {
                                receivedSticker.setVisibility(View.GONE);
                            }
                        }.start();
                        break;
                    }
                    case MESSAGE_YOU_LOSE: {
                        isWinner = true;
                        loserDiaglog.show();
                        break;
                    }
                    case MESSAGE_AGAIN: {
                        loserDiaglog.dismiss();
                        winnerDiaglog.dismiss();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameBluetoothActivity.this)
                                .setTitle("Thông báo")
                                .setMessage("Đối thủ muốn chơi lại");
                        alertDialogBuilder.setPositiveButton("Đồng ý",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        isWinner = false;
                                        mBluetoothService.sendString(BluetoothService.SEND_ACCEPT, BluetoothService.SEND_ACCEPT);
                                        board.reset();
                                        mGridViewAdapter.notifyDataSetChanged();
                                        ToastCustom.show("Người thua đánh trước",GameBluetoothActivity.this);

                                    }
                                });
                        alertDialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBluetoothService.sendString(BluetoothService.SEND_EXIT, BluetoothService.SEND_EXIT);
                            }
                        });
                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                        break;
                    }
                    case MESSAGE_ACCEPT: {
                        if (startGame) {
                            isWinner = false;
                            acceptWatingDialog.dismiss();
                            board.reset();
                            mGridViewAdapter.notifyDataSetChanged();
                            ToastCustom.show("Người thua đánh trước",GameBluetoothActivity.this);
                        }

                        break;
                    }
                    case MESSAGE_EXIT: {
                        ExitedCompetitor = true;
                        competitorImg.setImageResource(R.drawable.user);
                        competitorName.setText("Đối phương");
                        mBluetoothService.Disconnect();
                        if (!isSerVer) {
                            ToastCustom.show("Chủ phòng đã thoát",GameBluetoothActivity.this);
                            finish();
                            //                     finish();
                        } else {
                            winnerDiaglog.dismiss();
                            loserDiaglog.dismiss();
                            ToastCustom.show("Đối thủ đã thoát",GameBluetoothActivity.this);
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameBluetoothActivity.this)
                                    .setTitle("Thông báo")
                                    .setMessage("Bạn muốn đợi đối thủ tiếp theo");
                            alertDialogBuilder.setPositiveButton("Đồng ý",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            mBluetoothService.start();
                                            board.reset();
                                            mGridViewAdapter.notifyDataSetChanged();
                                            dialog.show();
                                        }
                                    });
                            alertDialogBuilder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                    }
                    break;
                }
                case MESSAGE_BLUETOOTH: {
                    if (msg.arg1 == 1 && msg.arg2 == success) {
                        ToastCustom.show("Chủ phòng đánh trước",GameBluetoothActivity.this);
                        ExitedCompetitor = false;
                        isWinner = false;
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
    }

    ;
}

    //Hàm cập nhật giao diện nếu bạn đã có thông tin cá nhân
    private void updateUI() {
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
    }

    //hàm ánh xạ View
    void mappingID() {
        sentMessage = findViewById(R.id.sentString);
        sentSticker = findViewById(R.id.sentSticker);
        receivedString = findViewById(R.id.receivedString);
        receivedSticker = findViewById(R.id.receivedSticker);
        chatting = findViewById(R.id.chatting_dialog);
        exitGame = findViewById(R.id.exit_GameActivity);
        competitorImg = findViewById(R.id.avatar_competitor);
        yourImg = findViewById(R.id.avatar_me);
        competitorName = findViewById(R.id.name_competitor);
        yourName = findViewById(R.id.name_me);
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

                if (!yourTurn || !startGame || isWinner) {

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
                    isWinner = true;
                    mBluetoothService.sendString(BluetoothService.SEND_YOU_LOSE, BluetoothService.SEND_YOU_LOSE);
                    winnerDiaglog.show();
                }
            }
        });
    }

    // hàm tạo Box chat
    void createChattingDialog() {
        chattingDialog = new Dialog(GameBluetoothActivity.this);
        chattingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        chattingDialog.setContentView(R.layout.dialog_chat);
        Window window = chattingDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttr = window.getAttributes();
        windowAttr.gravity = Gravity.CENTER;
        window.setAttributes(windowAttr);
        ImageView exittingClick = chattingDialog.findViewById(R.id.exit_sending);
        EditText message = chattingDialog.findViewById(R.id.messageChatting);
        TextView sending = chattingDialog.findViewById(R.id.sendMessage);
        GridView gridView = chattingDialog.findViewById(R.id.gridViewChat);
        ChattingAdapter chattingAdapter = new ChattingAdapter(MenuGameActivity.mListStick, GameBluetoothActivity.this);
        gridView.setAdapter(chattingAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chattingDialog.dismiss();
                sendIconDrawble(position);
                new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        sentSticker.setImageResource(mListStick.get(position));
                        sentSticker.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFinish() {
                        sentSticker.setVisibility(View.GONE);
                    }
                }.start();
            }
        });
        exittingClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chattingDialog.dismiss();
            }
        });
        sending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chattingDialog.dismiss();
                mBluetoothService.sendString(message.getText().toString(), BluetoothService.SEND_STRING_CHAT);
                sentMessage.setText(message.getText().toString());
                new CountDownTimer(3000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        sentMessage.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFinish() {
                        sentMessage.setVisibility(View.GONE);
                    }
                }.start();
            }
        });

    }

    //hàm gửi Icon cho đối phương
    void sendIconDrawble(int Drawable) {
        String stickerPath = MySharedPerferences.getValue(GameBluetoothActivity.this, "imagePath") + "/" + String.valueOf(mListStick.get(Drawable)) + ".jpg";
        byte[] bufferImage = ImageFromInternal.readImageFromInternal(stickerPath);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mBluetoothService.sendImage(bufferImage, BluetoothService.SEND_IMAGE_CHAT);
            }
        }).start();
    }

    // Hàm send avatar khi vào phòng cho đối pương
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

    //Hàm  tạo winner Dialog nếu bạn thắng
    void initWinnerDialog() {
        winnerDiaglog = new Dialog(GameBluetoothActivity.this);
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
                winnerDiaglog.dismiss();
                if (!ExitedCompetitor) {
                    mBluetoothService.sendString(BluetoothService.SEND_PLAY_AGAIN, BluetoothService.SEND_PLAY_AGAIN);
                    showWatingAccept();
                } else {
                    ToastCustom.show("Đối thủ đã thoát",GameBluetoothActivity.this);
                    mBluetoothService.Disconnect();
                    if (isSerVer) {
                        mBluetoothService.start();
                        board.reset();
                        mGridViewAdapter.notifyDataSetChanged();
                        dialog.show();
                    }
                }
            }
        });
        button2 = winnerDiaglog.findViewById(R.id.exit);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                competitorImg.setImageResource(R.drawable.user);
                competitorName.setText("Đối phương");
                mBluetoothService.sendString(BluetoothService.SEND_EXIT, BluetoothService.SEND_EXIT);
                mBluetoothService.Disconnect();
                winnerDiaglog.dismiss();
                if (!isSerVer) {
                    finish();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameBluetoothActivity.this)
                            .setTitle("Thông báo")
                            .setMessage("Bạn muốn đợi đối thủ tiếp theo");
                    alertDialogBuilder.setPositiveButton("Đồng ý",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    mBluetoothService.start();
                                    board.reset();
                                    mGridViewAdapter.notifyDataSetChanged();
                                    dialog.show();
                                }
                            });
                    alertDialogBuilder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
    }

    //Hàm  tạo wating Dilog nếu gửi yêu cầu chơi lại
    void showWatingAccept() {
        acceptWatingDialog = new Dialog(GameBluetoothActivity.this);
        acceptWatingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        acceptWatingDialog.setContentView(R.layout.wating_play_again);
        Window window = acceptWatingDialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);
        acceptWatingDialog.setCancelable(false);
        TextView button1;
        button1 = acceptWatingDialog.findViewById(R.id.cancel_again);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame = false;
                acceptWatingDialog.dismiss();
            }
        });
        acceptWatingDialog.show();
    }

    // Hàm tạo loser Dialog nếu bạn thua
    void initLoserDialog() {
        loserDiaglog = new Dialog(GameBluetoothActivity.this);
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
                loserDiaglog.dismiss();
                if (!ExitedCompetitor) {
                    mBluetoothService.sendString(BluetoothService.SEND_PLAY_AGAIN, BluetoothService.SEND_PLAY_AGAIN);
                    showWatingAccept();
                } else {
                    ToastCustom.show("Đối thủ đã thoát",GameBluetoothActivity.this);
                    mBluetoothService.Disconnect();
                    if (isSerVer) {
                        mBluetoothService.start();
                        board.reset();
                        mGridViewAdapter.notifyDataSetChanged();
                        dialog.show();
                    }
                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                competitorImg.setImageResource(R.drawable.user);
                competitorName.setText("Đối phương");
                mBluetoothService.sendString(BluetoothService.SEND_EXIT, BluetoothService.SEND_EXIT);
                mBluetoothService.Disconnect();
                loserDiaglog.dismiss();
                if (!isSerVer) {
                    finish();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameBluetoothActivity.this)
                            .setTitle("Thông báo")
                            .setMessage("Bạn muốn đợi đối thủ tiếp theo");
                    alertDialogBuilder.setPositiveButton("Đồng ý",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    mBluetoothService.start();
                                    board.reset();
                                    mGridViewAdapter.notifyDataSetChanged();
                                    dialog.show();
                                }
                            });
                    alertDialogBuilder.setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
    }

    //Hàm override lại chức năng của phím back
    @Override
    public void onBackPressed() {
        if (mBluetoothService.mConnectedThread != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameBluetoothActivity.this)
                    .setTitle("Thông báo")
                    .setMessage("Bạn chắc chắn muốn thoát");
            alertDialogBuilder.setPositiveButton("Đồng ý",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            mBluetoothService.sendString(BluetoothService.SEND_EXIT, BluetoothService.SEND_EXIT);
                            mBluetoothService.Disconnect();
                            finish();
                        }
                    });
            alertDialogBuilder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            super.onBackPressed();
        }
    }
}