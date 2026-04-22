package com.ufit.model.enums;

public enum FitnessGoal {
    LOSE_FAT("Lose Fat", -500),
    GAIN_MUSCLE("Gain Muscle", 300),
    MAINTAIN("Maintain Weight", 0),
    RECOMP("Body Recomposition", 0);

    private final String displayName;
    private final int calorieAdjustment;

    FitnessGoal(String displayName, int calorieAdjustment) {
        this.displayName = displayName;
        this.calorieAdjustment = calorieAdjustment;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCalorieAdjustment() {
        return calorieAdjustment;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
