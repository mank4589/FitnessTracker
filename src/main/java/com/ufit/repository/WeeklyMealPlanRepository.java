package com.ufit.repository;

import com.ufit.model.UserProfile;
import com.ufit.model.WeeklyMealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyMealPlanRepository extends JpaRepository<WeeklyMealPlan, Long> {
    
    List<WeeklyMealPlan> findByUserProfile(UserProfile userProfile);
    
    Optional<WeeklyMealPlan> findByUserProfileAndWeekStartDate(UserProfile userProfile, LocalDate weekStartDate);
    
    @Query("SELECT w FROM WeeklyMealPlan w WHERE w.userProfile = :profile ORDER BY w.weekStartDate DESC")
    List<WeeklyMealPlan> findByUserProfileOrderByDateDesc(@Param("profile") UserProfile profile);
    
    @Query("SELECT w FROM WeeklyMealPlan w WHERE w.userProfile.id = :profileId ORDER BY w.weekStartDate DESC LIMIT 1")
    Optional<WeeklyMealPlan> findLatestByUserProfileId(@Param("profileId") Long profileId);
}
