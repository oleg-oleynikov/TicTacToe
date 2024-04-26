package com.example.TicTacToe.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Player {
    private Long chatId;
    private Long gameMessageId;

    public Player(Long chatId) {
        this.chatId = chatId;
    }
}
