package com.ufit.repository;

import com.ufit.model.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByLogDateOrderByCreatedAtAsc(LocalDate logDate);

    List<FoodLog> findByLogDateAndMealTypeOrderByCreatedAtAsc(LocalDate logDate, String mealType);

    List<FoodLog> findByLogDateBetweenOrderByLogDateAsc(LocalDate start, LocalDate end);
}
