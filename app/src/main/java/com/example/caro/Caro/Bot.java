package com.example.caro.Caro;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Bot extends Player {
    static int winScore = 100000000;

    @Override
    public void requestName() {
        // TODO
    }

    @Override
    public Position takeTurn(Board board) {
        Field[][] matrix_copy = board.getFields();
        Field[][] matrix = new Field[matrix_copy.length][];
        for (int i = 0; i < matrix_copy.length; i++) {
            matrix[i] = matrix_copy[i].clone();
        }
        return findWinningMove(matrix);
    }

    private Position findWinningMove(Field[][] board) {
        List<Position> allPosibleMoves = generateMoves(board);

        int bestScore = -1;
        Position winningMove = new Position(-1, -1);
        for (Position move : allPosibleMoves) {
            int score = Math.max(getScore(tryMove(board, move, Field.OPPONENT), Field.OPPONENT),
                    getScore(tryMove(board, move, Field.PLAYER), Field.PLAYER));
            if (score > bestScore) {
                bestScore = score;
                winningMove = move;
            }
        }

        return winningMove;
    }

    private List<Position> generateMoves(Field[][] board) {
        int lenX = board.length;
        int lenY = board[0].length;
        List<Position> moves = new ArrayList<Position>();
        for (int i = 0; i < lenX; i++) {
            for (int j = 0; j < lenY; j++) {

                if (board[i][j] != Field.EMPTY) continue;

                if ((i > 0 && j > 0 && board[i - 1][j - 1] != Field.EMPTY)
                        || (i > 0 && board[i - 1][j] != Field.EMPTY)
                        || (i > 0 && j < lenY - 1 && board[i - 1][j + 1] != Field.EMPTY)
                        || (j > 0 && board[i][j - 1] != Field.EMPTY)
                        || (j < lenY - 1 && board[i][j + 1] != Field.EMPTY)
                        || (i < lenX - 1 && j > 0 && board[i + 1][j - 1] != Field.EMPTY)
                        || (i < lenX - 1 && board[i + 1][j] != Field.EMPTY)
                        || (i < lenX - 1 && j < lenY - 1 && board[i + 1][j + 1] != Field.EMPTY)) {
                    moves.add(new Position(i, j));
                }
            }
        }

        return moves;
    }

    private Field[][] tryMove(Field[][] board, Position move, Field player) {
        Field[][] matrix = new Field[board.length][];
        for (int i = 0; i < board.length; i++) {
            matrix[i] = board[i].clone();
        }
        matrix[move.x][move.y] = player;
        return matrix;
    }

    private int getScore(Field[][] board, Field player) {
        int score1 = evaluateHorizontal(board, player);
        int score2 = evaluateVertical(board, player);
        int score3 = evaluateDiagonal(board, player);
        return  score1 + score2 + score3;
    }

    private int evaluateHorizontal(Field[][] board, Field player) {

        int consecutive = 0;
        int blocks = 2;
        int score = 0;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {

                if (board[i][j] == player) {
                    consecutive++;
                }

                // gặp ô trống
                else if (board[i][j] == Field.EMPTY) {
                    // kết thúc chuỗi
                    if (consecutive > 0) {
                        // giảm block, tính điểm, reset
                        blocks--;
                        score += getConsecutiveSetScore(consecutive, blocks);
                        consecutive = 0;
                        blocks = 1;
                    }
                    // chưa bắt đầu chuỗi
                    else {
                        // reset
                        blocks = 1;
                    }
                }

                // gặp quân địch
                // kết thúc chuỗi
                else if (board[i][j] != player && consecutive > 0) {
                    // tính điểm, reset
                    score += getConsecutiveSetScore(consecutive, blocks);
                    consecutive = 0;
                    blocks = 2;
                }
                // chưa bắt đầu chuỗi
                else {
                    // reset
                    blocks = 2;
                }
            }

            // cuối dòng mà còn liên tục
            if (consecutive > 0) {
                // tính điểm
                score += getConsecutiveSetScore(consecutive, blocks);

            }
            // reset
            consecutive = 0;
            blocks = 2;

        }
        return score;
    }

    private int evaluateVertical(Field[][] board, Field player) {

        int consecutive = 0;
        int blocks = 2;
        int score = 0;

        for (int j = 0; j < board[0].length; j++) {
            for (int i = 0; i < board.length; i++) {

                if (board[i][j] == player) {
                    consecutive++;
                }

                // gặp ô trống
                else if (board[i][j] == Field.EMPTY) {
                    // kết thúc chuỗi
                    if (consecutive > 0) {
                        // giảm block, tính điểm, reset
                        blocks--;
                        score += getConsecutiveSetScore(consecutive, blocks);
                        consecutive = 0;
                        blocks = 1;
                    }
                    // chưa bắt đầu chuỗi
                    else {
                        // reset
                        blocks = 1;
                    }
                }

                // gặp quân địch
                // kết thúc chuỗi
                else if (board[i][j] != player && consecutive > 0) {
                    // tính điểm, reset
                    score += getConsecutiveSetScore(consecutive, blocks);
                    consecutive = 0;
                    blocks = 2;
                }
                // chưa bắt đầu chuỗi
                else {
                    // reset
                    blocks = 2;
                }
            }

            // cuối dòng mà còn liên tục
            if (consecutive > 0) {
                // tính điểm
                score += getConsecutiveSetScore(consecutive, blocks);

            }
            // reset
            consecutive = 0;
            blocks = 2;

        }
        return score;
    }

    private int evaluateDiagonal(Field[][] board, Field player) {

        int consecutive = 0;
        int blocks = 2;
        int score = 0;

        // Đường chéo /
        for (int k = 0; k <= 2 * (board.length - 1); k++) {
            int iStart = Math.max(0, k - board.length + 1);
            int iEnd = Math.min(board.length - 1, k);
            for (int i = iStart; i <= iEnd; ++i) {
                int j = k - i;

                if (board[i][j] == player) {
                    consecutive++;
                } else if (board[i][j] == Field.EMPTY) {
                    if (consecutive > 0) {
                        blocks--;
                        score += getConsecutiveSetScore(consecutive, blocks);
                        consecutive = 0;
                        blocks = 1;
                    } else {
                        blocks = 1;
                    }
                } else if (board[i][j] != player && consecutive > 0) {
                    score += getConsecutiveSetScore(consecutive, blocks);
                    consecutive = 0;
                    blocks = 2;
                } else {
                    blocks = 2;
                }

            }
            if (consecutive > 0) {
                score += getConsecutiveSetScore(consecutive, blocks);

            }
            consecutive = 0;
            blocks = 2;
        }

        // Đường chéo \
        for (int k = 1 - board.length; k < board.length; k++) {
            int iStart = Math.max(0, k);
            int iEnd = Math.min(board.length + k - 1, board.length - 1);
            for (int i = iStart; i <= iEnd; ++i) {
                int j = i - k;

                if (board[i][j] == player) {
                    consecutive++;
                } else if (board[i][j] == Field.EMPTY) {
                    if (consecutive > 0) {
                        blocks--;
                        score += getConsecutiveSetScore(consecutive, blocks);
                        consecutive = 0;
                        blocks = 1;
                    } else {
                        blocks = 1;
                    }
                } else if (board[i][j] != player && consecutive > 0) {
                    score += getConsecutiveSetScore(consecutive, blocks);
                    consecutive = 0;
                    blocks = 2;
                } else {
                    blocks = 2;
                }

            }
            if (consecutive > 0) {
                score += getConsecutiveSetScore(consecutive, blocks);

            }
            consecutive = 0;
            blocks = 2;
        }
        return score;
    }

    private int getConsecutiveSetScore(int count, int blocks) {
        final int winGuarantee = 1000000;
        if (blocks == 2 && count < 5) return 0;
        switch (count) {
            case 5: {
                return winScore;
            }
            case 4: {
                if (blocks == 0) return winGuarantee / 4;
                else return 200;
            }
            case 3: {
                if (blocks == 0) {
                    return 200;
                } else {
                    return 5;
                }
            }
            case 2: {
                if (blocks == 0) {
                    return 5;
                } else {
                    return 3;
                }
            }
            case 1: {
                return 1;
            }
        }
        return winScore * 2;
    }
}
