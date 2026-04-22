package com.ufit.repository;

import com.ufit.model.WorkoutPlan;
import com.ufit.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    List<WorkoutPlan> findByUserProfileOrderByStartDateDesc(UserProfile userProfile);

    List<WorkoutPlan> findByUserProfileIdOrderByStartDateDesc(Long profileId);

    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.userProfile.id = :profileId AND wp.isActive = true ORDER BY wp.startDate DESC")
    List<WorkoutPlan> findActiveByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.userProfile.id = :profileId AND wp.isActive = true ORDER BY wp.startDate DESC LIMIT 1")
    Optional<WorkoutPlan> findLatestActiveByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.userProfile.id = :profileId AND wp.isCustom = true ORDER BY wp.startDate DESC")
    List<WorkoutPlan> findCustomPlansByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.userProfile.id = :profileId AND wp.isCustom = false ORDER BY wp.startDate DESC")
    List<WorkoutPlan> findGeneratedPlansByProfileId(@Param("profileId") Long profileId);

    List<WorkoutPlan> findByPlanType(WorkoutPlan.PlanType planType);

    @Query("SELECT wp FROM WorkoutPlan wp WHERE wp.userProfile IS NULL AND wp.isCustom = false")
    List<WorkoutPlan> findTemplatePlans();

    long countByUserProfileId(Long profileId);
}
