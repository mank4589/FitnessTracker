package com.ufit.model;

import com.ufit.model.enums.DifficultyLevel;
import com.ufit.model.enums.FitnessGoal;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_plans")
public class WorkoutPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    @Enumerated(EnumType.STRING)
    private FitnessGoal targetGoal;

    @Column(nullable = false)
    private int daysPerWeek;

    @Column(nullable = false)
    private int durationWeeks;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private boolean isCustom = false;

    @Column(nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("dayOrder ASC")
    private List<WorkoutDay> workoutDays = new ArrayList<>();

    public enum PlanType {
        PUSH_PULL_LEGS("Push/Pull/Legs", 3),
        UPPER_LOWER("Upper/Lower Split", 4),
        FULL_BODY("Full Body", 3),
        BRO_SPLIT("Bro Split (5-day)", 5),
        UPPER_LOWER_PPL("Upper/Lower + PPL", 5),
        CUSTOM("Custom Plan", 0);

        private final String displayName;
        private final int defaultDays;

        PlanType(String displayName, int defaultDays) {
            this.displayName = displayName;
            this.defaultDays = defaultDays;
        }

        public String getDisplayName() { return displayName; }
        public int getDefaultDays() { return defaultDays; }
    }

    // Constructors
    public WorkoutPlan() {}

    public WorkoutPlan(String name, PlanType planType, UserProfile userProfile) {
        this.name = name;
        this.planType = planType;
        this.userProfile = userProfile;
        this.startDate = LocalDate.now();
        this.daysPerWeek = planType.getDefaultDays();
        this.durationWeeks = 12;
    }

    // Helper methods
    public void addWorkoutDay(WorkoutDay day) {
        workoutDays.add(day);
        day.setWorkoutPlan(this);
    }

    public void removeWorkoutDay(WorkoutDay day) {
        workoutDays.remove(day);
        day.setWorkoutPlan(null);
    }

    public WorkoutDay getWorkoutDayByOrder(int order) {
        return workoutDays.stream()
                .filter(d -> d.getDayOrder() == order)
                .findFirst()
                .orElse(null);
    }

    public int getTotalExercises() {
        return workoutDays.stream()
                .mapToInt(d -> d.getPlannedExercises().size())
                .sum();
    }

    public int getEstimatedDurationMinutes() {
        return workoutDays.stream()
                .mapToInt(WorkoutDay::getEstimatedDurationMinutes)
                .sum() / Math.max(1, workoutDays.size());
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }

    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }

    public FitnessGoal getTargetGoal() { return targetGoal; }
    public void setTargetGoal(FitnessGoal targetGoal) { this.targetGoal = targetGoal; }

    public int getDaysPerWeek() { return daysPerWeek; }
    public void setDaysPerWeek(int daysPerWeek) { this.daysPerWeek = daysPerWeek; }

    public int getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(int durationWeeks) { this.durationWeeks = durationWeeks; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public List<WorkoutDay> getWorkoutDays() { return workoutDays; }
    public void setWorkoutDays(List<WorkoutDay> workoutDays) { this.workoutDays = workoutDays; }
}
