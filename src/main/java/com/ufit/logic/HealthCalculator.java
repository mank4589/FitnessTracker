package com.ufit.logic;

public class HealthCalculator {

    // ═══════════════ BMI ═══════════════

    public static double calculateBMI(double weightKg, double heightM) {
        return weightKg / (Math.pow(heightM, 2));
    }

    public static String getBmiCategory(double bmi) {
        if (bmi < 16.0) return "Severe Underweight";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        if (bmi < 35.0) return "Obese Class I";
        if (bmi < 40.0) return "Obese Class II";
        return "Obese Class III";
    }

    // ═══════════════ BMR ═══════════════

    /** Mifflin-St Jeor formula */
    public static double calculateBMR(double weightKg, double heightM, int age, String gender) {
        double heightCm = heightM * 100;
        if (gender.equalsIgnoreCase("male")) {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            return (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
        }
    }

    /** Harris-Benedict (revised 1984) */
    public static double calculateHarrisBenedictBMR(double weightKg, double heightM, int age, String gender) {
        double heightCm = heightM * 100;
        if (gender.equalsIgnoreCase("male")) {
            return 88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age);
        } else {
            return 447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age);
        }
    }

    // ═══════════════ BODY FAT ═══════════════

    /** U.S. Navy body-fat estimation. All measurements in cm. */
    public static double calculateBodyFat(double waistCm, double neckCm, double heightCm, String gender) {
        if (gender.equalsIgnoreCase("male")) {
            return 495 / (1.0324 - 0.19077 * Math.log10(waistCm - neckCm)
                    + 0.15456 * Math.log10(heightCm)) - 450;
        } else {
            return 495 / (1.29579 - 0.35004 * Math.log10(waistCm - neckCm)
                    + 0.22100 * Math.log10(heightCm)) - 450;
        }
    }

    /** Backwards-compatible overload (assumes male) */
    public static double calculateBodyFat(double waistCm, double neckCm, double heightCm) {
        return calculateBodyFat(waistCm, neckCm, heightCm, "male");
    }

    public static String getBodyFatCategory(double bodyFat, String gender) {
        if (gender.equalsIgnoreCase("male")) {
            if (bodyFat < 6) return "Essential Fat";
            if (bodyFat < 14) return "Athletic";
            if (bodyFat < 18) return "Fitness";
            if (bodyFat < 25) return "Average";
            return "Above Average";
        } else {
            if (bodyFat < 14) return "Essential Fat";
            if (bodyFat < 21) return "Athletic";
            if (bodyFat < 25) return "Fitness";
            if (bodyFat < 32) return "Average";
            return "Above Average";
        }
    }

    // ═══════════════ TDEE ═══════════════

    public static double calculateTDEE(double bmr, String activityLevel) {
        return bmr * getTDEEMultiplier(activityLevel);
    }

    public static double getTDEEMultiplier(String activityLevel) {
        return switch (activityLevel.toLowerCase()) {
            case "light"        -> 1.375;
            case "moderate"     -> 1.55;
            case "active"       -> 1.725;
            case "extra active" -> 1.9;
            default             -> 1.2;   // sedentary
        };
    }

    // ═══════════════ IDEAL WEIGHT ═══════════════

    /** Devine formula (1974) */
    public static double calculateIdealWeight(double heightCm, String gender) {
        double inchesAbove5ft = Math.max(0, (heightCm - 152.4) / 2.54);
        if (gender.equalsIgnoreCase("male")) {
            return 50 + 2.3 * inchesAbove5ft;
        } else {
            return 45.5 + 2.3 * inchesAbove5ft;
        }
    }

    /** Robinson formula (1983) */
    public static double calculateIdealWeightRobinson(double heightCm, String gender) {
        double inchesAbove5ft = Math.max(0, (heightCm - 152.4) / 2.54);
        if (gender.equalsIgnoreCase("male")) {
            return 52 + 1.9 * inchesAbove5ft;
        } else {
            return 49 + 1.7 * inchesAbove5ft;
        }
    }

    /** Miller formula (1983) */
    public static double calculateIdealWeightMiller(double heightCm, String gender) {
        double inchesAbove5ft = Math.max(0, (heightCm - 152.4) / 2.54);
        if (gender.equalsIgnoreCase("male")) {
            return 56.2 + 1.41 * inchesAbove5ft;
        } else {
            return 53.1 + 1.36 * inchesAbove5ft;
        }
    }

    /** Hamwi formula (1964) */
    public static double calculateIdealWeightHamwi(double heightCm, String gender) {
        double inchesAbove5ft = Math.max(0, (heightCm - 152.4) / 2.54);
        if (gender.equalsIgnoreCase("male")) {
            return 48.0 + 2.7 * inchesAbove5ft;
        } else {
            return 45.5 + 2.2 * inchesAbove5ft;
        }
    }

    // ═══════════════ MACROS ═══════════════

    /** Returns [protein g, carbs g, fat g] based on TDEE and goal */
    public static double[] calculateMacros(double tdee, String goal) {
        double proteinPct, carbsPct, fatPct;
        switch (goal.toLowerCase()) {
            case "lose weight" -> { proteinPct = 0.40; carbsPct = 0.30; fatPct = 0.30; }
            case "gain muscle" -> { proteinPct = 0.30; carbsPct = 0.50; fatPct = 0.20; }
            default            -> { proteinPct = 0.30; carbsPct = 0.45; fatPct = 0.25; }
        }
        return new double[] {
            (tdee * proteinPct) / 4.0,   // protein: 4 cal/g
            (tdee * carbsPct) / 4.0,     // carbs: 4 cal/g
            (tdee * fatPct) / 9.0        // fat: 9 cal/g
        };
    }
}
