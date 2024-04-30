package com.example.TicTacToe.util;

import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface MessageSender {
    Message sendMessage(SendMessage message);
    void sendAnswerCallback(AnswerCallbackQuery answer);
    void sendEditMessage(EditMessageText editMessageText);
}
