package com.ufit.repository;

import com.ufit.model.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    /** Fuzzy name search for the food search bar */
    List<FoodItem> findByNameContainingIgnoreCase(String query);

    /** Exact name match (used to avoid duplicate inserts) */
    Optional<FoodItem> findByNameIgnoreCase(String name);

    /** Browse by category */
    List<FoodItem> findByCategoryIgnoreCase(String category);

    /** Count items (used to check if DB is already seeded) */
    long count();

    /** Search with LIKE for partial matches, ordered by relevance (exact start first) */
    @Query("SELECT f FROM FoodItem f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :q, '%')) ORDER BY CASE WHEN LOWER(f.name) LIKE LOWER(CONCAT(:q, '%')) THEN 0 ELSE 1 END, f.name")
    List<FoodItem> searchByName(@Param("q") String query);

    // --- Legacy meal queries (kept for backward compatibility) ---

    @Query("SELECT f FROM FoodItem f WHERE f.isMeal = true AND f.mealType = :mealType AND f.calories BETWEEN :minCal AND :maxCal")
    List<FoodItem> findMealsByCalorieRange(@Param("minCal") double minCal, @Param("maxCal") double maxCal, @Param("mealType") com.ufit.model.enums.MealType mealType);

    @Query("SELECT f FROM FoodItem f WHERE f.isMeal = true AND f.mealType = :mealType AND f.dietaryCategory = :preference AND f.calories BETWEEN :minCal AND :maxCal")
    List<FoodItem> findMealsByCalorieRangeAndPreference(@Param("minCal") double minCal, @Param("maxCal") double maxCal, 
                                                    @Param("mealType") com.ufit.model.enums.MealType mealType, @Param("preference") com.ufit.model.enums.DietaryPreference preference);

    // --- New queries for the redesigned algorithm ---

    /** Find foods by categories in a calorie range */
    @Query("SELECT f FROM FoodItem f WHERE LOWER(f.category) IN :categories AND f.calories BETWEEN :minCal AND :maxCal")
    List<FoodItem> findByCategoriesAndCalorieRange(@Param("categories") List<String> categories, 
                                                   @Param("minCal") double minCal, 
                                                   @Param("maxCal") double maxCal);

    /** Find foods by calorie range only (no category filter) */
    @Query("SELECT f FROM FoodItem f WHERE f.calories BETWEEN :minCal AND :maxCal")
    List<FoodItem> findByCalorieRange(@Param("minCal") double minCal, @Param("maxCal") double maxCal);

    /** Find all foods in specific categories */
    @Query("SELECT f FROM FoodItem f WHERE LOWER(f.category) IN :categories")
    List<FoodItem> findByCategories(@Param("categories") List<String> categories);

    // --- DishType-based queries for template meal composition ---

    /** Find all foods with a specific dishType */
    @Query("SELECT f FROM FoodItem f WHERE f.dishType = :type")
    List<FoodItem> findByDishType(@Param("type") String type);

    /** Find all foods with dishType in a list */
    @Query("SELECT f FROM FoodItem f WHERE f.dishType IN :types")
    List<FoodItem> findByDishTypes(@Param("types") List<String> types);

    /** Find foods by dishType and dietary preference */
    @Query("SELECT f FROM FoodItem f WHERE f.dishType = :type AND (f.dietaryCategory = :pref OR f.dietaryCategory = 'VEGAN' OR f.dietaryCategory = 'VEGETARIAN' OR :pref = com.ufit.model.enums.DietaryPreference.NONE)")
    List<FoodItem> findByDishTypeAndDiet(@Param("type") String type, @Param("pref") com.ufit.model.enums.DietaryPreference pref);

    /** Find foods by cuisine */
    @Query("SELECT f FROM FoodItem f WHERE f.cuisine = :cuisine")
    List<FoodItem> findByCuisine(@Param("cuisine") String cuisine);
}
