package com.ufit.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class DailyGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private LocalDate goalDate;

    private double calorieGoal;
    private double proteinGoal;    // grams
    private double carbsGoal;      // grams
    private double fatGoal;        // grams
    
    @Column(name = "water_goal")
    private Double waterGoal;      // milliliters (default 2000ml = ~8 glasses)

    public DailyGoal() {
        this.goalDate = LocalDate.now();
        this.waterGoal = 2000.0;
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }

    public LocalDate getGoalDate() { return goalDate; }
    public void setGoalDate(LocalDate goalDate) { this.goalDate = goalDate; }

    public double getCalorieGoal() { return calorieGoal; }
    public void setCalorieGoal(double calorieGoal) { this.calorieGoal = calorieGoal; }

    public double getProteinGoal() { return proteinGoal; }
    public void setProteinGoal(double proteinGoal) { this.proteinGoal = proteinGoal; }

    public double getCarbsGoal() { return carbsGoal; }
    public void setCarbsGoal(double carbsGoal) { this.carbsGoal = carbsGoal; }

    public double getFatGoal() { return fatGoal; }
    public void setFatGoal(double fatGoal) { this.fatGoal = fatGoal; }

    public Double getWaterGoal() { return waterGoal != null ? waterGoal : 2000.0; }
    public void setWaterGoal(Double waterGoal) { this.waterGoal = waterGoal; }
}
