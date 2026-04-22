package com.ufit.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "weekly_meal_plans")
public class WeeklyMealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    private LocalDate weekStartDate;

    private int targetCalories;
    private double targetProtein;
    private double targetCarbs;
    private double targetFat;

    @OneToMany(mappedBy = "weeklyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyMealPlan> dailyPlans = new ArrayList<>();

    public WeeklyMealPlan() {}

    public WeeklyMealPlan(UserProfile userProfile, LocalDate weekStartDate) {
        this.userProfile = userProfile;
        this.weekStartDate = weekStartDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }

    public LocalDate getWeekStartDate() { return weekStartDate; }
    public void setWeekStartDate(LocalDate weekStartDate) { this.weekStartDate = weekStartDate; }

    public int getTargetCalories() { return targetCalories; }
    public void setTargetCalories(int targetCalories) { this.targetCalories = targetCalories; }

    public double getTargetProtein() { return targetProtein; }
    public void setTargetProtein(double targetProtein) { this.targetProtein = targetProtein; }

    public double getTargetCarbs() { return targetCarbs; }
    public void setTargetCarbs(double targetCarbs) { this.targetCarbs = targetCarbs; }

    public double getTargetFat() { return targetFat; }
    public void setTargetFat(double targetFat) { this.targetFat = targetFat; }

    public List<DailyMealPlan> getDailyPlans() { return dailyPlans; }
    public void setDailyPlans(List<DailyMealPlan> dailyPlans) { this.dailyPlans = dailyPlans; }

    public void addDailyPlan(DailyMealPlan dailyPlan) {
        dailyPlans.add(dailyPlan);
        dailyPlan.setWeeklyPlan(this);
    }

    public DailyMealPlan getDailyPlanFor(DayOfWeek dayOfWeek) {
        return dailyPlans.stream()
                .filter(dp -> dp.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElse(null);
    }
}
