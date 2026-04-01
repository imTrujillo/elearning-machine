package com.learning_engine.dto;

import java.time.LocalDateTime;

public record ErrorDto(String timestamp, int status, String error, String message, String path) {
    public ErrorDto(int status, String error, String message, String path){
        this(LocalDateTime.now().toString(), status, error, message, path);
    }
}
