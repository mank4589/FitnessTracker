package com.ufit.model;

import jakarta.persistence.*;

/**
 * Represents a locally-cached exercise in the database.
 * Seeded from free-exercise-db (https://github.com/yuhonas/free-exercise-db) on first startup.
 * Contains 800+ exercises with images and instructions.
 */
@Entity
@Table(name = "exercise_items", indexes = {
    @Index(name = "idx_exercise_name", columnList = "name"),
    @Index(name = "idx_exercise_primary_muscle", columnList = "primaryMuscle"),
    @Index(name = "idx_exercise_category", columnList = "category"),
    @Index(name = "idx_exercise_exercise_id", columnList = "exerciseId", unique = true)
})
public class ExerciseItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique exercise ID from free-exercise-db (e.g., "Barbell_Bench_Press") */
    @Column(nullable = false, unique = true)
    private String exerciseId;

    @Column(nullable = false)
    private String name;

    /** e.g. "pull", "push", "static" */
    private String force;

    /** e.g. "beginner", "intermediate", "expert" */
    private String level;

    /** e.g. "compound", "isolation" */
    private String mechanic;

    /** e.g. "barbell", "dumbbell", "body only", "cable", "machine" */
    private String equipment;

    /** Primary target muscle (e.g. "chest", "quadriceps", "biceps") */
    private String primaryMuscle;

    /** Comma-separated secondary muscles */
    private String secondaryMuscles;

    /** e.g. "strength", "stretching", "cardio", "plyometrics" */
    private String category;

    /** JSON array of instruction steps stored as text */
    @Column(columnDefinition = "TEXT")
    private String instructions;

    /** Primary image path (e.g., "Barbell_Bench_Press/0.jpg") */
    private String imagePath;

    public ExerciseItem() {}

    // ── Getters & Setters ──

    public Long getId() { return id; }

    public String getExerciseId() { return exerciseId; }
    public void setExerciseId(String exerciseId) { this.exerciseId = exerciseId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getForce() { return force; }
    public void setForce(String force) { this.force = force; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getMechanic() { return mechanic; }
    public void setMechanic(String mechanic) { this.mechanic = mechanic; }

    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getPrimaryMuscle() { return primaryMuscle; }
    public void setPrimaryMuscle(String primaryMuscle) { this.primaryMuscle = primaryMuscle; }

    public String getSecondaryMuscles() { return secondaryMuscles; }
    public void setSecondaryMuscles(String secondaryMuscles) { this.secondaryMuscles = secondaryMuscles; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    /** Returns full GitHub-hosted image URL */
    public String getImageUrl() {
        if (imagePath == null || imagePath.isEmpty()) return null;
        return "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/" + imagePath;
    }

    @Override
    public String toString() {
        return name + " (" + primaryMuscle + ", " + category + ")";
    }
}
