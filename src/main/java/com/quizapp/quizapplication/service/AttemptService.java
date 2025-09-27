package com.quizapp.quizapplication.service;

import com.quizapp.quizapplication.dto.AttemptResponse;
import com.quizapp.quizapplication.dto.ScoreResponse;
import com.quizapp.quizapplication.dto.SubmitAnswerRequest;
import com.quizapp.quizapplication.dto.UserAnswerResponse;
import com.quizapp.quizapplication.entity.*;
import com.quizapp.quizapplication.enums.QuestionType;
import com.quizapp.quizapplication.enums.Role;
import com.quizapp.quizapplication.repository.QuizAttemptRepository;
import com.quizapp.quizapplication.repository.QuizRepository;
import com.quizapp.quizapplication.repository.UserAnswerRepository;
import com.quizapp.quizapplication.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttemptService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserAnswerRepository answerRepository;



    public ScoreResponse submitAnswers(Long quizId, SubmitAnswerRequest request) {
        User currentUser = getCurrentUser();
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new RuntimeException("Quiz not found"));

        if (request.getAnswers().size() != quiz.getQuestions().size()) {
            throw new RuntimeException("Must answer all questions");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(currentUser);
        attempt.setQuiz(quiz);
        attempt.setTotalQuestions(quiz.getQuestions().size());

        attempt = attemptRepository.save(attempt); // Save to make it persistent



        int score = 0;
        for (SubmitAnswerRequest.AnswerEntry entry : request.getAnswers()) {
            Question question = quiz.getQuestions().stream()
                    .filter(q -> q.getId().equals(entry.getQuestionId()))
                    .findFirst().orElseThrow(() -> new RuntimeException("Invalid question ID"));

            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setAttempt(attempt);
            userAnswer.setQuestion(question);

            boolean isCorrect = false;
            if (question.getType() == QuestionType.TEXT) {
                if (entry.getAnswerText() == null || entry.getAnswerText().length() > 300) {
                    throw new RuntimeException("Text answer must be under 300 characters");
                }
                userAnswer.setAnswerText(entry.getAnswerText());
                isCorrect = entry.getAnswerText().equalsIgnoreCase(question.getCorrectAnswerText());
            } else {
                // Choice types
                if (entry.getSelectedOptionIds() == null || entry.getSelectedOptionIds().isEmpty()) {
                    throw new RuntimeException("Options required for choice questions");
                }
                userAnswer.setSelectedOptionIds(String.join(",", entry.getSelectedOptionIds().stream().map(String::valueOf).toList()));

                // Check correctness
                List<Long> correctIds = question.getOptions().stream()
                        .filter(Option::isCorrect)
                        .map(Option::getId)
                        .toList();
                List<Long> selected = entry.getSelectedOptionIds();

                if (question.getType() == QuestionType.SINGLE_CHOICE && selected.size() != 1) {
                    throw new RuntimeException("Single choice allows only one selection");
                }

                isCorrect = selected.containsAll(correctIds) && correctIds.containsAll(selected);
            }

            if (isCorrect) score++;
            answerRepository.save(userAnswer);
        }

        attempt.setScore(score);
        attemptRepository.save(attempt); // Update the saved attempt

        ScoreResponse response = new ScoreResponse();
        response.setScore(score);
        response.setTotal(attempt.getTotalQuestions());
        return response;
    }

    public List<AttemptResponse> getMyScores() {
        User currentUser = getCurrentUser();
        return mapToAttemptResponses(attemptRepository.findByUser(currentUser));
    }

    public List<AttemptResponse> getAllScores() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("Access denied");
        }
        return mapToAttemptResponses(attemptRepository.findAll());
    }

    public AttemptResponse getAttemptDetails(Long attemptId) {
        User currentUser = getCurrentUser();
        QuizAttempt attempt = attemptRepository.findById(attemptId).orElseThrow(() -> new RuntimeException("Attempt not found"));

        if (currentUser.getRole() != Role.ADMIN && !attempt.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        return mapToAttemptResponse(attempt);
    }

    private List<AttemptResponse> mapToAttemptResponses(List<QuizAttempt> attempts) {
        return attempts.stream().map(this::mapToAttemptResponse).toList();
    }

    private AttemptResponse mapToAttemptResponse(QuizAttempt attempt) {
        AttemptResponse resp = new AttemptResponse();
        resp.setId(attempt.getId());
        resp.setUserId(attempt.getUser().getId());
        resp.setQuizId(attempt.getQuiz().getId());
        resp.setScore(attempt.getScore());
        resp.setTotal(attempt.getTotalQuestions());
        resp.setAttemptedAt(attempt.getAttemptedAt());
        resp.setAnswers(attempt.getAnswers().stream().map(a -> {
            UserAnswerResponse uar = new UserAnswerResponse();
            uar.setQuestionId(a.getQuestion().getId());
            uar.setSelectedOptionIds(a.getSelectedOptionIds());
            uar.setAnswerText(a.getAnswerText());
            return uar;
        }).toList());
        return resp;
    }

//    private User getCurrentUser() {
//        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
//    }
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Principal: " + principal.getClass().getName());

        if (!(principal instanceof CustomUserDetails)) {
            throw new RuntimeException("Invalid user principal");
        }

        return ((CustomUserDetails) principal).getUser();
    }

}