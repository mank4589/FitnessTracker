package com.ufit.model;

import jakarta.persistence.*;

/**
 * Represents a cached food item in the local nutrition database.
 * Data is pulled from the CalorieNinjas API and stored locally
 * so subsequent searches are instant and work offline.
 */
@Entity
@Table(indexes = {
    @Index(name = "idx_food_name", columnList = "name")
})
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private double calories;       // kcal
    private double servingSize;    // grams
    private double protein;        // grams
    private double carbs;          // grams (total_carbohydrates)
    private double fat;            // grams (total_fat)
    private double fiber;          // grams
    private double sugar;          // grams
    private double sodium;         // mg
    private double potassium;      // mg
    private double cholesterol;    // mg
    private double saturatedFat;   // grams

    /** Recommended serving size in grams (e.g., peanut butter = 32g, biryani = 250g) */
    private Double recommendedServingG;

    /** Human-readable serving description (e.g., '2 rotis', '1 bowl', '5 eggs') */
    private String servingDescription;

    /** Category for quick browsing: fruit, vegetable, grain, dairy, meat, snack, indian, beverage, etc. */
    private String category;

    /** Dish type: curry, dal, sabzi, egg_dish, meat_dish, snack, dessert, drink, staple, fruit, nut, side, condiment, ingredient, pasta, rice_dish, breakfast_item, complete_meal */
    private String dishType;

    /** Food group for duplicate prevention (e.g., 'milk' for milk/skim milk/whole milk) */
    private String foodGroup;

    /** Cuisine: INDIAN, CHINESE, JAPANESE, THAI, ITALIAN, MEXICAN, KOREAN, AMERICAN, MEDITERRANEAN, UNIVERSAL */
    private String cuisine;

    // --- MEAL ATTRIBUTES (Populated only if isMeal = true) ---
    private Boolean isMeal = false;

    @Enumerated(EnumType.STRING)
    private com.ufit.model.enums.MealType mealType;

    @Enumerated(EnumType.STRING)
    private com.ufit.model.enums.DietaryPreference dietaryCategory = com.ufit.model.enums.DietaryPreference.NONE;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "food_tags", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "tag")
    private java.util.Set<String> tags = new java.util.HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "food_restrictions", joinColumns = @JoinColumn(name = "food_id"))
    @Column(name = "restriction")
    private java.util.Set<com.ufit.model.enums.DietaryRestriction> satisfiesRestrictions = new java.util.HashSet<>();

    private Integer prepTimeMinutes;
    private Boolean isCustom = false;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String ingredients;


    public FoodItem() {}

    public FoodItem(String name) {
        this.name = name;
    }

    // --- GETTERS AND SETTERS ---

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getServingSize() { return servingSize; }
    public void setServingSize(double servingSize) { this.servingSize = servingSize; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }

    public double getFiber() { return fiber; }
    public void setFiber(double fiber) { this.fiber = fiber; }

    public double getSugar() { return sugar; }
    public void setSugar(double sugar) { this.sugar = sugar; }

    public double getSodium() { return sodium; }
    public void setSodium(double sodium) { this.sodium = sodium; }

    public double getPotassium() { return potassium; }
    public void setPotassium(double potassium) { this.potassium = potassium; }

    public double getCholesterol() { return cholesterol; }
    public void setCholesterol(double cholesterol) { this.cholesterol = cholesterol; }

    public double getSaturatedFat() { return saturatedFat; }
    public void setSaturatedFat(double saturatedFat) { this.saturatedFat = saturatedFat; }

    public Double getRecommendedServingG() { return recommendedServingG; }
    public void setRecommendedServingG(Double recommendedServingG) { this.recommendedServingG = recommendedServingG; }

    public String getServingDescription() { return servingDescription; }
    public void setServingDescription(String servingDescription) { this.servingDescription = servingDescription; }

    public String getDishType() { return dishType; }
    public void setDishType(String dishType) { this.dishType = dishType; }

    public String getFoodGroup() { return foodGroup; }
    public void setFoodGroup(String foodGroup) { this.foodGroup = foodGroup; }

    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }

    /** Get the multiplier to scale per-100g values to recommended serving */
    public double getServingMultiplier() {
        if (recommendedServingG != null && recommendedServingG > 0 && servingSize > 0) {
            return recommendedServingG / servingSize;
        }
        return 1.0; // Default: use as-is (per 100g)
    }

    /** Calories for one recommended serving */
    public double getServingCalories() {
        return calories * getServingMultiplier();
    }

    /** Protein for one recommended serving */
    public double getServingProtein() {
        return protein * getServingMultiplier();
    }

    /** Carbs for one recommended serving */
    public double getServingCarbs() {
        return carbs * getServingMultiplier();
    }

    /** Fat for one recommended serving */
    public double getServingFat() {
        return fat * getServingMultiplier();
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // Meal fields getters and setters
    public Boolean isMeal() { return isMeal != null && isMeal; }
    public void setMeal(Boolean meal) { isMeal = meal; }

    public com.ufit.model.enums.MealType getMealType() { return mealType; }
    public void setMealType(com.ufit.model.enums.MealType mealType) { this.mealType = mealType; }

    public com.ufit.model.enums.DietaryPreference getDietaryCategory() { return dietaryCategory; }
    public void setDietaryCategory(com.ufit.model.enums.DietaryPreference dietaryCategory) { this.dietaryCategory = dietaryCategory; }

    public java.util.Set<String> getTags() { return tags; }
    public void setTags(java.util.Set<String> tags) { this.tags = tags; }

    public java.util.Set<com.ufit.model.enums.DietaryRestriction> getSatisfiesRestrictions() { return satisfiesRestrictions; }
    public void setSatisfiesRestrictions(java.util.Set<com.ufit.model.enums.DietaryRestriction> satisfiesRestrictions) { this.satisfiesRestrictions = satisfiesRestrictions; }

    public Integer getPrepTimeMinutes() { return prepTimeMinutes != null ? prepTimeMinutes : 0; }
    public void setPrepTimeMinutes(Integer prepTimeMinutes) { this.prepTimeMinutes = prepTimeMinutes; }

    public Boolean isCustom() { return isCustom != null && isCustom; }
    public void setCustom(Boolean custom) { isCustom = custom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public boolean satisfiesAllRestrictions(java.util.Set<com.ufit.model.enums.DietaryRestriction> requiredRestrictions) {
        if (requiredRestrictions == null || requiredRestrictions.isEmpty()) {
            return true;
        }
        return this.satisfiesRestrictions.containsAll(requiredRestrictions);
    }

    @Override
    public String toString() {
        return name + " (" + calories + " kcal, " + servingSize + "g)";
    }
}
