package com.ufit.model.enums;

public enum InjuryType {
    LOWER_BACK("Lower Back"),
    UPPER_BACK("Upper Back"),
    NECK("Neck"),
    SHOULDER("Shoulder"),
    ELBOW("Elbow"),
    WRIST("Wrist"),
    HIP("Hip"),
    KNEE("Knee"),
    ANKLE("Ankle"),
    ROTATOR_CUFF("Rotator Cuff");

    private final String displayName;

    InjuryType(String displayName) {
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
