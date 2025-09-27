package com.quizapp.quizapplication.service;

import com.quizapp.quizapplication.dto.AuthResponse;
import com.quizapp.quizapplication.dto.LoginRequest;
import com.quizapp.quizapplication.dto.RegisterRequest;
import com.quizapp.quizapplication.entity.User;
import com.quizapp.quizapplication.enums.Role;
import com.quizapp.quizapplication.repository.UserRepository;
import com.quizapp.quizapplication.security.*;
import com.quizapp.quizapplication.security.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(Role.USER);  // Default to USER
        userRepository.save(user);

        String token = jwtUtils.generateToken(new CustomUserDetails(user));
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        String token = jwtUtils.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        return response;
    }
}
