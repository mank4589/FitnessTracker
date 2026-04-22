package com.ufit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_meal_plans")
public class DailyMealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "weekly_plan_id")
    @JsonIgnore
    private WeeklyMealPlan weeklyPlan;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    // Multi-item meals: each meal slot is a list of foods
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "daily_meal_breakfast",
        joinColumns = @JoinColumn(name = "daily_plan_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<FoodItem> breakfastItems = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "daily_meal_lunch",
        joinColumns = @JoinColumn(name = "daily_plan_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<FoodItem> lunchItems = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "daily_meal_dinner",
        joinColumns = @JoinColumn(name = "daily_plan_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<FoodItem> dinnerItems = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "daily_meal_snacks",
        joinColumns = @JoinColumn(name = "daily_plan_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<FoodItem> snacks = new ArrayList<>();

    // Calculated totals for the day
    private double totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFat;

    public DailyMealPlan() {}

    public DailyMealPlan(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WeeklyMealPlan getWeeklyPlan() { return weeklyPlan; }
    public void setWeeklyPlan(WeeklyMealPlan weeklyPlan) { this.weeklyPlan = weeklyPlan; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public List<FoodItem> getBreakfastItems() { return breakfastItems; }
    public void setBreakfastItems(List<FoodItem> breakfastItems) { this.breakfastItems = breakfastItems; }

    public List<FoodItem> getLunchItems() { return lunchItems; }
    public void setLunchItems(List<FoodItem> lunchItems) { this.lunchItems = lunchItems; }

    public List<FoodItem> getDinnerItems() { return dinnerItems; }
    public void setDinnerItems(List<FoodItem> dinnerItems) { this.dinnerItems = dinnerItems; }

    public List<FoodItem> getSnacks() { return snacks; }
    public void setSnacks(List<FoodItem> snacks) { this.snacks = snacks; }

    // Legacy single-item getters (for backward compat with frontend)
    // Returns the first item of each meal
    @Transient
    public FoodItem getBreakfast() { return breakfastItems.isEmpty() ? null : breakfastItems.get(0); }
    @Transient
    public FoodItem getLunch() { return lunchItems.isEmpty() ? null : lunchItems.get(0); }
    @Transient
    public FoodItem getDinner() { return dinnerItems.isEmpty() ? null : dinnerItems.get(0); }

    // Legacy setters
    public void setBreakfast(FoodItem item) {
        if (item != null) { this.breakfastItems.clear(); this.breakfastItems.add(item); }
    }
    public void setLunch(FoodItem item) {
        if (item != null) { this.lunchItems.clear(); this.lunchItems.add(item); }
    }
    public void setDinner(FoodItem item) {
        if (item != null) { this.dinnerItems.clear(); this.dinnerItems.add(item); }
    }

    public double getTotalCalories() { return totalCalories; }
    public void setTotalCalories(double totalCalories) { this.totalCalories = totalCalories; }

    public double getTotalProtein() { return totalProtein; }
    public void setTotalProtein(double totalProtein) { this.totalProtein = totalProtein; }

    public double getTotalCarbs() { return totalCarbs; }
    public void setTotalCarbs(double totalCarbs) { this.totalCarbs = totalCarbs; }

    public double getTotalFat() { return totalFat; }
    public void setTotalFat(double totalFat) { this.totalFat = totalFat; }

    public void calculateTotals() {
        totalCalories = 0;
        totalProtein = 0;
        totalCarbs = 0;
        totalFat = 0;

        for (FoodItem item : breakfastItems) {
            totalCalories += item.getServingCalories();
            totalProtein += item.getServingProtein();
            totalCarbs += item.getServingCarbs();
            totalFat += item.getServingFat();
        }
        for (FoodItem item : lunchItems) {
            totalCalories += item.getServingCalories();
            totalProtein += item.getServingProtein();
            totalCarbs += item.getServingCarbs();
            totalFat += item.getServingFat();
        }
        for (FoodItem item : dinnerItems) {
            totalCalories += item.getServingCalories();
            totalProtein += item.getServingProtein();
            totalCarbs += item.getServingCarbs();
            totalFat += item.getServingFat();
        }
        for (FoodItem snack : snacks) {
            totalCalories += snack.getServingCalories();
            totalProtein += snack.getServingProtein();
            totalCarbs += snack.getServingCarbs();
            totalFat += snack.getServingFat();
        }
    }
}
