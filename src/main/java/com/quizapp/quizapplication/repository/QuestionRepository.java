package com.quizapp.quizapplication.repository;

import com.quizapp.quizapplication.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.quiz.id = :quizId AND q.isActive = true")
    List<Question> findAllActiveByQuizId(Long quizId);

    @Query("SELECT q FROM Question q WHERE q.id = :id AND q.isActive = true")
    Optional<Question> findActiveById(Long id);
}
