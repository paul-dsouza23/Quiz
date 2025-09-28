package com.quizapp.quizapplication.controller;

import com.quizapp.quizapplication.dto.AuthResponse;
import com.quizapp.quizapplication.dto.LoginRequest;
import com.quizapp.quizapplication.dto.RegisterRequest;
import com.quizapp.quizapplication.exception.AuthenticationFailedException;
import com.quizapp.quizapplication.exception.UserAlreadyExistsException;
import com.quizapp.quizapplication.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);

        } catch (UserAlreadyExistsException ex) {
            log.warn("Registration failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());

        } catch (Exception ex) {
            log.error("Unexpected error during registration: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during registration");
        }    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (AuthenticationFailedException ex) {
            log.warn("Authentication failed: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());

        } catch (Exception ex) {
            log.error("Unexpected error during login: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred during login");
        }    }
}
