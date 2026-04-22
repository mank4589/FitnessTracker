package com.ufit.model.enums;

public enum ExerciseType {
    COMPOUND("Compound"),
    ISOLATION("Isolation"),
    CARDIO("Cardio"),
    STRETCHING("Stretching"),
    PLYOMETRIC("Plyometric"),
    CALISTHENICS("Calisthenics");

    private final String displayName;

    ExerciseType(String displayName) {
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
