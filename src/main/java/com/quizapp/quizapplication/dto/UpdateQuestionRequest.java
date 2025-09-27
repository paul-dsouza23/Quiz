package com.quizapp.quizapplication.dto;

import com.quizapp.quizapplication.enums.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class UpdateQuestionRequest {
    @NotBlank
    private String text;

    @NotNull
    private QuestionType type;

    private List<AddOptionRequest> options; // For choice types

    private String correctAnswerText; // For TEXT type
}
