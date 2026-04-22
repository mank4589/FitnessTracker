package com.ufit.recommendation;

import com.ufit.model.FoodItem;
import com.ufit.model.enums.MedicalCondition;

import java.util.*;

/**
 * Rule engine that evaluates food suitability based on medical conditions.
 * 
 * Thresholds are based on:
 * - WHO: <5g added sugar/day, <5g salt (2000mg sodium)/day, <10% sat fat
 * - AHA: <1500mg sodium/day for hypertension, <300mg cholesterol/day
 * - NKF: Potassium <2000mg/day for kidney disease, protein moderation
 * - ADA: Carbs 45-60g/meal, glycemic load awareness
 * 
 * All thresholds are PER 100g of food (matching our DB format).
 * Soft boundary: uses a scoring system (0.0-1.0) rather than hard cutoffs.
 * Foods scoring below 0.3 are excluded; 0.3-0.6 get warnings; >0.6 are safe.
 */
public class MedicalConditionFilter {

    // ==================== THRESHOLDS (per 100g) ====================
    // Based on WHO/AHA/NKF guidelines, proportioned to per-100g values

    // --- DIABETES (ADA Guidelines) ---
    private static final double DIABETES_SUGAR_LIMIT = 8.0;       // g/100g — WHO: limit free sugars
    private static final double DIABETES_CARB_LIMIT = 30.0;       // g/100g — moderate carb
    private static final double DIABETES_FIBER_IDEAL = 3.0;       // g/100g — high fiber preferred

    // --- HYPERTENSION (WHO + AHA) ---
    private static final double HYPERTENSION_SODIUM_LIMIT = 300.0; // mg/100g — WHO: <2000mg/day total
    private static final double HYPERTENSION_SAT_FAT_LIMIT = 4.0;  // g/100g — AHA: <13g/day
    private static final double HYPERTENSION_POTASSIUM_IDEAL = 20.0; // mg/100g — higher = better

    // --- HIGH CHOLESTEROL (AHA) ---
    private static final double CHOLESTEROL_LIMIT = 80.0;          // mg/100g — AHA: <300mg/day
    private static final double CHOLESTEROL_SAT_FAT_LIMIT = 4.0;   // g/100g — AHA: <13g/day
    private static final double CHOLESTEROL_FIBER_IDEAL = 3.0;     // g/100g — soluble fiber helps

    // --- KIDNEY ISSUES (NKF Guidelines) ---
    private static final double KIDNEY_POTASSIUM_LIMIT = 200.0;   // mg/100g — NKF: <2000mg/day
    private static final double KIDNEY_SODIUM_LIMIT = 300.0;      // mg/100g — NKF: <2000mg/day
    private static final double KIDNEY_PROTEIN_LIMIT = 18.0;      // g/100g — moderate protein

    // --- HEART DISEASE (AHA) ---
    private static final double HEART_SAT_FAT_LIMIT = 3.5;        // g/100g — AHA: <11-13g/day
    private static final double HEART_SODIUM_LIMIT = 300.0;       // mg/100g
    private static final double HEART_CHOLESTEROL_LIMIT = 60.0;   // mg/100g — stricter than general

    // --- PCOD / Hormonal (Endocrine Society) ---
    private static final double PCOD_SUGAR_LIMIT = 6.0;           // g/100g — low GI focus
    private static final double PCOD_CARB_LIMIT = 25.0;           // g/100g — lower carb
    private static final double PCOD_FIBER_IDEAL = 3.0;           // g/100g — high fiber preferred

    // --- THYROID (ATA) ---
    private static final double THYROID_SUGAR_LIMIT = 10.0;       // g/100g
    // Note: soy-based foods flagged by food group, not just nutrients

    // --- OBESITY (WHO) ---
    private static final double OBESITY_CALORIE_LIMIT = 200.0;    // kcal/100g — energy density
    private static final double OBESITY_FAT_LIMIT = 10.0;         // g/100g — WHO: <30% energy from fat
    private static final double OBESITY_SUGAR_LIMIT = 8.0;        // g/100g
    private static final double OBESITY_FIBER_IDEAL = 3.0;        // g/100g — satiety

    // Exclusion threshold — below this score, food is excluded from meal plans
    public static final double EXCLUDE_THRESHOLD = 0.3;
    // Warning threshold — below this, show a warning badge
    public static final double WARNING_THRESHOLD = 0.6;

