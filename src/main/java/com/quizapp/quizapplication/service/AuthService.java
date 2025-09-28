package com.quizapp.quizapplication.service;

import com.quizapp.quizapplication.dto.AuthResponse;
import com.quizapp.quizapplication.dto.LoginRequest;
import com.quizapp.quizapplication.dto.RegisterRequest;
import com.quizapp.quizapplication.entity.User;
import com.quizapp.quizapplication.enums.Role;
import com.quizapp.quizapplication.exception.AuthenticationFailedException;
import com.quizapp.quizapplication.exception.UserAlreadyExistsException;
import com.quizapp.quizapplication.repository.UserRepository;
import com.quizapp.quizapplication.security.*;
import com.quizapp.quizapplication.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;


    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            log.warn("Registration failed: Username {} already exists", request.getUsername());
            throw new UserAlreadyExistsException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Registration failed: Email {} already exists", request.getEmail());
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);  // Default to USER
        userRepository.save(user);

        log.info("User {} registered successfully", request.getUsername());

        String token = jwtUtils.generateToken(new CustomUserDetails(user));
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        log.info("User attempting login: {}", request.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            String token = jwtUtils.generateToken((UserDetails) authentication.getPrincipal());
            log.info("User {} logged in successfully", request.getUsername());

            AuthResponse response = new AuthResponse();
            response.setToken(token);
            return response;
        } catch (BadCredentialsException e) {
            log.error("Authentication failed for username={}", request.getUsername());
            throw new AuthenticationFailedException("Invalid username or password");
        }
    }
}
