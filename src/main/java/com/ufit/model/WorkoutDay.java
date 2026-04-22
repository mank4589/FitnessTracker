package com.ufit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workout_days")
public class WorkoutDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_plan_id", nullable = false)
    @JsonIgnore
    private WorkoutPlan workoutPlan;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayFocus focus;

    @Column(nullable = false)
    private int dayOrder;

    @Column
    private String dayOfWeek;

    @Column(nullable = false)
    private boolean isRestDay = false;

    @OneToMany(mappedBy = "workoutDay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("exerciseOrder ASC")
    private List<PlannedExercise> plannedExercises = new ArrayList<>();

    public enum DayFocus {
        PUSH("Push", "Chest, Shoulders, Triceps"),
        PULL("Pull", "Back, Biceps, Rear Delts"),
        LEGS("Legs", "Quadriceps, Hamstrings, Glutes, Calves"),
        UPPER("Upper Body", "All upper body muscles"),
        LOWER("Lower Body", "All lower body muscles"),
        FULL_BODY("Full Body", "All major muscle groups"),
        CHEST("Chest Day", "Chest focused with supporting muscles"),
        BACK("Back Day", "Back focused with biceps"),
        SHOULDERS("Shoulder Day", "Shoulders and traps"),
        ARMS("Arms Day", "Biceps, Triceps, Forearms"),
        CORE("Core", "Abdominals, Obliques, Lower Back"),
        CARDIO("Cardio", "Cardiovascular training"),
        REST("Rest Day", "Recovery and rest"),
        CUSTOM("Custom", "User-defined focus");

        private final String displayName;
        private final String description;

        DayFocus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }

    // Constructors
    public WorkoutDay() {}

    public WorkoutDay(String name, DayFocus focus, int dayOrder) {
        this.name = name;
        this.focus = focus;
        this.dayOrder = dayOrder;
        this.isRestDay = (focus == DayFocus.REST);
    }

    // Helper methods
    public void addExercise(PlannedExercise exercise) {
        plannedExercises.add(exercise);
        exercise.setWorkoutDay(this);
        exercise.setExerciseOrder(plannedExercises.size());
    }

    public void removeExercise(PlannedExercise exercise) {
        plannedExercises.remove(exercise);
        exercise.setWorkoutDay(null);
        reorderExercises();
    }

    public void reorderExercises() {
        for (int i = 0; i < plannedExercises.size(); i++) {
            plannedExercises.get(i).setExerciseOrder(i + 1);
        }
    }

    public int getEstimatedDurationMinutes() {
        if (isRestDay) return 0;
        return plannedExercises.stream()
                .mapToInt(PlannedExercise::getEstimatedDurationMinutes)
                .sum() + 10; // Add 10 min for warmup
    }

    public int getTotalSets() {
        return plannedExercises.stream()
                .mapToInt(PlannedExercise::getSets)
                .sum();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkoutPlan getWorkoutPlan() { return workoutPlan; }
    public void setWorkoutPlan(WorkoutPlan workoutPlan) { this.workoutPlan = workoutPlan; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DayFocus getFocus() { return focus; }
    public void setFocus(DayFocus focus) { this.focus = focus; }

    public int getDayOrder() { return dayOrder; }
    public void setDayOrder(int dayOrder) { this.dayOrder = dayOrder; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public boolean isRestDay() { return isRestDay; }
    public void setRestDay(boolean restDay) { isRestDay = restDay; }

    public List<PlannedExercise> getPlannedExercises() { return plannedExercises; }
    public void setPlannedExercises(List<PlannedExercise> plannedExercises) { this.plannedExercises = plannedExercises; }
}
