package com.quizapp.quizapplication.dto;

import com.quizapp.quizapplication.enums.QuestionType;
import lombok.Data;

import java.util.List;

@Data
public class QuestionResponse {
    private Long id;
    private String text;
    private QuestionType type;
    private List<OptionResponse> options;  // Without isCorrect
}
