package com.ufit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ufit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Inputs (The numbers the user provides) ---
    private double weight;
    private double height;
    private int age;
    private String gender;
    private double waist;
    private double neck;
    private String activityLevel;

    // --- Results (The numbers your logic calculates) ---
    private double bmi;
    private double bmr;
    private double bodyFat;
    private double tdee;
    private double idealWeight;

    private LocalDateTime entryDate;

    // Constructor
    public ufit() {
        this.entryDate = LocalDateTime.now();
    }

    // --- GETTERS AND SETTERS ---
    // These allow ufitService to "get" your inputs and "set" the results.

    public Long getId() { return id; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public double getWaist() { return waist; }
    public void setWaist(double waist) { this.waist = waist; }

    public double getNeck() { return neck; }
    public void setNeck(double neck) { this.neck = neck; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public double getBmr() { return bmr; }
    public void setBmr(double bmr) { this.bmr = bmr; }

    public double getBodyFat() { return bodyFat; }
    public void setBodyFat(double bodyFat) { this.bodyFat = bodyFat; }

    public double getTdee() { return tdee; }
    public void setTdee(double tdee) { this.tdee = tdee; }

    public double getIdealWeight() { return idealWeight; }
    public void setIdealWeight(double idealWeight) { this.idealWeight = idealWeight; }

    public LocalDateTime getEntryDate() { return entryDate; }
}