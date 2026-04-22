package com.ufit.recommendation;

import com.ufit.model.FoodItem;
import com.ufit.model.UserProfile;
import com.ufit.model.enums.CuisinePreference;
import com.ufit.model.enums.DietaryPreference;
import com.ufit.model.enums.DietaryRestriction;
import com.ufit.model.enums.MealType;
import com.ufit.repository.FoodItemRepository;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Redesigned MealMatcher that works with ALL food items in the database,
 * using the category and dietaryCategory fields instead of the isMeal flag.
 */
@Component
public class MealMatcher {

    private final FoodItemRepository foodItemRepository;

    // Category-to-meal-slot mappings
    private static final Set<String> BREAKFAST_CATEGORIES = Set.of(
        "breakfast", "grains", "dairy", "fruits"
    );
    private static final Set<String> LUNCH_DINNER_CATEGORIES = Set.of(
        "indian", "chinese", "japanese", "thai", "italian", "mexican",
        "meat", "seafood", "soups", "legumes", "grains", "vegetables"
    );
    private static final Set<String> SNACK_CATEGORIES = Set.of(
        "snacks", "fruits", "dairy", "desserts", "beverages"
    );

    // Keywords for breakfast foods that might be in other categories
    private static final Set<String> BREAKFAST_KEYWORDS = Set.of(
        "oatmeal", "pancake", "waffle", "toast", "cereal", "granola",
        "egg", "omelette", "scrambled", "yogurt", "smoothie", "muffin",
        "bagel", "croissant", "breakfast", "poha", "upma", "idli",
        "dosa", "paratha", "oats", "milk", "banana", "apple"
    );

    // Map CuisinePreference enum to category strings in the DB
    private static final Map<CuisinePreference, List<String>> CUISINE_TO_CATEGORIES = Map.of(
        CuisinePreference.INDIAN, List.of("indian"),
        CuisinePreference.CHINESE, List.of("chinese"),
        CuisinePreference.JAPANESE, List.of("japanese"),
        CuisinePreference.THAI, List.of("thai"),
        CuisinePreference.ITALIAN, List.of("italian"),
        CuisinePreference.MEXICAN, List.of("mexican"),
        CuisinePreference.AMERICAN, List.of("breakfast", "snacks", "meat", "grains", "dairy"),
        CuisinePreference.MEDITERRANEAN, List.of("seafood", "vegetables", "legumes", "grains"),
        CuisinePreference.KOREAN, List.of("korean"),
        CuisinePreference.ALL, List.of("indian", "chinese", "japanese", "thai", "italian", "mexican",
            "meat", "seafood", "grains", "dairy", "vegetables", "fruits", "snacks", "desserts",
            "beverages", "legumes", "condiments", "soups", "breakfast", "other")
    );

    public MealMatcher(FoodItemRepository foodItemRepository) {
        this.foodItemRepository = foodItemRepository;
    }

    /**
     * Find candidate foods for a specific meal slot, filtered by user preferences.
     * Uses SERVING-BASED calories for matching (not per-100g).
     */
    public List<FoodItem> findCandidates(UserProfile profile, MealType mealSlot, double targetCalories, double tolerance) {
        // Query a wider calorie range from DB (since DB stores per-100g values)
        // Then filter by serving-adjusted calories in Java
        List<String> searchCategories = getSearchCategories(profile, mealSlot);

        List<FoodItem> candidates;
        if (searchCategories.isEmpty()) {
            candidates = foodItemRepository.findAll();
        } else {
            candidates = foodItemRepository.findByCategories(searchCategories);
        }

        double minCal = targetCalories * (1 - tolerance);
        double maxCal = targetCalories * (1 + tolerance);

        return candidates.stream()
            .filter(food -> isCompatibleWithDiet(food, profile.getDietaryPreference()))
            .filter(food -> isSuitableForSlot(food, mealSlot))
            .filter(food -> {
                double servingCals = food.getServingCalories();
                return servingCals >= minCal && servingCals <= maxCal;
            })
            .sorted((a, b) -> Double.compare(scoreMeal(b, profile, targetCalories), scoreMeal(a, profile, targetCalories)))
            .collect(Collectors.toList());
    }

