package com.ufit.model.enums;

public enum CuisinePreference {
    INDIAN("Indian"),
    CHINESE("Chinese"),
    JAPANESE("Japanese"),
    THAI("Thai"),
    ITALIAN("Italian"),
    MEXICAN("Mexican"),
    AMERICAN("American"),
    MEDITERRANEAN("Mediterranean"),
    KOREAN("Korean"),
    ALL("All Cuisines");

    private final String displayName;

    CuisinePreference(String displayName) {
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
