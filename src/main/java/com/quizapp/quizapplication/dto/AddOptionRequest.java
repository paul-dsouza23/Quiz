package com.quizapp.quizapplication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddOptionRequest {
    @NotBlank
    private String text;

    @JsonProperty("isCorrect")
    private boolean isCorrect;
}