package com.ufit.controller;

import com.ufit.model.*;
import com.ufit.service.FoodDatabaseSeeder;
import com.ufit.service.MealPlannerService;
import com.ufit.service.UserProfileService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/meal-plans")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"}, allowCredentials = "false")
public class MealPlanController {

    private final MealPlannerService plannerService;
    private final UserProfileService profileService;
    private final FoodDatabaseSeeder foodDatabaseSeeder;

    public MealPlanController(MealPlannerService plannerService,
                             UserProfileService profileService,
                             FoodDatabaseSeeder foodDatabaseSeeder) {
        this.plannerService = plannerService;
        this.profileService = profileService;
        this.foodDatabaseSeeder = foodDatabaseSeeder;
    }

    // ═══════════════ PLAN GENERATION ═══════════════

    @PostMapping("/generate/{profileId}")
    public ResponseEntity<WeeklyMealPlan> generateMealPlan(@PathVariable Long profileId) {
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) return ResponseEntity.notFound().build();
        try {
            WeeklyMealPlan plan = plannerService.generatePlanForCurrentWeek(profile.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(plan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/{profileId}/week")
    public ResponseEntity<WeeklyMealPlan> generateMealPlanForWeek(
            @PathVariable Long profileId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) return ResponseEntity.notFound().build();
        try {
            WeeklyMealPlan plan = plannerService.generatePlan(profile.get(), weekStartDate);
            return ResponseEntity.status(HttpStatus.CREATED).body(plan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/custom/{profileId}")
    public ResponseEntity<WeeklyMealPlan> createCustomMealPlan(
            @PathVariable Long profileId,
            @RequestBody WeeklyMealPlan plan) {
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) return ResponseEntity.notFound().build();
        try {
            WeeklyMealPlan created = plannerService.createCustomMealPlan(profile.get(), plan);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ═══════════════ PLAN RETRIEVAL ═══════════════

    @GetMapping("/latest/{profileId}")
    public ResponseEntity<WeeklyMealPlan> getLatestPlan(@PathVariable Long profileId) {
        Optional<WeeklyMealPlan> plan = plannerService.getLatestPlan(profileId);
        return plan.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<WeeklyMealPlan>> getAllPlansForProfile(@PathVariable Long profileId) {
        Optional<UserProfile> profile = profileService.findById(profileId);
        if (profile.isEmpty()) return ResponseEntity.notFound().build();
        List<WeeklyMealPlan> plans = plannerService.getAllPlans(profile.get());
        return ResponseEntity.ok(plans);
    }

    // ═══════════════ MEAL SLOT EDITING ═══════════════

    @PutMapping("/{planId}/days/{dayId}/slot/{slot}")
    public ResponseEntity<WeeklyMealPlan> updateMealSlot(
            @PathVariable Long planId, @PathVariable Long dayId,
            @PathVariable String slot, @RequestBody List<Long> foodIds) {
        try {
            return ResponseEntity.ok(plannerService.updateMealSlot(planId, dayId, slot, foodIds));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{planId}/days/{dayId}/slot/{slot}/add/{foodId}")
    public ResponseEntity<WeeklyMealPlan> addFoodToSlot(
            @PathVariable Long planId, @PathVariable Long dayId,
            @PathVariable String slot, @PathVariable Long foodId) {
        try {
            return ResponseEntity.ok(plannerService.addFoodToSlot(planId, dayId, slot, foodId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{planId}/days/{dayId}/slot/{slot}/remove/{foodId}")
    public ResponseEntity<WeeklyMealPlan> removeFoodFromSlot(
            @PathVariable Long planId, @PathVariable Long dayId,
            @PathVariable String slot, @PathVariable Long foodId) {
        try {
            return ResponseEntity.ok(plannerService.removeFoodFromSlot(planId, dayId, slot, foodId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ═══════════════ MEALS LIBRARY ═══════════════

    @GetMapping("/meals")
    public ResponseEntity<List<FoodItem>> getAllMeals() {
        return ResponseEntity.ok(plannerService.getAllMeals());
    }

    @PostMapping("/meals/custom")
    public ResponseEntity<FoodItem> addCustomMeal(@RequestBody FoodItem meal) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(plannerService.addCustomMeal(meal));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/meals/custom/{mealId}")
    public ResponseEntity<Void> deleteCustomMeal(@PathVariable Long mealId) {
        return plannerService.deleteCustomMeal(mealId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> deletePlan(@PathVariable Long planId) {
        try {
            plannerService.deletePlan(planId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ═══════════════ DATABASE MANAGEMENT ═══════════════

    @PostMapping("/reseed-classifications")
    public ResponseEntity<Map<String, String>> reseedClassifications() {
        foodDatabaseSeeder.reseedClassifications();
        return ResponseEntity.ok(Map.of("status", "Classification update started"));
    }
}
