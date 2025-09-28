package com.quizapp.quizapplication.controller;

import com.quizapp.quizapplication.dto.AddQuestionRequest;
import com.quizapp.quizapplication.dto.UpdateQuestionRequest;
import com.quizapp.quizapplication.exception.AccessDeniedException;
import com.quizapp.quizapplication.exception.InvalidQuestionException;
import com.quizapp.quizapplication.exception.QuestionNotFoundOrInactiveException;
import com.quizapp.quizapplication.exception.QuizNotFoundOrInactiveException;
import com.quizapp.quizapplication.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/admin/questions")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addQuestion(@PathVariable Long quizId, @Valid @RequestBody AddQuestionRequest request) {
        try {
            questionService.addQuestion(quizId, request);
            return ResponseEntity.ok("Question added successfully");

        } catch (InvalidQuestionException ex) {
            log.warn("Invalid question data: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (QuizNotFoundOrInactiveException ex) {
            log.warn("Quiz not found or inactive: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access Denied: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
        } catch (Exception ex) {
            log.error("Error adding question to quizId {}: {}", quizId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while adding the question");
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateQuestion(@PathVariable Long id, @Valid @RequestBody UpdateQuestionRequest request) {
        try {
            questionService.updateQuestion(id, request);
            return ResponseEntity.ok("Question updated successfully");

        } catch (QuestionNotFoundOrInactiveException ex) {
            log.warn("Question not found or inactive: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (QuizNotFoundOrInactiveException ex) {
            log.warn("Quiz not found or inactive: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }catch (InvalidQuestionException ex) {
            log.warn("Invalid question data: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(ex.getMessage());
        }catch (AccessDeniedException e) {
            log.error("Access Denied: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
        } catch (Exception ex) {
            log.error("Error updating question id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while updating the question");
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        try {
            questionService.deleteQuestion(id);
            return ResponseEntity.ok("Question deleted successfully");
        } catch (QuestionNotFoundOrInactiveException ex) {
            log.warn("Question not found or inactive: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (AccessDeniedException e) {
            log.error("Access Denied: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied.");
        }catch (Exception ex) {
            log.error("Error deleting question id {}: {}", id, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while deleting the question");
        }
    }
}