package com.ufit.repository;

import com.ufit.model.ExerciseLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {
    List<ExerciseLog> findByProfileIdAndLogDateOrderByIdDesc(Long profileId, LocalDate logDate);
}
