package com.ufit.model;

import com.ufit.model.enums.*;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "exercises")
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private MuscleGroup primaryMuscle;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "exercise_secondary_muscles", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "muscle_group")
    private Set<MuscleGroup> secondaryMuscles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private ExerciseType exerciseType;

    @Enumerated(EnumType.STRING)
    private Equipment equipment = Equipment.NONE;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel = DifficultyLevel.INTERMEDIATE;

    private int defaultSets = 3;
    private int defaultReps = 10;
    private int defaultDurationSeconds = 0;  // For cardio/timed exercises

    private double caloriesBurnedPerMinute = 5.0;

    // Injuries that make this exercise inadvisable
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "exercise_contraindications", joinColumns = @JoinColumn(name = "exercise_id"))
    @Column(name = "injury")
    private Set<InjuryType> contraindications = new HashSet<>();

    private boolean isCustom = false;

    @Column(length = 1000)
    private String instructions;

    public Exercise() {}

    public Exercise(String name, MuscleGroup primaryMuscle, ExerciseType exerciseType) {
        this.name = name;
        this.primaryMuscle = primaryMuscle;
        this.exerciseType = exerciseType;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MuscleGroup getPrimaryMuscle() { return primaryMuscle; }
    public void setPrimaryMuscle(MuscleGroup primaryMuscle) { this.primaryMuscle = primaryMuscle; }

    public Set<MuscleGroup> getSecondaryMuscles() { return secondaryMuscles; }
    public void setSecondaryMuscles(Set<MuscleGroup> secondaryMuscles) { this.secondaryMuscles = secondaryMuscles; }

    public ExerciseType getExerciseType() { return exerciseType; }
    public void setExerciseType(ExerciseType exerciseType) { this.exerciseType = exerciseType; }

    public Equipment getEquipment() { return equipment; }
    public void setEquipment(Equipment equipment) { this.equipment = equipment; }

    public DifficultyLevel getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public int getDefaultSets() { return defaultSets; }
    public void setDefaultSets(int defaultSets) { this.defaultSets = defaultSets; }

    public int getDefaultReps() { return defaultReps; }
    public void setDefaultReps(int defaultReps) { this.defaultReps = defaultReps; }

    public int getDefaultDurationSeconds() { return defaultDurationSeconds; }
    public void setDefaultDurationSeconds(int defaultDurationSeconds) { this.defaultDurationSeconds = defaultDurationSeconds; }

    public double getCaloriesBurnedPerMinute() { return caloriesBurnedPerMinute; }
    public void setCaloriesBurnedPerMinute(double caloriesBurnedPerMinute) { this.caloriesBurnedPerMinute = caloriesBurnedPerMinute; }

    public Set<InjuryType> getContraindications() { return contraindications; }
    public void setContraindications(Set<InjuryType> contraindications) { this.contraindications = contraindications; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public boolean isSafeFor(Set<InjuryType> userInjuries) {
        if (userInjuries == null || userInjuries.isEmpty()) {
            return true;
        }
        for (InjuryType injury : userInjuries) {
            if (this.contraindications.contains(injury)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return name + " (" + primaryMuscle + ")";
    }
}
