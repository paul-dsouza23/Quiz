package com.quizapp.quizapplication;

import com.quizapp.quizapplication.entity.User;
import com.quizapp.quizapplication.enums.Role;
import com.quizapp.quizapplication.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class QuizapplicationApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuizapplicationApplication.class, args);
	}

	@Bean
	public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (userRepository.findAll().stream().noneMatch(user -> user.getRole() == Role.ADMIN)) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(passwordEncoder.encode("admin@123"));
				admin.setEmail("admin@example.com");
				admin.setRole(Role.ADMIN);
				userRepository.save(admin);
				System.out.println("Admin user created with username: admin and password: admin@123");
			} else {
				System.out.println("Admin user already exists");
			}
		};
	}
}
