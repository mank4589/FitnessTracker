package com.ufit.recommendation;

import com.ufit.model.Exercise;
import com.ufit.model.UserProfile;
import com.ufit.model.enums.*;
import com.ufit.repository.ExerciseRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ExerciseMatcher {

    private final ExerciseRepository exerciseRepository;

    public ExerciseMatcher(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    /**
     * Find exercises for a specific muscle group, filtered by user's injuries
     */
    public List<Exercise> findExercisesForMuscle(MuscleGroup muscleGroup, UserProfile profile) {
        List<Exercise> exercises = exerciseRepository.findByPrimaryMuscle(muscleGroup);
        return filterAndScoreExercises(exercises, profile);
    }

    /**
     * Find compound exercises for a muscle group
     */
    public List<Exercise> findCompoundExercises(MuscleGroup muscleGroup, UserProfile profile) {
        List<Exercise> exercises = exerciseRepository.findByMuscleAndType(muscleGroup, ExerciseType.COMPOUND);
        return filterAndScoreExercises(exercises, profile);
    }

    /**
     * Find isolation exercises for a muscle group
     */
    public List<Exercise> findIsolationExercises(MuscleGroup muscleGroup, UserProfile profile) {
        List<Exercise> exercises = exerciseRepository.findByMuscleAndType(muscleGroup, ExerciseType.ISOLATION);
        return filterAndScoreExercises(exercises, profile);
    }

    /**
     * Find cardio exercises safe for user
     */
    public List<Exercise> findCardioExercises(UserProfile profile) {
        List<Exercise> cardio = exerciseRepository.findAllCardioExercises();
        return filterAndScoreExercises(cardio, profile);
    }

    /**
     * Find exercises by difficulty level
     */
    public List<Exercise> findExercisesByDifficulty(MuscleGroup muscleGroup, DifficultyLevel level, UserProfile profile) {
        List<Exercise> exercises = exerciseRepository.findByMuscleAndDifficulty(muscleGroup, level);
        return filterAndScoreExercises(exercises, profile);
    }

    /**
     * Filter exercises based on user's injuries and score them
     */
    private List<Exercise> filterAndScoreExercises(List<Exercise> exercises, UserProfile profile) {
        Set<InjuryType> userInjuries = profile.getInjuries();

        return exercises.stream()
            .filter(exercise -> isSafeForUser(exercise, userInjuries))
            .sorted((e1, e2) -> Double.compare(scoreExercise(e2, profile), scoreExercise(e1, profile)))
            .collect(Collectors.toList());
    }

    /**
     * Check if exercise is safe given user's injuries
     */
    private boolean isSafeForUser(Exercise exercise, Set<InjuryType> injuries) {
        if (injuries == null || injuries.isEmpty()) {
            return true;
        }
        
        Set<InjuryType> contraindications = exercise.getContraindications();
        if (contraindications == null || contraindications.isEmpty()) {
            return true;
        }

        // Exercise is safe if none of user's injuries are in contraindications
        for (InjuryType injury : injuries) {
            if (contraindications.contains(injury)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Score an exercise based on user's profile and goals
     */
    private double scoreExercise(Exercise exercise, UserProfile profile) {
        double score = 50.0; // Base score

        // Compounds are generally more effective
        if (exercise.getExerciseType() == ExerciseType.COMPOUND) {
            score += 20;
        }

        // Adjust based on fitness goal
        if (profile.getFitnessGoal() != null) {
            switch (profile.getFitnessGoal()) {
                case GAIN_MUSCLE -> {
                    // Prefer compounds and higher volume potential
                    if (exercise.getExerciseType() == ExerciseType.COMPOUND) {
                        score += 15;
                    }
                    score += exercise.getDefaultReps() * 0.5;
                }
                case LOSE_FAT -> {
                    // Prefer exercises with higher calorie burn
                    score += exercise.getCaloriesBurnedPerMinute() * 3;
                    // Cardio is great for fat loss
                    if (exercise.getExerciseType() == ExerciseType.CARDIO) {
                        score += 20;
                    }
                }
                case RECOMP -> {
                    // Balance of compounds and some cardio
                    if (exercise.getExerciseType() == ExerciseType.COMPOUND) {
                        score += 10;
                    }
                    score += exercise.getCaloriesBurnedPerMinute() * 1.5;
                }
                case MAINTAIN -> {
                    // Balanced approach
                    score += 5;
                }
            }
        }

        // Bodyweight exercises are accessible
        if (exercise.getEquipment() == Equipment.NONE) {
            score += 5;
        }

        // Prefer exercises that hit secondary muscles in user's targets
        Set<MuscleGroup> targetMuscles = profile.getTargetMuscleGroups();
        if (targetMuscles != null && !targetMuscles.isEmpty()) {
            for (MuscleGroup secondary : exercise.getSecondaryMuscles()) {
                if (targetMuscles.contains(secondary)) {
                    score += 5;
                }
            }
        }

        return score;
    }

    /**
     * Get muscle groups for push exercises
     */
    public static Set<MuscleGroup> getPushMuscles() {
        return Set.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS);
    }

    /**
     * Get muscle groups for pull exercises
     */
    public static Set<MuscleGroup> getPullMuscles() {
        return Set.of(MuscleGroup.BACK, MuscleGroup.BICEPS, MuscleGroup.FOREARMS);
    }

    /**
     * Get leg muscle groups
     */
    public static Set<MuscleGroup> getLegMuscles() {
        return Set.of(MuscleGroup.QUADRICEPS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CALVES);
    }

    /**
     * Get random selection from top exercises for variety
     */
    public List<Exercise> getRandomTopExercises(List<Exercise> exercises, int count, Set<Long> excludeIds) {
        List<Exercise> available = exercises.stream()
            .filter(e -> !excludeIds.contains(e.getId()))
            .limit(Math.min(exercises.size(), count * 2))
            .collect(Collectors.toList());
        
        Collections.shuffle(available);
        return available.stream().limit(count).collect(Collectors.toList());
    }
}
