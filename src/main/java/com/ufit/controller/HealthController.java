package com.ufit.controller;

import com.ufit.logic.HealthCalculator;
import com.ufit.model.ufit;
import com.ufit.service.ufitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final ufitService service;

    public HealthController(ufitService service) {
        this.service = service;
    }

    // ═══════════════ BMI ═══════════════

    @PostMapping("/bmi")
    public ResponseEntity<Map<String, Object>> calculateBmi(@RequestBody Map<String, Object> body) {
        double weight = toDouble(body.get("weight"));
        double height = toDouble(body.get("height"));
        int age = toInt(body.get("age"));
        String gender = (String) body.get("gender");

        double bmi = HealthCalculator.calculateBMI(weight, height);
        String category = HealthCalculator.getBmiCategory(bmi);

        double normalLow = 18.5 * height * height;
        double normalHigh = 24.9 * height * height;

        String advice;
        if (bmi < 18.5) {
            advice = String.format("Gain %.1f kg to reach normal", normalLow - weight);
        } else if (bmi >= 25.0) {
            advice = String.format("Lose %.1f kg to reach normal", weight - normalHigh);
        } else {
            advice = "You are within the healthy range";
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bmi", round(bmi));
        result.put("category", category);
        result.put("age", age);
        result.put("gender", gender);
        result.put("healthyWeightRange", new double[]{round(normalLow), round(normalHigh)});
        result.put("weight", weight);
        result.put("advice", advice);
        result.put("bmiPrime", round(bmi / 25.0));

        return ResponseEntity.ok(result);
    }

    // ═══════════════ BMR ═══════════════

    @PostMapping("/bmr")
    public ResponseEntity<Map<String, Object>> calculateBmr(@RequestBody Map<String, Object> body) {
        double weight = toDouble(body.get("weight"));
        double height = toDouble(body.get("height"));
        int age = toInt(body.get("age"));
        String gender = (String) body.get("gender");

        double mifflin = HealthCalculator.calculateBMR(weight, height, age, gender);
        double harris = HealthCalculator.calculateHarrisBenedictBMR(weight, height, age, gender);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mifflinStJeor", round(mifflin));
        result.put("harrisBenedict", round(harris));

        return ResponseEntity.ok(result);
    }

    // ═══════════════ BODY FAT ═══════════════

    @PostMapping("/body-fat")
    public ResponseEntity<Map<String, Object>> calculateBodyFat(@RequestBody Map<String, Object> body) {
        double weight = toDouble(body.get("weight"));
        double waist = toDouble(body.get("waist"));
        double neck = toDouble(body.get("neck"));
        double height = toDouble(body.get("height"));
        String gender = (String) body.get("gender");

        double bodyFat = HealthCalculator.calculateBodyFat(waist, neck, height * 100, gender);
        String category = HealthCalculator.getBodyFatCategory(bodyFat, gender);

        double fatMass = weight * (bodyFat / 100.0);
        double leanMass = weight - fatMass;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bodyFat", round(bodyFat));
        result.put("category", category);
        result.put("fatMass", round(fatMass));
        result.put("leanMass", round(leanMass));

        return ResponseEntity.ok(result);
    }

    // ═══════════════ TDEE ═══════════════

    @PostMapping("/tdee")
    public ResponseEntity<Map<String, Object>> calculateTdee(@RequestBody Map<String, Object> body) {
        double weight = toDouble(body.get("weight"));
        double height = toDouble(body.get("height"));
        int age = toInt(body.get("age"));
        String gender = (String) body.get("gender");
        String activityLevel = (String) body.get("activityLevel");

        double bmr = HealthCalculator.calculateBMR(weight, height, age, gender);
        double tdee = HealthCalculator.calculateTDEE(bmr, activityLevel);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bmr", round(bmr));
        result.put("tdee", round(tdee));
        result.put("activityMultiplier", HealthCalculator.getTDEEMultiplier(activityLevel));

        return ResponseEntity.ok(result);
    }

    // ═══════════════ MACROS ═══════════════

    @PostMapping("/macros")
    public ResponseEntity<Map<String, Object>> calculateMacros(@RequestBody Map<String, Object> body) {
        double weight = toDouble(body.get("weight"));
        double height = toDouble(body.get("height"));
        int age = toInt(body.get("age"));
        String gender = (String) body.get("gender");
        String activityLevel = (String) body.get("activityLevel");
        String goal = (String) body.get("goal");
        String dietPlan = body.getOrDefault("dietPlan", "Balanced").toString();

        double bmr = HealthCalculator.calculateBMR(weight, height, age, gender);
        double tdee = HealthCalculator.calculateTDEE(bmr, activityLevel);
        double adjustment = HealthCalculator.getGoalCalorieAdjustment(goal);
        double targetCalories = tdee + adjustment;

        double[] macros = HealthCalculator.calculateAdvancedMacros(targetCalories, dietPlan);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("targetCalories", round(targetCalories));
        result.put("targetKj", round(macros[12]));
        result.put("protein", round(macros[0]));
        result.put("proteinMin", round(macros[1]));
        result.put("proteinMax", round(macros[2]));
        result.put("carbs", round(macros[3]));
        result.put("carbsMin", round(macros[4]));
        result.put("carbsMax", round(macros[5]));
        result.put("fat", round(macros[6]));
        result.put("fatMin", round(macros[7]));
        result.put("fatMax", round(macros[8]));
        result.put("sugar", round(macros[9]));
        result.put("saturatedFat", round(macros[10]));
        result.put("tdee", round(tdee));
        result.put("bmr", round(bmr));

        return ResponseEntity.ok(result);
    }

    // ═══════════════ IDEAL WEIGHT ═══════════════

    @PostMapping("/ideal-weight")
    public ResponseEntity<Map<String, Object>> calculateIdealWeight(@RequestBody Map<String, Object> body) {
        double height = toDouble(body.get("height"));
        String gender = (String) body.get("gender");
        double heightCm = height * 100;

        double devine = HealthCalculator.calculateIdealWeight(heightCm, gender);
        double robinson = HealthCalculator.calculateIdealWeightRobinson(heightCm, gender);
        double miller = HealthCalculator.calculateIdealWeightMiller(heightCm, gender);
        double hamwi = HealthCalculator.calculateIdealWeightHamwi(heightCm, gender);
        double[] range = HealthCalculator.calculateHealthyWeightRange(heightCm);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("devine", round(devine));
        result.put("robinson", round(robinson));
        result.put("miller", round(miller));
        result.put("hamwi", round(hamwi));
        result.put("healthyRange", new double[]{round(range[0]), round(range[1])});

        return ResponseEntity.ok(result);
    }

    // ═══════════════ UNIFIED CALCULATOR (All-in-One) ═══════════════

    @PostMapping("/calculate-all")
    public ResponseEntity<Map<String, Object>> calculateAll(@RequestBody Map<String, Object> body) {
        double weight = toDouble(body.get("weight"));
        double height = toDouble(body.get("height"));
        int age = toInt(body.get("age"));
        String gender = (String) body.get("gender");
        String activityLevel = (String) body.getOrDefault("activityLevel", "Sedentary");
        String goal = (String) body.getOrDefault("goal", "Maintain weight");
        String dietPlan = body.getOrDefault("dietPlan", "Balanced").toString();
        double waist = toDouble(body.get("waist"));
        double neck = toDouble(body.get("neck"));

        double heightCm = height * 100;

        // BMI
        double bmi = HealthCalculator.calculateBMI(weight, height);
        String bmiCategory = HealthCalculator.getBmiCategory(bmi);
        double normalLow = 18.5 * height * height;
        double normalHigh = 24.9 * height * height;
        String bmiAdvice;
        if (bmi < 18.5) bmiAdvice = String.format("Gain %.1f kg to reach normal", normalLow - weight);
        else if (bmi >= 25.0) bmiAdvice = String.format("Lose %.1f kg to reach normal", weight - normalHigh);
        else bmiAdvice = "You are within the healthy range";

        // BMR
        double bmrMifflin = HealthCalculator.calculateBMR(weight, height, age, gender);
        double bmrHarris = HealthCalculator.calculateHarrisBenedictBMR(weight, height, age, gender);

        // TDEE
        double tdee = HealthCalculator.calculateTDEE(bmrMifflin, activityLevel);
        double tdeeMultiplier = HealthCalculator.getExtendedTDEEMultiplier(activityLevel);

        // Body Fat (only if waist and neck are provided)
        Double bodyFat = null;
        String bodyFatCategory = null;
        Double fatMass = null;
        Double leanMass = null;
        if (waist > 0 && neck > 0) {
            bodyFat = HealthCalculator.calculateBodyFat(waist, neck, heightCm, gender);
            bodyFatCategory = HealthCalculator.getBodyFatCategory(bodyFat, gender);
            fatMass = weight * (bodyFat / 100.0);
            leanMass = weight - fatMass;
        }

        // Ideal Weight
        double devine = HealthCalculator.calculateIdealWeight(heightCm, gender);
        double robinson = HealthCalculator.calculateIdealWeightRobinson(heightCm, gender);
        double miller = HealthCalculator.calculateIdealWeightMiller(heightCm, gender);
        double hamwi = HealthCalculator.calculateIdealWeightHamwi(heightCm, gender);
        double[] healthyRange = HealthCalculator.calculateHealthyWeightRange(heightCm);

        // Macros
        double adjustment = HealthCalculator.getGoalCalorieAdjustment(goal);
        double targetCalories = tdee + adjustment;
        double[] macros = HealthCalculator.calculateAdvancedMacros(targetCalories, dietPlan);

        // Build response
        Map<String, Object> result = new LinkedHashMap<>();

        // BMI section
        result.put("bmi", round(bmi));
        result.put("bmiCategory", bmiCategory);
        result.put("bmiAdvice", bmiAdvice);
        result.put("bmiPrime", round(bmi / 25.0));
        result.put("healthyWeightRange", new double[]{round(normalLow), round(normalHigh)});

        // BMR section
        result.put("bmrMifflin", round(bmrMifflin));
        result.put("bmrHarris", round(bmrHarris));

        // TDEE section
        result.put("tdee", round(tdee));
        result.put("tdeeMultiplier", tdeeMultiplier);

        // Body Fat section
        if (bodyFat != null) {
            result.put("bodyFat", round(bodyFat));
            result.put("bodyFatCategory", bodyFatCategory);
            result.put("fatMass", round(fatMass));
            result.put("leanMass", round(leanMass));
        }

        // Ideal Weight section
        Map<String, Object> idealWeight = new LinkedHashMap<>();
        idealWeight.put("devine", round(devine));
        idealWeight.put("robinson", round(robinson));
        idealWeight.put("miller", round(miller));
        idealWeight.put("hamwi", round(hamwi));
        idealWeight.put("healthyRange", new double[]{round(healthyRange[0]), round(healthyRange[1])});
        result.put("idealWeight", idealWeight);

        // Macros section
        result.put("targetCalories", round(targetCalories));
        result.put("protein", round(macros[0]));
        result.put("proteinMin", round(macros[1]));
        result.put("proteinMax", round(macros[2]));
        result.put("carbs", round(macros[3]));
        result.put("carbsMin", round(macros[4]));
        result.put("carbsMax", round(macros[5]));
        result.put("fat", round(macros[6]));
        result.put("fatMin", round(macros[7]));
        result.put("fatMax", round(macros[8]));
        result.put("sugar", round(macros[9]));
        result.put("saturatedFat", round(macros[10]));

        return ResponseEntity.ok(result);
    }

    // ═══════════════ FULL HEALTH REPORT (History) ═══════════════

    @PostMapping("/report")
    public ResponseEntity<ufit> saveReport(@RequestBody ufit report) {
        ufit saved = service.saveFullHealthReport(report);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/history")
    public ResponseEntity<List<ufit>> getHistory() {
        List<ufit> all = service.repository.findAll();
        return ResponseEntity.ok(all);
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        service.repository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // ═══════════════ HELPERS ═══════════════

    private double toDouble(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0; }
    }

    private int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}

