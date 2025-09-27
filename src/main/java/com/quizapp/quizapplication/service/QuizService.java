package com.quizapp.quizapplication.service;

import com.quizapp.quizapplication.dto.*;
import com.quizapp.quizapplication.entity.Question;
import com.quizapp.quizapplication.entity.Quiz;
import com.quizapp.quizapplication.entity.User;
import com.quizapp.quizapplication.repository.QuizRepository;
import com.quizapp.quizapplication.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizResponse createQuiz(CreateQuizRequest request) {
        User currentUser = getCurrentUser();

        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setCreatedBy(currentUser);
        quiz.setActive(true);
        quiz = quizRepository.save(quiz);

        return mapToQuizResponse(quiz);
    }

    public QuizResponse getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findActiveById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found or inactive"));
        return mapToQuizResponse(quiz);
    }

    public List<QuizResponse> getAllActiveQuizzes() {
        return quizRepository.findAllActive().stream()
                .map(this::mapToQuizResponse)
                .toList();
    }

    public QuizResponse updateQuiz(Long quizId, UpdateQuizRequest request) {
        Quiz quiz = quizRepository.findActiveById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found or inactive"));
        User currentUser = getCurrentUser();
        if (!quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the creator can update the quiz");
        }
        quiz.setTitle(request.getTitle());
        quiz = quizRepository.save(quiz);
        return mapToQuizResponse(quiz);
    }

    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findActiveById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found or inactive"));
        User currentUser = getCurrentUser();
        if (!quiz.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the creator can delete the quiz");
        }
        quiz.setActive(false);
        quizRepository.save(quiz);
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