    /**
     * Check if a food item is safe enough for the given medical conditions.
     * Returns true if the food should be INCLUDED in the meal plan.
     */
    public static boolean isSafeForConditions(FoodItem food, Set<MedicalCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) return true;
        return getHealthScore(food, conditions) >= EXCLUDE_THRESHOLD;
    }

    /**
     * Get a 0.0-1.0 health score for a food item given medical conditions.
     * 1.0 = perfectly safe, 0.0 = should be completely avoided.
     * Uses the MINIMUM score across all conditions (strictest wins).
     */
    public static double getHealthScore(FoodItem food, Set<MedicalCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) return 1.0;

        double minScore = 1.0;
        for (MedicalCondition condition : conditions) {
            double score = scoreForCondition(food, condition);
            minScore = Math.min(minScore, score);
        }
        return minScore;
    }

    /**
     * Get specific warnings for a food item given medical conditions.
     * Returns a list of human-readable warning strings.
     */
    public static List<String> getWarnings(FoodItem food, Set<MedicalCondition> conditions) {
        if (conditions == null || conditions.isEmpty()) return Collections.emptyList();

        List<String> warnings = new ArrayList<>();
        for (MedicalCondition condition : conditions) {
            List<String> condWarnings = warningsForCondition(food, condition);
            warnings.addAll(condWarnings);
        }
        return warnings;
    }

    // ==================== PER-CONDITION SCORING ====================

    private static double scoreForCondition(FoodItem food, MedicalCondition condition) {
        return switch (condition) {
            case DIABETES -> scoreDiabetes(food);
            case HYPERTENSION -> scoreHypertension(food);
            case HIGH_CHOLESTEROL -> scoreCholesterol(food);
            case LOW_BP -> scoreLowBP(food);
            case KIDNEY_ISSUES -> scoreKidney(food);
            case PCOD -> scorePCOD(food);
            case THYROID -> scoreThyroid(food);
            case HEART_DISEASE -> scoreHeart(food);
            case OBESITY -> scoreObesity(food);
        };
    }

    // --- DIABETES ---
    private static double scoreDiabetes(FoodItem food) {
        double score = 1.0;
        double sugar = food.getSugar();
        double carbs = food.getCarbs();
        double fiber = food.getFiber();

        // Sugar penalty (WHO: <10% of energy from free sugars)
        if (sugar > DIABETES_SUGAR_LIMIT * 2) score -= 0.5;       // Very high sugar
        else if (sugar > DIABETES_SUGAR_LIMIT) score -= 0.3;       // High sugar

        // Carb penalty
        if (carbs > DIABETES_CARB_LIMIT * 1.5) score -= 0.3;      // Very high carbs
        else if (carbs > DIABETES_CARB_LIMIT) score -= 0.15;

        // Fiber bonus (soluble fiber slows glucose absorption)
        if (fiber >= DIABETES_FIBER_IDEAL * 2) score += 0.15;
        else if (fiber >= DIABETES_FIBER_IDEAL) score += 0.1;

        // Dessert/sweet dish penalty
        if ("dessert".equals(food.getDishType())) score -= 0.3;

        return clamp(score);
    }

    // --- HYPERTENSION ---
    private static double scoreHypertension(FoodItem food) {
        double score = 1.0;
        double sodium = food.getSodium();
        double satFat = food.getSaturatedFat();
        double potassium = food.getPotassium();

        // Sodium penalty (WHO: <2000mg/day = ~300mg/100g across ~7 servings)
        if (sodium > HYPERTENSION_SODIUM_LIMIT * 2) score -= 0.5;
        else if (sodium > HYPERTENSION_SODIUM_LIMIT) score -= 0.25;

        // Saturated fat penalty
        if (satFat > HYPERTENSION_SAT_FAT_LIMIT * 2) score -= 0.2;
        else if (satFat > HYPERTENSION_SAT_FAT_LIMIT) score -= 0.1;

        // Potassium bonus (DASH diet — potassium helps lower BP)
        if (potassium > HYPERTENSION_POTASSIUM_IDEAL * 3) score += 0.1;
        else if (potassium > HYPERTENSION_POTASSIUM_IDEAL) score += 0.05;

        return clamp(score);
    }

    // --- HIGH CHOLESTEROL ---
    private static double scoreCholesterol(FoodItem food) {
        double score = 1.0;
        double chol = food.getCholesterol();
        double satFat = food.getSaturatedFat();
        double fiber = food.getFiber();

        // Cholesterol penalty (AHA: <300mg/day)
        if (chol > CHOLESTEROL_LIMIT * 2) score -= 0.5;
        else if (chol > CHOLESTEROL_LIMIT) score -= 0.25;

        // Saturated fat penalty (raises LDL cholesterol)
        if (satFat > CHOLESTEROL_SAT_FAT_LIMIT * 2) score -= 0.3;
        else if (satFat > CHOLESTEROL_SAT_FAT_LIMIT) score -= 0.15;

        // Fiber bonus (soluble fiber lowers LDL)
        if (fiber >= CHOLESTEROL_FIBER_IDEAL * 2) score += 0.15;
        else if (fiber >= CHOLESTEROL_FIBER_IDEAL) score += 0.1;

        return clamp(score);
    }

    // --- LOW BP ---
    private static double scoreLowBP(FoodItem food) {
        // Low BP is the opposite — moderate sodium is OK, hydration matters
        // Most foods are fine; only penalize very low-sodium + diuretic foods
        double score = 1.0;
        double sodium = food.getSodium();

        // Slight bonus for moderate sodium (helps maintain BP)
        if (sodium >= 100 && sodium <= 400) score += 0.05;

        return clamp(score);
    }

    // --- KIDNEY ISSUES ---
    private static double scoreKidney(FoodItem food) {
        double score = 1.0;
        double potassium = food.getPotassium();
        double sodium = food.getSodium();
        double protein = food.getProtein();

        // Potassium penalty (NKF: limit to <2000mg/day)
        if (potassium > KIDNEY_POTASSIUM_LIMIT * 2) score -= 0.4;
        else if (potassium > KIDNEY_POTASSIUM_LIMIT) score -= 0.2;

        // Sodium penalty
        if (sodium > KIDNEY_SODIUM_LIMIT * 2) score -= 0.3;
        else if (sodium > KIDNEY_SODIUM_LIMIT) score -= 0.15;

        // Protein penalty (excess protein stresses kidneys)
        if (protein > KIDNEY_PROTEIN_LIMIT * 1.5) score -= 0.25;
        else if (protein > KIDNEY_PROTEIN_LIMIT) score -= 0.1;

        return clamp(score);
    }

    // --- PCOD / Hormonal ---
    private static double scorePCOD(FoodItem food) {
        double score = 1.0;
        double sugar = food.getSugar();
        double carbs = food.getCarbs();
        double fiber = food.getFiber();

        // Low GI focus — penalize high sugar and high carbs
        if (sugar > PCOD_SUGAR_LIMIT * 2) score -= 0.4;
        else if (sugar > PCOD_SUGAR_LIMIT) score -= 0.2;

        if (carbs > PCOD_CARB_LIMIT * 1.5) score -= 0.25;
        else if (carbs > PCOD_CARB_LIMIT) score -= 0.1;

        // Anti-inflammatory: fiber bonus
        if (fiber >= PCOD_FIBER_IDEAL * 2) score += 0.15;
        else if (fiber >= PCOD_FIBER_IDEAL) score += 0.1;

        // Dessert penalty
        if ("dessert".equals(food.getDishType())) score -= 0.25;

        return clamp(score);
    }

    // --- THYROID ---
    private static double scoreThyroid(FoodItem food) {
        double score = 1.0;
        double sugar = food.getSugar();

        // Sugar penalty
        if (sugar > THYROID_SUGAR_LIMIT * 2) score -= 0.3;
        else if (sugar > THYROID_SUGAR_LIMIT) score -= 0.15;

        // Goitrogen penalty — soy-based foods can interfere with thyroid
        String group = food.getFoodGroup();
        if (group != null && (group.contains("soy") || group.contains("tofu"))) {
            score -= 0.2;
        }

        // Protein bonus (thyroid needs adequate protein)
        if (food.getProtein() > 15) score += 0.1;

        return clamp(score);
    }

    // --- HEART DISEASE ---
    private static double scoreHeart(FoodItem food) {
        double score = 1.0;
        double satFat = food.getSaturatedFat();
        double sodium = food.getSodium();
        double chol = food.getCholesterol();
        double fiber = food.getFiber();

        // Saturated fat penalty (AHA: <5-6% of calories)
        if (satFat > HEART_SAT_FAT_LIMIT * 2) score -= 0.4;
        else if (satFat > HEART_SAT_FAT_LIMIT) score -= 0.2;

        // Sodium penalty
        if (sodium > HEART_SODIUM_LIMIT * 2) score -= 0.3;
        else if (sodium > HEART_SODIUM_LIMIT) score -= 0.15;

        // Cholesterol penalty
        if (chol > HEART_CHOLESTEROL_LIMIT * 2) score -= 0.3;
        else if (chol > HEART_CHOLESTEROL_LIMIT) score -= 0.15;

        // Fiber bonus
        if (fiber >= 3.0) score += 0.1;

        return clamp(score);
    }

    // --- OBESITY ---
    private static double scoreObesity(FoodItem food) {
        double score = 1.0;
        double cal = food.getCalories();
        double fat = food.getFat();
        double sugar = food.getSugar();
        double fiber = food.getFiber();

        // Calorie density penalty (WHO: favor low energy-dense foods)
        if (cal > OBESITY_CALORIE_LIMIT * 2) score -= 0.5;
        else if (cal > OBESITY_CALORIE_LIMIT) score -= 0.25;

        // Fat penalty (WHO: <30% energy from fat)
        if (fat > OBESITY_FAT_LIMIT * 2) score -= 0.3;
        else if (fat > OBESITY_FAT_LIMIT) score -= 0.15;

        // Sugar penalty
        if (sugar > OBESITY_SUGAR_LIMIT * 2) score -= 0.3;
        else if (sugar > OBESITY_SUGAR_LIMIT) score -= 0.15;

        // Fiber bonus (satiety — keeps you full longer)
        if (fiber >= OBESITY_FIBER_IDEAL * 2) score += 0.15;
        else if (fiber >= OBESITY_FIBER_IDEAL) score += 0.1;

        // Dessert / fried snack penalty
        String type = food.getDishType();
        if ("dessert".equals(type)) score -= 0.3;
        if ("snack".equals(type) && cal > 200) score -= 0.2;

        return clamp(score);
    }

    // ==================== PER-CONDITION WARNINGS ====================

    private static List<String> warningsForCondition(FoodItem food, MedicalCondition condition) {
        List<String> w = new ArrayList<>();
        switch (condition) {
            case DIABETES -> {
                if (food.getSugar() > DIABETES_SUGAR_LIMIT) w.add("⚠️ High sugar (" + fmt(food.getSugar()) + "g) — Diabetes");
                if (food.getCarbs() > DIABETES_CARB_LIMIT) w.add("⚠️ High carbs (" + fmt(food.getCarbs()) + "g) — Diabetes");
            }
            case HYPERTENSION -> {
                if (food.getSodium() > HYPERTENSION_SODIUM_LIMIT) w.add("⚠️ High sodium (" + fmt(food.getSodium()) + "mg) — BP");
            }
            case HIGH_CHOLESTEROL -> {
                if (food.getCholesterol() > CHOLESTEROL_LIMIT) w.add("⚠️ High cholesterol (" + fmt(food.getCholesterol()) + "mg)");
                if (food.getSaturatedFat() > CHOLESTEROL_SAT_FAT_LIMIT) w.add("⚠️ High sat. fat (" + fmt(food.getSaturatedFat()) + "g)");
            }
            case KIDNEY_ISSUES -> {
                if (food.getPotassium() > KIDNEY_POTASSIUM_LIMIT) w.add("⚠️ High potassium (" + fmt(food.getPotassium()) + "mg) — Kidney");
                if (food.getProtein() > KIDNEY_PROTEIN_LIMIT) w.add("⚠️ High protein (" + fmt(food.getProtein()) + "g) — Kidney");
            }
            case HEART_DISEASE -> {
                if (food.getSaturatedFat() > HEART_SAT_FAT_LIMIT) w.add("⚠️ High sat. fat — Heart");
                if (food.getSodium() > HEART_SODIUM_LIMIT) w.add("⚠️ High sodium — Heart");
            }
            case PCOD -> {
                if (food.getSugar() > PCOD_SUGAR_LIMIT) w.add("⚠️ High sugar — PCOD");
                if (food.getCarbs() > PCOD_CARB_LIMIT) w.add("⚠️ High carbs — PCOD");
            }
            case THYROID -> {
                String group = food.getFoodGroup();
                if (group != null && (group.contains("soy") || group.contains("tofu"))) w.add("⚠️ Goitrogen (soy) — Thyroid");
            }
            case OBESITY -> {
                if (food.getCalories() > OBESITY_CALORIE_LIMIT) w.add("⚠️ Calorie-dense (" + fmt(food.getCalories()) + " kcal/100g)");
                if (food.getFat() > OBESITY_FAT_LIMIT) w.add("⚠️ High fat — Obesity");
            }
            case LOW_BP -> {
                // Low BP rarely needs warnings — most foods OK
            }
        }
        return w;
    }

    // ==================== UTILS ====================

    private static double clamp(double score) {
        return Math.max(0.0, Math.min(1.0, score));
    }

    private static String fmt(double v) {
        if (v == (int) v) return String.valueOf((int) v);
        return String.format("%.1f", v);
    }
}
