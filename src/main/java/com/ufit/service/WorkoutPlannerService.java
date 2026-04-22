package com.ufit.service;

import com.ufit.model.Exercise;
import com.ufit.model.UserProfile;
import com.ufit.model.enums.*;
import com.ufit.recommendation.ExerciseMatcher;
import com.ufit.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WorkoutPlannerService {

    private final ExerciseMatcher exerciseMatcher;
    private final ExerciseRepository exerciseRepository;

    public WorkoutPlannerService(ExerciseMatcher exerciseMatcher,
                                ExerciseRepository exerciseRepository) {
        this.exerciseMatcher = exerciseMatcher;
        this.exerciseRepository = exerciseRepository;
    }

    /**
     * Get recommended exercises for a specific muscle group
     */
    public List<Exercise> getExercisesForMuscle(MuscleGroup muscleGroup, UserProfile profile) {
        return exerciseMatcher.findExercisesForMuscle(muscleGroup, profile);
    }

    /**
     * Get compound exercises for a muscle group
     */
    public List<Exercise> getCompoundExercises(MuscleGroup muscleGroup, UserProfile profile) {
        return exerciseMatcher.findCompoundExercises(muscleGroup, profile);
    }

    /**
     * Get isolation exercises for a muscle group
     */
    public List<Exercise> getIsolationExercises(MuscleGroup muscleGroup, UserProfile profile) {
        return exerciseMatcher.findIsolationExercises(muscleGroup, profile);
    }

    /**
     * Get cardio exercises safe for user
     */
    public List<Exercise> getCardioExercises(UserProfile profile) {
        return exerciseMatcher.findCardioExercises(profile);
    }

    /**
     * Get exercises by difficulty level
     */
    public List<Exercise> getExercisesByDifficulty(MuscleGroup muscleGroup, DifficultyLevel level, UserProfile profile) {
        return exerciseMatcher.findExercisesByDifficulty(muscleGroup, level, profile);
    }

    /**
     * Get exercises by equipment type
     */
    public List<Exercise> getExercisesByEquipment(Equipment equipment, UserProfile profile) {
        List<Exercise> exercises = exerciseRepository.findByEquipment(equipment);
        // Filter by user injuries
        return exercises.stream()
            .filter(e -> e.isSafeFor(profile.getInjuries()))
            .sorted((e1, e2) -> e1.getName().compareTo(e2.getName()))
            .toList();
    }

    /**
     * Get recommended exercises for user's target muscle groups
     */
    public Map<MuscleGroup, List<Exercise>> getRecommendedWorkout(UserProfile profile) {
        Map<MuscleGroup, List<Exercise>> workout = new HashMap<>();
        
        Set<MuscleGroup> targetMuscles = profile.getTargetMuscleGroups();
        if (targetMuscles == null || targetMuscles.isEmpty()) {
            // Default to full body
            targetMuscles = Set.of(
                MuscleGroup.CHEST, MuscleGroup.BACK, MuscleGroup.SHOULDERS,
                MuscleGroup.QUADRICEPS, MuscleGroup.HAMSTRINGS, MuscleGroup.CORE
            );
        }

        for (MuscleGroup muscle : targetMuscles) {
            List<Exercise> exercises = exerciseMatcher.findExercisesForMuscle(muscle, profile);
            // Limit to top 5 exercises per muscle group
            workout.put(muscle, exercises.stream().limit(5).toList());
        }

        // Add cardio if user wants it
        if (profile.isIncludeCardio()) {
            List<Exercise> cardio = exerciseMatcher.findCardioExercises(profile);
            if (!cardio.isEmpty()) {
                workout.put(MuscleGroup.FULL_BODY, cardio.stream().limit(3).toList());
            }
        }

        return workout;
    }

    /**
     * Generate a push/pull/legs split recommendation
     */
    public Map<String, Map<MuscleGroup, List<Exercise>>> getPushPullLegsSplit(UserProfile profile) {
        Map<String, Map<MuscleGroup, List<Exercise>>> split = new HashMap<>();

        // Push day
        Map<MuscleGroup, List<Exercise>> pushDay = new HashMap<>();
        for (MuscleGroup muscle : ExerciseMatcher.getPushMuscles()) {
            List<Exercise> exercises = exerciseMatcher.findExercisesForMuscle(muscle, profile);
            pushDay.put(muscle, exercises.stream().limit(3).toList());
        }
        split.put("push", pushDay);

        // Pull day
        Map<MuscleGroup, List<Exercise>> pullDay = new HashMap<>();
        for (MuscleGroup muscle : ExerciseMatcher.getPullMuscles()) {
            List<Exercise> exercises = exerciseMatcher.findExercisesForMuscle(muscle, profile);
            pullDay.put(muscle, exercises.stream().limit(3).toList());
        }
        split.put("pull", pullDay);

        // Leg day
        Map<MuscleGroup, List<Exercise>> legDay = new HashMap<>();
        for (MuscleGroup muscle : ExerciseMatcher.getLegMuscles()) {
            List<Exercise> exercises = exerciseMatcher.findExercisesForMuscle(muscle, profile);
            legDay.put(muscle, exercises.stream().limit(3).toList());
        }
        split.put("legs", legDay);

        return split;
    }

    /**
     * Get all exercises
     */
    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }

    /**
     * Add a custom exercise
     */
    @Transactional
    public Exercise addCustomExercise(Exercise exercise) {
        exercise.setCustom(true);
        return exerciseRepository.save(exercise);
    }

    /**
     * Delete a custom exercise (only if it's custom)
     */
    @Transactional
    public boolean deleteCustomExercise(Long exerciseId) {
        Optional<Exercise> exerciseOpt = exerciseRepository.findById(exerciseId);
        if (exerciseOpt.isPresent() && exerciseOpt.get().isCustom()) {
            exerciseRepository.deleteById(exerciseId);
            return true;
        }
        return false;
    }
}
