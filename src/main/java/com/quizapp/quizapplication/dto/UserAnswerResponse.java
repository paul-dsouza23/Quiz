package com.quizapp.quizapplication.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserAnswerResponse {
    private Long questionId;
    private String selectedOptionIds;

    @Size(max = 300)
    private String answerText;
}
