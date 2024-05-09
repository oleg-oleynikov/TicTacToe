package com.example.TicTacToe.service;

import com.example.TicTacToe.model.Game;
import com.example.TicTacToe.model.GameState;
import com.example.TicTacToe.model.Player;
import com.example.TicTacToe.model.StatusPlayer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class GameService {
    private List<GameState> games = new ArrayList<>();
    private PlayerService playerService;

    @Autowired
    public GameService(PlayerService playerService){
        this.playerService = playerService;
    }

    public GameState getGameStateByChatId(Long chatId){
        return games.stream()
                .filter(g -> Arrays.asList(Arrays.stream(g.getPlayers()).map(Player::getChatId).toArray()).contains(chatId))
                .findAny()
                .orElse(null);
    }

    public boolean move(Long chatId, int x, int y){
        GameState gameState = getGameStateByChatId(chatId);
        Player player = playerService.findPlayerByChatId(chatId).orElse(null);
        if(player != null && gameState.isPlayerMove(player)){
            return gameState.makeMove(x - 1, y - 1);
        }
        return false;
    }

    public GameState addGameState(List<Player> players){
        Player firstPlayer = players.get(0);
        Player secondPlayer = players.get(1);
        firstPlayer.setStatus(StatusPlayer.IN_GAME);
        secondPlayer.setStatus(StatusPlayer.IN_GAME);
        GameState newGameState = new GameState(new Game(firstPlayer, secondPlayer));
        games.add(newGameState);
        return newGameState;
    }

    public Player getAnotherPlayer(Long chatId){
        GameState gameState = getGameStateByChatId(chatId);
        Player firstPlayer = gameState.getPlayers()[0];
        Player secondPlayer = gameState.getPlayers()[1];
        return firstPlayer.getChatId().equals(chatId) ? secondPlayer : firstPlayer;
    }

    public void removeGameState(GameState gameState) {
        games.remove(gameState);
    }
}
