package com.learning_engine.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LearningApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> LearningApiResponse<T> success (String message, T data){
        LearningApiResponse<T> r = new LearningApiResponse<>();
        r.success = true;
        r.message = message;
        r.data = data;
        r.timestamp = LocalDateTime.now();

        return r;
    }

    public static <T> LearningApiResponse<T> error (String message){
        LearningApiResponse<T> r = new LearningApiResponse<>();
        r.success = false;
        r.message = message;
        r.data = null;
        r.timestamp = LocalDateTime.now();

        return r;
    }
}
