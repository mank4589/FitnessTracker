package com.ufit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufit.model.FoodItem;
import com.ufit.repository.FoodItemRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

/**
 * Loads food nutrition data from an embedded JSON resource (food-database.json)
 * into the local database at startup.
 */
@Service
public class FoodDatabaseSeeder {

    private final FoodItemRepository foodItemRepo;
    private final ObjectMapper objectMapper;

    private volatile boolean seedingComplete = false;
    private volatile int totalItems = 0;
    private volatile int completedItems = 0;
    private volatile String currentStatus = "Idle";

    public FoodDatabaseSeeder(FoodItemRepository foodItemRepo) {
        this.foodItemRepo = foodItemRepo;
        this.objectMapper = new ObjectMapper();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedOnStartup() {
        long count = foodItemRepo.count();
        if (count > 0) {
            // DB already has data — run upsert (inserts missing + updates classifications)
            currentStatus = "Database has " + count + " items. Syncing from JSON...";
            System.out.println("[FoodDB] " + currentStatus);
            Thread upsertThread = new Thread(this::upsertFromJson, "food-db-upsert");
            upsertThread.setDaemon(true);
            upsertThread.start();
        } else {
            // Fresh DB — run full seeding
            Thread seederThread = new Thread(this::runSeeding, "food-db-seeder");
            seederThread.setDaemon(true);
            seederThread.start();
        }
    }
    /**
     * Full upsert from JSON: inserts new items and updates ALL fields on existing ones.
     * This makes food-database.json the single source of truth.
     */
    private void upsertFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("food-database.json");
            List<Map<String, Object>> items;
            try (InputStream is = resource.getInputStream()) {
                items = objectMapper.readValue(is, new TypeReference<>() {});
            }

            totalItems = items.size();
            completedItems = 0;
            int updated = 0, inserted = 0;
            currentStatus = "Syncing from JSON... 0/" + totalItems;
            System.out.println("[FoodDB] Upserting " + totalItems + " items from JSON...");

            for (Map<String, Object> item : items) {
                String name = (String) item.get("name");
                if (name == null || name.isBlank()) {
                    completedItems++;
                    continue;
                }

                Optional<FoodItem> existing = foodItemRepo.findByNameIgnoreCase(name.trim());
                FoodItem food = existing.orElseGet(FoodItem::new);
                boolean isNew = existing.isEmpty();

                // Set ALL fields from JSON
                food.setName(name.trim());
                food.setCalories(toDouble(item.get("calories")));
                food.setServingSize(toDouble(item.get("serving_size_g")));
                food.setProtein(toDouble(item.get("protein_g")));
                food.setCarbs(toDouble(item.get("carbohydrates_total_g")));
                food.setFat(toDouble(item.get("fat_total_g")));
                food.setFiber(toDouble(item.get("fiber_g")));
                food.setSugar(toDouble(item.get("sugar_g")));
                food.setSodium(toDouble(item.get("sodium_mg")));
                food.setPotassium(toDouble(item.get("potassium_mg")));
                food.setCholesterol(toDouble(item.get("cholesterol_mg")));
                food.setSaturatedFat(toDouble(item.get("fat_saturated_g")));

                // Classification fields
                food.setCategory((String) item.getOrDefault("category", "general"));
                food.setDishType((String) item.getOrDefault("dishType", "ingredient"));
                food.setFoodGroup((String) item.get("foodGroup"));
                food.setCuisine((String) item.getOrDefault("cuisine", "UNIVERSAL"));

                Number recServing = (Number) item.get("recommendedServingG");
                if (recServing != null) food.setRecommendedServingG(recServing.doubleValue());

                String servDesc = (String) item.get("servingDescription");
                if (servDesc != null && !servDesc.isEmpty()) food.setServingDescription(servDesc);

                String dietStr = (String) item.get("dietaryCategory");
                if (dietStr != null && !dietStr.isEmpty()) {
                    try { food.setDietaryCategory(com.ufit.model.enums.DietaryPreference.valueOf(dietStr.toUpperCase())); } catch (Exception e) {}
                }

                Boolean isMeal = (Boolean) item.get("isMeal");
                food.setMeal(isMeal != null && isMeal);

                String typeStr = (String) item.get("mealType");
                if (typeStr != null && !typeStr.isEmpty()) {
                    try { food.setMealType(com.ufit.model.enums.MealType.valueOf(typeStr.toUpperCase())); } catch (Exception e) {}
                }

                foodItemRepo.save(food);

                if (isNew) {
                    inserted++;
                } else {
                    updated++;
                }

                completedItems++;
                if (completedItems % 200 == 0) {
                    currentStatus = "Syncing from JSON... " + completedItems + "/" + totalItems;
                }
            }

            seedingComplete = true;
            currentStatus = "Sync complete! " + inserted + " added, " + updated + " updated. Total: " + foodItemRepo.count();
            System.out.println("[FoodDB] " + currentStatus);

        } catch (Exception e) {
            seedingComplete = true;
            currentStatus = "Error syncing from JSON: " + e.getMessage();
            System.out.println("[FoodDB] " + currentStatus);
            e.printStackTrace();
        }
    }

    private void runSeeding() {
        currentStatus = "Loading embedded food database...";
        System.out.println("[FoodDB] Loading food data from embedded resource...");

        try {
            ClassPathResource resource = new ClassPathResource("food-database.json");
            List<Map<String, Object>> items;

            try (InputStream is = resource.getInputStream()) {
                items = objectMapper.readValue(is, new TypeReference<>() {});
            }

            totalItems = items.size();
            completedItems = 0;
            currentStatus = "Seeding food database... 0/" + totalItems;
            System.out.println("[FoodDB] Found " + totalItems + " food items in embedded database.");

            int successCount = 0;
            int skipCount = 0;

            for (Map<String, Object> item : items) {
                String name = (String) item.get("name");
                if (name == null || name.isBlank()) {
                    completedItems++;
                    continue;
                }

                if (foodItemRepo.findByNameIgnoreCase(name.trim()).isPresent()) {
                    skipCount++;
                    completedItems++;
                    currentStatus = "Seeding food database... " + completedItems + "/" + totalItems;
                    continue;
                }

                FoodItem food = new FoodItem();
                food.setName(name.trim());
                food.setCalories(toDouble(item.get("calories")));
                food.setServingSize(toDouble(item.get("serving_size_g")));
                food.setProtein(toDouble(item.get("protein_g")));
                food.setCarbs(toDouble(item.get("carbohydrates_total_g")));
                food.setFat(toDouble(item.get("fat_total_g")));
                food.setFiber(toDouble(item.get("fiber_g")));
                food.setSugar(toDouble(item.get("sugar_g")));
                food.setSodium(toDouble(item.get("sodium_mg")));
                food.setPotassium(toDouble(item.get("potassium_mg")));
                food.setCholesterol(toDouble(item.get("cholesterol_mg")));
                food.setSaturatedFat(toDouble(item.get("fat_saturated_g")));
                food.setCategory((String) item.getOrDefault("category", "general"));
                food.setDishType((String) item.getOrDefault("dishType", "ingredient"));
                food.setFoodGroup((String) item.get("foodGroup"));
                food.setCuisine((String) item.getOrDefault("cuisine", "UNIVERSAL"));

                // Parse recommendedServingG
                Number recServing = (Number) item.get("recommendedServingG");
                if (recServing != null) {
                    food.setRecommendedServingG(recServing.doubleValue());
                }

                // Parse servingDescription (e.g., "2 rotis", "1 bowl")
                String servDesc = (String) item.get("servingDescription");
                if (servDesc != null && !servDesc.isEmpty()) {
                    food.setServingDescription(servDesc);
                }

                // Parse dietaryCategory for ALL items (not just meals)
                String dietStr = (String) item.get("dietaryCategory");
                if (dietStr != null && !dietStr.isEmpty()) {
                    try { food.setDietaryCategory(com.ufit.model.enums.DietaryPreference.valueOf(dietStr.toUpperCase())); } catch (Exception e) {}
                }

                // Parse Meal fields
                Boolean isMeal = (Boolean) item.get("isMeal");
                food.setMeal(isMeal != null && isMeal);
                
                // Parse mealType for ALL items (not just meals)
                String typeStr = (String) item.get("mealType");
                if (typeStr != null && !typeStr.isEmpty()) {
                    try { food.setMealType(com.ufit.model.enums.MealType.valueOf(typeStr.toUpperCase())); } catch (Exception e) {}
                }
                
                if (food.isMeal()) {

                    List<String> tags = (List<String>) item.get("tags");
                    if (tags != null) food.setTags(new java.util.HashSet<>(tags));

                    List<String> restricts = (List<String>) item.get("satisfiesRestrictions");
                    if (restricts != null) {
                        java.util.Set<com.ufit.model.enums.DietaryRestriction> drSet = new java.util.HashSet<>();
                        for (String r : restricts) {
                            try { drSet.add(com.ufit.model.enums.DietaryRestriction.valueOf(r.toUpperCase())); } catch (Exception e) {}
                        }
                        food.setSatisfiesRestrictions(drSet);
                    }

                    Number prepTime = (Number) item.get("prepTimeMinutes");
                    if (prepTime != null) food.setPrepTimeMinutes(prepTime.intValue());

                    food.setDescription((String) item.get("description"));
                    food.setIngredients((String) item.get("ingredients"));
                }

                foodItemRepo.save(food);
                successCount++;

                completedItems++;
                currentStatus = "Seeding food database... " + completedItems + "/" + totalItems;
            }

            seedingComplete = true;
            currentStatus = "Database ready! " + successCount + " foods loaded (" + skipCount + " duplicates skipped).";
            System.out.println("[FoodDB] " + currentStatus);
            System.out.println("[FoodDB] Total food items in database: " + foodItemRepo.count());

        } catch (Exception e) {
            seedingComplete = true;
            currentStatus = "Error loading food database: " + e.getMessage();
            System.out.println("[FoodDB] " + currentStatus);
            e.printStackTrace();
        }
    }

    public void reseed() {
        seedingComplete = false;
        completedItems = 0;
        currentStatus = "Clearing database...";
        foodItemRepo.deleteAll();

        Thread seederThread = new Thread(this::runSeeding, "food-db-seeder");
        seederThread.setDaemon(true);
        seederThread.start();
    }

    /** Re-run full upsert from JSON without clearing the database */
    public void reseedClassifications() {
        seedingComplete = false;
        completedItems = 0;
        Thread updateThread = new Thread(this::upsertFromJson, "food-db-upsert");
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public boolean isSeedingComplete() { return seedingComplete; }
    public String getCurrentStatus() { return currentStatus; }
    public int getTotalItems() { return totalItems; }
    public int getCompletedItems() { return completedItems; }

    private double toDouble(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception e) { return 0; }
    }
}
