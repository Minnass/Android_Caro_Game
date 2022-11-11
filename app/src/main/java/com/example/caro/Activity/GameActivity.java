package com.example.caro.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
import com.example.caro.Caro.Board;
import com.example.caro.Caro.Field;
import com.example.caro.Caro.Human;
import com.example.caro.Caro.Player;
import com.example.caro.Caro.Position;
import com.example.caro.Caro.Util;
import com.example.caro.Model.ItemState;
import com.example.caro.R;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.example.caro.Activity.MenuGameActivity.mBluetoothService;

public class GameActivity extends AppCompatActivity {

    private Board board;
    private Player player;
    private Player opponent;
    private Player activePlayer;
    private Field winner;
    private Position lastMove;
    GridView mBoardView;
    GridViewAdapter mGridViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        player = new Human();
        opponent = new Human();
        if (Util.randomBit()) {
            activePlayer = player;
        } else {
            activePlayer = opponent;
        }
        winner = Field.EMPTY;
        boardInit();
    }

    private void boardInit() {
        mBoardView = findViewById(R.id.board);
        mBoardView.setNumColumns(5);
        board = new Board(5, 5);
        mGridViewAdapter = new GridViewAdapter(this, board);
        mBoardView.setAdapter(mGridViewAdapter);

        mBoardView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (winner != Field.EMPTY || board.getField(position) != Field.EMPTY) {
                    return;
                }

                lastMove = new Position(position % board.getDimensionX(), position / board.getDimensionY());
                if (activePlayer == player) {
                    board.fillPostion(lastMove, Field.PLAYER);
                    mGridViewAdapter.notifyDataSetChanged();
                } else if (activePlayer == opponent) {
                    board.fillPostion(lastMove, Field.OPPONENT);
                    mGridViewAdapter.notifyDataSetChanged();
                }

                winner = board.findWinner(lastMove);
                if (winner == Field.EMPTY) {
                    nextPlayer();
                } else {
                    congratulate();
                }
            }

            private void congratulate() {
                Toast.makeText(getApplicationContext(), "found winner", Toast.LENGTH_LONG).show();
            }

            private void nextPlayer() {
                activePlayer = activePlayer == player ? opponent : player;
            }
        });
    }
}