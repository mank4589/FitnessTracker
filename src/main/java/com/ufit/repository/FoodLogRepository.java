package com.ufit.repository;

import com.ufit.model.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByProfileIdAndLogDateOrderByCreatedAtAsc(Long profileId, LocalDate logDate);

    List<FoodLog> findByProfileIdAndLogDateAndMealTypeOrderByCreatedAtAsc(Long profileId, LocalDate logDate, String mealType);

    List<FoodLog> findByProfileIdAndLogDateBetweenOrderByLogDateAsc(Long profileId, LocalDate start, LocalDate end);
}
