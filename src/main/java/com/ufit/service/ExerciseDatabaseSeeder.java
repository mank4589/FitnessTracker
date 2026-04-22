package com.ufit.service;

import com.ufit.model.Exercise;
import com.ufit.model.enums.*;
import com.ufit.repository.ExerciseRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class ExerciseDatabaseSeeder {

    private final ExerciseRepository exerciseRepository;

    public ExerciseDatabaseSeeder(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    @PostConstruct
    @Transactional
    public void seedExercises() {
        if (exerciseRepository.count() > 0) {
            System.out.println("[ExerciseSeeder] Database already contains exercises. Skipping seed.");
            return;
        }

        System.out.println("[ExerciseSeeder] Seeding exercise database...");

        seedChestExercises();
        seedBackExercises();
        seedShoulderExercises();
        seedArmExercises();
        seedLegExercises();
        seedCoreExercises();
        seedCardioExercises();

        System.out.println("[ExerciseSeeder] Exercise database seeded successfully with " + exerciseRepository.count() + " exercises!");
    }

    private void seedChestExercises() {
        exerciseRepository.save(createExercise(
            "Barbell Bench Press", MuscleGroup.CHEST, ExerciseType.COMPOUND,
            Equipment.BARBELL, DifficultyLevel.INTERMEDIATE, 4, 8, 0, 6.0,
            Set.of(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
            Set.of(InjuryType.SHOULDER, InjuryType.ROTATOR_CUFF),
            "Lie on bench, lower bar to chest, press up"
        ));

        exerciseRepository.save(createExercise(
            "Push-Ups", MuscleGroup.CHEST, ExerciseType.COMPOUND,
            Equipment.NONE, DifficultyLevel.BEGINNER, 3, 15, 0, 5.5,
            Set.of(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS, MuscleGroup.CORE),
            Set.of(InjuryType.WRIST, InjuryType.SHOULDER),
            "Standard push-up position, lower chest to ground, push up"
        ));

        exerciseRepository.save(createExercise(
            "Dumbbell Bench Press", MuscleGroup.CHEST, ExerciseType.COMPOUND,
            Equipment.DUMBBELLS, DifficultyLevel.INTERMEDIATE, 4, 10, 0, 6.0,
            Set.of(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS),
            Set.of(InjuryType.SHOULDER, InjuryType.ROTATOR_CUFF),
            "Lie on bench with dumbbells, press up from chest level"
        ));

        exerciseRepository.save(createExercise(
            "Dumbbell Flyes", MuscleGroup.CHEST, ExerciseType.ISOLATION,
            Equipment.DUMBBELLS, DifficultyLevel.INTERMEDIATE, 3, 12, 0, 5.0,
            Set.of(MuscleGroup.SHOULDERS),
            Set.of(InjuryType.SHOULDER, InjuryType.ROTATOR_CUFF),
            "Lie on bench, lower dumbbells out to sides, squeeze chest"
        ));

        exerciseRepository.save(createExercise(
            "Cable Crossovers", MuscleGroup.CHEST, ExerciseType.ISOLATION,
            Equipment.CABLE_MACHINE, DifficultyLevel.INTERMEDIATE, 3, 15, 0, 4.5,
            Set.of(),
            Set.of(InjuryType.SHOULDER),
            "Cross cables in front of body, squeeze chest"
        ));
    }

    private void seedBackExercises() {
        exerciseRepository.save(createExercise(
            "Deadlift", MuscleGroup.BACK, ExerciseType.COMPOUND,
            Equipment.BARBELL, DifficultyLevel.ADVANCED, 4, 6, 0, 7.5,
            Set.of(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CORE),
            Set.of(InjuryType.LOWER_BACK, InjuryType.UPPER_BACK),
            "Hip-hinge movement, lift barbell from ground to standing"
        ));

        exerciseRepository.save(createExercise(
            "Pull-Ups", MuscleGroup.BACK, ExerciseType.COMPOUND,
            Equipment.PULL_UP_BAR, DifficultyLevel.INTERMEDIATE, 3, 10, 0, 6.5,
            Set.of(MuscleGroup.BICEPS, MuscleGroup.FOREARMS),
            Set.of(InjuryType.SHOULDER, InjuryType.ELBOW),
            "Hang from bar, pull body up until chin over bar"
        ));

        exerciseRepository.save(createExercise(
            "Barbell Rows", MuscleGroup.BACK, ExerciseType.COMPOUND,
            Equipment.BARBELL, DifficultyLevel.INTERMEDIATE, 4, 10, 0, 6.0,
            Set.of(MuscleGroup.BICEPS, MuscleGroup.CORE),
            Set.of(InjuryType.LOWER_BACK),
            "Bend at hips, row barbell to lower chest"
        ));

        exerciseRepository.save(createExercise(
            "Lat Pulldown", MuscleGroup.BACK, ExerciseType.ISOLATION,
            Equipment.CABLE_MACHINE, DifficultyLevel.BEGINNER, 3, 12, 0, 5.5,
            Set.of(MuscleGroup.BICEPS),
            Set.of(InjuryType.SHOULDER),
            "Pull bar down to upper chest, squeeze lats"
        ));

        exerciseRepository.save(createExercise(
            "Seated Cable Rows", MuscleGroup.BACK, ExerciseType.ISOLATION,
            Equipment.CABLE_MACHINE, DifficultyLevel.BEGINNER, 3, 12, 0, 5.0,
            Set.of(MuscleGroup.BICEPS),
            Set.of(InjuryType.LOWER_BACK),
            "Pull cable to torso while seated, squeeze shoulder blades"
        ));
    }

    private void seedShoulderExercises() {
        exerciseRepository.save(createExercise(
            "Overhead Press", MuscleGroup.SHOULDERS, ExerciseType.COMPOUND,
            Equipment.BARBELL, DifficultyLevel.INTERMEDIATE, 4, 8, 0, 6.0,
            Set.of(MuscleGroup.TRICEPS, MuscleGroup.CORE),
            Set.of(InjuryType.SHOULDER, InjuryType.ROTATOR_CUFF, InjuryType.LOWER_BACK),
            "Press barbell overhead from shoulders"
        ));

        exerciseRepository.save(createExercise(
            "Dumbbell Shoulder Press", MuscleGroup.SHOULDERS, ExerciseType.COMPOUND,
            Equipment.DUMBBELLS, DifficultyLevel.BEGINNER, 3, 10, 0, 5.5,
            Set.of(MuscleGroup.TRICEPS),
            Set.of(InjuryType.SHOULDER, InjuryType.ROTATOR_CUFF),
            "Press dumbbells overhead from shoulder height"
        ));

        exerciseRepository.save(createExercise(
            "Lateral Raises", MuscleGroup.SHOULDERS, ExerciseType.ISOLATION,
            Equipment.DUMBBELLS, DifficultyLevel.BEGINNER, 3, 15, 0, 4.5,
            Set.of(),
            Set.of(InjuryType.SHOULDER, InjuryType.ROTATOR_CUFF),
            "Raise dumbbells to sides until shoulder height"
        ));

        exerciseRepository.save(createExercise(
            "Front Raises", MuscleGroup.SHOULDERS, ExerciseType.ISOLATION,
            Equipment.DUMBBELLS, DifficultyLevel.BEGINNER, 3, 12, 0, 4.5,
            Set.of(),
            Set.of(InjuryType.SHOULDER),
            "Raise dumbbells to front until shoulder height"
        ));
    }

    private void seedArmExercises() {
        exerciseRepository.save(createExercise(
            "Barbell Curls", MuscleGroup.BICEPS, ExerciseType.ISOLATION,
            Equipment.BARBELL, DifficultyLevel.BEGINNER, 3, 12, 0, 4.5,
            Set.of(MuscleGroup.FOREARMS),
            Set.of(InjuryType.ELBOW),
            "Curl barbell from thighs to shoulders"
        ));

        exerciseRepository.save(createExercise(
            "Dumbbell Curls", MuscleGroup.BICEPS, ExerciseType.ISOLATION,
            Equipment.DUMBBELLS, DifficultyLevel.BEGINNER, 3, 12, 0, 4.5,
            Set.of(MuscleGroup.FOREARMS),
            Set.of(InjuryType.ELBOW),
            "Alternate or simultaneous dumbbell curls"
        ));

        exerciseRepository.save(createExercise(
            "Hammer Curls", MuscleGroup.BICEPS, ExerciseType.ISOLATION,
            Equipment.DUMBBELLS, DifficultyLevel.BEGINNER, 3, 12, 0, 4.5,
            Set.of(MuscleGroup.FOREARMS),
            Set.of(InjuryType.ELBOW),
            "Curl dumbbells with neutral grip (palms facing each other)"
        ));

        exerciseRepository.save(createExercise(
            "Tricep Dips", MuscleGroup.TRICEPS, ExerciseType.COMPOUND,
            Equipment.BENCH, DifficultyLevel.INTERMEDIATE, 3, 12, 0, 5.5,
            Set.of(MuscleGroup.CHEST, MuscleGroup.SHOULDERS),
            Set.of(InjuryType.SHOULDER, InjuryType.ELBOW),
            "Lower body using parallel bars or bench, press back up"
        ));

        exerciseRepository.save(createExercise(
            "Overhead Tricep Extension", MuscleGroup.TRICEPS, ExerciseType.ISOLATION,
            Equipment.DUMBBELLS, DifficultyLevel.BEGINNER, 3, 12, 0, 4.5,
            Set.of(),
            Set.of(InjuryType.ELBOW, InjuryType.SHOULDER),
            "Lower dumbbell behind head, extend arms overhead"
        ));

        exerciseRepository.save(createExercise(
            "Cable Tricep Pushdown", MuscleGroup.TRICEPS, ExerciseType.ISOLATION,
            Equipment.CABLE_MACHINE, DifficultyLevel.BEGINNER, 3, 15, 0, 4.5,
            Set.of(),
            Set.of(InjuryType.ELBOW),
            "Push cable down until arms fully extended"
        ));
    }

    private void seedLegExercises() {
        exerciseRepository.save(createExercise(
            "Barbell Squat", MuscleGroup.QUADRICEPS, ExerciseType.COMPOUND,
            Equipment.BARBELL, DifficultyLevel.INTERMEDIATE, 4, 10, 0, 7.0,
            Set.of(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES, MuscleGroup.CORE),
            Set.of(InjuryType.KNEE, InjuryType.LOWER_BACK, InjuryType.HIP),
            "Squat down with barbell on back, drive through heels"
        ));

        exerciseRepository.save(createExercise(
            "Lunges", MuscleGroup.QUADRICEPS, ExerciseType.COMPOUND,
            Equipment.DUMBBELLS, DifficultyLevel.BEGINNER, 3, 12, 0, 6.0,
            Set.of(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES),
            Set.of(InjuryType.KNEE),
            "Step forward, lower back knee, return to start"
        ));

        exerciseRepository.save(createExercise(
            "Romanian Deadlift", MuscleGroup.HAMSTRINGS, ExerciseType.COMPOUND,
            Equipment.BARBELL, DifficultyLevel.INTERMEDIATE, 3, 10, 0, 6.5,
            Set.of(MuscleGroup.GLUTES, MuscleGroup.BACK),
            Set.of(InjuryType.LOWER_BACK, InjuryType.HIP),
            "Hip hinge with slight knee bend, lower bar to shins"
        ));

        exerciseRepository.save(createExercise(
            "Bulgarian Split Squat", MuscleGroup.QUADRICEPS, ExerciseType.COMPOUND,
            Equipment.DUMBBELLS, DifficultyLevel.INTERMEDIATE, 3, 10, 0, 6.0,
            Set.of(MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES),
            Set.of(InjuryType.KNEE, InjuryType.HIP),
            "Rear foot elevated, squat on front leg"
        ));

        exerciseRepository.save(createExercise(
            "Leg Extensions", MuscleGroup.QUADRICEPS, ExerciseType.ISOLATION,
            Equipment.MACHINE, DifficultyLevel.BEGINNER, 3, 15, 0, 4.5,
            Set.of(),
            Set.of(InjuryType.KNEE),
            "Extend legs from seated position"
        ));

        exerciseRepository.save(createExercise(
            "Leg Curls", MuscleGroup.HAMSTRINGS, ExerciseType.ISOLATION,
            Equipment.MACHINE, DifficultyLevel.BEGINNER, 3, 15, 0, 4.5,
            Set.of(),
            Set.of(InjuryType.KNEE),
            "Curl legs from lying or seated position"
        ));

        exerciseRepository.save(createExercise(
            "Calf Raises", MuscleGroup.CALVES, ExerciseType.ISOLATION,
            Equipment.NONE, DifficultyLevel.BEGINNER, 3, 20, 0, 4.0,
            Set.of(),
            Set.of(InjuryType.ANKLE),
            "Raise up on toes, lower back down"
        ));
    }

    private void seedCoreExercises() {
        exerciseRepository.save(createExercise(
            "Plank", MuscleGroup.CORE, ExerciseType.ISOLATION,
            Equipment.NONE, DifficultyLevel.BEGINNER, 3, 0, 60, 4.5,
            Set.of(),
            Set.of(InjuryType.LOWER_BACK, InjuryType.SHOULDER),
            "Hold push-up position on forearms"
        ));

        exerciseRepository.save(createExercise(
            "Crunches", MuscleGroup.CORE, ExerciseType.ISOLATION,
            Equipment.NONE, DifficultyLevel.BEGINNER, 3, 20, 0, 4.0,
            Set.of(),
            Set.of(InjuryType.NECK, InjuryType.LOWER_BACK),
            "Lie on back, lift shoulders off ground"
        ));

        exerciseRepository.save(createExercise(
            "Russian Twists", MuscleGroup.CORE, ExerciseType.ISOLATION,
            Equipment.NONE, DifficultyLevel.INTERMEDIATE, 3, 30, 0, 5.0,
            Set.of(),
            Set.of(InjuryType.LOWER_BACK),
            "Sit with feet elevated, rotate torso side to side"
        ));

        exerciseRepository.save(createExercise(
            "Hanging Leg Raises", MuscleGroup.CORE, ExerciseType.ISOLATION,
            Equipment.PULL_UP_BAR, DifficultyLevel.ADVANCED, 3, 12, 0, 5.5,
            Set.of(),
            Set.of(InjuryType.LOWER_BACK, InjuryType.SHOULDER),
            "Hang from bar, raise legs to 90 degrees"
        ));
    }

    private void seedCardioExercises() {
        exerciseRepository.save(createExercise(
            "Running", MuscleGroup.FULL_BODY, ExerciseType.CARDIO,
            Equipment.NONE, DifficultyLevel.BEGINNER, 1, 0, 1800, 10.0,
            Set.of(MuscleGroup.QUADRICEPS, MuscleGroup.HAMSTRINGS, MuscleGroup.CALVES),
            Set.of(InjuryType.KNEE, InjuryType.ANKLE, InjuryType.LOWER_BACK),
            "Moderate to high intensity running"
        ));

        exerciseRepository.save(createExercise(
            "Jump Rope", MuscleGroup.FULL_BODY, ExerciseType.CARDIO,
            Equipment.JUMP_ROPE, DifficultyLevel.BEGINNER, 3, 0, 300, 12.0,
            Set.of(MuscleGroup.CALVES, MuscleGroup.SHOULDERS),
            Set.of(InjuryType.ANKLE, InjuryType.KNEE),
            "Jump over rope continuously"
        ));

        exerciseRepository.save(createExercise(
            "Cycling", MuscleGroup.FULL_BODY, ExerciseType.CARDIO,
            Equipment.STATIONARY_BIKE, DifficultyLevel.BEGINNER, 1, 0, 1800, 8.0,
            Set.of(MuscleGroup.QUADRICEPS, MuscleGroup.HAMSTRINGS, MuscleGroup.GLUTES),
            Set.of(InjuryType.KNEE),
            "Steady state or interval cycling"
        ));

        exerciseRepository.save(createExercise(
            "Rowing", MuscleGroup.FULL_BODY, ExerciseType.CARDIO,
            Equipment.ROWING_MACHINE, DifficultyLevel.INTERMEDIATE, 1, 0, 1200, 11.0,
            Set.of(MuscleGroup.BACK, MuscleGroup.QUADRICEPS, MuscleGroup.CORE),
            Set.of(InjuryType.LOWER_BACK, InjuryType.KNEE),
            "Full body rowing motion"
        ));

        exerciseRepository.save(createExercise(
            "Burpees", MuscleGroup.FULL_BODY, ExerciseType.PLYOMETRIC,
            Equipment.NONE, DifficultyLevel.INTERMEDIATE, 3, 15, 0, 10.0,
            Set.of(MuscleGroup.CHEST, MuscleGroup.QUADRICEPS, MuscleGroup.CORE),
            Set.of(InjuryType.KNEE, InjuryType.LOWER_BACK, InjuryType.WRIST),
            "Jump down to plank, push-up, jump back up"
        ));

        exerciseRepository.save(createExercise(
            "Mountain Climbers", MuscleGroup.FULL_BODY, ExerciseType.CARDIO,
            Equipment.NONE, DifficultyLevel.BEGINNER, 3, 30, 0, 8.5,
            Set.of(MuscleGroup.CORE, MuscleGroup.SHOULDERS),
            Set.of(InjuryType.WRIST, InjuryType.KNEE),
            "Plank position, alternate bringing knees to chest"
        ));
    }

    private Exercise createExercise(String name, MuscleGroup primaryMuscle, ExerciseType type,
                                   Equipment equipment, DifficultyLevel difficulty, int sets, int reps,
                                   int durationSeconds, double caloriesPerMin, Set<MuscleGroup> secondaryMuscles,
                                   Set<InjuryType> contraindications, String instructions) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setPrimaryMuscle(primaryMuscle);
        exercise.setExerciseType(type);
        exercise.setEquipment(equipment);
        exercise.setDifficultyLevel(difficulty);
        exercise.setDefaultSets(sets);
        exercise.setDefaultReps(reps);
        exercise.setDefaultDurationSeconds(durationSeconds);
        exercise.setCaloriesBurnedPerMinute(caloriesPerMin);
        exercise.setSecondaryMuscles(secondaryMuscles);
        exercise.setContraindications(contraindications);
        exercise.setInstructions(instructions);
        exercise.setDescription(instructions);
        exercise.setCustom(false);
        return exercise;
    }
}
