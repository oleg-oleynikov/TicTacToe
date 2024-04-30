package com.example.TicTacToe.service;

import com.example.TicTacToe.config.BotConfig;
import com.example.TicTacToe.util.CommandHandler;
import com.example.TicTacToe.util.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot implements MessageSender {

    private final BotConfig config;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final CommandHandler handler;

    @Autowired
    public TelegramBot(BotConfig botConfig, CommandHandler handler) {
        this.config = botConfig;
        this.handler = handler;
        handler.setMessageSender(this);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasCallbackQuery()){
            handler.handleCallback(update);
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();
            if (messageText.startsWith("/help"))
                handler.handleHelpCommand(message);
            else if (messageText.startsWith("/start"))
                handler.handleStartCommand(message);
            else if (messageText.startsWith("/search"))
                handler.handleSearchCommand(message);

//            switch (message.getText()){
//                case "/help": handler.handleHelpCommand();
//                case "/search"
//            }
//            String textMessage = message.getText();
//            if("/help".equals(textMessage) || "/start".equals(textMessage)) {
//                sendMessage(chatId, "Добро пожаловать в игру крестики-нолики!\n" +
//                        "Это классическая игра, в которой два игрока ходят по очереди, " +
//                        "ставя свои символы (крестики и нолики) на игровое поле 3x3. " +
//                        "Цель игры - выстроить три своих символа в ряд по горизонтали, вертикали или диагонали." +
//                        "Вот список команд, которые вы можете использовать в игре:\n" +
//                        "\n" +
//                        "/help - Показывает это сообщение с описанием игры и доступных команд.\n" +
//                        "/search - Начинает новый поиск соперника.\n" +
//                        "/move <row> <column> - Делает ход, размещая символ на указанных координатах игрового поля. " +
//                        "Например, /move 1 2 разместит ваш символ в первой строке и втором столбце.\n" +
//                        "/endgame - Завершает текущую игру.");
//            }

//            if ("/search".equals(textMessage)) {
////                sendMessage(chatId, "");
//                searchPlayer(chatId);
//            }

//            String[] args = textMessage.trim().split(" ");
//            if ("/move".equals(args[0]) && games.containsKey(chatId)) {
//                executorService.execute(() -> {
//                    if (args.length == 3) {
//                        try {
//                            int x = Integer.parseInt(args[1]) - 1;
//                            int y = Integer.parseInt(args[2]) - 1;
//                            playerMove(chatId, x, y);
//                        } catch (NumberFormatException e) {
//                            sendMessage(chatId, "Неверное использование команды /move. Пример /move 1 3");
//                        }
//                    } else {
//                        sendMessage(chatId, "Неверное использование команды /move. Пример /move 1 2");
//                    }
//                });
//            }
//
//            if("endgame".equals(textMessage)) {
//            }
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

//    public String getSymbolByNum(int num) {
//        return switch (num) {
//            case 0 -> "⭕";
//            case 1 -> "❌";
//            default -> "⬛";
//        };
//    }


//    public String getBoardString(Game game) {
//        StringBuilder sb = new StringBuilder();
//        int[][] board = game.getBoard();
//        for (int[] i : board) {
////            sb.append("——————————\n");
//            for (int j = 0; j < board.length; j++) {
////                sb.append("|");
//                sb.append(i[j]).append(" ");
//            }
//            sb.append("\n");
////            sb.append("|").append("\n");
//        }
////        sb.append("——————————");
//        return sb.toString()
//                .replace("0 ", "⭕\uFE0F")
////                .replace("0 ", " 0 ")
//                .replace("-1 ", "⬛\uFE0F")
//                .replace("1 ", "❌");
////                .replace("1 ", " X ");
//    }



    @Override
    public Message sendMessage(SendMessage sendMessage) {
        Message message = null;
        try {
            message = execute(sendMessage);
        } catch (TelegramApiException exception){
            log.error(exception.getMessage());
        }
        return message;
    }

    @Override
    public void sendAnswerCallback(AnswerCallbackQuery answer) {
        try {
            execute(answer);
        } catch (TelegramApiException exception){
            log.error(exception.getMessage());
        }
    }

    @Override
    public void sendEditMessage(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException exception){
            log.error(exception.getMessage());
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
