package com.ufit.controller;

import com.ufit.model.DailyGoal;
import com.ufit.model.FoodLog;
import com.ufit.model.WaterLog;
import com.ufit.service.CalorieService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import com.ufit.model.enums.MedicalCondition;

@RestController
@RequestMapping("/api/food")
public class CalorieController {

    private final CalorieService calorieService;

    public CalorieController(CalorieService calorieService) {
        this.calorieService = calorieService;
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

    // ═══════════════ FOOD SUGGEST (Auto-complete) ═══════════════

    @GetMapping("/suggest")
    public ResponseEntity<List<Map<String, Object>>> suggestFood(
            @RequestParam String q,
            @RequestParam(required = false) String conditions) {
        Set<MedicalCondition> medConditions = parseConditions(conditions);
        return ResponseEntity.ok(calorieService.suggestFood(q, medConditions));
    }

    private Set<MedicalCondition> parseConditions(String conditions) {
        if (conditions == null || conditions.isBlank()) return null;
        Set<MedicalCondition> result = new HashSet<>();
        for (String c : conditions.split(",")) {
            try { result.add(MedicalCondition.valueOf(c.trim().toUpperCase())); } catch (Exception ignored) {}
        }
        return result.isEmpty() ? null : result;
    }

    // ═══════════════ FOOD SEARCH ═══════════════

    @GetMapping("/search")
    public ResponseEntity<List<Map<String, Object>>> searchFood(@RequestParam String query) {
        return ResponseEntity.ok(calorieService.searchFood(query));
    }

    // ═══════════════ FOOD LOG (scoped by profileId) ═══════════════

    @PostMapping("/{profileId}/log")
    public ResponseEntity<FoodLog> logFood(@PathVariable Long profileId, @RequestBody FoodLog entry) {
        return ResponseEntity.ok(calorieService.logFood(profileId, entry));
    }

    @GetMapping("/{profileId}/log/{date}")
    public ResponseEntity<List<FoodLog>> getLogByDate(@PathVariable Long profileId, @PathVariable String date) {
        LocalDate d = parseDateSafe(date);
        return ResponseEntity.ok(calorieService.getLogByDate(profileId, d));
    }

    @GetMapping("/{profileId}/log/{date}/{mealType}")
    public ResponseEntity<List<FoodLog>> getLogByDateAndMeal(
            @PathVariable Long profileId, @PathVariable String date, @PathVariable String mealType) {
        LocalDate d = parseDateSafe(date);
        return ResponseEntity.ok(calorieService.getLogByDateAndMeal(profileId, d, mealType));
    }

    @DeleteMapping("/log/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        calorieService.deleteLogEntry(id);
        return ResponseEntity.ok().build();
    }

    // ═══════════════ DAILY SUMMARY (scoped by profileId) ═══════════════

    @GetMapping("/{profileId}/summary/{date}")
    public ResponseEntity<Map<String, Object>> getDailySummary(@PathVariable Long profileId, @PathVariable String date) {
        LocalDate d = parseDateSafe(date);
        return ResponseEntity.ok(calorieService.getDailySummary(profileId, d));
    }

    @GetMapping("/{profileId}/weekly/{date}")
    public ResponseEntity<List<Map<String, Object>>> getWeeklySummary(@PathVariable Long profileId, @PathVariable String date) {
        LocalDate d = parseDateSafe(date);
        return ResponseEntity.ok(calorieService.getWeeklySummary(profileId, d));
    }

    // ═══════════════ DAILY GOAL (scoped by profileId) ═══════════════

    @PostMapping("/{profileId}/goal")
    public ResponseEntity<DailyGoal> setGoal(@PathVariable Long profileId, @RequestBody Map<String, Object> body) {
        DailyGoal goal = new DailyGoal();
        goal.setProfileId(profileId);
        if (body.containsKey("goalDate")) {
            goal.setGoalDate(parseDateSafe((String) body.get("goalDate")));
        }
        if (body.containsKey("calorieGoal")) {
            goal.setCalorieGoal(((Number) body.get("calorieGoal")).doubleValue());
        }
        if (body.containsKey("proteinGoal")) {
            goal.setProteinGoal(((Number) body.get("proteinGoal")).doubleValue());
        }
        if (body.containsKey("carbsGoal")) {
            goal.setCarbsGoal(((Number) body.get("carbsGoal")).doubleValue());
        }
        if (body.containsKey("fatGoal")) {
            goal.setFatGoal(((Number) body.get("fatGoal")).doubleValue());
        }
        if (body.containsKey("waterGoal")) {
            goal.setWaterGoal(((Number) body.get("waterGoal")).doubleValue());
        }
        return ResponseEntity.ok(calorieService.setGoal(profileId, goal));
    }

    @GetMapping("/{profileId}/goal/{date}")
    public ResponseEntity<DailyGoal> getGoal(@PathVariable Long profileId, @PathVariable String date) {
        LocalDate d = parseDateSafe(date);
        return ResponseEntity.ok(calorieService.getGoal(profileId, d));
    }

    // ═══════════════ WATER INTAKE (scoped by profileId) ═══════════════

    @PostMapping("/{profileId}/water")
    public ResponseEntity<WaterLog> logWater(@PathVariable Long profileId, @RequestBody Map<String, Object> body) {
        LocalDate date = body.containsKey("date") ?
            parseDateSafe((String) body.get("date")) : LocalDate.now();
        double amount = body.containsKey("amountMl") ?
            ((Number) body.get("amountMl")).doubleValue() : 250;
        return ResponseEntity.ok(calorieService.logWater(profileId, date, amount));
    }

    @GetMapping("/{profileId}/water/{date}")
    public ResponseEntity<Map<String, Object>> getWaterSummary(@PathVariable Long profileId, @PathVariable String date) {
        LocalDate d = parseDateSafe(date);
        return ResponseEntity.ok(calorieService.getWaterSummary(profileId, d));
    }

    @DeleteMapping("/water/{id}")
    public ResponseEntity<Void> deleteWaterLog(@PathVariable Long id) {
        calorieService.deleteWaterLog(id);
        return ResponseEntity.ok().build();
    }

    // ═══════════════ DB STATUS ═══════════════

    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> getDbStatus() {
        return ResponseEntity.ok(Map.of(
            "foodCount", calorieService.getLocalFoodCount(),
            "seedingComplete", calorieService.getSeeder().isSeedingComplete(),
            "status", calorieService.getSeeder().getCurrentStatus()
        ));
    }
}
