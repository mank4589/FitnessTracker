package com.ufit.controller;

import com.ufit.model.*;
import com.ufit.repository.ExerciseRepository;
import com.ufit.service.StructuredWorkoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/workout-plans")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "false")
public class WorkoutPlanController {

    private final StructuredWorkoutService workoutService;
    private final ExerciseRepository exerciseRepository;

    public WorkoutPlanController(StructuredWorkoutService workoutService, 
                                  ExerciseRepository exerciseRepository) {
        this.workoutService = workoutService;
        this.exerciseRepository = exerciseRepository;
    }

    // ═══════════════ PLAN GENERATION ═══════════════

    /**
     * Generate a structured workout plan for a user
     */
    @PostMapping("/generate/{profileId}")
    public ResponseEntity<WorkoutPlan> generatePlan(
            @PathVariable Long profileId,
            @RequestParam(defaultValue = "PUSH_PULL_LEGS") WorkoutPlan.PlanType planType) {
        try {
            WorkoutPlan plan = workoutService.generateWorkoutPlan(profileId, planType);
            return ResponseEntity.status(HttpStatus.CREATED).body(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get available plan types
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, Object>>> getPlanTypes() {
        List<Map<String, Object>> types = List.of(
            Map.of("type", "PUSH_PULL_LEGS", "name", "Push/Pull/Legs", "days", 3, 
                   "description", "Classic 3-day split. Push (chest, shoulders, triceps), Pull (back, biceps), Legs."),
            Map.of("type", "UPPER_LOWER", "name", "Upper/Lower Split", "days", 4,
                   "description", "4-day split alternating upper and lower body for balanced development."),
            Map.of("type", "FULL_BODY", "name", "Full Body", "days", 3,
                   "description", "Hit all muscle groups each session. Great for beginners."),
            Map.of("type", "BRO_SPLIT", "name", "5-Day Bro Split", "days", 5,
                   "description", "Dedicated day for each muscle group. Maximum volume for hypertrophy.")
        );
        return ResponseEntity.ok(types);
    }

    // ═══════════════ PLAN RETRIEVAL ═══════════════

    /**
     * Get all plans for a user
     */
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<WorkoutPlan>> getAllPlans(@PathVariable Long profileId) {
        List<WorkoutPlan> plans = workoutService.findAllByProfile(profileId);
        return ResponseEntity.ok(plans);
    }

    /**
     * Get the active plan for a user
     */
    @GetMapping("/active/{profileId}")
    public ResponseEntity<WorkoutPlan> getActivePlan(@PathVariable Long profileId) {
        return workoutService.findActiveByProfile(profileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a specific plan by ID
     */
    @GetMapping("/{planId}")
    public ResponseEntity<WorkoutPlan> getPlan(@PathVariable Long planId) {
        return workoutService.findById(planId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get custom plans only
     */
    @GetMapping("/custom/{profileId}")
    public ResponseEntity<List<WorkoutPlan>> getCustomPlans(@PathVariable Long profileId) {
        List<WorkoutPlan> plans = workoutService.findCustomPlansByProfile(profileId);
        return ResponseEntity.ok(plans);
    }

    // ═══════════════ CUSTOM PLAN MANAGEMENT ═══════════════

    /**
     * Create a custom workout plan
     */
    @PostMapping("/custom/{profileId}")
    public ResponseEntity<WorkoutPlan> createCustomPlan(
            @PathVariable Long profileId,
            @RequestBody WorkoutPlan plan) {
        try {
            WorkoutPlan created = workoutService.createCustomPlan(profileId, plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update a workout plan
     */
    @PutMapping("/{planId}")
    public ResponseEntity<WorkoutPlan> updatePlan(
            @PathVariable Long planId,
            @RequestBody WorkoutPlan updates) {
        Optional<WorkoutPlan> existing = workoutService.findById(planId);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkoutPlan plan = existing.get();
        if (updates.getName() != null) plan.setName(updates.getName());
        if (updates.getDescription() != null) plan.setDescription(updates.getDescription());
        if (updates.getDaysPerWeek() > 0) plan.setDaysPerWeek(updates.getDaysPerWeek());
        
        return ResponseEntity.ok(workoutService.updatePlan(plan));
    }

    /**
     * Delete a workout plan
     */
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long planId) {
        workoutService.deletePlan(planId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set a plan as active
     */
    @PostMapping("/{planId}/activate/{profileId}")
    public ResponseEntity<WorkoutPlan> activatePlan(
            @PathVariable Long planId,
            @PathVariable Long profileId) {
        try {
            WorkoutPlan plan = workoutService.setActivePlan(profileId, planId);
            return ResponseEntity.ok(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ═══════════════ WORKOUT DAY MANAGEMENT ═══════════════

    /**
     * Add a workout day to a plan
     */
    @PostMapping("/{planId}/days")
    public ResponseEntity<WorkoutPlan> addWorkoutDay(
            @PathVariable Long planId,
            @RequestBody WorkoutDay day) {
        Optional<WorkoutPlan> planOpt = workoutService.findById(planId);
        if (planOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkoutPlan plan = planOpt.get();
        day.setDayOrder(plan.getWorkoutDays().size() + 1);
        plan.addWorkoutDay(day);
        return ResponseEntity.ok(workoutService.updatePlan(plan));
    }

    /**
     * Update a workout day
     */
    @PutMapping("/{planId}/days/{dayId}")
    public ResponseEntity<WorkoutPlan> updateWorkoutDay(
            @PathVariable Long planId,
            @PathVariable Long dayId,
            @RequestBody WorkoutDay updates) {
        Optional<WorkoutPlan> planOpt = workoutService.findById(planId);
        if (planOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkoutPlan plan = planOpt.get();
        WorkoutDay day = plan.getWorkoutDays().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElse(null);
        
        if (day == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (updates.getName() != null) day.setName(updates.getName());
        if (updates.getDescription() != null) day.setDescription(updates.getDescription());
        if (updates.getDayOfWeek() != null) day.setDayOfWeek(updates.getDayOfWeek());
        
        return ResponseEntity.ok(workoutService.updatePlan(plan));
    }

    /**
     * Delete a workout day from a plan
     */
    @DeleteMapping("/{planId}/days/{dayId}")
    public ResponseEntity<WorkoutPlan> deleteWorkoutDay(
            @PathVariable Long planId,
            @PathVariable Long dayId) {
        Optional<WorkoutPlan> planOpt = workoutService.findById(planId);
        if (planOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkoutPlan plan = planOpt.get();
        plan.getWorkoutDays().removeIf(d -> d.getId().equals(dayId));
        return ResponseEntity.ok(workoutService.updatePlan(plan));
    }

    // ═══════════════ EXERCISE MANAGEMENT ═══════════════

    /**
     * Add an exercise to a workout day
     */
    @PostMapping("/{planId}/days/{dayId}/exercises")
    public ResponseEntity<WorkoutPlan> addExerciseToDay(
            @PathVariable Long planId,
            @PathVariable Long dayId,
            @RequestBody PlannedExercise plannedExercise) {
        Optional<WorkoutPlan> planOpt = workoutService.findById(planId);
        if (planOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkoutPlan plan = planOpt.get();
        WorkoutDay day = plan.getWorkoutDays().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElse(null);
        
        if (day == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Fetch the actual exercise
        if (plannedExercise.getExercise() != null && plannedExercise.getExercise().getId() != null) {
            Exercise exercise = exerciseRepository.findById(plannedExercise.getExercise().getId())
                    .orElse(null);
            if (exercise == null) {
                return ResponseEntity.badRequest().build();
            }
            plannedExercise.setExercise(exercise);
        }
        
        day.addExercise(plannedExercise);
        return ResponseEntity.ok(workoutService.updatePlan(plan));
    }

    /**
     * Update an exercise in a workout day
     */
    @PutMapping("/{planId}/days/{dayId}/exercises/{exerciseId}")
    public ResponseEntity<WorkoutPlan> updateExercise(
            @PathVariable Long planId,
            @PathVariable Long dayId,
            @PathVariable Long exerciseId,
            @RequestBody PlannedExercise updates) {
        Optional<WorkoutPlan> planOpt = workoutService.findById(planId);
        if (planOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkoutPlan plan = planOpt.get();
        WorkoutDay day = plan.getWorkoutDays().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElse(null);
        
        if (day == null) {
            return ResponseEntity.notFound().build();
        }
        
        PlannedExercise exercise = day.getPlannedExercises().stream()
                .filter(e -> e.getId().equals(exerciseId))
                .findFirst()
                .orElse(null);
        
        if (exercise == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Update fields
        if (updates.getSets() > 0) exercise.setSets(updates.getSets());
        if (updates.getRepRange() != null) exercise.setRepRange(updates.getRepRange());
        if (updates.getRestSeconds() > 0) exercise.setRestSeconds(updates.getRestSeconds());
        if (updates.getWeight() != null) exercise.setWeight(updates.getWeight());
        if (updates.getRpe() != null) exercise.setRpe(updates.getRpe());
        if (updates.getNotes() != null) exercise.setNotes(updates.getNotes());
        
        return ResponseEntity.ok(workoutService.updatePlan(plan));
    }

    /**
     * Remove an exercise from a workout day
     */
    @DeleteMapping("/{planId}/days/{dayId}/exercises/{exerciseId}")
    public ResponseEntity<WorkoutPlan> removeExercise(
            @PathVariable Long planId,
            @PathVariable Long dayId,
            @PathVariable Long exerciseId) {
        Optional<WorkoutPlan> planOpt = workoutService.findById(planId);
        if (planOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkoutPlan plan = planOpt.get();
        WorkoutDay day = plan.getWorkoutDays().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElse(null);
        
        if (day == null) {
            return ResponseEntity.notFound().build();
        }
        
        day.getPlannedExercises().removeIf(e -> e.getId().equals(exerciseId));
        day.reorderExercises();
        
        return ResponseEntity.ok(workoutService.updatePlan(plan));
    }

    /**
     * Reorder exercises in a workout day
     */
    @PutMapping("/{planId}/days/{dayId}/reorder")
    public ResponseEntity<WorkoutPlan> reorderExercises(
            @PathVariable Long planId,
            @PathVariable Long dayId,
            @RequestBody List<Long> exerciseIds) {
        Optional<WorkoutPlan> planOpt = workoutService.findById(planId);
        if (planOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        WorkoutPlan plan = planOpt.get();
        WorkoutDay day = plan.getWorkoutDays().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElse(null);
        
        if (day == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Reorder based on provided IDs
        for (int i = 0; i < exerciseIds.size(); i++) {
            final int order = i + 1;
            final Long id = exerciseIds.get(i);
            day.getPlannedExercises().stream()
                    .filter(e -> e.getId().equals(id))
                    .findFirst()
                    .ifPresent(e -> e.setExerciseOrder(order));
        }
        
        return ResponseEntity.ok(workoutService.updatePlan(plan));
    }

    // ═══════════════ EXERCISE LIBRARY ═══════════════

    /**
     * Search exercises by name
     */
    @GetMapping("/exercises/search")
    public ResponseEntity<List<Exercise>> searchExercises(
            @RequestParam String query) {
        List<Exercise> exercises = exerciseRepository.findByNameContainingIgnoreCase(query);
        return ResponseEntity.ok(exercises);
    }

    /**
     * Get all exercises
     */
    @GetMapping("/exercises")
    public ResponseEntity<List<Exercise>> getAllExercises() {
        List<Exercise> exercises = exerciseRepository.findAll();
        return ResponseEntity.ok(exercises);
    }
}

