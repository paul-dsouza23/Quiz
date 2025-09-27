package com.quizapp.quizapplication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateQuizRequest {
    @NotBlank
    private String title;
}