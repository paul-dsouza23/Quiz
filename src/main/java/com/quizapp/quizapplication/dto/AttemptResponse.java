package com.quizapp.quizapplication.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AttemptResponse {
    private Long id;
    private Long userId;
    private Long quizId;
    private int score;
    private int total;
    private LocalDateTime attemptedAt;
    private List<UserAnswerResponse> answers;
}