package com.ufit.repository;

import com.ufit.model.WaterLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WaterLogRepository extends JpaRepository<WaterLog, Long> {

    List<WaterLog> findByLogDateOrderByCreatedAtAsc(LocalDate logDate);

    @Query("SELECT COALESCE(SUM(w.amountMl), 0.0) FROM WaterLog w WHERE w.logDate = :date")
    Double sumAmountByDate(@Param("date") LocalDate date);

    @Modifying
    @Transactional
    void deleteByLogDate(LocalDate logDate);
}
