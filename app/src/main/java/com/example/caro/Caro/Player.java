package com.example.caro.Caro;

public abstract class Player {
    String name;

    /**
     * request name of player
     */
    abstract void requestName();

    /**
     * player take turn and return a Position
     * @param board
     * @return
     */
    abstract Position takeTurn(Board board);
}
