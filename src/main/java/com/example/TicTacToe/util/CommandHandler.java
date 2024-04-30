package com.example.TicTacToe.util;

import com.example.TicTacToe.model.GameState;
import com.example.TicTacToe.model.Player;
import com.example.TicTacToe.service.GameService;
import com.example.TicTacToe.service.PlayerService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommandHandler {
    private PlayerService playerService;
    private GameService gameService;

    @Setter
    @Getter
    private MessageSender messageSender;

    @Autowired
    public CommandHandler(PlayerService playerService, GameService gameService) {
        this.playerService = playerService;
        this.gameService = gameService;
        this.gameService.setCommandHandler(this);
        this.playerService.setCommandHandler(this);
    }

    public void handleHelpCommand(Message message){
        messageSender.sendMessage(createSendMessage(message.getChatId(), "Текст обработки команды помощи"));
    }

    public void handleStartCommand(Message message){
        messageSender.sendMessage(createSendMessage(message.getChatId(), "Текст обработки команды старт"));
    }

    public void handleSearchCommand(Message message) {
        List<Player> players = playerService.addPlayer(message.getChatId());
        messageSender.sendMessage(createSendMessage(message.getChatId(), "Поиск соперника..."));
        if (players.size() == 2) {
            GameState gameState = gameService.addGameState(players);
            messageSender.sendMessage(createSendMessage(players.get(0).getChatId(), "Соперник найден, игра началась"));
            messageSender.sendMessage(createSendMessage(players.get(1).getChatId(), "Соперник найден, игра началась"));
            SendMessage firstPlayerGameMessage = createSendMessage(gameState.getPlayers()[0].getChatId(), "Игровая доска");
//            firstPlayerGameMessage.setReplyMarkup(null);
            SendMessage secondPlayerGameMessage = createSendMessage(gameState.getPlayers()[1].getChatId(), "Игровая доска");
//            secondPlayerGameMessage.setReplyMarkup(null);
            players.get(0).setGameMessageId(messageSender.sendMessage(firstPlayerGameMessage).getMessageId());
            players.get(1).setGameMessageId(messageSender.sendMessage(secondPlayerGameMessage).getMessageId());
        }
    }

    public void handleCallback(Update update){
        Long chatId = update.getMessage().getChatId();
        String callbackData = update.getCallbackQuery().getData();
        if(!callbackData.equals("Inactive")) {
            List<Integer> integers = Arrays.stream(callbackData.split(" ")).map(Integer::parseInt).toList();
            if(gameService.move(chatId, integers.get(0), integers.get(1))){
                editGameMessage(chatId);
            } else {
                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
                answerCallbackQuery.setText("Не ваш ход");
                answerCallbackQuery.setCallbackQueryId(update.getCallbackQuery().getId());
            }
        }
    }

    private void editGameMessage(Long chatId){
        Player player = playerService.getPlayerByChatId(chatId);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(player.getChatId()));
        editMessageText.setMessageId(player.getGameMessageId());
        editMessageText.setText("Измененная игровая доска");
        messageSender.sendEditMessage(editMessageText);
    }

    public SendMessage createSendMessage(Long chatId, String textMessage){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(textMessage);
        return sendMessage;
    }

    public InlineKeyboardMarkup getKeyboard(GameState gameState){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for(int i = 1; i < 4; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for(int j = 1; j < 4; j++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(i * j));
                button.setCallbackData(String.valueOf(i + " " + j));
                row.add(button);
            }
            rows.add(row);
        }
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }
}
