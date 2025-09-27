package com.quizapp.quizapplication.dto;

import lombok.Data;

import java.util.List;

@Data
public class QuizResponse {
    private Long id;
    private String title;
    private List<QuestionResponse> questions;
}