package com.example.TicTacToe.model;

public class GameState {
    private Game game;

    public GameState(Game game){
        this.game = game;
    }

    public boolean makeMove(int x, int y){
        return game.move(x, y);
    }

    public Player checkWin(){
        int[][] board = game.getBoard();
        Player firstPlayer = game.getFirstPlayer();
        Player secondPlayer = game.getSecondPlayer();

        for (int i = 0; i < 3; i++) {
            if (board[i][0] != -1 && board[i][0] == board[i][1] && board[i][0] == board[i][2]) {
                return board[i][0] == 0 ? firstPlayer : secondPlayer;
            }
        }

        for (int i = 0; i < 3; i++) {
            if (board[0][i] != -1 && board[0][i] == board[1][i] && board[0][i] == board[2][i]) {
                return board[0][i] == 0 ? firstPlayer : secondPlayer;
            }
        }

        if (board[0][0] != -1 && board[0][0] == board[1][1] && board[0][0] == board[2][2]) {
            return board[0][0] == 0 ? firstPlayer : secondPlayer;
        }

        if (board[0][2] != -1 && board[0][2] == board[1][1] && board[0][2] == board[2][0]) {
            return board[0][2] == 0 ? firstPlayer : secondPlayer;
        }

        return null;
    }

    public String[][] getBoard(){
        int[][] board = game.getBoard();
        String[][] sBoard = new String[3][3];
        for(int i = 0; i < sBoard.length; i++) {
            for(int j = 0; j < sBoard[i].length; j++){
                sBoard[i][j] = switch (board[i][j]) {
                    case 0 -> "⭕";
                    case 1 -> "❌";
                    default -> "⬛";
                };
            }
        }
        return sBoard;
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

    public boolean isPlayerMove(Player player){
        Integer playerMove = game.getPlayerMove();
        return (playerMove == 0 && player.equals(game.getFirstPlayer())) ||
                (playerMove == 1 && player.equals(game.getSecondPlayer()));
    }

    public Player[] getPlayers(){
        return new Player[]{game.getFirstPlayer(), game.getSecondPlayer()};
    }

    public Game getGame(){
        return game;
    }
}
