package com.ufit.model.enums;

public enum Equipment {
    NONE("No Equipment"),
    DUMBBELLS("Dumbbells"),
    BARBELL("Barbell"),
    KETTLEBELL("Kettlebell"),
    RESISTANCE_BAND("Resistance Band"),
    CABLE_MACHINE("Cable Machine"),
    MACHINE("Machine"),
    PULL_UP_BAR("Pull-up Bar"),
    BENCH("Bench"),
    STABILITY_BALL("Stability Ball"),
    FOAM_ROLLER("Foam Roller"),
    TREADMILL("Treadmill"),
    STATIONARY_BIKE("Stationary Bike"),
    ROWING_MACHINE("Rowing Machine"),
    JUMP_ROPE("Jump Rope");

    private final String displayName;

    Equipment(String displayName) {
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
