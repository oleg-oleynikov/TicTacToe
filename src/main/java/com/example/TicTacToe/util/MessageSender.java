package com.example.TicTacToe.util;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public interface MessageSender {
    public void sendMessage(SendMessage message);
}
