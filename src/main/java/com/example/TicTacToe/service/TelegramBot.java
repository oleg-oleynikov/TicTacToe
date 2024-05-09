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
            handler.handleCallback(update.getCallbackQuery());
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();
            if (messageText.startsWith("/help"))
                executorService.execute(() -> handler.handleHelpCommand(message));
            else if (messageText.startsWith("/start"))
                executorService.execute(() -> handler.handleStartCommand(message));
            else if (messageText.startsWith("/search"))
                executorService.execute(() -> handler.handleSearchCommand(message));
        }
    }


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
