package com.ufit.model;

import com.ufit.model.enums.*;
import com.ufit.model.enums.CuisinePreference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private FitnessGoal fitnessGoal;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_target_muscles", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "muscle_group")
    private Set<MuscleGroup> targetMuscleGroups = new HashSet<>();

    private int workoutDaysPerWeek = 3;
    private int workoutDurationMinutes = 60;

    @Enumerated(EnumType.STRING)
    private DietaryPreference dietaryPreference = DietaryPreference.NONE;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_dietary_restrictions", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "restriction")
    private Set<DietaryRestriction> dietaryRestrictions = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_injuries", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "injury")
    private Set<InjuryType> injuries = new HashSet<>();

    private boolean includeCardio = true;
    private int mealsPerDay = 3;
    private int snacksPerDay = 1;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_cuisine_preferences", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "cuisine")
    private Set<CuisinePreference> cuisinePreferences = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_medical_conditions", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "condition")
    private Set<MedicalCondition> medicalConditions = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Link to latest health snapshot
    @OneToOne
    @JoinColumn(name = "latest_health_snapshot_id")
    private ufit latestHealthSnapshot;

    public UserProfile() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public FitnessGoal getFitnessGoal() { return fitnessGoal; }
    public void setFitnessGoal(FitnessGoal fitnessGoal) { this.fitnessGoal = fitnessGoal; }

    public Set<MuscleGroup> getTargetMuscleGroups() { return targetMuscleGroups; }
    public void setTargetMuscleGroups(Set<MuscleGroup> targetMuscleGroups) { this.targetMuscleGroups = targetMuscleGroups; }

    public int getWorkoutDaysPerWeek() { return workoutDaysPerWeek; }
    public void setWorkoutDaysPerWeek(int workoutDaysPerWeek) { this.workoutDaysPerWeek = workoutDaysPerWeek; }

    public int getWorkoutDurationMinutes() { return workoutDurationMinutes; }
    public void setWorkoutDurationMinutes(int workoutDurationMinutes) { this.workoutDurationMinutes = workoutDurationMinutes; }

    public DietaryPreference getDietaryPreference() { return dietaryPreference; }
    public void setDietaryPreference(DietaryPreference dietaryPreference) { this.dietaryPreference = dietaryPreference; }

    public Set<DietaryRestriction> getDietaryRestrictions() { return dietaryRestrictions; }
    public void setDietaryRestrictions(Set<DietaryRestriction> dietaryRestrictions) { this.dietaryRestrictions = dietaryRestrictions; }

    public Set<InjuryType> getInjuries() { return injuries; }
    public void setInjuries(Set<InjuryType> injuries) { this.injuries = injuries; }

    public boolean isIncludeCardio() { return includeCardio; }
    public void setIncludeCardio(boolean includeCardio) { this.includeCardio = includeCardio; }

    public int getMealsPerDay() { return mealsPerDay; }
    public void setMealsPerDay(int mealsPerDay) { this.mealsPerDay = mealsPerDay; }

    public int getSnacksPerDay() { return snacksPerDay; }
    public void setSnacksPerDay(int snacksPerDay) { this.snacksPerDay = snacksPerDay; }

    public Set<CuisinePreference> getCuisinePreferences() { return cuisinePreferences; }
    public void setCuisinePreferences(Set<CuisinePreference> cuisinePreferences) { this.cuisinePreferences = cuisinePreferences; }

    public Set<MedicalCondition> getMedicalConditions() { return medicalConditions; }
    public void setMedicalConditions(Set<MedicalCondition> medicalConditions) { this.medicalConditions = medicalConditions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public ufit getLatestHealthSnapshot() { return latestHealthSnapshot; }
    public void setLatestHealthSnapshot(ufit latestHealthSnapshot) { this.latestHealthSnapshot = latestHealthSnapshot; }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
