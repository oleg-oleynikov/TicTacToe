package com.example.TicTacToe.service;

import com.example.TicTacToe.model.Player;
import com.example.TicTacToe.model.StatusPlayer;
import com.example.TicTacToe.util.CommandHandler;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private final Queue<Player> players = new ConcurrentLinkedQueue<>();

    @Setter
    private CommandHandler commandHandler;

    public Optional<Player> findPlayerByChatId(Long chatId) {
        return players.stream()
                .filter(player -> player.getChatId().equals(chatId))
                .findAny();
    }

    public Player addPlayer(Long chatId) {
        synchronized (players) {
            Player player = new Player(chatId);
            if (findPlayerByChatId(chatId).isEmpty()) {
                players.add(player);
            }
            return player;
        }
    }

    public Player getPlayerByChatId(Long chatId) {
        return players.stream().filter(p -> p.getChatId().equals(chatId)).findFirst().orElse(null);
    }

    public List<Player> checkMatchmaking() {
        return players.stream()
                .filter(player -> player.getStatus().equals(StatusPlayer.IN_SEARCH))
                .limit(2)
                .collect(Collectors.toList());
    }
}
