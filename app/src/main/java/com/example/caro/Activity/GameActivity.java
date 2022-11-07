package com.example.caro.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caro.Adapter.GridViewAdapter;
import com.example.caro.Model.ItemState;
import com.example.caro.R;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import  static  com.example.caro.Activity.MenuGameActivity.mBluetoothService;
public class GameActivity extends AppCompatActivity {


    public static final int STATUS_EMPTY = 0;
    public static final int STATUS_USER1 = 1;
    public static final int STATUS_USER2 = 2;

    public static final int MESSAGE_GAME = 1;
    public static final int MESSAGE_IMAGE = 2;
    public static final int MESSAGE_ENDGAME = 3;
    public static final int MESSAGE_CHAT = 4;
    public static final int MESSAGE_BLUETOOTH = 5;

    public static final int success = 1;
    public static final int fail = 0;

    public static Handler mGameHandler;
    private GridView mBoadGame;
    private List<ItemState> mItemList;
    private GridViewAdapter mGridViewAdapter;
    private TextView competitorName, yourName;
    private ImageView competitorImg, yourImg;

    private boolean yourTurn = false;
    private boolean startGame=false;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        final Dialog dialog = new Dialog(GameActivity.this);
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
        }
        else
        {
            startGame=true;
        }
        mGameHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case MESSAGE_GAME: {
                        int possion = (int) msg.obj;
                        mItemList.get(possion).setStatus(STATUS_USER2);
                        mGridViewAdapter.notifyDataSetChanged();
                        yourTurn=true;
                        break;
                    }
                    case MESSAGE_CHAT: {
                        break;
                    }
                    case MESSAGE_IMAGE: {
                        break;
                    }
                    case MESSAGE_ENDGAME: {
                        break;
                    }
                    case MESSAGE_BLUETOOTH: {
                        if (msg.arg1 == 1 && msg.arg2 == success) {
                            yourTurn=true;
                            startGame=true;
                            dialog.dismiss();
                        }
                        else if(msg.arg1==2&&msg.arg2==success)
                        {
                            // yourTurn=true;
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        };
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        mappingID();
        initGameBoard();
    }

    void mappingID() {
        mBoadGame = findViewById(R.id.gvGame);
        competitorImg = findViewById(R.id.avatar_competitor);
        yourImg = findViewById(R.id.avatar_me);
        competitorName = findViewById(R.id.name_competitor);
        yourName = findViewById(R.id.name_me);
    }

    void initGameBoard() {
        mItemList = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            mItemList.add(new ItemState(STATUS_EMPTY, R.drawable.status_empty));
        }
        mGridViewAdapter = new GridViewAdapter(this, mItemList);
        mBoadGame.setAdapter(mGridViewAdapter);
        mBoadGame.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!yourTurn||!startGame) {
                    //Toas kp
                    Toast.makeText(GameActivity.this, "Chưa đến lượt!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mItemList.get(position).getStatus() != STATUS_EMPTY) {
                    //Toast o nay da duoc danh
                    return;
                } else {
                    mItemList.get(position).setStatus(STATUS_USER1);
                    mGridViewAdapter.notifyDataSetChanged();
                }
                yourTurn = false;
                //Gui messa
                // competitorImg.setBackgroundResource(R.color.teal_200);
                mBluetoothService.write(String.valueOf(position).getBytes(StandardCharsets.UTF_8));
                if (checkWinner(position)) {
                    //Ban da thang
                    //send to Doi phuong
                }
            }
        });
    }

    boolean checkWinner(int position) {
        int xCoordinate = position % 20;
        int yCoordinate = position / 20;
        //Check horizental
        int count = 1;
        int i = 1;
        while ((xCoordinate - i) >= 0 && mItemList.get(yCoordinate * 20 + (xCoordinate - i)).getStatus() == STATUS_USER1) {
            count++;
            i++;
        }
        i = 1;
        while ((xCoordinate + i) < 20 && mItemList.get(yCoordinate * 20 + (xCoordinate + i)).getStatus() == STATUS_USER1) {
            count++;
            i++;
        }
        if (count == 5) return true;
        //Check Vertical
        count = 1;
        i = 1;
        while ((yCoordinate - i >= 0) && mItemList.get(xCoordinate + (yCoordinate - i) * 20).getStatus() == STATUS_USER1) {
            count++;
            i++;
        }
        i = 1;
        while ((yCoordinate + i < 20) && mItemList.get(xCoordinate + (yCoordinate + i) * 20).getStatus() == STATUS_USER1) {
            count++;
            i++;
        }
        if (count == 5) return true;
        //CheckXeo /
        count = 1;
        i = 1;
        while ((xCoordinate + i) < 20 && (yCoordinate - i) >= 0 && mItemList.get((yCoordinate - i) * 20 + xCoordinate + i).getStatus() == STATUS_USER1) {
            i++;
            count++;
        }
        i = 1;
        while ((xCoordinate - i) >= 0 && (yCoordinate + i) < 20 && mItemList.get(xCoordinate - i + 20 * (yCoordinate + i)).getStatus() == STATUS_USER1) {
            i++;
            count++;
        }
        if (count == 5) return true;
        //CheckXeo \
        count = 1;
        i = 1;
        while ((xCoordinate - i) >= 0 && (yCoordinate - i) >= 0 && mItemList.get((yCoordinate - i) * 20 + xCoordinate - i).getStatus() == STATUS_USER1) {
            i++;
            count++;
        }
        i = 1;
        while ((xCoordinate + i) < 20 && (yCoordinate + i) < 20 && mItemList.get(xCoordinate + i + 20 * (yCoordinate + i)).getStatus() == STATUS_USER1) {
            i++;
            count++;
        }
        if (count == 5) return true;
        return false;
    }
