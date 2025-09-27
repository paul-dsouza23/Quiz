package com.quizapp.quizapplication.repository;

import com.quizapp.quizapplication.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT q FROM Quiz q WHERE q.isActive = true")
    List<Quiz> findAllActive();

    @Query("SELECT q FROM Quiz q WHERE q.id = :id AND q.isActive = true")
    Optional<Quiz> findActiveById(Long id);


}
