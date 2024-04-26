package com.example.TicTacToe.service;

import com.example.TicTacToe.model.Player;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PlayerService {
    private final Queue<Player> players = new ConcurrentLinkedQueue<>();

    public Optional<Player> findPlayerByChatId(Long chatId) {
        return players.stream()
                .filter(player -> player.getChatId().equals(chatId))
                .findAny();
    }

    public void searchPlayer(Long chatId) {
        synchronized (players) {
            if (findPlayerByChatId(chatId).isEmpty()) {
                players.add(new Player(chatId));

//                sendMessage(chatId, "Поиск соперника...");
            }
//            checkMatchmaking();
        }
    }

    public Long getGameMessageIdByChatId(Long chatId) {
        Optional<Player> optionalPlayer = findPlayerByChatId(chatId);
        return optionalPlayer.map(Player::getChatId).orElse(null);
    }
}
