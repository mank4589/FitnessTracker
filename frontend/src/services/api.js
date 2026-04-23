import axios from 'axios';

const API = axios.create({
  baseURL: import.meta.env.VITE_API_URL || `http://${window.location.hostname}:8080/api`,
});

// ═══════════════ HEALTH CALCULATORS ═══════════════

export const calculateBmi = (data) => API.post('/health/bmi', data);
export const calculateBmr = (data) => API.post('/health/bmr', data);
export const calculateBodyFat = (data) => API.post('/health/body-fat', data);
export const calculateTdee = (data) => API.post('/health/tdee', data);
export const calculateMacros = (data) => API.post('/health/macros', data);
export const calculateIdealWeight = (data) => API.post('/health/ideal-weight', data);
export const calculateAll = (data) => API.post('/health/calculate-all', data);

// ═══════════════ HEALTH HISTORY ═══════════════

export const saveHealthReport = (data) => API.post('/health/report', data);
export const getHealthHistory = () => API.get('/health/history');
export const deleteHealthReport = (id) => API.delete(`/health/history/${id}`);

// ═══════════════ FOOD / CALORIE TRACKER (scoped by profileId) ═══════════════

export const suggestFood = (q, conditions) => {
  const params = { q };
  if (conditions && conditions.length > 0) params.conditions = conditions.join(',');
  return API.get('/food/suggest', { params });
};
export const searchFood = (query) => API.get('/food/search', { params: { query } });
export const logFood = (profileId, entry) => API.post(`/food/${profileId}/log`, entry);
export const getFoodLog = (profileId, date) => API.get(`/food/${profileId}/log/${date}`);
export const deleteFoodLog = (id) => API.delete(`/food/log/${id}`);
export const getDailySummary = (profileId, date) => API.get(`/food/${profileId}/summary/${date}`);
export const getWeeklySummary = (profileId, date) => API.get(`/food/${profileId}/weekly/${date}`);
export const setDailyGoal = (profileId, goal) => API.post(`/food/${profileId}/goal`, goal);
export const getDailyGoal = (profileId, date) => API.get(`/food/${profileId}/goal/${date}`);
export const getDbStatus = () => API.get('/food/db-status');

// ═══════════════ WATER INTAKE (scoped by profileId) ═══════════════

export const logWater = (profileId, data) => API.post(`/food/${profileId}/water`, data);
export const getWaterSummary = (profileId, date) => API.get(`/food/${profileId}/water/${date}`);
export const deleteWaterLog = (id) => API.delete(`/food/water/${id}`);

// ═══════════════ EXERCISE LOGGER (scoped by profileId) ═══════════════

export const searchExercises = (term, limit = 20, offset = 0) => API.get('/exercise/search', { params: { term, limit, offset } });
export const getExerciseDetail = (exerciseId) => API.get(`/exercise/detail/${exerciseId}`);
export const logExercise = (profileId, entry) => API.post(`/exercise/${profileId}/log`, entry);
export const getExerciseLog = (profileId, date) => API.get(`/exercise/${profileId}/log/${date}`);
export const deleteExerciseLog = (id) => API.delete(`/exercise/log/${id}`);
export const getExerciseSummary = (profileId, date) => API.get(`/exercise/${profileId}/summary/${date}`);

// ═══════════════ USER PROFILES ═══════════════

export const registerUser = (data) => API.post('/profiles/register', data);
export const loginUser = (data) => API.post('/profiles/login', data);
export const createProfile = (profile) => API.post('/profiles', profile);
export const getAllProfiles = () => API.get('/profiles');
export const getProfileById = (id) => API.get(`/profiles/${id}`);
export const getProfileByName = (name) => API.get(`/profiles/name/${name}`);
export const updateProfile = (id, profile) => API.put(`/profiles/${id}`, profile);
export const deleteProfile = (id) => API.delete(`/profiles/${id}`);
export const linkHealthSnapshot = (id) => API.post(`/profiles/${id}/link-health`);

// ═══════════════ MEAL PLANS ═══════════════

