package com.ufit.model.enums;

/**
 * Medical conditions that affect dietary requirements.
 * Used by MedicalConditionFilter to evaluate food suitability.
 */
public enum MedicalCondition {
    DIABETES("Diabetes"),
    HYPERTENSION("High Blood Pressure"),
    HIGH_CHOLESTEROL("High Cholesterol"),
    LOW_BP("Low Blood Pressure"),
    KIDNEY_ISSUES("Kidney Issues"),
    PCOD("PCOD / Hormonal"),
    THYROID("Thyroid"),
    HEART_DISEASE("Heart Disease"),
    OBESITY("Obesity");

    private final String displayName;

    MedicalCondition(String displayName) {
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
