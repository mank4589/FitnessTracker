import React, { useState, useEffect, useRef } from 'react';
import {
  suggestFood,
  createCustomMealPlan,
} from '../services/api';
import './CustomMealPlanBuilder.css';

const DISH_TYPE_LABELS = {
  curry: '🍛 Curry', dal: '🥘 Dal', sabzi: '🥬 Sabzi',
  egg_dish: '🍳 Egg', meat_dish: '🥩 Meat', rice_dish: '🍚 Rice',
  pasta: '🍝 Pasta', complete_meal: '🍽️ Full Meal', staple: '🫓 Staple',
  drink: '🥛 Drink', dessert: '🍰 Dessert', side: '🥗 Side',
  breakfast_item: '🍳 Breakfast', snack: '🍿 Snack', fruit: '🍎 Fruit',
  nut: '🥜 Nuts', condiment: '🧂 Condiment', ingredient: '📦 Ingredient',
};

const MEAL_SLOTS = [
  { key: 'breakfast', label: '🍳 Breakfast', pct: '30%' },
  { key: 'lunch', label: '🍱 Lunch', pct: '35%' },
  { key: 'snacks', label: '🍿 Snacks', pct: '10%' },
  { key: 'dinner', label: '🍽️ Dinner', pct: '25%' },
];

const DAYS_OF_WEEK = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY'];

const MEDICAL_CONDITIONS = [
  { key: 'DIABETES', label: '🩸 Diabetes' },
  { key: 'HYPERTENSION', label: '💓 High BP' },
  { key: 'HIGH_CHOLESTEROL', label: '🫀 Cholesterol' },
  { key: 'LOW_BP', label: '📉 Low BP' },
  { key: 'KIDNEY_ISSUES', label: '🫘 Kidney' },
  { key: 'PCOD', label: '♀️ PCOD' },
  { key: 'THYROID', label: '🦋 Thyroid' },
  { key: 'HEART_DISEASE', label: '❤️‍🩹 Heart' },
  { key: 'OBESITY', label: '⚖️ Obesity' },
];

const getDietBadge = (cat) => {
  switch (cat) {
    case 'VEGAN': return { label: '🌱 Vegan', cls: 'cmpb-badge-vegan' };
    case 'VEGETARIAN': return { label: '🥬 Veg', cls: 'cmpb-badge-veg' };
    case 'EGGETARIAN': return { label: '🥚 Egg', cls: 'cmpb-badge-egg' };
    case 'NON_VEGETARIAN': return { label: '🍖 Non-Veg', cls: 'cmpb-badge-nonveg' };
    default: return null;
  }
};

