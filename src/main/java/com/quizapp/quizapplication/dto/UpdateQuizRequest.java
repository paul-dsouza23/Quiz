package com.quizapp.quizapplication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateQuizRequest {
    @NotBlank
    private String title;
}