    /**
     * Fallback: find any compatible foods for a meal slot (no calorie filter).
     */
    public List<FoodItem> findCandidatesFallback(UserProfile profile, MealType mealSlot) {
        List<String> searchCategories = getSearchCategories(profile, mealSlot);
        List<FoodItem> candidates;

        if (searchCategories.isEmpty()) {
            candidates = foodItemRepository.findAll();
        } else {
            candidates = foodItemRepository.findByCategories(searchCategories);
        }

        return candidates.stream()
            .filter(food -> isCompatibleWithDiet(food, profile.getDietaryPreference()))
            .filter(food -> isSuitableForSlot(food, mealSlot))
            .sorted((a, b) -> Double.compare(scoreMeal(b, profile, 400), scoreMeal(a, profile, 400)))
            .collect(Collectors.toList());
    }

    /**
     * Find side items from specific categories within a calorie range.
     * Sides are smaller complementary items (fruit, curd, bread, etc.).
     */
    public List<FoodItem> findSideCandidates(UserProfile profile, List<String> sideCategories, double minCal, double maxCal) {
        List<FoodItem> candidates = foodItemRepository.findByCategories(
            sideCategories.stream().map(String::toLowerCase).collect(Collectors.toList())
        );

        return candidates.stream()
            .filter(food -> isCompatibleWithDiet(food, profile.getDietaryPreference()))
            .filter(food -> !food.isMeal()) // Prefer non-meal items as sides (ingredients like fruit, curd, etc.)
            .filter(food -> {
                double servingCals = food.getServingCalories();
                return servingCals >= minCal && servingCals <= maxCal;
            })
            .sorted((a, b) -> Double.compare(b.getServingProtein(), a.getServingProtein())) // Prefer higher protein sides
            .collect(Collectors.toList());
    }

    /**
     * Determine which DB categories to search based on meal slot + user cuisine preferences.
     */
    private List<String> getSearchCategories(UserProfile profile, MealType mealSlot) {
        Set<CuisinePreference> cuisinePrefs = profile.getCuisinePreferences();

        // Get the base categories for this meal slot
        Set<String> slotCategories;
        switch (mealSlot) {
            case BREAKFAST -> slotCategories = new HashSet<>(BREAKFAST_CATEGORIES);
            case LUNCH, DINNER -> slotCategories = new HashSet<>(LUNCH_DINNER_CATEGORIES);
            case SNACK -> slotCategories = new HashSet<>(SNACK_CATEGORIES);
            default -> slotCategories = new HashSet<>();
        }

        // If user has cuisine preferences (and not ALL), intersect with those
        if (cuisinePrefs != null && !cuisinePrefs.isEmpty() && !cuisinePrefs.contains(CuisinePreference.ALL)) {
            Set<String> userCuisineCategories = new HashSet<>();
            for (CuisinePreference cp : cuisinePrefs) {
                List<String> cats = CUISINE_TO_CATEGORIES.get(cp);
                if (cats != null) userCuisineCategories.addAll(cats);
            }
            // Always include universal categories for meal slots
            userCuisineCategories.addAll(Set.of("fruits", "vegetables", "dairy", "grains", "snacks", "breakfast"));
            
            // Use union of slot categories and user cuisine categories
            slotCategories.addAll(userCuisineCategories);
        }

        return new ArrayList<>(slotCategories);
    }

    /**
     * Check if a food is suitable for a particular meal slot using the mealType field.
     * This is the primary filter that prevents dal/curry from appearing as snacks.
     */
    private boolean isSuitableForSlot(FoodItem food, MealType mealSlot) {
        MealType foodType = food.getMealType();
        
        // Items without a mealType (raw ingredients) are never suitable as mains
        if (foodType == null) return false;
        
        switch (mealSlot) {
            case BREAKFAST -> {
                // Breakfast slot: accept BREAKFAST items only
                return foodType == MealType.BREAKFAST;
            }
            case LUNCH, DINNER -> {
                // Lunch/Dinner slot: accept LUNCH items (all prepared main dishes)
                // Also accept BREAKFAST items as fallback (parathas work for dinner too)
                return foodType == MealType.LUNCH || foodType == MealType.DINNER;
            }
            case SNACK -> {
                // Snack slot: ONLY accept SNACK items
                return foodType == MealType.SNACK;
            }
        }
        return false;
    }

