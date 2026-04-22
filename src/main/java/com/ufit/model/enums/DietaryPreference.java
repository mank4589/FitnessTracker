package com.ufit.model.enums;

public enum DietaryPreference {
    NONE("No Preference"),
    VEGETARIAN("Vegetarian"),
    EGGETARIAN("Vegetarian + Eggs"),
    NON_VEGETARIAN("Non-Vegetarian"),
    VEGAN("Vegan"),
    PESCATARIAN("Pescatarian"),
    KETO("Keto"),
    PALEO("Paleo"),
    MEDITERRANEAN("Mediterranean"),
    LOW_CARB("Low Carb"),
    HIGH_PROTEIN("High Protein");

    private final String displayName;

    DietaryPreference(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
