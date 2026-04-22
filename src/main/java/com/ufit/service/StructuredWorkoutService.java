package com.ufit.service;

import com.ufit.model.*;
import com.ufit.model.enums.*;
import com.ufit.repository.ExerciseRepository;
import com.ufit.repository.WorkoutPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StructuredWorkoutService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserProfileService profileService;

    public StructuredWorkoutService(WorkoutPlanRepository workoutPlanRepository,
                                     ExerciseRepository exerciseRepository,
                                     UserProfileService profileService) {
        this.workoutPlanRepository = workoutPlanRepository;
        this.exerciseRepository = exerciseRepository;
        this.profileService = profileService;
    }

    /**
     * Generate a structured workout plan based on user's profile and preferences
     */
    @Transactional
    public WorkoutPlan generateWorkoutPlan(Long profileId, WorkoutPlan.PlanType planType) {
        UserProfile profile = profileService.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + profileId));

        // Deactivate any existing active plans
        workoutPlanRepository.findActiveByProfileId(profileId)
                .forEach(plan -> plan.setActive(false));

        WorkoutPlan plan = new WorkoutPlan();
        plan.setUserProfile(profile);
        plan.setPlanType(planType);
        plan.setTargetGoal(profile.getFitnessGoal());
        plan.setDifficulty(determineDifficulty(profile));
        plan.setStartDate(LocalDate.now());
        plan.setDurationWeeks(12);
        plan.setActive(true);
        plan.setCustom(false);

        switch (planType) {
            case PUSH_PULL_LEGS -> generatePPLPlan(plan, profile);
            case UPPER_LOWER -> generateUpperLowerPlan(plan, profile);
            case FULL_BODY -> generateFullBodyPlan(plan, profile);
            case BRO_SPLIT -> generateBroSplitPlan(plan, profile);
            case UPPER_LOWER_PPL -> generateUpperLowerPPLPlan(plan, profile);
            default -> throw new IllegalArgumentException("Cannot generate: " + planType);
        }

        return workoutPlanRepository.save(plan);
    }

    /**
     * Generate a Push/Pull/Legs workout plan (like Hevy beginner program)
     */
    private void generatePPLPlan(WorkoutPlan plan, UserProfile profile) {
        plan.setName("Push/Pull/Legs Program");
        plan.setDescription("A classic 3-day split targeting push muscles, pull muscles, and legs separately. Great for building strength and muscle.");
        plan.setDaysPerWeek(3);

        Set<InjuryType> injuries = profile.getInjuries() != null ? profile.getInjuries() : Set.of();
        DifficultyLevel level = plan.getDifficulty();

        // Day 1: Push
        WorkoutDay pushDay = new WorkoutDay("Push Day", WorkoutDay.DayFocus.PUSH, 1);
        pushDay.setDayOfWeek("MONDAY");
        pushDay.setDescription("Chest, Shoulders, and Triceps");
        addPushExercises(pushDay, injuries, level, profile.getFitnessGoal());
        plan.addWorkoutDay(pushDay);

        // Day 2: Pull
        WorkoutDay pullDay = new WorkoutDay("Pull Day", WorkoutDay.DayFocus.PULL, 2);
        pullDay.setDayOfWeek("WEDNESDAY");
        pullDay.setDescription("Back, Biceps, and Rear Delts");
        addPullExercises(pullDay, injuries, level, profile.getFitnessGoal());
        plan.addWorkoutDay(pullDay);

        // Day 3: Legs
        WorkoutDay legDay = new WorkoutDay("Leg Day", WorkoutDay.DayFocus.LEGS, 3);
        legDay.setDayOfWeek("FRIDAY");
        legDay.setDescription("Quadriceps, Hamstrings, Glutes, and Calves");
        addLegExercises(legDay, injuries, level, profile.getFitnessGoal());
        plan.addWorkoutDay(legDay);
    }

    /**
     * Generate an Upper/Lower workout plan
     */
    private void generateUpperLowerPlan(WorkoutPlan plan, UserProfile profile) {
        plan.setName("Upper/Lower Split Program");
        plan.setDescription("A 4-day split alternating between upper and lower body workouts. Excellent for balanced development.");
        plan.setDaysPerWeek(4);

        Set<InjuryType> injuries = profile.getInjuries() != null ? profile.getInjuries() : Set.of();
        DifficultyLevel level = plan.getDifficulty();

        // Day 1: Upper A
        WorkoutDay upperA = new WorkoutDay("Upper Body A", WorkoutDay.DayFocus.UPPER, 1);
        upperA.setDayOfWeek("MONDAY");
        upperA.setDescription("Chest and Back focused");
        addUpperExercises(upperA, injuries, level, profile.getFitnessGoal(), true);
        plan.addWorkoutDay(upperA);

        // Day 2: Lower A
        WorkoutDay lowerA = new WorkoutDay("Lower Body A", WorkoutDay.DayFocus.LOWER, 2);
        lowerA.setDayOfWeek("TUESDAY");
        lowerA.setDescription("Quad dominant");
        addLowerExercises(lowerA, injuries, level, profile.getFitnessGoal(), true);
        plan.addWorkoutDay(lowerA);

        // Day 3: Upper B
        WorkoutDay upperB = new WorkoutDay("Upper Body B", WorkoutDay.DayFocus.UPPER, 3);
        upperB.setDayOfWeek("THURSDAY");
        upperB.setDescription("Shoulders and Arms focused");
        addUpperExercises(upperB, injuries, level, profile.getFitnessGoal(), false);
        plan.addWorkoutDay(upperB);

        // Day 4: Lower B
        WorkoutDay lowerB = new WorkoutDay("Lower Body B", WorkoutDay.DayFocus.LOWER, 4);
        lowerB.setDayOfWeek("FRIDAY");
        lowerB.setDescription("Hamstring and glute dominant");
        addLowerExercises(lowerB, injuries, level, profile.getFitnessGoal(), false);
        plan.addWorkoutDay(lowerB);
    }

    /**
     * Generate a Full Body workout plan
     */
    private void generateFullBodyPlan(WorkoutPlan plan, UserProfile profile) {
        plan.setName("Full Body Program");
        plan.setDescription("Hit all major muscle groups 3 times per week. Perfect for beginners or those with limited time.");
        plan.setDaysPerWeek(3);

        Set<InjuryType> injuries = profile.getInjuries() != null ? profile.getInjuries() : Set.of();
        DifficultyLevel level = plan.getDifficulty();

        String[] days = {"MONDAY", "WEDNESDAY", "FRIDAY"};
        for (int i = 0; i < 3; i++) {
            WorkoutDay fullBody = new WorkoutDay("Full Body " + (i + 1), WorkoutDay.DayFocus.FULL_BODY, i + 1);
            fullBody.setDayOfWeek(days[i]);
            fullBody.setDescription("Complete workout targeting all major muscle groups");
            addFullBodyExercises(fullBody, injuries, level, profile.getFitnessGoal(), i);
            plan.addWorkoutDay(fullBody);
        }
    }

    /**
     * Generate a Bro Split (5-day) plan
     */
    private void generateBroSplitPlan(WorkoutPlan plan, UserProfile profile) {
        plan.setName("5-Day Bro Split");
        plan.setDescription("Classic bodybuilding split with dedicated days for each muscle group. High volume for maximum hypertrophy.");
        plan.setDaysPerWeek(5);

        Set<InjuryType> injuries = profile.getInjuries() != null ? profile.getInjuries() : Set.of();
        DifficultyLevel level = plan.getDifficulty();

        // Day 1: Chest
        WorkoutDay chestDay = new WorkoutDay("Chest Day", WorkoutDay.DayFocus.CHEST, 1);
        chestDay.setDayOfWeek("MONDAY");
        addChestExercises(chestDay, injuries, level);
        plan.addWorkoutDay(chestDay);

        // Day 2: Back
        WorkoutDay backDay = new WorkoutDay("Back Day", WorkoutDay.DayFocus.BACK, 2);
        backDay.setDayOfWeek("TUESDAY");
        addBackExercises(backDay, injuries, level);
        plan.addWorkoutDay(backDay);

        // Day 3: Shoulders
        WorkoutDay shoulderDay = new WorkoutDay("Shoulder Day", WorkoutDay.DayFocus.SHOULDERS, 3);
        shoulderDay.setDayOfWeek("WEDNESDAY");
        addShoulderExercises(shoulderDay, injuries, level);
        plan.addWorkoutDay(shoulderDay);

        // Day 4: Legs
        WorkoutDay legDay = new WorkoutDay("Leg Day", WorkoutDay.DayFocus.LEGS, 4);
        legDay.setDayOfWeek("THURSDAY");
        addLegExercises(legDay, injuries, level, profile.getFitnessGoal());
        plan.addWorkoutDay(legDay);

        // Day 5: Arms
        WorkoutDay armsDay = new WorkoutDay("Arms Day", WorkoutDay.DayFocus.ARMS, 5);
        armsDay.setDayOfWeek("FRIDAY");
        addArmsExercises(armsDay, injuries, level);
        plan.addWorkoutDay(armsDay);
    }

    /**
     * Generate an Upper/Lower + PPL hybrid plan (5 days)
     */
    private void generateUpperLowerPPLPlan(WorkoutPlan plan, UserProfile profile) {
        plan.setName("Upper/Lower + PPL Hybrid");
        plan.setDescription("A 5-day hybrid combining upper/lower for strength with push/pull/legs for volume. Best of both worlds.");
        plan.setDaysPerWeek(5);

        Set<InjuryType> injuries = profile.getInjuries() != null ? profile.getInjuries() : Set.of();
        DifficultyLevel level = plan.getDifficulty();

        // Day 1: Upper (Strength focus)
        WorkoutDay upperDay = new WorkoutDay("Upper Strength", WorkoutDay.DayFocus.UPPER, 1);
        upperDay.setDayOfWeek("MONDAY");
        upperDay.setDescription("Heavy compound upper body for strength");
        addUpperExercises(upperDay, injuries, level, profile.getFitnessGoal(), true);
        plan.addWorkoutDay(upperDay);

        // Day 2: Lower (Strength focus)
        WorkoutDay lowerDay = new WorkoutDay("Lower Strength", WorkoutDay.DayFocus.LOWER, 2);
        lowerDay.setDayOfWeek("TUESDAY");
        lowerDay.setDescription("Heavy compound lower body for strength");
        addLowerExercises(lowerDay, injuries, level, profile.getFitnessGoal(), true);
        plan.addWorkoutDay(lowerDay);

        // Day 3: Push (Hypertrophy)
        WorkoutDay pushDay = new WorkoutDay("Push Hypertrophy", WorkoutDay.DayFocus.PUSH, 3);
        pushDay.setDayOfWeek("THURSDAY");
        pushDay.setDescription("Chest, Shoulders, Triceps for volume");
        addPushExercises(pushDay, injuries, level, profile.getFitnessGoal());
        plan.addWorkoutDay(pushDay);

        // Day 4: Pull (Hypertrophy)
        WorkoutDay pullDay = new WorkoutDay("Pull Hypertrophy", WorkoutDay.DayFocus.PULL, 4);
        pullDay.setDayOfWeek("FRIDAY");
        pullDay.setDescription("Back, Biceps, Rear Delts for volume");
        addPullExercises(pullDay, injuries, level, profile.getFitnessGoal());
        plan.addWorkoutDay(pullDay);

        // Day 5: Legs (Hypertrophy)
        WorkoutDay legDay = new WorkoutDay("Legs Hypertrophy", WorkoutDay.DayFocus.LEGS, 5);
        legDay.setDayOfWeek("SATURDAY");
        legDay.setDescription("Quads, Hamstrings, Glutes, Calves for volume");
        addLegExercises(legDay, injuries, level, profile.getFitnessGoal());
        plan.addWorkoutDay(legDay);
    }

    // ===================== EXERCISE SELECTION METHODS =====================

    private void addPushExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level, FitnessGoal goal) {
        List<Exercise> chestExercises = getSafeExercises(MuscleGroup.CHEST, injuries);
        List<Exercise> shoulderExercises = getSafeExercises(MuscleGroup.SHOULDERS, injuries);
        List<Exercise> tricepExercises = getSafeExercises(MuscleGroup.TRICEPS, injuries);

        // Main chest compound
        addBestExercise(day, chestExercises, ExerciseType.COMPOUND, 5, "10-12", 90, "Main chest movement");
        
        // Shoulder press
        addBestExercise(day, shoulderExercises, ExerciseType.COMPOUND, 3, "12-15", 60, null);
        
        // Chest isolation
        addBestExercise(day, chestExercises, ExerciseType.ISOLATION, 3, "15-20", 45, null);
        
        // Shoulder isolation
        addBestExercise(day, shoulderExercises, ExerciseType.ISOLATION, 3, "15-20", 45, null);
        
        // Triceps
        addBestExercise(day, tricepExercises, null, 3, "15-20", 45, null);
    }

    private void addPullExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level, FitnessGoal goal) {
        List<Exercise> backExercises = getSafeExercises(MuscleGroup.BACK, injuries);
        List<Exercise> bicepExercises = getSafeExercises(MuscleGroup.BICEPS, injuries);

        // Main back compound (vertical pull)
        addBestExercise(day, backExercises, ExerciseType.COMPOUND, 3, "10-12", 90, "Vertical pulling");
        
        // Secondary back (horizontal pull)
        addBestExercise(day, backExercises, ExerciseType.COMPOUND, 3, "12-15", 60, "Horizontal pulling");
        
        // Traps/Upper back
        addBestExercise(day, backExercises, ExerciseType.ISOLATION, 3, "12-15", 45, null);
        
        // Biceps
        addBestExercise(day, bicepExercises, null, 3, "12-15", 45, null);
        
        // Face pulls / rear delts
        List<Exercise> shoulderExercises = getSafeExercises(MuscleGroup.SHOULDERS, injuries);
        addBestExercise(day, shoulderExercises, ExerciseType.ISOLATION, 3, "15-20", 45, "Rear delts");
    }

    private void addLegExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level, FitnessGoal goal) {
        List<Exercise> quadExercises = getSafeExercises(MuscleGroup.QUADRICEPS, injuries);
        List<Exercise> hamExercises = getSafeExercises(MuscleGroup.HAMSTRINGS, injuries);
        List<Exercise> gluteExercises = getSafeExercises(MuscleGroup.GLUTES, injuries);
        List<Exercise> calfExercises = getSafeExercises(MuscleGroup.CALVES, injuries);

        // Main leg compound
        addBestExercise(day, quadExercises, ExerciseType.COMPOUND, 5, "10-12", 120, "Main leg movement");
        
        // Hamstring
        addBestExercise(day, hamExercises, null, 3, "12-15", 60, null);
        
        // Quad isolation
        addBestExercise(day, quadExercises, ExerciseType.ISOLATION, 3, "12-15", 45, null);
        
        // Calves
        addBestExercise(day, calfExercises, null, 3, "15-20", 45, null);
    }

    private void addUpperExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level, FitnessGoal goal, boolean isVariantA) {
        List<Exercise> chestExercises = getSafeExercises(MuscleGroup.CHEST, injuries);
        List<Exercise> backExercises = getSafeExercises(MuscleGroup.BACK, injuries);
        List<Exercise> shoulderExercises = getSafeExercises(MuscleGroup.SHOULDERS, injuries);
        List<Exercise> bicepExercises = getSafeExercises(MuscleGroup.BICEPS, injuries);
        List<Exercise> tricepExercises = getSafeExercises(MuscleGroup.TRICEPS, injuries);

        if (isVariantA) {
            addBestExercise(day, chestExercises, ExerciseType.COMPOUND, 4, "8-10", 90, null);
            addBestExercise(day, backExercises, ExerciseType.COMPOUND, 4, "8-10", 90, null);
            addBestExercise(day, shoulderExercises, ExerciseType.COMPOUND, 3, "10-12", 60, null);
            addBestExercise(day, bicepExercises, null, 3, "12-15", 45, null);
            addBestExercise(day, tricepExercises, null, 3, "12-15", 45, null);
        } else {
            addBestExercise(day, shoulderExercises, ExerciseType.COMPOUND, 4, "10-12", 90, null);
            addBestExercise(day, chestExercises, ExerciseType.ISOLATION, 3, "12-15", 60, null);
            addBestExercise(day, backExercises, ExerciseType.ISOLATION, 3, "12-15", 60, null);
            addBestExercise(day, bicepExercises, null, 4, "10-12", 45, null);
            addBestExercise(day, tricepExercises, null, 4, "10-12", 45, null);
        }
    }

    private void addLowerExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level, FitnessGoal goal, boolean isQuadDominant) {
        List<Exercise> quadExercises = getSafeExercises(MuscleGroup.QUADRICEPS, injuries);
        List<Exercise> hamExercises = getSafeExercises(MuscleGroup.HAMSTRINGS, injuries);
        List<Exercise> gluteExercises = getSafeExercises(MuscleGroup.GLUTES, injuries);
        List<Exercise> calfExercises = getSafeExercises(MuscleGroup.CALVES, injuries);

        if (isQuadDominant) {
            addBestExercise(day, quadExercises, ExerciseType.COMPOUND, 4, "6-8", 120, null);
            addBestExercise(day, hamExercises, null, 3, "10-12", 60, null);
            addBestExercise(day, quadExercises, ExerciseType.ISOLATION, 3, "12-15", 45, null);
            addBestExercise(day, calfExercises, null, 4, "12-15", 45, null);
        } else {
            addBestExercise(day, hamExercises, ExerciseType.COMPOUND, 4, "8-10", 90, null);
            addBestExercise(day, gluteExercises, null, 4, "10-12", 60, null);
            addBestExercise(day, quadExercises, ExerciseType.ISOLATION, 3, "15-20", 45, null);
            addBestExercise(day, calfExercises, null, 4, "15-20", 45, null);
        }
    }

    private void addFullBodyExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level, FitnessGoal goal, int dayVariant) {
        List<Exercise> quadExercises = getSafeExercises(MuscleGroup.QUADRICEPS, injuries);
        List<Exercise> chestExercises = getSafeExercises(MuscleGroup.CHEST, injuries);
        List<Exercise> backExercises = getSafeExercises(MuscleGroup.BACK, injuries);
        List<Exercise> shoulderExercises = getSafeExercises(MuscleGroup.SHOULDERS, injuries);
        List<Exercise> hamExercises = getSafeExercises(MuscleGroup.HAMSTRINGS, injuries);

        // Rotate primary focus
        switch (dayVariant) {
            case 0 -> {
                addBestExercise(day, quadExercises, ExerciseType.COMPOUND, 4, "8-10", 90, null);
                addBestExercise(day, chestExercises, ExerciseType.COMPOUND, 3, "10-12", 60, null);
                addBestExercise(day, backExercises, ExerciseType.COMPOUND, 3, "10-12", 60, null);
                addBestExercise(day, shoulderExercises, ExerciseType.ISOLATION, 3, "12-15", 45, null);
            }
            case 1 -> {
                addBestExercise(day, backExercises, ExerciseType.COMPOUND, 4, "8-10", 90, null);
                addBestExercise(day, chestExercises, ExerciseType.COMPOUND, 3, "10-12", 60, null);
                addBestExercise(day, hamExercises, ExerciseType.COMPOUND, 3, "10-12", 60, null);
                addBestExercise(day, shoulderExercises, ExerciseType.COMPOUND, 3, "12-15", 60, null);
            }
            case 2 -> {
                addBestExercise(day, shoulderExercises, ExerciseType.COMPOUND, 4, "10-12", 90, null);
                addBestExercise(day, quadExercises, ExerciseType.COMPOUND, 3, "10-12", 60, null);
                addBestExercise(day, backExercises, ExerciseType.ISOLATION, 3, "12-15", 45, null);
                addBestExercise(day, chestExercises, ExerciseType.ISOLATION, 3, "15-20", 45, null);
            }
        }
    }

    private void addChestExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level) {
        List<Exercise> exercises = getSafeExercises(MuscleGroup.CHEST, injuries);
        addBestExercise(day, exercises, ExerciseType.COMPOUND, 4, "8-10", 90, "Flat press");
        addBestExercise(day, exercises, ExerciseType.COMPOUND, 3, "10-12", 60, "Incline");
        addBestExercise(day, exercises, ExerciseType.ISOLATION, 3, "12-15", 45, null);
        addBestExercise(day, exercises, ExerciseType.ISOLATION, 3, "15-20", 45, null);
    }

    private void addBackExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level) {
        List<Exercise> exercises = getSafeExercises(MuscleGroup.BACK, injuries);
        addBestExercise(day, exercises, ExerciseType.COMPOUND, 4, "6-8", 120, "Heavy compound");
        addBestExercise(day, exercises, ExerciseType.COMPOUND, 3, "10-12", 60, null);
        addBestExercise(day, exercises, ExerciseType.ISOLATION, 3, "12-15", 45, null);
        addBestExercise(day, exercises, ExerciseType.ISOLATION, 3, "12-15", 45, null);
    }

    private void addShoulderExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level) {
        List<Exercise> exercises = getSafeExercises(MuscleGroup.SHOULDERS, injuries);
        addBestExercise(day, exercises, ExerciseType.COMPOUND, 4, "8-10", 90, null);
        addBestExercise(day, exercises, ExerciseType.ISOLATION, 3, "12-15", 45, "Front");
        addBestExercise(day, exercises, ExerciseType.ISOLATION, 3, "12-15", 45, "Lateral");
        addBestExercise(day, exercises, ExerciseType.ISOLATION, 3, "15-20", 45, "Rear");
    }

    private void addArmsExercises(WorkoutDay day, Set<InjuryType> injuries, DifficultyLevel level) {
        List<Exercise> biceps = getSafeExercises(MuscleGroup.BICEPS, injuries);
        List<Exercise> triceps = getSafeExercises(MuscleGroup.TRICEPS, injuries);
        
        addBestExercise(day, biceps, null, 3, "10-12", 60, null);
        addBestExercise(day, triceps, null, 3, "10-12", 60, null);
        addBestExercise(day, biceps, null, 3, "12-15", 45, null);
        addBestExercise(day, triceps, null, 3, "12-15", 45, null);
        addBestExercise(day, biceps, null, 3, "15-20", 45, null);
        addBestExercise(day, triceps, null, 3, "15-20", 45, null);
    }

    // ===================== HELPER METHODS =====================

    private List<Exercise> getSafeExercises(MuscleGroup muscle, Set<InjuryType> injuries) {
        List<Exercise> exercises = exerciseRepository.findByPrimaryMuscle(muscle);
        if (injuries == null || injuries.isEmpty()) {
            return exercises;
        }
        return exercises.stream()
                .filter(e -> e.getContraindications() == null || 
                        Collections.disjoint(e.getContraindications(), injuries))
                .collect(Collectors.toList());
    }

    private void addBestExercise(WorkoutDay day, List<Exercise> exercises, ExerciseType type, 
                                  int sets, String reps, int rest, String notes) {
        if (exercises.isEmpty()) return;
        
        List<Exercise> filtered = exercises;
        if (type != null) {
            filtered = exercises.stream()
                    .filter(e -> e.getExerciseType() == type)
                    .collect(Collectors.toList());
            if (filtered.isEmpty()) filtered = exercises;
        }

        // Avoid duplicates in the same day
        Set<Long> usedIds = day.getPlannedExercises().stream()
                .map(pe -> pe.getExercise().getId())
                .collect(Collectors.toSet());
        
        Optional<Exercise> selected = filtered.stream()
                .filter(e -> !usedIds.contains(e.getId()))
                .findFirst();
        
        if (selected.isPresent()) {
            PlannedExercise planned = new PlannedExercise(selected.get(), sets, reps, rest);
            if (notes != null) planned.setNotes(notes);
            day.addExercise(planned);
        }
    }

    private DifficultyLevel determineDifficulty(UserProfile profile) {
        int workoutDays = profile.getWorkoutDaysPerWeek();
        if (workoutDays <= 2) return DifficultyLevel.BEGINNER;
        if (workoutDays <= 4) return DifficultyLevel.INTERMEDIATE;
        return DifficultyLevel.ADVANCED;
    }

    // ===================== CRUD OPERATIONS =====================

    public Optional<WorkoutPlan> findById(Long id) {
        return workoutPlanRepository.findById(id);
    }

    public List<WorkoutPlan> findAllByProfile(Long profileId) {
        return workoutPlanRepository.findByUserProfileIdOrderByStartDateDesc(profileId);
    }

    public Optional<WorkoutPlan> findActiveByProfile(Long profileId) {
        return workoutPlanRepository.findLatestActiveByProfileId(profileId);
    }

    public List<WorkoutPlan> findCustomPlansByProfile(Long profileId) {
        return workoutPlanRepository.findCustomPlansByProfileId(profileId);
    }

    @Transactional
    public WorkoutPlan createCustomPlan(Long profileId, WorkoutPlan plan) {
        UserProfile profile = profileService.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + profileId));
        
        // Deactivate other active plans if this one is active
        if (plan.isActive()) {
            workoutPlanRepository.findActiveByProfileId(profileId)
                    .forEach(p -> p.setActive(false));
        }
        
        plan.setUserProfile(profile);
        plan.setPlanType(WorkoutPlan.PlanType.CUSTOM);
        plan.setCustom(true);
        plan.setStartDate(LocalDate.now());
        
        // Process workout days and exercises
        if (plan.getWorkoutDays() != null) {
            for (WorkoutDay day : plan.getWorkoutDays()) {
                day.setWorkoutPlan(plan);
                
                if (day.getPlannedExercises() != null) {
                    for (PlannedExercise pe : day.getPlannedExercises()) {
                        pe.setWorkoutDay(day);
                        
                        // Resolve exercise reference if only ID is provided
                        if (pe.getExerciseId() != null && pe.getExercise() == null) {
                            Exercise exercise = exerciseRepository.findById(pe.getExerciseId())
                                    .orElse(null);
                            pe.setExercise(exercise);
                        } else if (pe.getExercise() != null && pe.getExercise().getId() != null) {
                            Exercise exercise = exerciseRepository.findById(pe.getExercise().getId())
                                    .orElse(null);
                            pe.setExercise(exercise);
                        }
                    }
                }
            }
        }
        
        return workoutPlanRepository.save(plan);
    }

    @Transactional
    public WorkoutPlan updatePlan(WorkoutPlan plan) {
        return workoutPlanRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Long planId) {
        workoutPlanRepository.deleteById(planId);
    }

    @Transactional
    public WorkoutPlan setActivePlan(Long profileId, Long planId) {
        // Deactivate all other plans
        workoutPlanRepository.findActiveByProfileId(profileId)
                .forEach(p -> p.setActive(false));
        
        WorkoutPlan plan = workoutPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));
        plan.setActive(true);
        return workoutPlanRepository.save(plan);
    }
}
