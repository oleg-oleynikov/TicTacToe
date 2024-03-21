package com.example.TicTacToe.model;

public class GameState {
    private Game game;

    public GameState(Game game){
        this.game = game;
    }

    public boolean isGameOver(){
        return game.checkWin() != null || game.isBoardFull();
    }


    public Long getWinner(){
        return 1L;
    }

    public boolean makeMove(int x, int y){
        return game.move(x, y);
    }

    public Long checkWin(){
        int[][] board = game.getBoard();
        Long firstChatIdPlayer = game.getFirstChatIdPlayer();
        Long secondChatIdPlayer = game.getSecondChatIdPlayer();

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

        return null;
    }

    public boolean isBoardFull(){
        int[][] board = game.getBoard();
        for (int[] row : board) {
            for (int cell : row) {
                if (cell == -1) {
                    return false;
                }
            }
        }

        return true;
    }
}
