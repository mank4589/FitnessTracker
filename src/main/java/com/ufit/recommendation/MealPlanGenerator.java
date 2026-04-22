package com.ufit.recommendation;

import com.ufit.model.*;
import com.ufit.model.enums.DietaryPreference;
import com.ufit.model.enums.FitnessGoal;
import com.ufit.model.enums.CuisinePreference;
import com.ufit.model.enums.MealType;
import com.ufit.model.enums.MedicalCondition;
import com.ufit.logic.HealthCalculator;
import com.ufit.repository.FoodItemRepository;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Strict budget-aware meal plan generator.
 * 
 * Each meal assembly tracks a running calorie budget and STOPS adding items
 * when the budget is filled. After assembly, servings are scaled to exactly
 * hit the target. NEVER exceeds the daily calorie limit.
 * 
 * Calorie split: Breakfast 30% | Lunch 35% | Snack 10% | Dinner 25%
 */
@Component
public class MealPlanGenerator {

    private final FoodItemRepository foodRepo;
    private final Random random = new Random();

    private static final double BREAKFAST_RATIO = 0.30;
    private static final double LUNCH_RATIO     = 0.35;
    private static final double SNACK_RATIO     = 0.10;
    private static final double DINNER_RATIO    = 0.25;

    public MealPlanGenerator(FoodItemRepository foodRepo) {
        this.foodRepo = foodRepo;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private MealMatcher mealMatcher;

    // ==================== FOOD POOLS ====================

    private static class FoodPools {
        // Main dishes (curry, dal, sabzi, egg, meat — used for lunch/dinner)
        List<FoodItem> curries = new ArrayList<>();      // curry type
        List<FoodItem> dals = new ArrayList<>();         // dal type
        List<FoodItem> sabzis = new ArrayList<>();       // sabzi type
        List<FoodItem> eggDishes = new ArrayList<>();    // egg_dish type
        List<FoodItem> meatDishes = new ArrayList<>();   // meat_dish type
        // Carb bases
        List<FoodItem> staples = new ArrayList<>();      // staple (roti/rice)
        List<FoodItem> riceDishes = new ArrayList<>();   // rice_dish (biryani etc)
        List<FoodItem> pastas = new ArrayList<>();       // pasta
        List<FoodItem> completeMeals = new ArrayList<>();// complete_meal
        // Other
        List<FoodItem> desserts = new ArrayList<>();     // dessert
        List<FoodItem> breakfastItems = new ArrayList<>();// breakfast_item
        List<FoodItem> snacks = new ArrayList<>();       // snack
        List<FoodItem> fruits = new ArrayList<>();       // fruit
        List<FoodItem> nuts = new ArrayList<>();         // nut
        // Drinks — sub-categorized by foodGroup
        List<FoodItem> milks = new ArrayList<>();
        List<FoodItem> buttermilkLassi = new ArrayList<>();
        List<FoodItem> teaCoffee = new ArrayList<>();
        List<FoodItem> otherDrinks = new ArrayList<>();
        // Sides — sub-categorized
        List<FoodItem> salads = new ArrayList<>();
        List<FoodItem> otherSides = new ArrayList<>();
    }

    private FoodPools loadFoodPools(DietaryPreference diet, Set<String> allowedCuisines, Set<MedicalCondition> medicalConditions) {
        FoodPools pools = new FoodPools();
        for (FoodItem item : foodRepo.findAll()) {
            if (!isDietCompatible(item, diet)) continue;
            String type = item.getDishType();
            if (type == null || type.isEmpty() || "ingredient".equals(type) || "condiment".equals(type)) continue;
            
            // Cuisine filter
            if (allowedCuisines != null && !allowedCuisines.isEmpty()) {
                String cuisine = item.getCuisine();
                if (cuisine != null && !"UNIVERSAL".equals(cuisine) && !allowedCuisines.contains(cuisine)) continue;
            }

            // Medical condition filter — exclude foods that score below threshold
            if (medicalConditions != null && !medicalConditions.isEmpty()) {
                if (!MedicalConditionFilter.isSafeForConditions(item, medicalConditions)) continue;
            }

            switch (type) {
                case "curry"         -> pools.curries.add(item);
                case "dal"           -> pools.dals.add(item);
                case "sabzi"         -> pools.sabzis.add(item);
                case "egg_dish"      -> pools.eggDishes.add(item);
                case "meat_dish"     -> pools.meatDishes.add(item);
                case "staple"        -> pools.staples.add(item);
                case "rice_dish"     -> pools.riceDishes.add(item);
                case "pasta"         -> pools.pastas.add(item);
                case "complete_meal" -> pools.completeMeals.add(item);
                case "dessert"       -> pools.desserts.add(item);
                case "breakfast_item"-> pools.breakfastItems.add(item);
                case "snack"         -> pools.snacks.add(item);
                case "fruit"         -> pools.fruits.add(item);
                case "nut"           -> pools.nuts.add(item);
                case "drink"         -> categorizeDrink(item, pools);
                case "side"          -> categorizeSide(item, pools);
            }
        }
        return pools;
    }

    private void categorizeDrink(FoodItem item, FoodPools pools) {
        String group = item.getFoodGroup() != null ? item.getFoodGroup() : "";
        if (group.equals("milk"))                                    pools.milks.add(item);
        else if (group.equals("buttermilk") || group.equals("lassi")) pools.buttermilkLassi.add(item);
        else if (group.equals("tea_coffee"))                         pools.teaCoffee.add(item);
        else                                                         pools.otherDrinks.add(item);
    }

    private void categorizeSide(FoodItem item, FoodPools pools) {
        String name = item.getName().toLowerCase();
        String group = item.getFoodGroup() != null ? item.getFoodGroup() : "";
        if (name.contains("salad") || group.equals("salad"))   pools.salads.add(item);
        else if (name.contains("soup"))                        pools.otherSides.add(item);
        else if (group.equals("raita"))                        pools.buttermilkLassi.add(item);
        else                                                   pools.otherSides.add(item);
    }

    // ==================== WEEKLY PLAN ====================

    public WeeklyMealPlan generateWeeklyPlan(UserProfile profile, LocalDate weekStartDate) {
        int dailyCal = calculateDailyCalories(profile);
        Set<MedicalCondition> conditions = profile.getMedicalConditions();
        
        // Use medical-aware macro targets
        String fitnessGoalName = profile.getFitnessGoal() != null ? profile.getFitnessGoal().name() : "MAINTAIN";
        double[] macros = HealthCalculator.calculateMacrosForMedicalConditions(dailyCal, fitnessGoalName, conditions);

        WeeklyMealPlan weeklyPlan = new WeeklyMealPlan(profile, weekStartDate);
        weeklyPlan.setTargetCalories(dailyCal);
        weeklyPlan.setTargetProtein(macros[0]);
        weeklyPlan.setTargetCarbs(macros[1]);
        weeklyPlan.setTargetFat(macros[2]);

        DietaryPreference diet = profile.getDietaryPreference() != null 
            ? profile.getDietaryPreference() : DietaryPreference.NONE;
        
        boolean dairyFree = profile.getDietaryRestrictions() != null &&
            profile.getDietaryRestrictions().stream()
                .anyMatch(r -> r.name().equals("DAIRY_FREE"));

        // Build allowed cuisine set from user's preferences
        Set<String> allowedCuisines = new HashSet<>();
        if (profile.getCuisinePreferences() != null && !profile.getCuisinePreferences().isEmpty()) {
            for (CuisinePreference cp : profile.getCuisinePreferences()) {
                if (cp == CuisinePreference.ALL) {
                    allowedCuisines = null; // null = allow everything
                    break;
                }
                allowedCuisines.add(cp.name()); // e.g., "INDIAN", "CHINESE"
            }
        }

        Set<MedicalCondition> medicalConditions = profile.getMedicalConditions();
        FoodPools pools = loadFoodPools(diet, allowedCuisines, medicalConditions);

        for (DayOfWeek day : DayOfWeek.values()) {
            weeklyPlan.addDailyPlan(generateDailyPlan(pools, dailyCal, day, dairyFree, medicalConditions));
        }

        return weeklyPlan;
    }

    // ==================== DAILY PLAN ====================

    private DailyMealPlan generateDailyPlan(FoodPools pools, int dailyCal, DayOfWeek day, boolean dairyFree, Set<MedicalCondition> conditions) {
        DailyMealPlan plan = new DailyMealPlan(day);
        Set<String> usedGroups = new HashSet<>();

        // Strict calorie budgets per meal
        double bfBudget = dailyCal * BREAKFAST_RATIO;
        double luBudget = dailyCal * LUNCH_RATIO;
        double snBudget = dailyCal * SNACK_RATIO;
        double dnBudget = dailyCal * DINNER_RATIO;

        List<FoodItem> breakfast = assembleBreakfast(pools, usedGroups, dairyFree, bfBudget, conditions);
        List<FoodItem> lunch     = assembleLunchDinner(pools, usedGroups, false, luBudget, conditions);
        List<FoodItem> snacks    = assembleSnacks(pools, usedGroups, snBudget, conditions);
        List<FoodItem> dinner    = assembleLunchDinner(pools, usedGroups, true, dnBudget, conditions);

        // STRICT SCALING: force each meal to exactly match its budget
        scaleToTarget(breakfast, bfBudget);
        scaleToTarget(lunch, luBudget);
        scaleToTarget(snacks, snBudget);
        scaleToTarget(dinner, dnBudget);

        plan.setBreakfastItems(breakfast);
        plan.setLunchItems(lunch);
        plan.setSnacks(snacks);
        plan.setDinnerItems(dinner);
        plan.calculateTotals();
        return plan;
    }

    // ==================== BREAKFAST ====================
    // Template: main dish + milk (if allowed) + mixed fruits
    // Budget-aware: tracks running total, skips items that would exceed

    private List<FoodItem> assembleBreakfast(FoodPools pools, Set<String> usedGroups, boolean dairyFree, double budget, Set<MedicalCondition> conditions) {
        List<FoodItem> items = new ArrayList<>();
        double running = 0;

        // 1. Main breakfast dish
        FoodItem main = pickRandom(pools.breakfastItems, usedGroups, conditions);
        if (main != null && running + itemCal(main) <= budget * 1.1) {
            items.add(main);
            running += itemCal(main);
            usedGroups.add(main.getFoodGroup());
        }

        // 2. Milk (skip if main has milk, or dairy-free, or would exceed budget)
        boolean mainHasMilk = main != null && main.getName().toLowerCase().contains("milk");
        if (!dairyFree && !mainHasMilk && !pools.milks.isEmpty()) {
            FoodItem milk = pickRandom(pools.milks, usedGroups, conditions);
            if (milk != null && running + itemCal(milk) <= budget * 1.1) {
                items.add(milk);
                running += itemCal(milk);
                usedGroups.add(milk.getFoodGroup());
            }
        }

        // 3. Mixed Fruits (only if room in budget)
        if (running < budget * 0.85) {
            FoodItem fruit = pickByName(pools.fruits, "mixed fruit", usedGroups);
            if (fruit == null) fruit = pickRandom(pools.fruits, usedGroups, conditions);
            if (fruit != null && running + itemCal(fruit) <= budget * 1.1) {
                items.add(fruit);
                usedGroups.add(fruit.getFoodGroup());
            }
        }

        return items;
    }

    // ==================== LUNCH / DINNER ====================
    // Template: 2 curries + staple + buttermilk/lassi/raita + salad [+ dessert for dinner]

    private List<FoodItem> assembleLunchDinner(FoodPools pools, Set<String> usedGroups, boolean isDinner, double budget, Set<MedicalCondition> conditions) {
        List<FoodItem> items = new ArrayList<>();
        double running = 0;

        // Build combined main dish pool (curry + dal + sabzi + egg + meat)
        List<FoodItem> mainPool = new ArrayList<>();
        mainPool.addAll(pools.curries);
        mainPool.addAll(pools.dals);
        mainPool.addAll(pools.sabzis);
        mainPool.addAll(pools.eggDishes);
        mainPool.addAll(pools.meatDishes);

        // 1. First main dish
        FoodItem main1 = pickRandom(mainPool, usedGroups, conditions);
        if (main1 != null) {
            items.add(main1);
            running += itemCal(main1);
            usedGroups.add(main1.getFoodGroup());
        }

        // 2. Second main dish (only if budget allows)
        if (running < budget * 0.5) {
            FoodItem main2 = pickRandom(mainPool, usedGroups, conditions);
            if (main2 != null && running + itemCal(main2) <= budget * 0.85) {
                items.add(main2);
                running += itemCal(main2);
                usedGroups.add(main2.getFoodGroup());
            }
        }

        // 3. Staple (roti/rice) — only if budget room
        if (running < budget * 0.7) {
            FoodItem staple = pickRandom(pools.staples, usedGroups, conditions);
            if (staple != null && running + itemCal(staple) <= budget * 0.9) {
                items.add(staple);
                running += itemCal(staple);
                usedGroups.add(staple.getFoodGroup());
            }
        }

        // 4. Buttermilk/Lassi/Raita — only if budget room
        if (running < budget * 0.85 && !pools.buttermilkLassi.isEmpty()) {
            FoodItem drink = pickRandom(pools.buttermilkLassi, usedGroups, conditions);
            if (drink != null && running + itemCal(drink) <= budget * 1.05) {
                items.add(drink);
                running += itemCal(drink);
                usedGroups.add(drink.getFoodGroup());
            }
        }

        // 5. Small salad — only if budget room
        if (running < budget * 0.9 && !pools.salads.isEmpty()) {
            FoodItem salad = pickRandom(pools.salads, usedGroups, conditions);
            if (salad != null && running + itemCal(salad) <= budget * 1.05) {
                items.add(salad);
                running += itemCal(salad);
                usedGroups.add(salad.getFoodGroup());
            }
        }

        // 6. Dinner: small dessert (40% chance, only if room)
        if (isDinner && random.nextInt(10) < 4 && running < budget * 0.85 && !pools.desserts.isEmpty()) {
            FoodItem dessert = pickRandom(pools.desserts, usedGroups, conditions);
            if (dessert != null && running + itemCal(dessert) <= budget * 1.05) {
                items.add(dessert);
                usedGroups.add(dessert.getFoodGroup());
            }
        }

        return items;
    }

    // ==================== SNACKS ====================
    // Template: 1-2 snacks + tea/coffee

    private List<FoodItem> assembleSnacks(FoodPools pools, Set<String> usedGroups, double budget, Set<MedicalCondition> conditions) {
        List<FoodItem> items = new ArrayList<>();
        double running = 0;

        List<FoodItem> snackPool = new ArrayList<>();
        snackPool.addAll(pools.snacks);
        snackPool.addAll(pools.nuts);

        // 1. Main snack
        FoodItem snack1 = pickRandom(snackPool, usedGroups, conditions);
        if (snack1 != null && running + itemCal(snack1) <= budget * 1.1) {
            items.add(snack1);
            running += itemCal(snack1);
            usedGroups.add(snack1.getFoodGroup());
        }

        // 2. Second snack (only if budget allows)
        if (running < budget * 0.5 && random.nextInt(10) < 3) {
            FoodItem snack2 = pickRandom(snackPool, usedGroups, conditions);
            if (snack2 != null && running + itemCal(snack2) <= budget * 1.05) {
                items.add(snack2);
                running += itemCal(snack2);
                usedGroups.add(snack2.getFoodGroup());
            }
        }

        // 3. Tea/Coffee (low cal, almost always fits)
        if (!pools.teaCoffee.isEmpty()) {
            FoodItem drink = pickRandom(pools.teaCoffee, usedGroups, conditions);
            if (drink != null && running + itemCal(drink) <= budget * 1.1) {
                items.add(drink);
                usedGroups.add(drink.getFoodGroup());
            }
        }

        return items;
    }

    // ==================== STRICT CALORIE SCALING ====================

    /**
     * Scale all items in a meal so total calories EXACTLY match the target.
     * No minimum serving floor — the calorie budget is the hard constraint.
     * Fruit items capped at 250g max.
     */
    private void scaleToTarget(List<FoodItem> items, double targetCal) {
        if (items.isEmpty()) return;
        double currentCal = calcTotalCal(items);
        if (currentCal <= 0) return;

        double scaleFactor = targetCal / currentCal;
        // Hard clamp: 0.3x to 2.5x (drastic if needed)
        scaleFactor = Math.max(0.3, Math.min(2.5, scaleFactor));

        for (FoodItem item : items) {
            double origServing = item.getRecommendedServingG();
            if (origServing <= 0) origServing = item.getServingSize() > 0 ? item.getServingSize() : 100;

            double newServing = Math.round(origServing * scaleFactor / 5.0) * 5;
            newServing = Math.max(25, newServing); // absolute minimum 25g

            // Fruit cap: 200-250g
            if ("fruit".equals(item.getDishType())) {
                newServing = Math.min(250, newServing);
            }
            // Drink cap: max 250ml
            if ("drink".equals(item.getDishType())) {
                newServing = Math.min(250, newServing);
            }

            item.setRecommendedServingG(newServing);
            item.setServingDescription(generateServingDesc(item.getName(), (int) newServing, item));
        }
    }

    /** Get calories for an item at its recommended serving */
    private double itemCal(FoodItem item) {
        return item.getServingCalories();
    }

    private double calcTotalCal(List<FoodItem> items) {
        return items.stream().mapToDouble(FoodItem::getServingCalories).sum();
    }

    private String generateServingDesc(String name, int servingG, FoodItem item) {
        String lower = name.toLowerCase();
        double cal = item.getCalories() * servingG / 100.0;

        if (lower.contains("roti") || lower.contains("chapati") || lower.contains("naan")) {
            int count = Math.max(1, servingG / 40);
            return count + " " + (count == 1 ? "piece" : "pieces") + " (" + servingG + "g)";
        }
        if (lower.contains("milk")) {
            return servingG >= 200 ? "1 glass (" + servingG + "ml)" : servingG + "ml";
        }
        if (lower.contains("tea") || lower.contains("coffee") || lower.contains("latte") || lower.contains("cappuccino")) {
            return "1 cup (" + servingG + "ml)";
        }
        if (lower.contains("rice") || lower.contains("dal") || lower.contains("curry") || lower.contains("sabzi")) {
            return "1 bowl (" + servingG + "g, ~" + Math.round(cal) + " cal)";
        }
        if (lower.contains("salad") || lower.contains("fruit")) {
            return "1 bowl (" + servingG + "g)";
        }
        return servingG + "g (~" + Math.round(cal) + " cal)";
    }

    // ==================== ITEM PICKERS ====================

    /**
     * Pick a random item from the pool, weighted by its health score for the given conditions.
     * Uses Stochastic Universal Sampling style selection to prefer healthier foods while maintaining variety.
     */
    private FoodItem pickRandom(List<FoodItem> pool, Set<String> usedGroups, Set<MedicalCondition> conditions) {
        if (pool == null || pool.isEmpty()) return null;
        List<FoodItem> available = pool.stream()
            .filter(f -> f.getFoodGroup() == null || !usedGroups.contains(f.getFoodGroup()))
            .collect(Collectors.toList());
        
        if (available.isEmpty()) return null;

        // No conditions? Plain random.
        if (conditions == null || conditions.isEmpty()) {
            return available.get(random.nextInt(available.size()));
        }

        // Weighted Selection
        // Weight = e^(score * 3.0) -> heavily favors high scores (1.0 vs 0.3)
        double totalWeight = 0;
        double[] weights = new double[available.size()];
        for (int i = 0; i < available.size(); i++) {
            double score = MedicalConditionFilter.getHealthScore(available.get(i), conditions);
            weights[i] = Math.exp(score * 3.0); 
            totalWeight += weights[i];
        }

        double target = random.nextDouble() * totalWeight;
        double current = 0;
        for (int i = 0; i < available.size(); i++) {
            current += weights[i];
            if (current >= target) return available.get(i);
        }

        return available.get(available.size() - 1);
    }

    private FoodItem pickByName(List<FoodItem> pool, String keyword, Set<String> usedGroups) {
        if (pool == null) return null;
        return pool.stream()
            .filter(f -> f.getName().toLowerCase().contains(keyword.toLowerCase()))
            .filter(f -> f.getFoodGroup() == null || !usedGroups.contains(f.getFoodGroup()))
            .findFirst().orElse(null);
    }

    // ==================== DIET COMPATIBILITY ====================

    private boolean isDietCompatible(FoodItem food, DietaryPreference userPref) {
        if (userPref == null || userPref == DietaryPreference.NONE) return true;
        DietaryPreference foodDiet = food.getDietaryCategory();
        if (foodDiet == null) return true;

        return switch (userPref) {
            case VEGAN      -> foodDiet == DietaryPreference.VEGAN;
            case VEGETARIAN -> foodDiet == DietaryPreference.VEGAN || foodDiet == DietaryPreference.VEGETARIAN;
            case EGGETARIAN -> foodDiet == DietaryPreference.VEGAN || foodDiet == DietaryPreference.VEGETARIAN || foodDiet == DietaryPreference.EGGETARIAN;
            default -> true;
        };
    }

    // ==================== CALORIE / MACRO TARGETS ====================

    private int calculateDailyCalories(UserProfile profile) {
        ufit snapshot = profile.getLatestHealthSnapshot();
        double tdee = 2000; // Conservative fallback

        if (snapshot != null && snapshot.getTdee() > 0) {
            tdee = snapshot.getTdee();
        } else if (snapshot != null) {
            // Manual calculate if TDEE is 0 but inputs exist
            double bmr = HealthCalculator.calculateBMR(
                snapshot.getWeight() > 0 ? snapshot.getWeight() : 70, 
                snapshot.getHeight() > 0 ? snapshot.getHeight() / 100.0 : 1.7, 
                snapshot.getAge() > 0 ? snapshot.getAge() : 30, 
                snapshot.getGender() != null ? snapshot.getGender() : "MALE"
            );
            tdee = HealthCalculator.calculateTDEE(bmr, snapshot.getActivityLevel() != null ? snapshot.getActivityLevel() : "MODERATE");
        }

        int adjustment = profile.getFitnessGoal() != null ?
            profile.getFitnessGoal().getCalorieAdjustment() : 0;
        
        return (int) (tdee + adjustment);
    }
}
