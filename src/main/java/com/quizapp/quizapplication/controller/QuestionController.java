package com.quizapp.quizapplication.controller;

import com.quizapp.quizapplication.dto.AddQuestionRequest;
import com.quizapp.quizapplication.dto.UpdateQuestionRequest;
import com.quizapp.quizapplication.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/questions")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addQuestion(@PathVariable Long quizId, @Valid @RequestBody AddQuestionRequest request) {
        questionService.addQuestion(quizId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateQuestion(@PathVariable Long id, @Valid @RequestBody UpdateQuestionRequest request) {
        questionService.updateQuestion(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok().build();
    }
}