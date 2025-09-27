package com.quizapp.quizapplication.controller;

import com.quizapp.quizapplication.dto.AttemptResponse;
import com.quizapp.quizapplication.dto.ScoreResponse;
import com.quizapp.quizapplication.dto.SubmitAnswerRequest;
import com.quizapp.quizapplication.service.AttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptService attemptService;


    @PostMapping("/{quizId}")
    public ResponseEntity<ScoreResponse> submitAnswers(@PathVariable Long quizId, @Valid @RequestBody SubmitAnswerRequest request) {
        return ResponseEntity.ok(attemptService.submitAnswers(quizId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<AttemptResponse>> getMyScores() {
        return ResponseEntity.ok(attemptService.getMyScores());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttemptResponse>> getAllScores() {
        return ResponseEntity.ok(attemptService.getAllScores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttemptResponse> getAttemptDetails(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.getAttemptDetails(id));
    }
}
