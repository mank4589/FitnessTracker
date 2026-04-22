package com.ufit.service;

import com.ufit.model.*;
import com.ufit.recommendation.MealPlanGenerator;
import com.ufit.repository.FoodItemRepository;
import com.ufit.repository.WeeklyMealPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MealPlannerService {

    private final MealPlanGenerator planGenerator;
    private final WeeklyMealPlanRepository planRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserProfileService profileService;

    public MealPlannerService(MealPlanGenerator planGenerator, 
                              WeeklyMealPlanRepository planRepository,
                              FoodItemRepository foodItemRepository,
                              UserProfileService profileService) {
        this.planGenerator = planGenerator;
        this.planRepository = planRepository;
        this.foodItemRepository = foodItemRepository;
        this.profileService = profileService;
    }

    /**
     * Generate a new weekly meal plan for the current week
     */
    @Transactional
    public WeeklyMealPlan generatePlanForCurrentWeek(UserProfile profile) {
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return generatePlan(profile, weekStart);
    }

    /**
     * Generate a new weekly meal plan for a specific week
     */
    @Transactional
    public WeeklyMealPlan generatePlan(UserProfile profile, LocalDate weekStartDate) {
        // Ensure we link latest health data
        profileService.linkLatestHealthSnapshot(profile.getId());
        
        // Refresh profile to get updated health snapshot
        profile = profileService.findById(profile.getId()).orElse(profile);
        
        // Check if plan already exists for this week
        Optional<WeeklyMealPlan> existing = planRepository.findByUserProfileAndWeekStartDate(profile, weekStartDate);
        if (existing.isPresent()) {
            // Delete old plan to regenerate
            planRepository.delete(existing.get());
        }

        // Generate new plan
        WeeklyMealPlan plan = planGenerator.generateWeeklyPlan(profile, weekStartDate);
        return planRepository.save(plan);
    }

    /**
     * Create a custom weekly meal plan built by the user
     */
    @Transactional
    public WeeklyMealPlan createCustomMealPlan(UserProfile profile, WeeklyMealPlan clientPlan) {
        LocalDate weekStart = clientPlan.getWeekStartDate() != null
                ? clientPlan.getWeekStartDate()
                : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        WeeklyMealPlan plan = new WeeklyMealPlan(profile, weekStart);
        plan.setTargetCalories(clientPlan.getTargetCalories());
        plan.setTargetProtein(clientPlan.getTargetProtein());
        plan.setTargetCarbs(clientPlan.getTargetCarbs());
        plan.setTargetFat(clientPlan.getTargetFat());

        for (DailyMealPlan clientDay : clientPlan.getDailyPlans()) {
            DailyMealPlan day = new DailyMealPlan(clientDay.getDayOfWeek());

            // Resolve food IDs to actual FoodItem entities
            day.setBreakfastItems(resolveFoodItems(clientDay.getBreakfastItems()));
            day.setLunchItems(resolveFoodItems(clientDay.getLunchItems()));
            day.setDinnerItems(resolveFoodItems(clientDay.getDinnerItems()));
            day.setSnacks(resolveFoodItems(clientDay.getSnacks()));
            day.calculateTotals();

            plan.addDailyPlan(day);
        }

        return planRepository.save(plan);
    }

    /**
     * Update a specific meal slot in a daily plan
     */
    @Transactional
    public WeeklyMealPlan updateMealSlot(Long planId, Long dayId, String slot, List<Long> foodIds) {
        WeeklyMealPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + planId));

        DailyMealPlan day = plan.getDailyPlans().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Day not found: " + dayId));

        List<FoodItem> foods = resolveFoodIds(foodIds);
        switch (slot.toLowerCase()) {
            case "breakfast" -> day.setBreakfastItems(foods);
            case "lunch" -> day.setLunchItems(foods);
            case "dinner" -> day.setDinnerItems(foods);
            case "snacks", "snack" -> day.setSnacks(foods);
        }
        day.calculateTotals();

        return planRepository.save(plan);
    }

    /**
     * Add a food item to a meal slot
     */
    @Transactional
    public WeeklyMealPlan addFoodToSlot(Long planId, Long dayId, String slot, Long foodId) {
        WeeklyMealPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        DailyMealPlan day = plan.getDailyPlans().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Day not found"));

        FoodItem food = foodItemRepository.findById(foodId)
                .orElseThrow(() -> new IllegalArgumentException("Food not found"));

        switch (slot.toLowerCase()) {
            case "breakfast" -> day.getBreakfastItems().add(food);
            case "lunch" -> day.getLunchItems().add(food);
            case "dinner" -> day.getDinnerItems().add(food);
            case "snacks", "snack" -> day.getSnacks().add(food);
        }
        day.calculateTotals();

        return planRepository.save(plan);
    }

    /**
     * Remove a food item from a meal slot
     */
    @Transactional
    public WeeklyMealPlan removeFoodFromSlot(Long planId, Long dayId, String slot, Long foodId) {
        WeeklyMealPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        DailyMealPlan day = plan.getDailyPlans().stream()
                .filter(d -> d.getId().equals(dayId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Day not found"));

        switch (slot.toLowerCase()) {
            case "breakfast" -> day.getBreakfastItems().removeIf(f -> f.getId().equals(foodId));
            case "lunch" -> day.getLunchItems().removeIf(f -> f.getId().equals(foodId));
            case "dinner" -> day.getDinnerItems().removeIf(f -> f.getId().equals(foodId));
            case "snacks", "snack" -> day.getSnacks().removeIf(f -> f.getId().equals(foodId));
        }
        day.calculateTotals();

        return planRepository.save(plan);
    }

    /** Resolve a list of food item IDs to entities */
    private List<FoodItem> resolveFoodIds(List<Long> foodIds) {
        if (foodIds == null || foodIds.isEmpty()) return new ArrayList<>();
        List<FoodItem> result = new ArrayList<>();
        for (Long id : foodIds) {
            foodItemRepository.findById(id).ifPresent(result::add);
        }
        return result;
    }

    /** Resolve food items that may only have IDs populated (from client JSON) */
    private List<FoodItem> resolveFoodItems(List<FoodItem> clientItems) {
        if (clientItems == null || clientItems.isEmpty()) return new ArrayList<>();
        List<FoodItem> result = new ArrayList<>();
        for (FoodItem item : clientItems) {
            if (item.getId() != null) {
                foodItemRepository.findById(item.getId()).ifPresent(result::add);
            }
        }
        return result;
    }

    /**
     * Get the latest meal plan for a user
     */
    public Optional<WeeklyMealPlan> getLatestPlan(Long profileId) {
        return planRepository.findLatestByUserProfileId(profileId);
    }

    /**
     * Get all meal plans for a user
     */
    public List<WeeklyMealPlan> getAllPlans(UserProfile profile) {
        return planRepository.findByUserProfileOrderByDateDesc(profile);
    }

    /**
     * Find a plan by ID
     */
    public Optional<WeeklyMealPlan> findById(Long planId) {
        return planRepository.findById(planId);
    }

    /**
     * Swap a meal in a daily plan
     */
    @Transactional
    public DailyMealPlan swapMeal(DailyMealPlan dailyPlan, String mealSlot, Long newMealId) {
        Optional<FoodItem> newMealOpt = foodItemRepository.findById(newMealId);
        if (newMealOpt.isEmpty() || !newMealOpt.get().isMeal()) {
            return dailyPlan;
        }

        FoodItem newMeal = newMealOpt.get();
        switch (mealSlot.toLowerCase()) {
            case "breakfast" -> dailyPlan.setBreakfast(newMeal);
            case "lunch" -> dailyPlan.setLunch(newMeal);
            case "dinner" -> dailyPlan.setDinner(newMeal);
        }
        
        dailyPlan.calculateTotals();
        return dailyPlan;
    }

    /**
     * Get all available meals
     */
    public List<FoodItem> getAllMeals() {
        return foodItemRepository.findAll().stream().filter(FoodItem::isMeal).toList();
    }

    /**
     * Add a custom meal
     */
    @Transactional
    public FoodItem addCustomMeal(FoodItem meal) {
        meal.setMeal(true);
        meal.setCustom(true);
        return foodItemRepository.save(meal);
    }

    /**
     * Delete a custom meal (only if it's custom)
     */
    @Transactional
    public boolean deleteCustomMeal(Long mealId) {
        Optional<FoodItem> mealOpt = foodItemRepository.findById(mealId);
        if (mealOpt.isPresent() && mealOpt.get().isCustom()) {
            foodItemRepository.deleteById(mealId);
            return true;
        }
        return false;
    }

    /**
     * Delete a meal plan
     */
    @Transactional
    public void deletePlan(Long planId) {
        planRepository.deleteById(planId);
    }
}
