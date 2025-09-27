package com.quizapp.quizapplication.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "user_answers")
@Data
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // For choice questions: comma-separated option IDs selected
    private String selectedOptionIds;

    // For text questions
    private String answerText;
}