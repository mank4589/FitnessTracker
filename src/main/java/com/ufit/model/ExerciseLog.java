package com.ufit.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "exercise_logs")
public class ExerciseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long profileId;

    private String exerciseName;
    private String category;

    private int sets;
    private int reps;
    private double weightKg;
    private int durationMinutes;

    private String notes;
    private LocalDate logDate;

    // ── Getters & Setters ──

    public Long getId() { return id; }

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }

    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }
}
