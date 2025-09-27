package com.quizapp.quizapplication.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswerRequest {
    private List<AnswerEntry> answers;

    @Data
    public static class AnswerEntry {
        private Long questionId;
        private List<Long> selectedOptionIds;  // For multiple choice
        private String answerText;  // For text
    }
}