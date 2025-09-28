package com.quizapp.quizapplication.exception;

public class QuestionNotFoundOrInactiveException extends RuntimeException {
    public QuestionNotFoundOrInactiveException(String message) {
        super(message);
    }
}
