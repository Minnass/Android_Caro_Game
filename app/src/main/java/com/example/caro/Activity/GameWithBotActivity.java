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
import com.example.caro.Caro.Bot;
import com.example.caro.Caro.Field;
import com.example.caro.Caro.Human;
import com.example.caro.Caro.Player;
import com.example.caro.Caro.Position;
import com.example.caro.Caro.Util;
import com.example.caro.R;

public class GameWithBotActivity extends AppCompatActivity {

    private Board board;
    private Player player, bot, activePlayer;
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
        bot = new Bot();
        initGameState();
        initBoard(20, 20);
    }

    private void initBoard(int dimensionX, int dimensionY) {
        mBoardView = findViewById(R.id.board);
        mBoardView.setNumColumns(dimensionY);
        board = new Board(dimensionX, dimensionY);
        mGridViewAdapter = new GridViewAdapter(this, board);
        mBoardView.setAdapter(mGridViewAdapter);

        if (activePlayer == bot) {
            board.fillPostion(new Position((int) Math.ceil((double) dimensionX / 2),
                    (int) Math.ceil((double) dimensionY / 2)), Field.OPPONENT);
            mGridViewAdapter.notifyDataSetChanged();
        }

        mBoardView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (winner != Field.EMPTY || board.getField(position) != Field.EMPTY) {
                    return;
                }

                lastMove = new Position(position % board.getDimensionX(), position / board.getDimensionY());
                board.fillPostion(lastMove, Field.PLAYER);
                mGridViewAdapter.notifyDataSetChanged();

                winner = board.findWinner(lastMove);
                if (winner == Field.EMPTY) {
                    if (board.isFull()) {
                        showDialogDraw();
                    }
                    else {
                        lastMove = bot.takeTurn(board);
                        board.fillPostion(lastMove, Field.OPPONENT);
                        winner = board.findWinner(lastMove);
                        if (winner == Field.EMPTY) {
                            if (board.isFull()) {
                                showDialogDraw();
                            }
                        } else {
                            showDialogWin(winner);
                        }
                    }
                }
                else {
                    showDialogWin(winner);
                }
            }

            private void congratulate() {
                Toast.makeText(getApplicationContext(), "found winner", Toast.LENGTH_LONG).show();
            }

            private void nextPlayer() {
                activePlayer = activePlayer == player ? bot : player;
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
            activePlayer = bot;
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