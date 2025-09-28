package com.quizapp.quizapplication.controller;

import com.quizapp.quizapplication.dto.CreateQuizRequest;
import com.quizapp.quizapplication.dto.QuizResponse;
import com.quizapp.quizapplication.dto.UpdateQuizRequest;
import com.quizapp.quizapplication.exception.AccessDeniedException;
import com.quizapp.quizapplication.exception.QuizNotFoundOrInactiveException;
import com.quizapp.quizapplication.service.QuizService;
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
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        try {
            QuizResponse response = quizService.createQuiz(request);
            return ResponseEntity.ok(response);

        } catch (AccessDeniedException e) {
            log.error("Access Denied: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");

        }catch (Exception ex) {
            log.error("Error creating quiz: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while creating the quiz");
        }    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuiz(@PathVariable Long id) {
        try {
            QuizResponse response = quizService.getQuiz(id);
            return ResponseEntity.ok(response);

        } catch (QuizNotFoundOrInactiveException ex) {
            log.warn("Quiz not found or inactive: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());

        } catch (Exception ex) {
            log.error("Error fetching quiz id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while fetching the quiz");
        }    }

    @GetMapping
    public ResponseEntity<?> getAllQuizzes() {
        try {
            List<QuizResponse> quizzes = quizService.getAllActiveQuizzes();
            return ResponseEntity.ok(quizzes);

        } catch (Exception ex) {
            log.error("Error fetching quizzes: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while fetching quizzes");
        }    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateQuiz(@PathVariable Long id, @Valid @RequestBody UpdateQuizRequest request) {
        try {
            QuizResponse response = quizService.updateQuiz(id, request);
            return ResponseEntity.ok(response);
        } catch (QuizNotFoundOrInactiveException ex) {
            log.warn("Quiz not found or inactive: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }catch (AccessDeniedException e) {
                log.error("Access Denied: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
        } catch (Exception ex) {
            log.error("Error updating quiz id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while updating the quiz");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id) {
        try {
            quizService.deleteQuiz(id);
            return ResponseEntity.ok().build();

        } catch (QuizNotFoundOrInactiveException ex) {
            log.warn("Quiz not found or inactive: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }catch (AccessDeniedException e) {
                log.error("Access Denied: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
        } catch (Exception ex) {
            log.error("Error deleting quiz id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while deleting the quiz");
        }
    }
}