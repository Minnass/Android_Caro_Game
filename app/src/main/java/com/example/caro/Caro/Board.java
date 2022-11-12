package com.example.caro.Caro;

public class Board {
    private int dimensionX;
    private int dimensionY;
    private Field fields[][];


    public Board(int dimensionX, int dimensionY) {
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        fields = new Field[dimensionX][dimensionY];
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                fields[i][j] = Field.EMPTY;
            }
        }
    }

    public void reset()
    {
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                fields[i][j] = Field.EMPTY;
            }
        }
    }

    public int getDimensionX() {
        return dimensionX;
    }

    public int getDimensionY() {
        return dimensionY;
    }

    public Field getField(Position pos) {
        return fields[pos.x][pos.y];
    }

    public Field getField(int pos) {
        return fields[pos % dimensionX][pos / dimensionY];
    }

    void print() {
    }    //TODO

    /**
     * fill position `pos` with `type`
     *
     * @param pos
     * @param field
     * @return
     */
    public boolean fillPostion(Position pos, Field field) {
        if (fields[pos.x][pos.y] != Field.EMPTY) {
            return false;
        }
        if (field == Field.EMPTY) {
            return false;
        }
        fields[pos.x][pos.y] = field;
        return true;
    }

    /**
     * find position that `threateningPlayer` could win by filling it
     *
     * @param threateningPlayer
     * @return
     */
    Position findThreatenedField(Field threateningPlayer) {

        return new Position(-1, -1);
    }   // TODO

    /**
     * count number of `type` field
     *
     * @param type
     * @return
     */
    int countFields(Field type) {
        int res = 0;
        for (int i = 0; i < dimensionX; i++) {
            for (int j = 0; j < dimensionY; j++) {
                if (fields[i][j] == type) res++;
            }
        }
        return res;
    }

    /**
     * determine whether winner is PLAYER or OPPONENT
     * if winner is not determined yet, EMPTY is return
     *
     * @param lastMove
     * @return
     */
    public Field findWinner(Position lastMove) {
        int increaseX[] = {1, 0, 1, -1};    // row, col, diag, anti diag
        int increaseY[] = {0, 1, 1, 1};
        int x = lastMove.x;
        int y = lastMove.y;
        Field type = fields[lastMove.x][lastMove.y];

        for (int i = 0; i < 4; i++) {
            int incX = increaseX[i];
            int incY = increaseY[i];
            int countFields = 0;
            // count forward
            for (int j = 0; j < 5; j++) {
                if (x + j * incX >= 0 && x + j * incX < dimensionX && y + j * incY >= 0 && y + j * incY < dimensionY
                        && fields[x + j * incX][y + j * incY] == type) {
                    countFields++;
                } else {
                    break;
                }
            }
            // count backward
            for (int j = 1; j < 5; j++) {
                if (x - j * incX >= 0 && x - j * incX < dimensionX && y - j * incY >= 0 && y - j * incY < dimensionY
                        && fields[x - j * incX][y - j * incY] == type) {
                    countFields++;
                } else {
                    break;
                }
            }
            // check winner
            if (countFields == 5) {
                return type;
            }
        }
        return Field.EMPTY;
    }
}