//    void showDiaglogWinner()
//    {
//        final Dialog winnerDiaglog=new Dialog(this);
//        winnerDiaglog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        winnerDiaglog.setContentView(R.layout.winner_diaglog);
//        Window window = dialog.getWindow();
//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        WindowManager.LayoutParams windowAttributes = window.getAttributes();
//        windowAttributes.gravity = Gravity.CENTER;
//        window.setAttributes(windowAttributes);
//        winnerDiaglog.setCancelable(false);
//        TextView button1,button2;
//        button1 = winnerDiaglog.findViewById(R.id.again);
//        button2=winnerDiaglog.findViewById(R.id.exit);
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for(int i=0;i<400;i++)
//                {
//                    mItemList.get(i).setStatus(STATUS_EMPTY);
//                }
//                //sendRequest
//                winnerDiaglog.dismiss();
//            }
//        });
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mBluetoothService.Disconnect();
//                finish();
//            }
//        });
//        winnerDiaglog.show();
//    }
//    void showDiaglogLoser()
//    {
//        final Dialog loserDiaglog=new Dialog(this);
//        loserDiaglog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        loserDiaglog.setContentView(R.layout.loser_dialog);
//        Window window = dialog.getWindow();
//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        WindowManager.LayoutParams windowAttributes = window.getAttributes();
//        windowAttributes.gravity = Gravity.CENTER;
//        window.setAttributes(windowAttributes);
//       loserDiaglog.setCancelable(false);
//        TextView button1,button2;
//        button1 = loserDiaglog.findViewById(R.id.again_loser);
//        button2=loserDiaglog.findViewById(R.id.exit_loser);
//        button1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for(int i=0;i<400;i++)
//                {
//                    mItemList.get(i).setStatus(STATUS_EMPTY);
//                }
//                //sendRequest
//                loserDiaglog.dismiss();
//            }
//        });
//        button2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mBluetoothService.Disconnect();
//                finish();
//            }
//        });
//        loserDiaglog.show();
//    }
}