const CustomMealPlanBuilder = ({ profileId, onBack, onPlanCreated }) => {
  const [step, setStep] = useState(1);
  const [planName] = useState('My Custom Meal Plan');
  const [targetCalories, setTargetCalories] = useState('2000');
  const [targetProtein, setTargetProtein] = useState('150');
  const [targetCarbs, setTargetCarbs] = useState('250');
  const [targetFat, setTargetFat] = useState('65');
  const [selectedDayIndex, setSelectedDayIndex] = useState(0);
  const [activeSlot, setActiveSlot] = useState('breakfast');
  const [saving, setSaving] = useState(false);
  const [selectedConditions, setSelectedConditions] = useState([]);

  // 7 days × 4 slots
  const [dayMeals, setDayMeals] = useState(() =>
    DAYS_OF_WEEK.map(() => ({
      breakfast: [], lunch: [], snacks: [], dinner: [],
    }))
  );

  // Search state
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [showResults, setShowResults] = useState(false);

  const searchTimeoutRef = useRef(null);
  const searchBoxRef = useRef(null);

  // Close on outside click
  useEffect(() => {
    const handler = (e) => {
      if (searchBoxRef.current && !searchBoxRef.current.contains(e.target)) {
        setShowResults(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  // Debounced search
  const handleSearchInput = (val) => {
    setSearchQuery(val);
    if (searchTimeoutRef.current) clearTimeout(searchTimeoutRef.current);
    if (val.trim().length < 2) {
      setSearchResults([]);
      setShowResults(false);
      return;
    }
    searchTimeoutRef.current = setTimeout(async () => {
      setSearching(true);
      try {
        const { data } = await suggestFood(val, selectedConditions);
        setSearchResults(data);
        setShowResults(data.length > 0);
      } catch {
        setSearchResults([]);
      }
      setSearching(false);
    }, 250);
  };

  // Add food to current day / slot
  const addFood = (food) => {
    // Normalize API field names
    const normalized = {
      ...food,
      servingSize: food.serving_size_g || food.servingSize || 100,
      protein: food.protein_g || food.protein || 0,
      carbs: food.carbohydrates_total_g || food.carbs || 0,
      fat: food.fat_total_g || food.fat || 0,
    };
    setDayMeals(prev => {
      const updated = [...prev];
      const day = { ...updated[selectedDayIndex] };
      const slot = [...day[activeSlot]];
      // Avoid duplicate by id
      if (slot.some(f => f.id === normalized.id)) return prev;
      slot.push(normalized);
      day[activeSlot] = slot;
      updated[selectedDayIndex] = day;
      return updated;
    });
    setSearchQuery('');
    setSearchResults([]);
    setShowResults(false);
  };

  // Remove food from current day / slot
  const removeFood = (slot, index) => {
    setDayMeals(prev => {
      const updated = [...prev];
      const day = { ...updated[selectedDayIndex] };
      day[slot] = day[slot].filter((_, i) => i !== index);
      updated[selectedDayIndex] = day;
      return updated;
    });
  };

  // Calculate totals for a day
  const getDayTotals = (dayIndex) => {
    const day = dayMeals[dayIndex];
    const allFoods = [...day.breakfast, ...day.lunch, ...day.snacks, ...day.dinner];
    return allFoods.reduce((acc, f) => {
      const mult = (f.recommendedServingG && f.servingSize > 0)
        ? f.recommendedServingG / f.servingSize : 1;
      acc.calories += (f.calories || 0) * mult;
      acc.protein += (f.protein || f.protein_g || 0) * mult;
      acc.carbs += (f.carbs || f.carbohydrates_total_g || 0) * mult;
      acc.fat += (f.fat || f.fat_total_g || 0) * mult;
      return acc;
    }, { calories: 0, protein: 0, carbs: 0, fat: 0 });
  };

  // Calculate slot calories
  const getSlotCalories = (slot) => {
    const foods = dayMeals[selectedDayIndex][slot];
    return foods.reduce((sum, f) => {
      const mult = (f.recommendedServingG && f.servingSize > 0)
        ? f.recommendedServingG / f.servingSize : 1;
      return sum + (f.calories || 0) * mult;
    }, 0);
  };

  const getFoodServingCalories = (f) => {
    const mult = (f.recommendedServingG && f.servingSize > 0)
      ? f.recommendedServingG / f.servingSize : 1;
    return Math.round((f.calories || 0) * mult);
  };

  // Count total foods across current day
  const dayFoodCount = (dayIndex) => {
    const d = dayMeals[dayIndex];
    return d.breakfast.length + d.lunch.length + d.snacks.length + d.dinner.length;
  };

  // Save plan
  const handleSave = async () => {
    const hasAnyFood = dayMeals.some(d =>
      d.breakfast.length + d.lunch.length + d.snacks.length + d.dinner.length > 0
    );
    if (!hasAnyFood) {
      alert('Please add at least one food item to your plan');
      return;
    }

    setSaving(true);
    try {
      const plan = {
        targetCalories: parseInt(targetCalories) || 2000,
        targetProtein: parseFloat(targetProtein) || 150,
        targetCarbs: parseFloat(targetCarbs) || 250,
        targetFat: parseFloat(targetFat) || 65,
        dailyPlans: DAYS_OF_WEEK.map((dow, i) => ({
          dayOfWeek: dow,
          breakfastItems: dayMeals[i].breakfast.map(f => ({ id: f.id })),
          lunchItems: dayMeals[i].lunch.map(f => ({ id: f.id })),
          dinnerItems: dayMeals[i].dinner.map(f => ({ id: f.id })),
          snacks: dayMeals[i].snacks.map(f => ({ id: f.id })),
        })),
      };
      await createCustomMealPlan(profileId, plan);
      alert('Custom meal plan created successfully!');
      onPlanCreated();
    } catch (error) {
      console.error('Error saving meal plan:', error);
      alert('Error saving meal plan: ' + (error.response?.data?.message || error.message));
    } finally {
      setSaving(false);
    }
  };

  const currentDay = dayMeals[selectedDayIndex];
  const dayTotals = getDayTotals(selectedDayIndex);

  // ─── Step 1: Setup ────
  if (step === 1) {
    return (
      <div className="cmpb-container">
        <div className="cmpb-header">
          <button className="cmpb-back-btn" onClick={onBack}>← Back</button>
          <h2>Create Custom Meal Plan</h2>
        </div>
        <div className="cmpb-setup-form">
          <div className="cmpb-form-group">
            <label>Daily Calorie & Macro Targets</label>
            <div className="cmpb-targets-grid">
              <div className="cmpb-form-group">
                <label>Calories</label>
                <input type="number" value={targetCalories}
                  onChange={e => setTargetCalories(e.target.value)}
                  className="cmpb-input" />
              </div>
              <div className="cmpb-form-group">
                <label>Protein (g)</label>
                <input type="number" value={targetProtein}
                  onChange={e => setTargetProtein(e.target.value)}
                  className="cmpb-input" />
              </div>
              <div className="cmpb-form-group">
                <label>Carbs (g)</label>
                <input type="number" value={targetCarbs}
                  onChange={e => setTargetCarbs(e.target.value)}
                  className="cmpb-input" />
              </div>
              <div className="cmpb-form-group">
                <label>Fat (g)</label>
                <input type="number" value={targetFat}
                  onChange={e => setTargetFat(e.target.value)}
                  className="cmpb-input" />
              </div>
            </div>
          </div>

          {/* Medical Conditions */}
          <div className="cmpb-form-group">
            <label>Medical Conditions <span style={{fontSize:'0.75rem',opacity:0.6}}>(optional — filters unsafe foods)</span></label>
            <div className="cmpb-conditions-grid">
              {MEDICAL_CONDITIONS.map(({ key, label }) => (
                <button key={key}
                  className={`cmpb-condition-btn ${selectedConditions.includes(key) ? 'active' : ''}`}
                  onClick={() => setSelectedConditions(prev =>
                    prev.includes(key) ? prev.filter(c => c !== key) : [...prev, key]
                  )}
                >
                  {label}
                </button>
              ))}
            </div>
            {selectedConditions.length > 0 && (
              <p className="cmpb-conditions-note">
                ⚠️ Foods unsuitable for your conditions will be flagged or excluded from suggestions.
                <br/><em style={{fontSize:'0.7rem',opacity:0.5}}>Based on WHO / AHA / NKF guidelines. Not a substitute for medical advice.</em>
              </p>
            )}
          </div>

          <button className="cmpb-next-btn" onClick={() => setStep(2)}>
            Continue to Build Days →
          </button>
        </div>
      </div>
    );
  }

  // ─── Step 2: Build Days ────
  return (
    <div className="cmpb-container">
      <div className="cmpb-header">
        <button className="cmpb-back-btn" onClick={() => setStep(1)}>← Setup</button>
        <h2>{planName}</h2>
        <button className="cmpb-save-btn" onClick={handleSave} disabled={saving}>
          {saving ? 'Saving...' : '💾 Save Plan'}
        </button>
      </div>

      {/* Day Tabs */}
      <div className="cmpb-day-tabs">
        {DAYS_OF_WEEK.map((dow, i) => (
          <button
            key={dow}
            className={`cmpb-day-tab ${selectedDayIndex === i ? 'active' : ''}`}
            onClick={() => setSelectedDayIndex(i)}
          >
            <span>{dow.slice(0, 3)}</span>
            <span className="cmpb-tab-count">{dayFoodCount(i)} items</span>
          </button>
        ))}
      </div>

      <div className="cmpb-builder-layout">
        {/* Left: Meal Slots */}
        <div className="cmpb-day-editor">
          {MEAL_SLOTS.map(({ key, label, pct }) => (
            <div key={key} className={`cmpb-slot ${activeSlot === key ? 'active' : ''}`}
                 onClick={() => setActiveSlot(key)}>
              <div className="cmpb-slot-header">
                <span className="cmpb-slot-title">
                  {label} <span className="cmpb-slot-pct">{pct}</span>
                </span>
                <span className="cmpb-slot-cal">
                  {Math.round(getSlotCalories(key))} cal
                </span>
              </div>

              {currentDay[key].length === 0 ? (
                <div className="cmpb-slot-empty">
                  Search and add foods from the panel →
                </div>
              ) : (
                currentDay[key].map((food, idx) => {
                  const badge = getDietBadge(food.dietaryCategory);
                  const servG = food.recommendedServingG || food.servingSize || 100;
                  const isDrink = food.dishType === 'drink' || food.category === 'beverage';
                  const servLabel = food.servingDescription || (isDrink ? `${Math.round(servG)}ml` : `${Math.round(servG)}g`);
                  return (
                    <div key={`${food.id}-${idx}`} className="cmpb-food-card">
                      <div className="cmpb-food-top-row">
                        <h5 className="cmpb-food-name">{food.name}</h5>
                        <button className="cmpb-food-remove"
                          onClick={(e) => { e.stopPropagation(); removeFood(key, idx); }}>×</button>
                      </div>
                      <div className="cmpb-food-badges">
                        {badge && <span className={`cmpb-badge ${badge.cls}`}>{badge.label}</span>}
                        {food.dishType && food.dishType !== 'ingredient' && (
                          <span className="cmpb-badge cmpb-badge-dish">
                            {DISH_TYPE_LABELS[food.dishType] || food.dishType}
                          </span>
                        )}
                        {food.cuisine && food.cuisine !== 'UNIVERSAL' && (
                          <span className="cmpb-badge cmpb-badge-cuisine">{food.cuisine}</span>
                        )}
                        <span className="cmpb-badge cmpb-badge-serving">{servLabel}</span>
                      </div>
                      <div className="cmpb-food-macros">
                        <span>{getFoodServingCalories(food)} cal</span>
                        <span>P: {((food.protein || 0) * ((food.recommendedServingG || 100) / (food.servingSize || 100))).toFixed(1)}g</span>
                        <span>C: {((food.carbs || 0) * ((food.recommendedServingG || 100) / (food.servingSize || 100))).toFixed(1)}g</span>
                        <span>F: {((food.fat || 0) * ((food.recommendedServingG || 100) / (food.servingSize || 100))).toFixed(1)}g</span>
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          ))}

          {/* Day Summary */}
          <div className="cmpb-day-summary">
            <div className="cmpb-day-stat">
              <span className="cmpb-day-stat-val">{Math.round(dayTotals.calories)}</span>
              <span className="cmpb-day-stat-lbl">Calories</span>
            </div>
            <div className="cmpb-day-stat">
              <span className="cmpb-day-stat-val">{dayTotals.protein.toFixed(1)}g</span>
              <span className="cmpb-day-stat-lbl">Protein</span>
            </div>
            <div className="cmpb-day-stat">
              <span className="cmpb-day-stat-val">{dayTotals.carbs.toFixed(1)}g</span>
              <span className="cmpb-day-stat-lbl">Carbs</span>
            </div>
            <div className="cmpb-day-stat">
              <span className="cmpb-day-stat-val">{dayTotals.fat.toFixed(1)}g</span>
              <span className="cmpb-day-stat-lbl">Fat</span>
            </div>
          </div>
        </div>

        {/* Right: Search Panel */}
        <div className="cmpb-search-panel">
          <h3>🔍 Find Foods</h3>
          <p className="cmpb-search-hint">Search the food database to add items to your meal plan</p>

          {/* Slot filter buttons */}
          <div className="cmpb-slot-filter">
            {MEAL_SLOTS.map(({ key, label }) => (
              <button key={key}
                className={`cmpb-filter-btn ${activeSlot === key ? 'active' : ''}`}
                onClick={() => setActiveSlot(key)}
              >
                {label.split(' ')[0]} {label.split(' ').slice(1).join(' ')}
              </button>
            ))}
          </div>

          <div className="cmpb-search-box" ref={searchBoxRef}>
            <div className="cmpb-search-input-wrapper">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => handleSearchInput(e.target.value)}
                placeholder="Search foods (e.g. paneer, chicken, rice)..."
                className="cmpb-search-input"
                autoComplete="off"
              />
              {searching && <div className="cmpb-search-spinner"></div>}
            </div>

            {showResults && (
              <div className="cmpb-search-dropdown">
                {searchResults.length === 0 ? (
                  <div className="cmpb-search-msg">
                    🔍 No foods found for "{searchQuery}"
                  </div>
                ) : (
                  searchResults.map((food, i) => {
                    const badge = getDietBadge(food.dietaryCategory);
                    const servG = food.recommendedServingG || food.servingSize || 100;
                    const isDrink = food.dishType === 'drink' || food.category === 'beverage';
                    const servUnit = isDrink ? 'ml' : 'g';
                    return (
                      <div key={i} className={`cmpb-search-result ${food.healthScore != null && food.healthScore < 0.6 ? 'cmpb-result-warn' : ''}`} onClick={() => addFood(food)}>
                        <div className="cmpb-result-info">
                          <span className="cmpb-result-name">{food.name}</span>
                          <div className="cmpb-result-meta">
                            <span className="cmpb-result-cal">
                              {Math.round((food.calories || 0) * servG / (food.servingSize || 100))} cal
                              · {Math.round(servG)}{servUnit}
                            </span>
                            {badge && <span className={`cmpb-result-pill ${badge.cls}`}>{badge.label}</span>}
                            {food.dishType && food.dishType !== 'ingredient' && (
                              <span className="cmpb-result-pill cmpb-badge-dish">
                                {DISH_TYPE_LABELS[food.dishType] || food.dishType}
                              </span>
                            )}
                          </div>
                          {food.medicalWarnings && food.medicalWarnings.length > 0 && (
                            <div className="cmpb-medical-warnings">
                              {food.medicalWarnings.map((w, wi) => (
                                <span key={wi} className="cmpb-warning-pill">{w}</span>
                              ))}
                            </div>
                          )}
                        </div>
                        <button className="cmpb-add-btn">+</button>
                      </div>
                    );
                  })
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CustomMealPlanBuilder;
