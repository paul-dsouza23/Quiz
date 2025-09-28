package com.quizapp.quizapplication.exception;

import com.quizapp.quizapplication.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.log4j.Log4j2;
import org.hibernate.TransientPropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String error, String message, HttpServletRequest request) {
        ApiError apiError = ApiError.builder()
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(apiError, status);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidationException(ValidationException ex, HttpServletRequest request) {
        log.warn("Validation exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(TransientPropertyValueException.class)
    public ResponseEntity<ApiError> handleTransientPropertyValueException(TransientPropertyValueException ex, HttpServletRequest request) {
        log.error("Database error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "DATABASE_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("Runtime exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "RUNTIME_EXCEPTION", ex.getMessage(), request);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler({
            InvalidAnswerException.class,
            AttemptNotFoundException.class
    })
    public ResponseEntity<ApiError> handleCustomExceptions(RuntimeException ex, HttpServletRequest request) {
        log.warn("Custom exception: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "CUSTOM_EXCEPTION", ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage(), request);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("User already exists: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", ex.getMessage(), request);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiError> handleAuthenticationFailed(AuthenticationFailedException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", ex.getMessage(), request);
    }

    @ExceptionHandler({
            QuizNotFoundOrInactiveException.class,
            QuestionNotFoundOrInactiveException.class,
            InvalidQuestionException.class
    })
    public ResponseEntity<ApiError> handleQuestionExceptions(RuntimeException ex, HttpServletRequest request) {
        log.warn("Quiz/Question error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "QUIZ_OR_QUESTION_ERROR", ex.getMessage(), request);
    }
}
