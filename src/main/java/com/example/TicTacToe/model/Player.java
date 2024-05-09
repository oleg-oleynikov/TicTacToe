package com.example.TicTacToe.model;

import lombok.Data;

@Data
public class Player {
    private Long chatId;
    private Integer gameMessageId;
    private StatusPlayer status;

    public Player(Long chatId) {
        this.chatId = chatId;
        this.status = StatusPlayer.FREE;
    }
}
