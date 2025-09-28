package com.quizapp.quizapplication.service;

import com.quizapp.quizapplication.dto.AttemptResponse;
import com.quizapp.quizapplication.dto.ScoreResponse;
import com.quizapp.quizapplication.dto.SubmitAnswerRequest;
import com.quizapp.quizapplication.dto.UserAnswerResponse;
import com.quizapp.quizapplication.entity.*;
import com.quizapp.quizapplication.enums.QuestionType;
import com.quizapp.quizapplication.enums.Role;
import com.quizapp.quizapplication.exception.*;
import com.quizapp.quizapplication.repository.QuizAttemptRepository;
import com.quizapp.quizapplication.repository.QuizRepository;
import com.quizapp.quizapplication.repository.UserAnswerRepository;
import com.quizapp.quizapplication.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class AttemptService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserAnswerRepository answerRepository;


    public ScoreResponse submitAnswers(Long quizId, SubmitAnswerRequest request) {
        log.info("Submitting answers for quizId={} by user", quizId);

        try {
            User currentUser = getCurrentUser();
            Quiz quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new QuizNotFoundOrInactiveException("Quiz not found with id " + quizId));

            if (request.getAnswers().size() != quiz.getQuestions().size()) {
                throw new InvalidAnswerException("Must answer all questions");
            }

            QuizAttempt attempt = new QuizAttempt();
            attempt.setUser(currentUser);
            attempt.setQuiz(quiz);
            attempt.setTotalQuestions(quiz.getQuestions().size());

            attempt = attemptRepository.save(attempt);

            int score = 0;
            for (SubmitAnswerRequest.AnswerEntry entry : request.getAnswers()) {
                Question question = quiz.getQuestions().stream()
                        .filter(q -> q.getId().equals(entry.getQuestionId()))
                        .findFirst()
                        .orElseThrow(() -> new InvalidAnswerException("Invalid question ID"));

                UserAnswer userAnswer = new UserAnswer();
                userAnswer.setAttempt(attempt);
                userAnswer.setQuestion(question);

                boolean isCorrect = false;
                if (question.getType() == QuestionType.TEXT) {
                    if (entry.getAnswerText() == null || entry.getAnswerText().length() > 300) {
                        throw new InvalidAnswerException("Text answer must be under 300 characters");
                    }
                    userAnswer.setAnswerText(entry.getAnswerText());
                    isCorrect = entry.getAnswerText().equalsIgnoreCase(question.getCorrectAnswerText());
                } else {
                    if (entry.getSelectedOptionIds() == null || entry.getSelectedOptionIds().isEmpty()) {
                        throw new InvalidAnswerException("Options required for choice questions");
                    }
                    userAnswer.setSelectedOptionIds(String.join(",", entry.getSelectedOptionIds().stream().map(String::valueOf).toList()));

                    List<Long> correctIds = question.getOptions().stream()
                            .filter(Option::isCorrect)
                            .map(Option::getId)
                            .toList();
                    List<Long> selected = entry.getSelectedOptionIds();

                    if (question.getType() == QuestionType.SINGLE_CHOICE && selected.size() != 1) {
                        throw new InvalidAnswerException("Single choice allows only one selection");
                    }

                    isCorrect = selected.containsAll(correctIds) && correctIds.containsAll(selected);
                }

                if (isCorrect) score++;
                answerRepository.save(userAnswer);
            }

            attempt.setScore(score);
            attemptRepository.save(attempt);

            log.info("Quiz attempt saved successfully. Score: {}/{}", score, attempt.getTotalQuestions());

            ScoreResponse response = new ScoreResponse();
            response.setScore(score);
            response.setTotal(attempt.getTotalQuestions());
            return response;

        } catch (RuntimeException ex) {
            log.error("Error while submitting answers for quizId={}: {}", quizId, ex.getMessage(), ex);
            throw ex;
        }
    }

    public List<AttemptResponse> getMyScores() {
        User currentUser = getCurrentUser();
        log.info("Fetching scores for userId={}", currentUser.getId());
        return mapToAttemptResponses(attemptRepository.findByUser(currentUser));
    }

    public List<AttemptResponse> getAllScores() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != Role.ADMIN) {
            log.warn("Access denied for userId={} while fetching all scores", currentUser.getId());
            throw new AccessDeniedException("Access denied");
        }
        return mapToAttemptResponses(attemptRepository.findAll());
    }

    public AttemptResponse getAttemptDetails(Long attemptId) {
        User currentUser = getCurrentUser();
        log.info("Fetching attempt details for attemptId={} by userId={}", attemptId, currentUser.getId());

        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new AttemptNotFoundException("Attempt not found with id " + attemptId));

        if (currentUser.getRole() != Role.ADMIN && !attempt.getUser().getId().equals(currentUser.getId())) {
            log.warn("Unauthorized access attempt. userId={} tried to access attemptId={}", currentUser.getId(), attemptId);
            throw new AccessDeniedException("Access denied");
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

    private User getCurrentUser() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
    }
}