    /**
     * Check if a food is compatible with the user's dietary preference.
     * Uses the dietaryCategory field (VEGAN, VEGETARIAN, PESCATARIAN, NON_VEGETARIAN/NONE).
     */
    private boolean isCompatibleWithDiet(FoodItem food, DietaryPreference preference) {
        if (preference == null || preference == DietaryPreference.NONE) {
            return true; // No restriction
        }

        DietaryPreference foodDiet = food.getDietaryCategory();
        if (foodDiet == null) {
            foodDiet = DietaryPreference.NONE; // Unknown = assume non-restricted
        }

        return switch (preference) {
            case VEGAN -> foodDiet == DietaryPreference.VEGAN;
            case VEGETARIAN -> foodDiet == DietaryPreference.VEGAN || 
                               foodDiet == DietaryPreference.VEGETARIAN;
            case PESCATARIAN -> foodDiet == DietaryPreference.VEGAN || 
                                foodDiet == DietaryPreference.VEGETARIAN ||
                                foodDiet == DietaryPreference.PESCATARIAN;
            case KETO -> food.getCarbs() < 20 && food.getFat() > food.getProtein();
            case LOW_CARB -> food.getCarbs() < 30;
            case HIGH_PROTEIN -> food.getProtein() >= 15;
            case PALEO, MEDITERRANEAN -> true; // Broad diets, let cuisine filter handle it
            default -> true;
        };
    }

    /**
     * Score a food item based on how well it fits the user's macro targets and preferences.
     */
    private double scoreMeal(FoodItem food, UserProfile profile, double targetCalories) {
        double score = 50.0;

        // 1. Calorie closeness (closer to target = better)
        double calorieDiff = Math.abs(food.getCalories() - targetCalories);
        score -= calorieDiff * 0.05; // Small penalty for being off-target

        // 2. Protein bonus (important for fitness goals)
        if (food.getProtein() >= 20) score += 15;
        else if (food.getProtein() >= 10) score += 8;

        // 3. Fiber bonus
        if (food.getFiber() >= 5) score += 8;

        // 4. Fitness goal adjustments
        if (profile.getFitnessGoal() != null) {
            switch (profile.getFitnessGoal()) {
                case GAIN_MUSCLE -> {
                    score += food.getProtein() * 0.5;
                    score += food.getCalories() * 0.005;
                }
                case LOSE_FAT -> {
                    score += food.getProtein() * 0.3;
                    score -= food.getCalories() * 0.005;
                    score += food.getFiber() * 2;
                    score -= food.getSugar() * 0.3;
                }
                case RECOMP -> {
                    score += food.getProtein() * 0.5;
                    score -= Math.abs(food.getCalories() - targetCalories) * 0.02;
                }
                case MAINTAIN -> {
                    score += food.getProtein() * 0.2;
                    score += food.getFiber() * 1;
                }
            }
        }

        // 5. Cuisine preference bonus
        Set<CuisinePreference> cuisinePrefs = profile.getCuisinePreferences();
        if (cuisinePrefs != null && !cuisinePrefs.isEmpty() && !cuisinePrefs.contains(CuisinePreference.ALL)) {
            String foodCat = food.getCategory() != null ? food.getCategory().toLowerCase() : "";
            boolean isPreferredCuisine = cuisinePrefs.stream().anyMatch(cp -> {
                List<String> cats = CUISINE_TO_CATEGORIES.get(cp);
                return cats != null && cats.contains(foodCat);
            });
            if (isPreferredCuisine) score += 20;
        }

        return Math.max(score, 0);
    }

    /**
     * Pick a random item from the top candidates, avoiding already-used items.
     */
    public FoodItem pickRandom(List<FoodItem> candidates, Set<Long> usedIds) {
        // Take top 5 candidates not yet used
        List<FoodItem> available = candidates.stream()
            .filter(f -> !usedIds.contains(f.getId()))
            .limit(5)
            .collect(Collectors.toList());

        if (available.isEmpty()) {
            // If all used, allow repeats from top candidates
            available = candidates.stream().limit(3).collect(Collectors.toList());
        }

        if (available.isEmpty()) return null;

        Collections.shuffle(available);
        return available.get(0);
    }
}
