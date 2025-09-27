package com.quizapp.quizapplication.repository;

import com.quizapp.quizapplication.entity.QuizAttempt;
import com.quizapp.quizapplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUser(User user);
    List<QuizAttempt> findByQuizId(Long quizId);
}