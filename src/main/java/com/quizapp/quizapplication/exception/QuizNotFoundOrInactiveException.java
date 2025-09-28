package com.quizapp.quizapplication.exception;

public class QuizNotFoundOrInactiveException extends RuntimeException {
    public QuizNotFoundOrInactiveException(String message) {
        super(message);
    }
}
