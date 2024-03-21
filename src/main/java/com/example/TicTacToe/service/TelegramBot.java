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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
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
//    private final CommandHandler handler;

//    @Autowired
//    public TelegramBot(BotConfig config, CommandHandler handler) {
//        this.config = config;
//        this.handler = handler;
//    }
    @Autowired
    public TelegramBot(BotConfig botConfig) {
        this.config = botConfig;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Long chatId = message.getChatId();

            String textMessage = message.getText();
            if("/help".equals(textMessage) || "/start".equals(textMessage)) {
                sendMessage(chatId, "Добро пожаловать в игру крестики-нолики!\n" +
                        "Это классическая игра, в которой два игрока ходят по очереди, " +
                        "ставя свои символы (крестики и нолики) на игровое поле 3x3. " +
                        "Цель игры - выстроить три своих символа в ряд по горизонтали, вертикали или диагонали." +
                        "Вот список команд, которые вы можете использовать в игре:\n" +
                        "\n" +
                        "/help - Показывает это сообщение с описанием игры и доступных команд.\n" +
                        "/search - Начинает новый поиск соперника.\n" +
                        "/move <row> <column> - Делает ход, размещая символ на указанных координатах игрового поля. " +
                        "Например, /move 1 2 разместит ваш символ в первой строке и втором столбце.\n" +
                        "/endgame - Завершает текущую игру.");
            }

            if ("/search".equals(textMessage)) {
//                sendMessage(chatId, "");
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

            if("endgame".equals(textMessage)) {
            }
        }
    }

    public void searchPlayer(Long chatId) {
        synchronized (players) {
            if (!players.contains(chatId)) {
                players.add(chatId);

                sendMessage(chatId, "Поиск соперника...");

                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId.toString());
                sendMessage.setText("  y  ");
                sendMessage.setReplyMarkup(getKeybord());
//        sendMessage.setParseMode("HTML");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                }
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
        sendMessage(game.getIdPlayerWalks(), getBoardString(game));
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

//    public String getBoardString(Game game) {
//        StringBuilder sb = new StringBuilder();
//        int[][] board = game.getBoard();
//
//        for(int i = 0; i < board.length; i++){
//            for(int j = 0; j < board[i].length; j++){
////                sb.append("<a href='/move ")
////                        .append(i)
////                        .append(" ")
////                        .append(j)
////                        .append("'>")
////                        .append(getSymbolByNum(board[i][j]))
////                        .append("</a>")
////                        .append(" ");
//                sb.append("[").append(getSymbolByNum(board[i][j])).append("]").append("(");
//            }
//        }
//
//        return sb.toString();
//    }

    public String getSymbolByNum(int num) {
        return switch (num) {
            case 0 -> "⭕";
            case 1 -> "❌";
            default -> "⬛";
        };
    }

    public InlineKeyboardMarkup getKeybord(){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for(int i = 1; i <= 4; i++){
            List<InlineKeyboardButton> row = new ArrayList<>();
            for(int j = 1; j <= 4; j++){
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i * j));
                button.setCallbackData(i + " " + j);
                row.add(button);
            }
            rows.add(row);
        }
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
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
                .replace("0 ", "⭕\uFE0F")
//                .replace("0 ", " 0 ")
                .replace("-1 ", "⬛\uFE0F")
                .replace("1 ", "❌");
//                .replace("1 ", " X ");
    }

    public void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(message);
//        sendMessage.setParseMode("HTML");
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

    public void inAdvanceEndGame(Long chatId){
        if(games.containsKey(chatId)) {
            Game game = games.get(chatId);
            Long firstChatId = game.getFirstChatIdPlayer();
            Long secondChatId = game.getSecondChatIdPlayer();

            games.remove(firstChatId);
            games.remove(secondChatId);

            sendMessage(firstChatId, "Игра была завершена досрочно.");
            sendMessage(secondChatId, "Игра была завершена досрочно.");
        } else {
            sendMessage(chatId, "Вы должны находиться в игре для того чтобы заранее закончить");
        }
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
