package com.ufit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufit.model.ExerciseItem;
import com.ufit.repository.ExerciseItemRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Seeds the exercise_items table from the embedded exercises.json file
 * (sourced from https://github.com/yuhonas/free-exercise-db).
 * Only seeds if the table is empty.
 */
@Service
public class ExerciseItemSeeder {

    private final ExerciseItemRepository exerciseItemRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExerciseItemSeeder(ExerciseItemRepository exerciseItemRepo) {
        this.exerciseItemRepo = exerciseItemRepo;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        if (exerciseItemRepo.count() > 0) {
            System.out.println("[ExerciseItemSeeder] Exercise items already seeded (" + exerciseItemRepo.count() + " items). Skipping.");
            return;
        }

        System.out.println("[ExerciseItemSeeder] Seeding exercise items from exercises.json...");

        try {
            ClassPathResource resource = new ClassPathResource("exercises.json");
            InputStream is = resource.getInputStream();
            List<Map<String, Object>> exercises = objectMapper.readValue(is, new TypeReference<>() {});

            int count = 0;
            int skipped = 0;

            for (Map<String, Object> ex : exercises) {
                try {
                    String exerciseId = (String) ex.get("id");
                    String name = (String) ex.get("name");

                    if (exerciseId == null || name == null || exerciseId.isBlank() || name.isBlank()) {
                        skipped++;
                        continue;
                    }

                    // Skip if already exists (safety check)
                    if (exerciseItemRepo.findByExerciseId(exerciseId).isPresent()) {
                        skipped++;
                        continue;
                    }

                    ExerciseItem item = new ExerciseItem();
                    item.setExerciseId(exerciseId);
                    item.setName(name);
                    item.setForce((String) ex.get("force"));
                    item.setLevel((String) ex.get("level"));
                    item.setMechanic((String) ex.get("mechanic"));
                    item.setEquipment((String) ex.get("equipment"));
                    item.setCategory((String) ex.get("category"));

                    // Primary muscle - take first from array
                    @SuppressWarnings("unchecked")
                    List<String> primaryMuscles = (List<String>) ex.get("primaryMuscles");
                    if (primaryMuscles != null && !primaryMuscles.isEmpty()) {
                        item.setPrimaryMuscle(primaryMuscles.get(0));
                    }

                    // Secondary muscles - join into comma-separated string
                    @SuppressWarnings("unchecked")
                    List<String> secondaryMuscles = (List<String>) ex.get("secondaryMuscles");
                    if (secondaryMuscles != null && !secondaryMuscles.isEmpty()) {
                        item.setSecondaryMuscles(String.join(", ", secondaryMuscles));
                    }

                    // Instructions - store as JSON array string
                    @SuppressWarnings("unchecked")
                    List<String> instructions = (List<String>) ex.get("instructions");
                    if (instructions != null && !instructions.isEmpty()) {
                        item.setInstructions(objectMapper.writeValueAsString(instructions));
                    }

                    // Image path - take first image
                    @SuppressWarnings("unchecked")
                    List<String> images = (List<String>) ex.get("images");
                    if (images != null && !images.isEmpty()) {
                        item.setImagePath(images.get(0));
                    }

                    exerciseItemRepo.save(item);
                    count++;
                } catch (Exception e) {
                    skipped++;
                    System.err.println("[ExerciseItemSeeder] Error processing exercise: " + ex.get("name") + " - " + e.getMessage());
                }
            }

            System.out.println("[ExerciseItemSeeder] ✅ Seeded " + count + " exercise items! (" + skipped + " skipped)");

        } catch (Exception e) {
            System.err.println("[ExerciseItemSeeder] ❌ Failed to seed exercise items: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
