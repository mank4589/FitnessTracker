package com.ufit.repository;

import com.ufit.model.DailyGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyGoalRepository extends JpaRepository<DailyGoal, Long> {

    Optional<DailyGoal> findByProfileIdAndGoalDate(Long profileId, LocalDate goalDate);

    Optional<DailyGoal> findFirstByProfileIdAndGoalDateBeforeOrderByGoalDateDesc(Long profileId, LocalDate goalDate);
}
