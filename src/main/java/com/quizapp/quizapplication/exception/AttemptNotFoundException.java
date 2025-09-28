package com.quizapp.quizapplication.exception;

public class AttemptNotFoundException extends RuntimeException {
    public AttemptNotFoundException(String message) {
        super(message);
    }
}
