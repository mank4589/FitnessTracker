package com.ufit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "planned_exercises")
public class PlannedExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_day_id", nullable = false)
    @JsonIgnore
    private WorkoutDay workoutDay;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    // Transient field for JSON deserialization
    @Transient
    private Long exerciseId;

    @Column(nullable = false)
    private int exerciseOrder;

    @Column(nullable = false)
    private int sets = 3;

    @Column(nullable = false)
    private String repRange = "8-12";

    @Column
    private Integer reps;

    @Column
    private Integer durationSeconds;

    @Column
    private int restSeconds = 90;

    @Column
    private Double weight;

    @Column
    private Integer rpe;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    private boolean isWarmup = false;

    @Column(nullable = false)
    private boolean isDropSet = false;

    @Column(nullable = false)
    private boolean isSuperSet = false;

    @Column
    private Long superSetWithId;

    // Constructors
    public PlannedExercise() {}

    public PlannedExercise(Exercise exercise, int sets, String repRange) {
        this.exercise = exercise;
        this.sets = sets;
        this.repRange = repRange;
    }

    public PlannedExercise(Exercise exercise, int sets, String repRange, int restSeconds) {
        this(exercise, sets, repRange);
        this.restSeconds = restSeconds;
    }

    // Helper methods
    public int getEstimatedDurationMinutes() {
        int timePerSet = 45; // seconds per set
        int totalTime = (sets * timePerSet) + ((sets - 1) * restSeconds);
        return (int) Math.ceil(totalTime / 60.0);
    }

    public String getFormattedRepRange() {
        if (durationSeconds != null && durationSeconds > 0) {
            return durationSeconds + "s";
        }
        if (reps != null && reps > 0) {
            return reps + " reps";
        }
        return repRange + " reps";
    }

    public String getFormattedRest() {
        if (restSeconds >= 60) {
            int mins = restSeconds / 60;
            int secs = restSeconds % 60;
            return secs > 0 ? mins + "m " + secs + "s" : mins + " min";
        }
        return restSeconds + "s";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WorkoutDay getWorkoutDay() { return workoutDay; }
    public void setWorkoutDay(WorkoutDay workoutDay) { this.workoutDay = workoutDay; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public int getExerciseOrder() { return exerciseOrder; }
    public void setExerciseOrder(int exerciseOrder) { this.exerciseOrder = exerciseOrder; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public String getRepRange() { return repRange; }
    public void setRepRange(String repRange) { this.repRange = repRange; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getRestSeconds() { return restSeconds; }
    public void setRestSeconds(int restSeconds) { this.restSeconds = restSeconds; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getRpe() { return rpe; }
    public void setRpe(Integer rpe) { this.rpe = rpe; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isWarmup() { return isWarmup; }
    public void setWarmup(boolean warmup) { isWarmup = warmup; }

    public boolean isDropSet() { return isDropSet; }
    public void setDropSet(boolean dropSet) { isDropSet = dropSet; }

    public boolean isSuperSet() { return isSuperSet; }
    public void setSuperSet(boolean superSet) { isSuperSet = superSet; }

    public Long getSuperSetWithId() { return superSetWithId; }
    public void setSuperSetWithId(Long superSetWithId) { this.superSetWithId = superSetWithId; }
}
