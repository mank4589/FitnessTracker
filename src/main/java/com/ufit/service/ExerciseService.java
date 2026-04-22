package com.ufit.service;

import com.ufit.model.ExerciseItem;
import com.ufit.model.ExerciseLog;
import com.ufit.repository.ExerciseItemRepository;
import com.ufit.repository.ExerciseLogRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ExerciseService {

    private final ExerciseLogRepository logRepo;
    private final ExerciseItemRepository exerciseItemRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExerciseService(ExerciseLogRepository logRepo, ExerciseItemRepository exerciseItemRepo) {
        this.logRepo = logRepo;
        this.exerciseItemRepo = exerciseItemRepo;
    }

    // ═══════════════ LOCAL DB SEARCH ═══════════════

    public List<Map<String, Object>> searchExercises(String term, int limit, int offset) {
        List<ExerciseItem> results = exerciseItemRepo.searchByTerm(term.trim());

        // Apply offset and limit
        int fromIndex = Math.min(offset, results.size());
        int toIndex = Math.min(fromIndex + limit, results.size());
        List<ExerciseItem> paged = results.subList(fromIndex, toIndex);

        // Convert to the Map format the frontend expects
        List<Map<String, Object>> response = new ArrayList<>();
        for (ExerciseItem ex : paged) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("exerciseId", ex.getExerciseId());
            item.put("name", ex.getName());
            item.put("imageUrl", ex.getImageUrl());

            // Frontend expects arrays for muscles
            item.put("primaryMuscles", ex.getPrimaryMuscle() != null && !ex.getPrimaryMuscle().isEmpty()
                    ? List.of(ex.getPrimaryMuscle()) : List.of());
            
            // Parse secondary muscles
            List<String> secMuscles = new ArrayList<>();
            if (ex.getSecondaryMuscles() != null && !ex.getSecondaryMuscles().isBlank()) {
                for (String m : ex.getSecondaryMuscles().split(",")) {
                    secMuscles.add(m.trim());
                }
            }
            item.put("secondaryMuscles", secMuscles);
            
            item.put("equipment", ex.getEquipment() != null ? ex.getEquipment() : "");
            item.put("category", ex.getCategory() != null ? ex.getCategory() : "");
            item.put("level", ex.getLevel() != null ? ex.getLevel() : "");
            item.put("force", ex.getForce() != null ? ex.getForce() : "");
            item.put("mechanic", ex.getMechanic() != null ? ex.getMechanic() : "");

            response.add(item);
        }
        return response;
    }

    // ═══════════════ EXERCISE DETAIL (from local DB) ═══════════════

    public Map<String, Object> getExerciseDetail(String exerciseId) {
        Optional<ExerciseItem> opt = exerciseItemRepo.findByExerciseId(exerciseId);
        if (opt.isEmpty()) {
            return Map.of("error", "Exercise not found");
        }

        ExerciseItem ex = opt.get();
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("exerciseId", ex.getExerciseId());
        detail.put("name", ex.getName());
        detail.put("imageUrl", ex.getImageUrl());
        
        // Second image URL (if exists, pattern is {id}/1.jpg)
        if (ex.getImagePath() != null && !ex.getImagePath().isEmpty()) {
            String secondImage = ex.getImagePath().replace("/0.jpg", "/1.jpg");
            detail.put("secondImageUrl", "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/" + secondImage);
        }

        detail.put("force", ex.getForce() != null ? ex.getForce() : "");
        detail.put("level", ex.getLevel() != null ? ex.getLevel() : "");
        detail.put("mechanic", ex.getMechanic() != null ? ex.getMechanic() : "");
        detail.put("equipment", ex.getEquipment() != null ? ex.getEquipment() : "");
        detail.put("category", ex.getCategory() != null ? ex.getCategory() : "");

        // Primary muscles as array
        detail.put("primaryMuscles", ex.getPrimaryMuscle() != null && !ex.getPrimaryMuscle().isEmpty()
                ? List.of(ex.getPrimaryMuscle()) : List.of());

        // Parse secondary muscles
        List<String> secMuscles = new ArrayList<>();
        if (ex.getSecondaryMuscles() != null && !ex.getSecondaryMuscles().isBlank()) {
            for (String m : ex.getSecondaryMuscles().split(",")) {
                secMuscles.add(m.trim());
            }
        }
        detail.put("secondaryMuscles", secMuscles);

        // Parse instructions from JSON string
        List<String> instructions = new ArrayList<>();
        if (ex.getInstructions() != null && !ex.getInstructions().isBlank()) {
            try {
                instructions = objectMapper.readValue(ex.getInstructions(), new TypeReference<>() {});
            } catch (Exception e) {
                // If parsing fails, treat as single instruction
                instructions = List.of(ex.getInstructions());
            }
        }
        detail.put("instructions", instructions);

        return detail;
    }

    // ═══════════════ CRUD ═══════════════

    public ExerciseLog logExercise(ExerciseLog entry) {
        if (entry.getLogDate() == null) {
            entry.setLogDate(LocalDate.now());
        }
        return logRepo.save(entry);
    }

    public List<ExerciseLog> getLogsByDate(LocalDate date) {
        return logRepo.findByLogDateOrderByIdDesc(date);
    }

    public void deleteLog(Long id) {
        logRepo.deleteById(id);
    }

    public Map<String, Object> getDailySummary(LocalDate date) {
        List<ExerciseLog> logs = getLogsByDate(date);

        int totalSets = logs.stream().mapToInt(ExerciseLog::getSets).sum();
        int totalReps = logs.stream().mapToInt(ExerciseLog::getReps).sum();
        int totalDuration = logs.stream().mapToInt(ExerciseLog::getDurationMinutes).sum();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("exerciseCount", logs.size());
        summary.put("totalSets", totalSets);
        summary.put("totalReps", totalReps);
        summary.put("totalDuration", totalDuration);
        summary.put("logs", logs);
        return summary;
    }
}
