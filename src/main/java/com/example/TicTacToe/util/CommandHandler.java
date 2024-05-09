package com.example.TicTacToe.util;

import com.example.TicTacToe.model.GameState;
import com.example.TicTacToe.model.Player;
import com.example.TicTacToe.model.StatusPlayer;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        this.playerService.setCommandHandler(this);
    }

    public void handleHelpCommand(Message message){
        messageSender.sendMessage(createSendMessage(message.getChatId(), "Текст обработки команды помощи"));
    }

    public void handleStartCommand(Message message){
        messageSender.sendMessage(createSendMessage(message.getChatId(), "Текст обработки команды старт"));
    }

    public void handleSearchCommand(Message message) {
        Long chatId = message.getChatId();
        Player player = playerService.getPlayerByChatId(chatId);
        if(player == null) {
            player = playerService.addPlayer(chatId);
        }
        if(player.getStatus().equals(StatusPlayer.FREE)) {
            player.setStatus(StatusPlayer.IN_SEARCH);
            messageSender.sendMessage(createSendMessage(chatId, "Поиск соперника..."));
            findGame();
        } else if (player.getStatus().equals(StatusPlayer.IN_SEARCH)) {
            messageSender.sendMessage(createSendMessage(chatId, "Вы уже в поиске соперника"));
        } else if (player.getStatus().equals(StatusPlayer.IN_GAME)) {
            messageSender.sendMessage(createSendMessage(chatId, "Вы в игре"));
        }
    }

    private void findGame(){
        List<Player> players = playerService.checkMatchmaking();
        if (players.size() == 2) {
            GameState gameState = gameService.addGameState(players);
            messageSender.sendMessage(createSendMessage(players.get(0).getChatId(), "Соперник найден, игра началась"));
            messageSender.sendMessage(createSendMessage(players.get(1).getChatId(), "Соперник найден, игра началась"));
            SendMessage firstPlayerGameMessage = createSendMessage(gameState.getPlayers()[0].getChatId(), "Игровая доска");
            firstPlayerGameMessage.setReplyMarkup(getKeyboard(gameState));
            SendMessage secondPlayerGameMessage = createSendMessage(gameState.getPlayers()[1].getChatId(), "Игровая доска");
            secondPlayerGameMessage.setReplyMarkup(getKeyboard(gameState));
            players.get(0).setGameMessageId(messageSender.sendMessage(firstPlayerGameMessage).getMessageId());
            players.get(1).setGameMessageId(messageSender.sendMessage(secondPlayerGameMessage).getMessageId());
        }
    }

    public void handleCallback(CallbackQuery callbackQuery){
        Long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();
        if(!callbackData.equals("Inactive") && playerService.getPlayerByChatId(chatId).getGameMessageId() != null) {
            List<Integer> integers = Arrays.stream(callbackData.split(" ")).map(Integer::parseInt).toList();
            if(gameService.move(chatId, integers.get(0), integers.get(1))){
                editGameMessage(chatId);
                editGameMessage(gameService.getAnotherPlayer(chatId).getChatId());
            } else {
                AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
                answerCallbackQuery.setText("Не ваш ход");
                answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
                messageSender.sendAnswerCallback(answerCallbackQuery);
            }
        }
        checkWin(chatId);
    }

    private void editGameMessage(Long chatId){
        Player player = playerService.getPlayerByChatId(chatId);
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(String.valueOf(player.getChatId()));
        editMessageText.setMessageId(player.getGameMessageId());
        editMessageText.setReplyMarkup(getKeyboard(gameService.getGameStateByChatId(chatId)));
        editMessageText.setText("Измененная игровая доска");
        messageSender.sendEditMessage(editMessageText);
    }

    public SendMessage createSendMessage(Long chatId, String textMessage){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(textMessage);
        return sendMessage;
    }

    private InlineKeyboardMarkup getKeyboard(GameState gameState){
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        String[][] board = gameState.getBoard();
        for(int i = 1; i < 4; i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            for(int j = 1; j < 4; j++) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(String.valueOf(board[i - 1][j - 1]));
                button.setCallbackData(i + " " + j);
                row.add(button);
            }
            rows.add(row);
        }
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private void checkWin(Long chatId){
        GameState gameState = gameService.getGameStateByChatId(chatId);
        Player firstPlayer = gameState.getPlayers()[0];
        Player secondPlayer = gameState.getPlayers()[1];
        Player winner = gameState.checkWin();
        if(winner != null) {
            resetPlayer(winner);
            messageSender.sendMessage(createSendMessage(winner.getChatId(), "Вы победили"));

            Player anotherPlayer = gameService.getAnotherPlayer(winner.getChatId());
            resetPlayer(anotherPlayer);
            messageSender.sendMessage(createSendMessage(anotherPlayer.getChatId(), "Вы проиграли"));

            gameService.removeGameState(gameState);
        } else if (gameState.isBoardFull()) {
            messageSender.sendMessage(createSendMessage(firstPlayer.getChatId(), "Ничья"));
            resetPlayer(firstPlayer);
            messageSender.sendMessage(createSendMessage(secondPlayer.getChatId(), "Ничья"));
            resetPlayer(secondPlayer);
            gameService.removeGameState(gameState);
        }
    }

    private void resetPlayer(Player player) {
        player.setStatus(StatusPlayer.FREE);
        player.setGameMessageId(null);
    }
}
