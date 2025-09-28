package com.quizapp.quizapplication.controller;

import com.quizapp.quizapplication.dto.AttemptResponse;
import com.quizapp.quizapplication.dto.ScoreResponse;
import com.quizapp.quizapplication.dto.SubmitAnswerRequest;
import com.quizapp.quizapplication.exception.AccessDeniedException;
import com.quizapp.quizapplication.exception.AttemptNotFoundException;
import com.quizapp.quizapplication.exception.InvalidAnswerException;
import com.quizapp.quizapplication.exception.QuizNotFoundOrInactiveException;
import com.quizapp.quizapplication.service.AttemptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptService attemptService;


    @PostMapping("/{quizId}")
    public ResponseEntity<?> submitAnswers(@PathVariable Long quizId, @Valid @RequestBody SubmitAnswerRequest request) {
        try {
            log.info("Submitting answers for quizId: {}", quizId);
            ScoreResponse response =  attemptService.submitAnswers(quizId, request);
            return ResponseEntity.ok(response);
        }  catch (InvalidAnswerException e) {
            log.warn("Invalid answer during submission: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid answers submitted: " + e.getMessage());
        } catch (QuizNotFoundOrInactiveException ex) {
            log.warn("Quiz not found or inactive: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during answer submission: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("An error occurred while submitting answers.");
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyScores() {
        try {
            log.info("Fetching current user's attempt history");
            List<AttemptResponse> responses = attemptService.getMyScores();
            return ResponseEntity.ok(responses);
        }catch (AccessDeniedException e) {
            log.error("Access Denied: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
        } catch (Exception e) {
            log.error("Failed to fetch user attempts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Unable to retrieve your attempts at this time.");
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllScores() {
        try {
            log.info("Fetching all user attempts (admin access)");
            List<AttemptResponse> responses = attemptService.getAllScores();
            return ResponseEntity.ok(responses);
        } catch (AccessDeniedException e) {
            log.error("Access Denied: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
        } catch (Exception e) {
            log.error("Error fetching all attempts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Unable to retrieve attempts data.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAttemptDetails(@PathVariable Long id) {
        try {
            log.info("Fetching attempt details for id: {}", id);
            AttemptResponse response = attemptService.getAttemptDetails(id);
            return ResponseEntity.ok(response);
        } catch (AttemptNotFoundException e) {
            log.warn("Attempt not found: {}", e.getMessage());
            return ResponseEntity.status(404).body("Attempt not found with ID: " + id);
        } catch (Exception e) {
            log.error("Error fetching attempt details: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Unable to retrieve attempt details.");
        }    }
}
