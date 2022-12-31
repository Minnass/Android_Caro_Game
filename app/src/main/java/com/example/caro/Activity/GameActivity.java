package com.example.caro.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caro.Adapter.GridViewAdapter;
import com.example.caro.Caro.Board;
import com.example.caro.Caro.Field;
import com.example.caro.Caro.Human;
import com.example.caro.Caro.Player;
import com.example.caro.Caro.Position;
import com.example.caro.Caro.Util;
import com.example.caro.R;

public class GameActivity extends AppCompatActivity {

    private Board board;
    private Player player, opponent, activePlayer;
    private Field winner;
    private Position lastMove;
    GridView mBoardView;
    GridViewAdapter mGridViewAdapter;
    int countFilled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        player = new Human();
        opponent = new Human();
        initGameState();
        initBoard();
    }

    private void initBoard() {
        mBoardView = findViewById(R.id.board);
        mBoardView.setNumColumns(7);
        board = new Board(7, 7);
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
                    if (board.isFull()) {
                        showDialogDraw();
                    } else {
                        nextPlayer();
                    }
                } else {
                    showDialogWin(winner);
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

    private void restartGame() {
        board.reset();
        mGridViewAdapter.notifyDataSetChanged();
        initGameState();
    }

    private void initGameState() {
        if (Util.randomBit()) {
            activePlayer = player;
        } else {
            activePlayer = opponent;
        }
        winner = Field.EMPTY;
    }

    private void showDialogWin(Field winner) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_game_winner);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);
        dialog.setCancelable(false);

        TextView btn_again, btn_exit, txt_winner;
        txt_winner = dialog.findViewById(R.id.txt_winner);
        btn_again = dialog.findViewById(R.id.again);
        btn_exit = dialog.findViewById(R.id.exit);

        if (winner == Field.OPPONENT) {
            txt_winner.setText("X win!!!");
        } else if (winner == Field.PLAYER) {
            txt_winner.setText("Y win!!!");
        }
        btn_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
                dialog.dismiss();
            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialog.show();
    }

    private void showDialogDraw() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_game_draw);
        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);
        dialog.setCancelable(false);

        TextView btn_again, btn_exit;
        btn_again = dialog.findViewById(R.id.again);
        btn_exit = dialog.findViewById(R.id.exit);

        btn_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
                dialog.dismiss();
            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialog.show();
    }
}