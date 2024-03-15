package com.example.TicTacToe.model;

import lombok.Data;

@Data
public class Game {
    private int[][] board = {
            {-1, -1, -1},
            {-1, -1, -1},
            {-1, -1, -1}
    };

    private Integer playerMove = 0;
    private Long firstChatIdPlayer;
    private Long secondChatIdPlayer;

    public Long checkWin() {

        for (int i = 0; i < 3; i++) {
            if (board[i][0] != -1 && board[i][0] == board[i][1] && board[i][0] == board[i][2]) {
                return board[i][0] == 0 ? firstChatIdPlayer : secondChatIdPlayer;
            }
        }

        for (int i = 0; i < 3; i++) {
            if (board[0][i] != -1 && board[0][i] == board[1][i] && board[0][i] == board[2][i]) {
                return board[0][i] == 0 ? firstChatIdPlayer : secondChatIdPlayer;
            }
        }

        if (board[0][0] != -1 && board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
            return board[0][0] == 0 ? firstChatIdPlayer : secondChatIdPlayer;
        }

        if (board[0][2] != -1 && board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
            return board[0][2] == 0 ? firstChatIdPlayer : secondChatIdPlayer;
        }

        boolean isBoardFull = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == -1) {
                    isBoardFull = false;
                    break;
                }
            }
        }

        if (isBoardFull) {
            return -1L;
        }

        return null;
    }

    public boolean move(int x, int y){
        if(board[x][y] != -1)
            return false;
        board[x][y] = playerMove;
        playerMove = playerMove ^ 1;
        return true;
    }

//    public boolean isPlayerMove(Long chatId){
//        return (playerMove == 0 && chatId.equals(firstChatIdPlayer)) ||
//                (playerMove == 1 && chatId.equals(secondChatIdPlayer));
//    }

    public Long getIdPlayerWalks(){
        return playerMove == 0 ? firstChatIdPlayer : secondChatIdPlayer;
    }

    public Long getIdPlayerNotWalks(){
        return playerMove == 0 ? secondChatIdPlayer : firstChatIdPlayer;
    }
}
