package com.example.TicTacToe.util;

import com.example.TicTacToe.service.GameService;
import com.example.TicTacToe.service.PlayerService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class CommandHandler {
    private PlayerService playerService;
    private GameService gameService;
    @Setter
    private MessageSender messageSender;

    @Autowired
    public CommandHandler(PlayerService playerService, GameService gameService) {
        this.playerService = playerService;
        this.gameService = gameService;
    }

    public void handleHelpCommand(Message message){
        messageSender.sendMessage(createSendMessage(message.getChatId(), "Текст обработки команды помощи"));
    }

    public void handleStartCommand(Message message){

    }

    public void handleSearchCommand(Message message){
        messageSender.sendMessage(createSendMessage(message.getChatId(), "Текст обработки команды поиска"));
    }

    private SendMessage createSendMessage(Long chatId, String textMessage){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(textMessage);
        return sendMessage;
    }
}
