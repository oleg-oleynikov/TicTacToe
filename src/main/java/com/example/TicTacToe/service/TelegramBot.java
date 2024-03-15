package com.example.TicTacToe.service;

import com.example.TicTacToe.config.BotConfig;
import com.example.TicTacToe.model.Game;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Queue<Long> players = new ConcurrentLinkedQueue<>();
    private final Map<Long, Game> games = new ConcurrentHashMap<>();

    @Autowired
    public TelegramBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();

            String textMessage = message.getText();


            if ("/search".equals(textMessage)) {
                searchPlayer(chatId);
            }

            String[] args = textMessage.trim().split(" ");
            if ("/move".equals(args[0]) && games.containsKey(chatId)) {
                executorService.execute(() -> {
                    if (args.length == 3) {
                        try {
                            int x = Integer.parseInt(args[1]) - 1;
                            int y = Integer.parseInt(args[2]) - 1;
                            playerMove(chatId, x, y);
                        } catch (NumberFormatException e) {
                            sendMessage(chatId, "Неверное использование команды /move. Пример /move 1 3");
                        }
                    } else {
                        sendMessage(chatId, "Неверное использование команды /move. Пример /move 1 2");
                    }
                });
            }
        }
    }

    public void searchPlayer(Long chatId) {
//        log.info(chatId + " ищет соперника");
        synchronized (players) {
            if (!players.contains(chatId)) {
                players.add(chatId);

                sendMessage(chatId, "Поиск соперника...");
            }
            checkMatchmaking();
        }
    }

    public void checkMatchmaking() {
        synchronized (players) {
            while(players.size() >= 2) {
                Game game = new Game();
                game.setFirstChatIdPlayer(players.poll());
                game.setSecondChatIdPlayer(players.poll());

                games.put(game.getFirstChatIdPlayer(), game);
                games.put(game.getSecondChatIdPlayer(), game);

                sendMessage(game.getFirstChatIdPlayer(), "Соперник найден");
                sendMessage(game.getSecondChatIdPlayer(), "Соперник найден");

                startGame(game);
            }
        }
    }

    public void startGame(Game game) {
        sendMessage(game.getIdPlayerWalks(), "Игра началась. Ваш ход");
        sendMessage(game.getIdPlayerNotWalks(), "Игра началась.");
    }

    public void playerMove(Long chatId, int x, int y) {
        if (games.containsKey(chatId) && (x >= 0 && x < 3) && (y >= 0 && y < 3)){
            Game game = games.get(chatId);
            if(game.getIdPlayerWalks().equals(chatId)) {
                if(game.move(x, y)) {
                    sendMessage(game.getIdPlayerWalks(), getBoardString(game));
                    sendMessage(game.getIdPlayerNotWalks(), getBoardString(game));
                }

                if(game.checkWin() != null) {
                    String[] arrMessage = endGame(game);
                    sendMessage(game.getFirstChatIdPlayer(), arrMessage[0]);
                    sendMessage(game.getSecondChatIdPlayer(), arrMessage[1]);

                    synchronized (games) {
                        games.remove(game.getFirstChatIdPlayer());
                        games.remove(game.getSecondChatIdPlayer());
                    }
                }
            }
        }
    }

    public String getBoardString(Game game) {
        StringBuilder sb = new StringBuilder();
        int[][] board = game.getBoard();
        for (int[] i : board) {
//            sb.append("——————————\n");
            for (int j = 0; j < board.length; j++) {
//                sb.append("|");
                sb.append(i[j]).append(" ");
            }
            sb.append("\n");
//            sb.append("|").append("\n");
        }
//        sb.append("——————————");
        return sb.toString()
//                .replace("0 ", "⭕\uFE0F")
                .replace("0 ", " 0 ")
                .replace("-1 ", "⬛\uFE0F")
//                .replace("1 ", "❌");
                .replace("1 ", " X ");
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    public String[] endGame(Game game){
        Long resultGame = game.checkWin();
        Long firstGhatId = game.getFirstChatIdPlayer();
        Long secondChatId = game.getSecondChatIdPlayer();
        if(resultGame.equals(firstGhatId))
            return new String[]{"Вы выйграли", "Вы проиграли"};
        else if (resultGame.equals(secondChatId))
            return new String[]{"Вы проиграли", "Вы победили"};
        else
            return new String[]{"Ничья", "Ничья"};
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }


}
