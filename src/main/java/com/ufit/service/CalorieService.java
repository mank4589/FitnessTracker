package com.ufit.service;

import com.ufit.model.DailyGoal;
import com.ufit.model.FoodItem;
import com.ufit.model.FoodLog;
import com.ufit.model.WaterLog;
import com.ufit.model.enums.MedicalCondition;
import com.ufit.recommendation.MedicalConditionFilter;
import com.ufit.repository.DailyGoalRepository;
import com.ufit.repository.FoodItemRepository;
import com.ufit.repository.FoodLogRepository;
import com.ufit.repository.WaterLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Service
public class CalorieService {

    private final FoodLogRepository foodLogRepo;
    private final DailyGoalRepository goalRepo;
    private final FoodItemRepository foodItemRepo;
    private final WaterLogRepository waterLogRepo;
    private final FoodDatabaseSeeder foodDbSeeder;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${calorieninjas.api.key:}")
    private String apiKey;

    private static final String API_URL = "https://api.calorieninjas.com/v1/nutrition?query=";

    public CalorieService(FoodLogRepository foodLogRepo, DailyGoalRepository goalRepo,
                          FoodItemRepository foodItemRepo, WaterLogRepository waterLogRepo,
                          FoodDatabaseSeeder foodDbSeeder) {
        this.foodLogRepo = foodLogRepo;
        this.goalRepo = goalRepo;
        this.foodItemRepo = foodItemRepo;
        this.waterLogRepo = waterLogRepo;
        this.foodDbSeeder = foodDbSeeder;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    // ═══════════════ FOOD SUGGEST (Auto-complete) ═══════════════

    /**
     * Returns top 10 matching food names from local DB for auto-suggest dropdown.
     */
    public List<Map<String, Object>> suggestFood(String query) {
        return suggestFood(query, null);
    }

    /**
     * Suggest food with optional medical condition warnings.
     */
    public List<Map<String, Object>> suggestFood(String query, Set<MedicalCondition> conditions) {
        if (query == null || query.trim().length() < 2) return List.of();
        List<FoodItem> items = foodItemRepo.searchByName(query.trim());
        List<Map<String, Object>> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (FoodItem item : items) {
            String key = item.getName().toLowerCase();
            if (seen.contains(key)) continue;
            seen.add(key);
            Map<String, Object> map = foodItemToMap(item);

            // Add medical warnings if conditions specified
            if (conditions != null && !conditions.isEmpty()) {
                List<String> warnings = MedicalConditionFilter.getWarnings(item, conditions);
                double healthScore = MedicalConditionFilter.getHealthScore(item, conditions);
                if (!warnings.isEmpty()) map.put("medicalWarnings", warnings);
                map.put("healthScore", Math.round(healthScore * 100) / 100.0);
            }

            results.add(map);
            if (results.size() >= 10) break;
        }
        return results;
    }

    // ═══════════════ FOOD SEARCH (Local DB → API fallback) ═══════════════

    /**
     * Searches the local food database first. If results are found, returns them.
     * If no local results, falls back to CalorieNinjas API and caches the results
     * in the local database for future searches.
     */
    public List<Map<String, Object>> searchFood(String query) {
        // 1. Search local database first
        List<FoodItem> localResults = foodItemRepo.searchByName(query.trim());

        if (!localResults.isEmpty()) {
            List<Map<String, Object>> results = new ArrayList<>();
            for (FoodItem item : localResults) {
                results.add(foodItemToMap(item));
            }
            return results;
        }

        // 2. Fall back to CalorieNinjas API
        if (apiKey == null || apiKey.isBlank()) {
            return List.of(Map.of(
                "name", "No results found locally & API key not configured",
                "calories", 0,
                "error", "Please set calorieninjas.api.key in application.properties."
            ));
        }

        try {
            List<Map<String, Object>> apiResults = fetchFromApi(query);

            // 3. Cache API results into local DB for future searches
            cacheApiResults(apiResults);

            return apiResults;
        } catch (Exception e) {
            return List.of(Map.of(
                "name", "Error searching food",
                "calories", 0,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Force search via API (bypassing local DB), used when user wants fresh data.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchFromApi(String query) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + encodedQuery))
            .header("X-Api-Key", apiKey)
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> body = objectMapper.readValue(response.body(), Map.class);

        if (body != null && body.containsKey("items")) {
            return (List<Map<String, Object>>) body.get("items");
        }
        return List.of();
    }

    /** Cache API result maps into the local FoodItem table AND the JSON seed file */
    private void cacheApiResults(List<Map<String, Object>> items) {
        boolean addedNewItems = false;
        List<Map<String, Object>> newJsonEntries = new ArrayList<>();

        for (Map<String, Object> item : items) {
            String name = (String) item.get("name");
            if (name == null || name.isBlank()) continue;
            if (foodItemRepo.findByNameIgnoreCase(name.trim()).isPresent()) continue;

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
            food.setCategory("searched");

            foodItemRepo.save(food);
            
            // Prepare for JSON rewrite
            addedNewItems = true;
            newJsonEntries.add(foodItemToMap(food));
        }

        if (addedNewItems) {
            updateSeedFile(newJsonEntries);
        }
    }

    /** Append new items to the local JSON seed file */
    @SuppressWarnings("unchecked")
    private void updateSeedFile(List<Map<String, Object>> newItems) {
        try {
            // Path to the source JSON file
            Path jsonPath = Paths.get("src", "main", "resources", "food-database.json");
            if (!Files.exists(jsonPath)) return;

            // Read existing data
            String currentJson = Files.readString(jsonPath, StandardCharsets.UTF_8);
            List<Map<String, Object>> existingData = new ArrayList<>();
            if (!currentJson.isBlank()) {
                existingData = objectMapper.readValue(currentJson, List.class);
            }

            // Append new items
            existingData.addAll(newItems);

            // Write back formatted JSON
            String updatedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(existingData);
            Files.writeString(jsonPath, updatedJson, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Failed to update food-database.json: " + e.getMessage());
        }
    }

    /** Convert a FoodItem entity to the Map format the UI expects */
    private Map<String, Object> foodItemToMap(FoodItem item) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", item.getId());
        map.put("name", item.getName());
        map.put("calories", item.getCalories());
        map.put("serving_size_g", item.getServingSize());
        map.put("protein_g", item.getProtein());
        map.put("carbohydrates_total_g", item.getCarbs());
        map.put("fat_total_g", item.getFat());
        map.put("fiber_g", item.getFiber());
        map.put("sugar_g", item.getSugar());
        map.put("sodium_mg", item.getSodium());
        map.put("potassium_mg", item.getPotassium());
        map.put("cholesterol_mg", item.getCholesterol());
        map.put("fat_saturated_g", item.getSaturatedFat());
        // Classification fields
        if (item.getDietaryCategory() != null) {
            map.put("dietaryCategory", item.getDietaryCategory().name());
        }
        if (item.getDishType() != null) {
            map.put("dishType", item.getDishType());
        }
        if (item.getCuisine() != null) {
            map.put("cuisine", item.getCuisine());
        }
        if (item.getMealType() != null) {
            map.put("mealType", item.getMealType().name());
        }
        if (item.getFoodGroup() != null) {
            map.put("foodGroup", item.getFoodGroup());
        }
        if (item.getCategory() != null) {
            map.put("category", item.getCategory());
        }
        map.put("isMeal", item.isMeal());
        // Serving info
        if (item.getRecommendedServingG() != null && item.getRecommendedServingG() > 0) {
            map.put("recommendedServingG", item.getRecommendedServingG());
        }
        if (item.getServingDescription() != null) {
            map.put("servingDescription", item.getServingDescription());
        }
        return map;
    }

    /** Get total food items in local database */
    public long getLocalFoodCount() {
        return foodItemRepo.count();
    }

    /** Get the database seeder for status checking */
    public FoodDatabaseSeeder getSeeder() {
        return foodDbSeeder;
    }

    // ═══════════════ FOOD LOG (with profileId) ═══════════════

    public FoodLog logFood(Long profileId, FoodLog entry) {
        if (entry.getLogDate() == null) {
            entry.setLogDate(LocalDate.now());
        }
        entry.setProfileId(profileId);
        return foodLogRepo.save(entry);
    }

    public List<FoodLog> getLogByDate(Long profileId, LocalDate date) {
        return foodLogRepo.findByProfileIdAndLogDateOrderByCreatedAtAsc(profileId, date);
    }

    public List<FoodLog> getLogByDateAndMeal(Long profileId, LocalDate date, String mealType) {
        return foodLogRepo.findByProfileIdAndLogDateAndMealTypeOrderByCreatedAtAsc(profileId, date, mealType);
    }

    public void deleteLogEntry(Long id) {
        foodLogRepo.deleteById(id);
    }

    // ═══════════════ DAILY SUMMARY (with profileId) ═══════════════

    public Map<String, Object> getDailySummary(Long profileId, LocalDate date) {
        List<FoodLog> logs = getLogByDate(profileId, date);

        double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0, totalFiber = 0;
        Map<String, List<FoodLog>> mealGroups = new LinkedHashMap<>();
        mealGroups.put("breakfast", new ArrayList<>());
        mealGroups.put("lunch", new ArrayList<>());
        mealGroups.put("dinner", new ArrayList<>());
        mealGroups.put("snack", new ArrayList<>());

        for (FoodLog log : logs) {
            totalCalories += log.getCalories();
            totalProtein += log.getProtein();
            totalCarbs += log.getCarbs();
            totalFat += log.getFat();
            totalFiber += log.getFiber();

            String meal = log.getMealType() != null ? log.getMealType().toLowerCase() : "snack";
            mealGroups.computeIfAbsent(meal, k -> new ArrayList<>()).add(log);
        }

        // Get goals for this date (using the persistent logic from getGoal)
        DailyGoal goal = getGoal(profileId, date);

        // Water intake
        Double waterIntakeRaw = waterLogRepo.sumAmountByProfileIdAndDate(profileId, date);
        double waterIntake = waterIntakeRaw != null ? waterIntakeRaw : 0.0;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("date", date.toString());
        summary.put("totalCalories", round(totalCalories));
        summary.put("totalProtein", round(totalProtein));
        summary.put("totalCarbs", round(totalCarbs));
        summary.put("totalFat", round(totalFat));
        summary.put("totalFiber", round(totalFiber));
        summary.put("calorieGoal", goal.getCalorieGoal());
        summary.put("proteinGoal", goal.getProteinGoal());
        summary.put("carbsGoal", goal.getCarbsGoal());
        summary.put("fatGoal", goal.getFatGoal());
        summary.put("waterGoal", goal.getWaterGoal());
        summary.put("waterIntake", round(waterIntake));
        summary.put("caloriesRemaining", round(goal.getCalorieGoal() - totalCalories));
        summary.put("caloriePercent", round((totalCalories / goal.getCalorieGoal()) * 100));
        summary.put("meals", mealGroups);
        summary.put("totalEntries", logs.size());

        return summary;
    }

    // ═══════════════ WEEKLY SUMMARY (with profileId) ═══════════════

    public List<Map<String, Object>> getWeeklySummary(Long profileId, LocalDate endDate) {
        LocalDate startDate = endDate.minusDays(6);
        List<FoodLog> logs = foodLogRepo.findByProfileIdAndLogDateBetweenOrderByLogDateAsc(profileId, startDate, endDate);

        Map<LocalDate, Double> dailyCalories = new LinkedHashMap<>();
        for (int i = 0; i <= 6; i++) {
            dailyCalories.put(startDate.plusDays(i), 0.0);
        }

        for (FoodLog log : logs) {
            dailyCalories.merge(log.getLogDate(), log.getCalories(), Double::sum);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Double> entry : dailyCalories.entrySet()) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", entry.getKey().toString());
            day.put("calories", round(entry.getValue()));
            result.add(day);
        }

        return result;
    }

    // ═══════════════ DAILY GOAL (with profileId) ═══════════════

    public DailyGoal setGoal(Long profileId, DailyGoal goal) {
        if (goal.getGoalDate() == null) {
            goal.setGoalDate(LocalDate.now());
        }
        goal.setProfileId(profileId);
        Optional<DailyGoal> existing = goalRepo.findByProfileIdAndGoalDate(profileId, goal.getGoalDate());
        if (existing.isPresent()) {
            DailyGoal existingGoal = existing.get();
            existingGoal.setCalorieGoal(goal.getCalorieGoal());
            existingGoal.setProteinGoal(goal.getProteinGoal());
            existingGoal.setCarbsGoal(goal.getCarbsGoal());
            existingGoal.setFatGoal(goal.getFatGoal());
            existingGoal.setWaterGoal(goal.getWaterGoal());
            return goalRepo.save(existingGoal);
        }
        return goalRepo.save(goal);
    }

    public DailyGoal getGoal(Long profileId, LocalDate date) {
        return goalRepo.findByProfileIdAndGoalDate(profileId, date).orElseGet(() -> {
            // If no goal for today, try to find the most recent past goal
            Optional<DailyGoal> pastGoalOpt = goalRepo.findFirstByProfileIdAndGoalDateBeforeOrderByGoalDateDesc(profileId, date);
            
            DailyGoal defaultGoal = new DailyGoal();
            defaultGoal.setProfileId(profileId);
            if (pastGoalOpt.isPresent()) {
                DailyGoal pastGoal = pastGoalOpt.get();
                defaultGoal.setCalorieGoal(pastGoal.getCalorieGoal());
                defaultGoal.setProteinGoal(pastGoal.getProteinGoal());
                defaultGoal.setCarbsGoal(pastGoal.getCarbsGoal());
                defaultGoal.setFatGoal(pastGoal.getFatGoal());
                defaultGoal.setWaterGoal(pastGoal.getWaterGoal());
            } else {
                // Hardcoded defaults if no past goals exist at all
                defaultGoal.setCalorieGoal(2000);
                defaultGoal.setProteinGoal(150);
                defaultGoal.setCarbsGoal(250);
                defaultGoal.setFatGoal(65);
                defaultGoal.setWaterGoal(2000.0);
            }
            return defaultGoal;
        });
    }

    // ═══════════════ WATER INTAKE (with profileId) ═══════════════

    public WaterLog logWater(Long profileId, LocalDate date, double amountMl) {
        WaterLog log = new WaterLog();
        log.setProfileId(profileId);
        log.setLogDate(date);
        log.setAmountMl(amountMl);
        return waterLogRepo.save(log);
    }

    public Map<String, Object> getWaterSummary(Long profileId, LocalDate date) {
        Double totalRaw = waterLogRepo.sumAmountByProfileIdAndDate(profileId, date);
        double total = totalRaw != null ? totalRaw : 0.0;
        DailyGoal goal = getGoal(profileId, date);
        List<WaterLog> logs = waterLogRepo.findByProfileIdAndLogDateOrderByCreatedAtAsc(profileId, date);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", date.toString());
        result.put("totalMl", round(total));
        result.put("goalMl", goal.getWaterGoal());
        result.put("glasses", round(total / 250.0));
        result.put("percent", round((total / goal.getWaterGoal()) * 100));
        result.put("logs", logs);
        return result;
    }

    public void deleteWaterLog(Long id) {
        waterLogRepo.deleteById(id);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private double toDouble(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception e) { return 0; }
    }
}
