package com.quizapp.quizapplication.service;

import com.quizapp.quizapplication.dto.AddOptionRequest;
import com.quizapp.quizapplication.dto.AddQuestionRequest;
import com.quizapp.quizapplication.dto.UpdateQuestionRequest;
import com.quizapp.quizapplication.entity.Option;
import com.quizapp.quizapplication.entity.Question;
import com.quizapp.quizapplication.entity.Quiz;
import com.quizapp.quizapplication.enums.QuestionType;
import com.quizapp.quizapplication.repository.OptionRepository;
import com.quizapp.quizapplication.repository.QuestionRepository;
import com.quizapp.quizapplication.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;


    public void addQuestion(Long quizId, AddQuestionRequest request) {
        Quiz quiz = quizRepository.findActiveById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found or inactive"));

        validateQuestion(request);

        Question question = new Question();
        question.setText(request.getText());
        question.setType(request.getType());
        question.setQuiz(quiz);
        question.setActive(true);
        if (request.getType() == QuestionType.TEXT) {
            question.setCorrectAnswerText(request.getCorrectAnswerText());
        }
        question = questionRepository.save(question);

        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (AddOptionRequest optReq : request.getOptions()) {
                Option option = new Option();
                option.setText(optReq.getText());
                option.setCorrect(optReq.isCorrect());
                option.setQuestion(question);
                optionRepository.save(option);
            }
        }
    }

    public void updateQuestion(Long questionId, UpdateQuestionRequest request) {
        Question question = questionRepository.findActiveById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found or inactive"));
        validateQuestion(request);

        question.setText(request.getText());
        question.setType(request.getType());
        question.setCorrectAnswerText(request.getType() == QuestionType.TEXT ? request.getCorrectAnswerText() : null);

        // Clear existing options and add new ones
        question.getOptions().clear();
        questionRepository.save(question);

        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (AddOptionRequest optReq : request.getOptions()) {
                Option option = new Option();
                option.setText(optReq.getText());
                option.setCorrect(optReq.isCorrect());
                option.setQuestion(question);
                optionRepository.save(option);
            }
        }
        questionRepository.save(question);
    }

    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findActiveById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found or inactive"));
        question.setActive(false);
        questionRepository.save(question);
    }

    private void validateQuestion(UpdateQuestionRequest request) {
        QuestionType type = request.getType();
        if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE) {
            if (request.getOptions() == null || request.getOptions().isEmpty()) {
                throw new RuntimeException("Options required for choice questions");
            }
            long correctCount = request.getOptions().stream().filter(AddOptionRequest::isCorrect).count();
            if (type == QuestionType.SINGLE_CHOICE && correctCount != 1) {
                throw new RuntimeException("Single choice must have exactly one correct option");
            }
            if (type == QuestionType.MULTIPLE_CHOICE && correctCount < 1) {
                throw new RuntimeException("Multiple choice must have at least one correct option");
            }
        } else if (type == QuestionType.TEXT) {
            if (request.getCorrectAnswerText() == null || request.getCorrectAnswerText().isBlank()) {
                throw new RuntimeException("Correct answer text required for text questions");
            }
        }
    }

    private void validateQuestion(AddQuestionRequest request) {
        QuestionType type = request.getType();
        if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE) {
            if (request.getOptions() == null || request.getOptions().isEmpty()) {
                throw new RuntimeException("Options required for choice questions");
            }
            long correctCount = request.getOptions().stream().filter(AddOptionRequest::isCorrect).count();  // Updated to ::isCorrect
            log.info("Question type: {}, correct option count: {}", type, correctCount);
            if (type == QuestionType.SINGLE_CHOICE && correctCount != 1) {
                throw new RuntimeException("Single choice must have exactly one correct option");
            }
            if (type == QuestionType.MULTIPLE_CHOICE && correctCount < 1) {
                throw new RuntimeException("Multiple choice must have at least one correct option");
            }
        } else if (type == QuestionType.TEXT) {
            if (request.getCorrectAnswerText() == null || request.getCorrectAnswerText().isBlank()) {
                throw new RuntimeException("Correct answer text required for text questions");
            }
        }
    }
}