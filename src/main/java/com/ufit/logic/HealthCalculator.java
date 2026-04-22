package com.ufit.logic;
import com.ufit.model.enums.MedicalCondition;
import java.util.Set;

public class HealthCalculator {

    public static double calculateBMI(double weightKg, double heightM) {
        return weightKg / (Math.pow(heightM, 2));
    }

    public static String getBmiCategory(double bmi) {
        if (bmi < 16.0)
            return "Severe Underweight";
        if (bmi < 18.5)
            return "Underweight";
        if (bmi < 25.0)
            return "Normal";
        if (bmi < 30.0)
            return "Overweight";
        if (bmi < 35.0)
            return "Obese Class I";
        if (bmi < 40.0)
            return "Obese Class II";
        return "Obese Class III";
    }

    public static double calculateBMR(double weightKg, double heightM, int age, String gender) {
        double heightCm = heightM * 100;
        if (gender.equalsIgnoreCase("male")) {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
        }
    }

    public static double calculateHarrisBenedictBMR(double weightKg, double heightM, int age, String gender) {
        double heightCm = heightM * 100;
        if (gender.equalsIgnoreCase("male")) {
            return 88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age);
        } else {
            return 447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age);
        }
    }

    public static double calculateBodyFat(double waistCm, double neckCm, double heightCm, String gender) {
        if (gender.equalsIgnoreCase("male")) {
            return 495 / (1.0324 - 0.19077 * Math.log10(waistCm - neckCm)
                    + 0.15456 * Math.log10(heightCm)) - 450;
        } else {
            return 495 / (1.29579 - 0.35004 * Math.log10(waistCm - neckCm)
                    + 0.22100 * Math.log10(heightCm)) - 450;
        }
    }

    public static double calculateBodyFat(double waistCm, double neckCm, double heightCm) {
        return calculateBodyFat(waistCm, neckCm, heightCm, "male");
    }

    public static String getBodyFatCategory(double bodyFat, String gender) {
        if (gender.equalsIgnoreCase("male")) {
            if (bodyFat < 6)
                return "Essential Fat";
            if (bodyFat < 14)
                return "Athletic";
            if (bodyFat < 18)
                return "Fitness";
            if (bodyFat < 25)
                return "Average";
            return "Above Average";
        } else {
            if (bodyFat < 14)
                return "Essential Fat";
            if (bodyFat < 21)
                return "Athletic";
            if (bodyFat < 25)
                return "Fitness";
            if (bodyFat < 32)
                return "Average";
            return "Above Average";
        }
    }

    public static double calculateTDEE(double bmr, String activityLevel) {
        return bmr * getTDEEMultiplier(activityLevel);
    }

    public static double getTDEEMultiplier(String activityLevel) {
        return switch (activityLevel.toLowerCase()) {
            case "light" -> 1.375;
            case "moderate" -> 1.55;
            case "active" -> 1.725;
            case "extra active" -> 1.9;
            default -> 1.2;
        };
    }

    public static double calculateIdealWeight(double heightCm, String gender) {
        double inchesAbove5ft = Math.max(0, (heightCm - 152.4) / 2.54);
        if (gender.equalsIgnoreCase("male")) {
            return 50 + 2.3 * inchesAbove5ft;
        } else {
            return 45.5 + 2.3 * inchesAbove5ft;
        }
    }

    public static double calculateIdealWeightRobinson(double heightCm, String gender) {
        double inchesAbove5ft = Math.max(0, (heightCm - 152.4) / 2.54);
        if (gender.equalsIgnoreCase("male")) {
            return 52 + 1.9 * inchesAbove5ft;
        } else {
            return 49 + 1.7 * inchesAbove5ft;
        }
    }

    public static double calculateIdealWeightMiller(double heightCm, String gender) {
        double inchesAbove5ft = Math.max(0, (heightCm - 152.4) / 2.54);
        if (gender.equalsIgnoreCase("male")) {
            return 56.2 + 1.41 * inchesAbove5ft;
        } else {
            return 53.1 + 1.36 * inchesAbove5ft;
        }
    }

    public static double calculateIdealWeightHamwi(double heightCm, String gender) {
        double inchesAbove5ft = Math.max(0, (heightCm - 152.4) / 2.54);
        if (gender.equalsIgnoreCase("male")) {
            return 48.0 + 2.7 * inchesAbove5ft;
        } else {
            return 45.5 + 2.2 * inchesAbove5ft;
        }
    }

    public static double[] calculateHealthyWeightRange(double heightCm) {
        double heightM = heightCm / 100.0;
        double minWeight = 18.5 * heightM * heightM;
        double maxWeight = 24.9 * heightM * heightM;
        return new double[] { minWeight, maxWeight };
    }

    public static double[] calculateMacros(double tdee, String goal) {
        return calculateMacrosForMedicalConditions(tdee, goal, null);
    }

    public static double[] calculateMacrosForMedicalConditions(double tdee, String goal, Set<MedicalCondition> conditions) {
        double proteinPct, carbsPct, fatPct;
        String goalNormalized = goal.toLowerCase().replace("_", " ");
        
        // Base percentages based on goal
        switch (goalNormalized) {
            case "lose weight", "lose fat" -> {
                proteinPct = 0.40;
                carbsPct = 0.30;
                fatPct = 0.30;
            }
            case "gain muscle" -> {
                proteinPct = 0.30;
                carbsPct = 0.50;
                fatPct = 0.20;
            }
            case "recomp", "maintain" -> {
                proteinPct = 0.35;
                carbsPct = 0.35;
                fatPct = 0.30;
            }
            default -> {
                proteinPct = 0.30;
                carbsPct = 0.45;
                fatPct = 0.25;
            }
        }

        // Adjust based on medical conditions
        if (conditions != null && !conditions.isEmpty()) {
            for (MedicalCondition cond : conditions) {
                switch (cond) {
                    case DIABETES -> {
                        carbsPct -= 0.10; // Lower carbs for blood sugar
                        fatPct += 0.10;   // Prefer healthy fats
                    }
                    case KIDNEY_ISSUES -> {
                        proteinPct -= 0.15; // Lower protein to reduce work on kidneys
                        carbsPct += 0.15;
                    }
                    case PCOD, THYROID -> {
                        carbsPct -= 0.10;   // Lower carbs for insulin resistance
                        proteinPct += 0.10; // Higher protein for satiety
                    }
                    case OBESITY -> {
                        proteinPct += 0.10; // Satiety
                        carbsPct -= 0.05;
                        fatPct -= 0.05;
                    }
                    case HIGH_CHOLESTEROL, HEART_DISEASE -> {
                        fatPct -= 0.10;     // Lower fat
                        carbsPct += 0.10;   // Prefer complex carbs/fiber
                    }
                }
            }
        }

        // Safety Clamping (10% - 60%) to avoid irrational distributions
        proteinPct = Math.max(0.10, Math.min(0.60, proteinPct));
        carbsPct = Math.max(0.10, Math.min(0.60, carbsPct));
        fatPct = Math.max(0.10, Math.min(0.60, fatPct));

        // Normalize so sum is 1.0
        double sum = proteinPct + carbsPct + fatPct;
        proteinPct /= sum;
        carbsPct /= sum;
        fatPct /= sum;

        return new double[] {
                (tdee * proteinPct) / 4.0,
                (tdee * carbsPct) / 4.0,
                (tdee * fatPct) / 9.0
        };
    }

    public static double getGoalCalorieAdjustment(String goal) {
        return switch (goal) {
            case "Mild weight loss of 0.5 lb (0.25 kg) per week" -> -250;
            case "Weight loss of 1 lb (0.5 kg) per week" -> -500;
            case "Extreme weight loss of 2 lb (1 kg) per week" -> -1000;
            case "Mild weight gain of 0.5 lb (0.25 kg) per week" -> 250;
            case "Weight gain of 1 lb (0.5 kg) per week" -> 500;
            case "Extreme weight gain of 2 lb (1 kg) per week" -> 1000;
            default -> 0;
        };
    }

    public static double[] calculateAdvancedMacros(double targetCalories, String dietPlan) {
        double proteinPct, carbsPct, fatPct;
        double proteinMinPct, proteinMaxPct, carbsMinPct, carbsMaxPct, fatMinPct, fatMaxPct;

        switch (dietPlan) {
            case "Low Fat" -> {
                proteinPct = 0.25;
                proteinMinPct = 0.15;
                proteinMaxPct = 0.30;
                carbsPct = 0.60;
                carbsMinPct = 0.55;
                carbsMaxPct = 0.65;
                fatPct = 0.15;
                fatMinPct = 0.10;
                fatMaxPct = 0.20;
            }
            case "Low Carb" -> {
                proteinPct = 0.30;
                proteinMinPct = 0.25;
                proteinMaxPct = 0.35;
                carbsPct = 0.20;
                carbsMinPct = 0.10;
                carbsMaxPct = 0.30;
                fatPct = 0.50;
                fatMinPct = 0.40;
                fatMaxPct = 0.55;
            }
            case "High Protein" -> {
                proteinPct = 0.35;
                proteinMinPct = 0.30;
                proteinMaxPct = 0.40;
                carbsPct = 0.35;
                carbsMinPct = 0.25;
                carbsMaxPct = 0.45;
                fatPct = 0.30;
                fatMinPct = 0.25;
                fatMaxPct = 0.35;
            }
            default -> {
                proteinPct = 0.20;
                proteinMinPct = 0.10;
                proteinMaxPct = 0.35;
                carbsPct = 0.55;
                carbsMinPct = 0.45;
                carbsMaxPct = 0.65;
                fatPct = 0.25;
                fatMinPct = 0.20;
                fatMaxPct = 0.35;
            }
        }

        double protein = (targetCalories * proteinPct) / 4.0;
        double proteinMin = (targetCalories * proteinMinPct) / 4.0;
        double proteinMax = (targetCalories * proteinMaxPct) / 4.0;
        double carbs = (targetCalories * carbsPct) / 4.0;
        double carbsMin = (targetCalories * carbsMinPct) / 4.0;
        double carbsMax = (targetCalories * carbsMaxPct) / 4.0;
        double fat = (targetCalories * fatPct) / 9.0;
        double fatMin = (targetCalories * fatMinPct) / 9.0;
        double fatMax = (targetCalories * fatMaxPct) / 9.0;
        double sugar = targetCalories * 0.10 / 6.0;
        double satFat = targetCalories * 0.10 / 9.0;
        double kJ = targetCalories * 4.184;

        return new double[] {
                protein, proteinMin, proteinMax,
                carbs, carbsMin, carbsMax,
                fat, fatMin, fatMax,
                sugar, satFat, targetCalories, kJ
        };
    }

    public static String getActivityDescription(String level) {
        return switch (level) {
            case "Sedentary" -> "little or no exercise";
            case "Light" -> "exercise 1-3 times/week";
            case "Moderate" -> "exercise 4-5 times/week";
            case "Active" -> "daily exercise or intense exercise 3-4 times/week";
            case "Very Active" -> "intense exercise 6-7 times/week";
            case "Extra Active" -> "very intense exercise daily, or physical job";
            default -> "";
        };
    }

    public static double getExtendedTDEEMultiplier(String level) {
        return switch (level) {
            case "Sedentary" -> 1.2;
            case "Light" -> 1.375;
            case "Moderate" -> 1.55;
            case "Active" -> 1.725;
            case "Very Active" -> 1.9;
            case "Extra Active" -> 2.0;
            default -> 1.2;
        };
    }
}
