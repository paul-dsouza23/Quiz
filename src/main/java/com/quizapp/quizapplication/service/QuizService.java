package com.quizapp.quizapplication.service;

import com.quizapp.quizapplication.dto.*;
import com.quizapp.quizapplication.entity.Question;
import com.quizapp.quizapplication.entity.Quiz;
import com.quizapp.quizapplication.entity.User;
import com.quizapp.quizapplication.exception.AccessDeniedException;
import com.quizapp.quizapplication.exception.QuestionNotFoundOrInactiveException;
import com.quizapp.quizapplication.exception.QuizNotFoundOrInactiveException;
import com.quizapp.quizapplication.repository.QuizRepository;
import com.quizapp.quizapplication.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizResponse createQuiz(CreateQuizRequest request) {
        User currentUser = getCurrentUser();

        log.info("Creating quiz '{}' by userId={}", request.getTitle(), currentUser.getId());

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setCreatedBy(currentUser);
        quiz.setActive(true);
        quiz = quizRepository.save(quiz);

        return mapToQuizResponse(quiz);
    }

    public QuizResponse getQuiz(Long quizId) {
        log.info("Fetching quiz with id={}", quizId);

        Quiz quiz = quizRepository.findActiveById(quizId)
                .orElseThrow(() -> {
                    log.warn("Quiz with id={} not found or inactive", quizId);
                    return new QuizNotFoundOrInactiveException("Quiz not found or inactive");
                });
        return mapToQuizResponse(quiz);
    }

    public List<QuizResponse> getAllActiveQuizzes() {
        log.info("Fetching all active quizzes");

        return quizRepository.findAllActive().stream()
                .map(this::mapToQuizResponse)
                .toList();
    }

    public QuizResponse updateQuiz(Long quizId, UpdateQuizRequest request) {
        log.info("Updating quiz with id={}", quizId);

        Quiz quiz = quizRepository.findActiveById(quizId)
                .orElseThrow(() ->  {
                    log.warn("Quiz with id={} not found or inactive", quizId);
                    return new QuizNotFoundOrInactiveException("Quiz not found or inactive");
                });

        User currentUser = getCurrentUser();
        if (!quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            log.warn("User id={} tried to update quiz id={} without permission", currentUser.getId(), quizId);
            throw new AccessDeniedException("Only the creator can update the quiz");
        }
        quiz.setTitle(request.getTitle());
        quiz = quizRepository.save(quiz);

        log.info("Quiz id={} updated successfully", quizId);

        return mapToQuizResponse(quiz);
    }

    public void deleteQuiz(Long quizId) {
        log.info("Deleting quiz with id={}", quizId);

        Quiz quiz = quizRepository.findActiveById(quizId)
                .orElseThrow(() -> {
                    log.warn("Quiz with id={} not found or inactive", quizId);
                    return new QuizNotFoundOrInactiveException("Quiz not found or inactive");
                });

        User currentUser = getCurrentUser();
        if (!quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            log.warn("User id={} tried to delete quiz id={} without permission", currentUser.getId(), quizId);
            throw new AccessDeniedException("Only the creator can delete the quiz");
        }
        quiz.setActive(false);
        quizRepository.save(quiz);

        log.info("Quiz id={} marked inactive (soft deleted)", quizId);
    }

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        QuizResponse response = new QuizResponse();
        response.setId(quiz.getId());
        response.setTitle(quiz.getTitle());
        response.setQuestions(quiz.getQuestions().stream()
                .filter(Question::isActive)
                .map(q -> {
                    QuestionResponse qr = new QuestionResponse();
                    qr.setId(q.getId());
                    qr.setText(q.getText());
                    qr.setType(q.getType());
                    qr.setOptions(q.getOptions().stream().map(o -> {
                        OptionResponse or = new OptionResponse();
                        or.setId(o.getId());
                        or.setText(o.getText());
                        return or;
                    }).toList());
                    return qr;
                }).toList());
        return response;
    }

    private User getCurrentUser() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }
}