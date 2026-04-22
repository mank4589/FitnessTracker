package com.ufit.model.enums;

public enum DietaryRestriction {
    GLUTEN_FREE("Gluten Free"),
    DAIRY_FREE("Dairy Free"),
    NUT_FREE("Nut Free"),
    SOY_FREE("Soy Free"),
    EGG_FREE("Egg Free"),
    SHELLFISH_FREE("Shellfish Free"),
    FISH_FREE("Fish Free"),
    BEEF_FREE("Beef Free"),
    PORK_FREE("Pork Free"),
    LOW_SODIUM("Low Sodium"),
    LOW_SUGAR("Low Sugar"),
    HALAL("Halal"),
    KOSHER("Kosher");

    private final String displayName;

    DietaryRestriction(String displayName) {
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