export const generateMealPlan = (profileId) => API.post(`/meal-plans/generate/${profileId}`);
export const generateMealPlanForWeek = (profileId, weekStartDate) => API.post(`/meal-plans/generate/${profileId}/week`, null, { params: { weekStartDate } });
export const getLatestMealPlan = (profileId) => API.get(`/meal-plans/latest/${profileId}`);
export const getAllMealPlans = (profileId) => API.get(`/meal-plans/profile/${profileId}`);
export const getAllMeals = () => API.get('/meal-plans/meals');
export const addCustomMeal = (meal) => API.post('/meal-plans/meals/custom', meal);
export const deleteCustomMeal = (mealId) => API.delete(`/meal-plans/meals/custom/${mealId}`);
export const deleteMealPlan = (planId) => API.delete(`/meal-plans/${planId}`);
export const createCustomMealPlan = (profileId, plan) => API.post(`/meal-plans/custom/${profileId}`, plan);
export const updateMealSlot = (planId, dayId, slot, foodIds) => API.put(`/meal-plans/${planId}/days/${dayId}/slot/${slot}`, foodIds);
export const addFoodToSlot = (planId, dayId, slot, foodId) => API.post(`/meal-plans/${planId}/days/${dayId}/slot/${slot}/add/${foodId}`);
export const removeFoodFromSlot = (planId, dayId, slot, foodId) => API.delete(`/meal-plans/${planId}/days/${dayId}/slot/${slot}/remove/${foodId}`);
export const reseedClassifications = () => API.post('/meal-plans/reseed-classifications');

// ═══════════════ WORKOUT RECOMMENDATIONS ═══════════════

export const getExercisesForMuscle = (muscle, profileId) => API.get(`/workouts/muscles/${muscle}/profile/${profileId}`);
export const getCompoundExercises = (muscle, profileId) => API.get(`/workouts/muscles/${muscle}/compound/profile/${profileId}`);
export const getIsolationExercises = (muscle, profileId) => API.get(`/workouts/muscles/${muscle}/isolation/profile/${profileId}`);
export const getCardioExercises = (profileId) => API.get(`/workouts/cardio/profile/${profileId}`);
export const getExercisesByDifficulty = (muscle, level, profileId) => API.get(`/workouts/muscles/${muscle}/difficulty/${level}/profile/${profileId}`);
export const getExercisesByEquipment = (equipment, profileId) => API.get(`/workouts/equipment/${equipment}/profile/${profileId}`);
export const getRecommendedWorkout = (profileId) => API.get(`/workouts/recommended/${profileId}`);
export const getPushPullLegsSplit = (profileId) => API.get(`/workouts/ppl/${profileId}`);
export const getAllExercises = () => API.get('/workout-plans/exercises');
export const addCustomExercise = (exercise) => API.post('/workouts/exercises/custom', exercise);
export const deleteCustomExercise = (exerciseId) => API.delete(`/workouts/exercises/custom/${exerciseId}`);

// ═══════════════ STRUCTURED WORKOUT PLANS ═══════════════

export const getWorkoutPlanTypes = () => API.get('/workout-plans/types');
export const generateWorkoutPlan = (profileId, planType = 'PUSH_PULL_LEGS') => 
  API.post(`/workout-plans/generate/${profileId}?planType=${planType}`);
export const getAllWorkoutPlans = (profileId) => API.get(`/workout-plans/profile/${profileId}`);
export const getActiveWorkoutPlan = (profileId) => API.get(`/workout-plans/active/${profileId}`);
export const getWorkoutPlanById = (planId) => API.get(`/workout-plans/${planId}`);
export const getCustomWorkoutPlans = (profileId) => API.get(`/workout-plans/custom/${profileId}`);
export const createCustomWorkoutPlan = (profileId, plan) => API.post(`/workout-plans/custom/${profileId}`, plan);
export const updateWorkoutPlan = (planId, updates) => API.put(`/workout-plans/${planId}`, updates);
export const deleteWorkoutPlan = (planId) => API.delete(`/workout-plans/${planId}`);
export const activateWorkoutPlan = (planId, profileId) => API.post(`/workout-plans/${planId}/activate/${profileId}`);

// Workout Day management
export const addWorkoutDay = (planId, day) => API.post(`/workout-plans/${planId}/days`, day);
export const updateWorkoutDay = (planId, dayId, updates) => API.put(`/workout-plans/${planId}/days/${dayId}`, updates);
export const deleteWorkoutDay = (planId, dayId) => API.delete(`/workout-plans/${planId}/days/${dayId}`);

// Exercise management within days
export const addExerciseToDay = (planId, dayId, exercise) => 
  API.post(`/workout-plans/${planId}/days/${dayId}/exercises`, exercise);
export const updatePlannedExercise = (planId, dayId, exerciseId, updates) => 
  API.put(`/workout-plans/${planId}/days/${dayId}/exercises/${exerciseId}`, updates);
export const removeExerciseFromDay = (planId, dayId, exerciseId) => 
  API.delete(`/workout-plans/${planId}/days/${dayId}/exercises/${exerciseId}`);
export const reorderExercises = (planId, dayId, exerciseIds) => 
  API.put(`/workout-plans/${planId}/days/${dayId}/reorder`, exerciseIds);

// Workout plan exercise search (different from exercise logger search)
export const searchWorkoutExercises = (query) => API.get(`/workout-plans/exercises/search?query=${encodeURIComponent(query)}`);
