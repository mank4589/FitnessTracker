package com.ufit.controller;

import com.ufit.model.ExerciseLog;
import com.ufit.service.ExerciseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exercise")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    private LocalDate parseDateSafe(String date) {
        if (date != null && date.length() >= 10) {
            date = date.substring(0, 10);
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }

    // ═══════════════ SEARCH (ExerciseDB API) ═══════════════

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchExercises(
            @RequestParam String term,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity.ok(exerciseService.searchExercises(term, limit, offset));
    }

    // ═══════════════ EXERCISE DETAIL ═══════════════

    @GetMapping("/detail/{exerciseId}")
    public ResponseEntity<Map<String, Object>> getExerciseDetail(@PathVariable String exerciseId) {
        return ResponseEntity.ok(exerciseService.getExerciseDetail(exerciseId));
    }

    // ═══════════════ EXERCISE LOG CRUD (scoped by profileId) ═══════════════

    @PostMapping("/{profileId}/log")
    public ResponseEntity<ExerciseLog> logExercise(@PathVariable Long profileId, @RequestBody ExerciseLog entry) {
        return ResponseEntity.ok(exerciseService.logExercise(profileId, entry));
    }

    @GetMapping("/{profileId}/log/{date}")
    public ResponseEntity<List<ExerciseLog>> getLogsByDate(@PathVariable Long profileId, @PathVariable String date) {
        LocalDate d = parseDateSafe(date);
        return ResponseEntity.ok(exerciseService.getLogsByDate(profileId, d));
    }

    @DeleteMapping("/log/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        exerciseService.deleteLog(id);
        return ResponseEntity.ok().build();
    }

    // ═══════════════ DAILY SUMMARY (scoped by profileId) ═══════════════

    @GetMapping("/{profileId}/summary/{date}")
    public ResponseEntity<Map<String, Object>> getDailySummary(@PathVariable Long profileId, @PathVariable String date) {
        LocalDate d = parseDateSafe(date);
        return ResponseEntity.ok(exerciseService.getDailySummary(profileId, d));
    }
}
