package com.example.TicTacToe.model;

import lombok.Data;

@Data
public class Game {
    private final int[][] board = {
            {-1, -1, -1},
            {-1, -1, -1},
            {-1, -1, -1}
    };

    private Integer playerMove = 0;
    private Player firstPlayer;
    private Player secondPlayer;

    public Game(Player firstPlayer, Player secondPlayer) {
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
    }

    public boolean move(int x, int y){
        if(board[x][y] != -1)
            return false;
        board[x][y] = playerMove;
        playerMove = playerMove ^ 1;
        return true;
    }

//    public boolean isPlayerMove(Long chatId){
//        return (playerMove == 0 && chatId.equals(firstPlayer.getChatId())) ||
//                (playerMove == 1 && chatId.equals(secondPlayer.getChatId()));
//    }
}
