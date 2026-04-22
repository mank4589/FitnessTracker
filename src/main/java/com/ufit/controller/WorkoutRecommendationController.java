package com.ufit.controller;

import com.ufit.model.Exercise;
import com.ufit.model.UserProfile;
import com.ufit.model.enums.*;
import com.ufit.service.UserProfileService;
import com.ufit.service.WorkoutPlannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/workouts")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "false")
public class WorkoutRecommendationController {

    private final WorkoutPlannerService workoutService;
    private final UserProfileService profileService;

    public WorkoutRecommendationController(WorkoutPlannerService workoutService,
                                          UserProfileService profileService) {
        this.workoutService = workoutService;
        this.profileService = profileService;
    }

    /**
     * Get recommended exercises for a specific muscle group
     */
    @GetMapping("/muscles/{muscle}/profile/{profileId}")
    public ResponseEntity<List<Exercise>> getExercisesForMuscle(
            @PathVariable MuscleGroup muscle,
            @PathVariable Long profileId) {
        
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Exercise> exercises = workoutService.getExercisesForMuscle(muscle, profile.get());
        return ResponseEntity.ok(exercises);
    }

    /**
     * Get compound exercises for a muscle group
     */
    @GetMapping("/muscles/{muscle}/compound/profile/{profileId}")
    public ResponseEntity<List<Exercise>> getCompoundExercises(
            @PathVariable MuscleGroup muscle,
            @PathVariable Long profileId) {
        
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Exercise> exercises = workoutService.getCompoundExercises(muscle, profile.get());
        return ResponseEntity.ok(exercises);
    }

    /**
     * Get isolation exercises for a muscle group
     */
    @GetMapping("/muscles/{muscle}/isolation/profile/{profileId}")
    public ResponseEntity<List<Exercise>> getIsolationExercises(
            @PathVariable MuscleGroup muscle,
            @PathVariable Long profileId) {
        
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Exercise> exercises = workoutService.getIsolationExercises(muscle, profile.get());
        return ResponseEntity.ok(exercises);
    }

    /**
     * Get cardio exercises
     */
    @GetMapping("/cardio/profile/{profileId}")
    public ResponseEntity<List<Exercise>> getCardioExercises(@PathVariable Long profileId) {
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Exercise> exercises = workoutService.getCardioExercises(profile.get());
        return ResponseEntity.ok(exercises);
    }

    /**
     * Get exercises by difficulty
     */
    @GetMapping("/muscles/{muscle}/difficulty/{level}/profile/{profileId}")
    public ResponseEntity<List<Exercise>> getExercisesByDifficulty(
            @PathVariable MuscleGroup muscle,
            @PathVariable DifficultyLevel level,
            @PathVariable Long profileId) {
        
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Exercise> exercises = workoutService.getExercisesByDifficulty(muscle, level, profile.get());
        return ResponseEntity.ok(exercises);
    }

    /**
     * Get exercises by equipment
     */
    @GetMapping("/equipment/{equipment}/profile/{profileId}")
    public ResponseEntity<List<Exercise>> getExercisesByEquipment(
            @PathVariable Equipment equipment,
            @PathVariable Long profileId) {
        
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Exercise> exercises = workoutService.getExercisesByEquipment(equipment, profile.get());
        return ResponseEntity.ok(exercises);
    }

    /**
     * Get recommended workout based on user's profile
     */
    @GetMapping("/recommended/{profileId}")
    public ResponseEntity<Map<MuscleGroup, List<Exercise>>> getRecommendedWorkout(@PathVariable Long profileId) {
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<MuscleGroup, List<Exercise>> workout = workoutService.getRecommendedWorkout(profile.get());
        return ResponseEntity.ok(workout);
    }

    /**
     * Get push/pull/legs split workout plan
     */
    @GetMapping("/ppl/{profileId}")
    public ResponseEntity<Map<String, Map<MuscleGroup, List<Exercise>>>> getPushPullLegsSplit(@PathVariable Long profileId) {
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Map<MuscleGroup, List<Exercise>>> split = workoutService.getPushPullLegsSplit(profile.get());
        return ResponseEntity.ok(split);
    }

    /**
     * Get all exercises
     */
    @GetMapping("/exercises")
    public ResponseEntity<List<Exercise>> getAllExercises() {
        List<Exercise> exercises = workoutService.getAllExercises();
        return ResponseEntity.ok(exercises);
    }

    /**
     * Add a custom exercise
     */
    @PostMapping("/exercises/custom")
    public ResponseEntity<Exercise> addCustomExercise(@RequestBody Exercise exercise) {
        try {
            Exercise created = workoutService.addCustomExercise(exercise);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a custom exercise
     */
    @DeleteMapping("/exercises/custom/{exerciseId}")
    public ResponseEntity<Void> deleteCustomExercise(@PathVariable Long exerciseId) {
        boolean deleted = workoutService.deleteCustomExercise(exerciseId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

