import { useState, useEffect, useRef, useCallback } from 'react';
import { suggestFood, searchFood, logFood, deleteFoodLog, getDailySummary, setDailyGoal, getDbStatus, logWater, getWaterSummary } from '../services/api';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const MEALS = ['breakfast', 'lunch', 'dinner', 'snack'];

const SERVING_UNITS = [
  { id: 'grams',      label: 'Grams',       defaultQty: 100, gramsPerUnit: 1 },
  { id: 'ml',         label: 'ml',           defaultQty: 250, gramsPerUnit: 1 },
  { id: 'glass',      label: 'Glass (250ml)',defaultQty: 1,   gramsPerUnit: 250 },
  { id: 'cup',        label: 'Cup (240ml)',  defaultQty: 1,   gramsPerUnit: 240 },
  { id: 'bowl',       label: 'Bowl (250g)',  defaultQty: 1,   gramsPerUnit: 250 },
  { id: 'tablespoon', label: 'Tbsp (15g)',   defaultQty: 1,   gramsPerUnit: 15 },
  { id: 'piece',      label: 'Piece',        defaultQty: 1,   gramsPerUnit: null },
];

const DRINK_UNITS = ['ml', 'glass', 'cup'];

const getDietBadge = (cat) => {
  switch (cat) {
    case 'VEGAN': return { label: '🌱 Vegan', cls: 'badge-vegan' };
    case 'VEGETARIAN': return { label: '🥬 Veg', cls: 'badge-veg' };
    case 'EGGETARIAN': return { label: '🥚 Egg', cls: 'badge-egg' };
    case 'NON_VEGETARIAN': return { label: '🍖 Non-Veg', cls: 'badge-nonveg' };
    case 'NONE': case null: case undefined: return null;
    default: return null;
  }
};

export default function CalorieTracker() {
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [summary, setSummary] = useState(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [suggestions, setSuggestions] = useState([]);
  const [searchResults, setSearchResults] = useState([]);
  const [mealType, setMealType] = useState('breakfast');
  const [goalForm, setGoalForm] = useState({ calories: '2000', protein: '150', carbs: '250', fat: '65', water: '2000' });
  const [showGoalForm, setShowGoalForm] = useState(false);
  const [dbStatus, setDbStatus] = useState('');
  const [searching, setSearching] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);

  // Serving customizer
  const [selectedFood, setSelectedFood] = useState(null);
  const [servingUnit, setServingUnit] = useState('grams');
  const [servingQty, setServingQty] = useState('100');

  // Water
  const [waterData, setWaterData] = useState({ totalMl: 0, goalMl: 2000, percent: 0, glasses: 0 });

  const suggestTimeoutRef = useRef(null);
  const searchBoxRef = useRef(null);

  // ─── Data loading ──────────────────────
  const loadSummary = async () => {
    try {
      const { data } = await getDailySummary(date);
      setSummary(data);
      setGoalForm({
        calories: String(data.calorieGoal),
        protein: String(data.proteinGoal),
        carbs: String(data.carbsGoal),
        fat: String(data.fatGoal),
        water: String(data.waterGoal || 2000),
      });
    } catch { /* ignore */ }
  };

  const loadWater = async () => {
    try {
      const { data } = await getWaterSummary(date);
      setWaterData(data);
    } catch { /* ignore */ }
  };

  useEffect(() => { loadSummary(); loadWater(); }, [date]);

  useEffect(() => {
    getDbStatus().then(({ data }) => setDbStatus(data.status)).catch(() => {});
  }, []);

  // Close suggestions on outside click
  useEffect(() => {
    const handler = (e) => {
      if (searchBoxRef.current && !searchBoxRef.current.contains(e.target)) {
        setShowSuggestions(false);
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  // ─── Auto-suggest (debounced) ──────────
  const handleInputChange = (val) => {
    setSearchQuery(val);
    if (suggestTimeoutRef.current) clearTimeout(suggestTimeoutRef.current);
    if (val.trim().length < 2) {
      setSuggestions([]);
      setShowSuggestions(false);
      return;
    }
    suggestTimeoutRef.current = setTimeout(async () => {
      try {
        const { data } = await suggestFood(val);
        setSuggestions(data);
        setShowSuggestions(data.length > 0);
      } catch {
        setSuggestions([]);
      }
    }, 300);
  };

  // ─── Full search ───────────────────────
  const handleSearch = async () => {
    if (!searchQuery.trim()) return;
    setSearching(true);
    setShowSuggestions(false);
    setSelectedFood(null);
    try {
      const { data } = await searchFood(searchQuery);
      setSearchResults(data);
    } catch {
      setSearchResults([]);
    }
    setSearching(false);
  };

  // ─── Select from suggestions ───────────
  const handleSelectSuggestion = (food) => {
    setSearchQuery(food.name);
    setShowSuggestions(false);
    setSuggestions([]);
    selectFoodForServing(food);
  };

  // ─── Serving customizer ────────────────
  const selectFoodForServing = (food) => {
    setSelectedFood(food);
    // Auto-select drink units for drinks
    const isDrink = food.dishType === 'drink' || food.category === 'beverage';
    if (isDrink) {
      const desc = (food.servingDescription || '').toLowerCase();
      if (desc.includes('glass')) {
        setServingUnit('glass');
        const match = desc.match(/(\d+)/);
        setServingQty(match ? match[1] : '1');
      } else if (desc.includes('cup')) {
        setServingUnit('cup');
        setServingQty('1');
      } else {
        setServingUnit('ml');
        const defaultMl = food.recommendedServingG || 250;
        setServingQty(String(Math.round(defaultMl)));
      }
    } else {
      setServingUnit('grams');
      const defaultServing = food.recommendedServingG || food.serving_size_g || 100;
      setServingQty(String(Math.round(defaultServing)));
    }
    setSearchResults([]);
  };

  const getScaledNutrition = useCallback(() => {
    if (!selectedFood) return null;
    const unit = SERVING_UNITS.find(u => u.id === servingUnit);
    const qty = parseFloat(servingQty) || 0;
    let grams;
    if (unit.id === 'piece') {
      grams = (selectedFood.serving_size_g || 100) * qty;
    } else {
      grams = unit.gramsPerUnit * qty;
    }
    const factor = grams / 100;
    return {
      calories: Math.round((selectedFood.calories || 0) * factor * 10) / 10,
      protein: Math.round((selectedFood.protein_g || 0) * factor * 10) / 10,
      carbs: Math.round((selectedFood.carbohydrates_total_g || 0) * factor * 10) / 10,
      fat: Math.round((selectedFood.fat_total_g || 0) * factor * 10) / 10,
      fiber: Math.round((selectedFood.fiber_g || 0) * factor * 10) / 10,
      sugar: Math.round((selectedFood.sugar_g || 0) * factor * 10) / 10,
      grams,
    };
  }, [selectedFood, servingUnit, servingQty]);

  const handleAddCustomServing = async () => {
    const scaled = getScaledNutrition();
    if (!scaled) return;
    const entry = {
      foodName: selectedFood.name,
      servingSize: scaled.grams,
      calories: scaled.calories,
      protein: scaled.protein,
      carbs: scaled.carbs,
      fat: scaled.fat,
      fiber: scaled.fiber,
      sugar: scaled.sugar,
      mealType, logDate: date,
    };
    await logFood(entry);
    setSelectedFood(null);
    setSearchQuery('');
    loadSummary();
  };

  const handleAddFood = (food) => {
    selectFoodForServing(food);
  };

  const handleDeleteLog = async (id) => {
    await deleteFoodLog(id);
    loadSummary();
  };

  // ─── Goal ──────────────────────────────
  const handleSaveGoal = async () => {
    await setDailyGoal({
      goalDate: date,
      calorieGoal: parseFloat(goalForm.calories),
      proteinGoal: parseFloat(goalForm.protein),
      carbsGoal: parseFloat(goalForm.carbs),
      fatGoal: parseFloat(goalForm.fat),
      waterGoal: parseFloat(goalForm.water),
    });
    setShowGoalForm(false);
    loadSummary();
    loadWater();
  };

  // ─── Water ─────────────────────────────
  const handleAddWater = async (ml) => {
    await logWater({ date, amountMl: ml });
    loadWater();
    loadSummary();
  };

  const calPercent = summary ? Math.min((summary.totalCalories / summary.calorieGoal) * 100, 100) : 0;
  const remaining = summary ? summary.caloriesRemaining : 0;
  const scaled = getScaledNutrition();
  const waterPercent = Math.min((waterData.totalMl / waterData.goalMl) * 100, 100);

  return (
    <div className="calculator-card calorie-tracker">
      <h2>Calorie Tracker</h2>
      <p className="subtitle">Track your daily food intake and nutritional goals</p>

      {/* ── Date picker & goal (original layout) ── */}
      <div className="tracker-header">
        <div className="date-picker-group">
          <button className="date-nav-btn" onClick={() => {
            const d = new Date(date);
            d.setDate(d.getDate() - 1);
            setDate(d.toISOString().slice(0, 10));
          }}>◀</button>
          
          <input type="date" value={date} onChange={e => setDate(e.target.value)} className="date-picker" />
          
          <button className="date-nav-btn" onClick={() => {
            const d = new Date(date);
            d.setDate(d.getDate() + 1);
            setDate(d.toISOString().slice(0, 10));
          }}>▶</button>
        </div>
        
        <button className="btn-secondary" onClick={() => setShowGoalForm(!showGoalForm)}>
          {showGoalForm ? 'Cancel' : 'Set Goal'}
        </button>
      </div>

      {showGoalForm && (
        <div className="goal-form">
          <div className="form-row">
            <div className="form-group">
              <label>Calories</label>
              <input type="number" value={goalForm.calories} onChange={e => setGoalForm({...goalForm, calories: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Protein (g)</label>
              <input type="number" value={goalForm.protein} onChange={e => setGoalForm({...goalForm, protein: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Carbs (g)</label>
              <input type="number" value={goalForm.carbs} onChange={e => setGoalForm({...goalForm, carbs: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Fat (g)</label>
              <input type="number" value={goalForm.fat} onChange={e => setGoalForm({...goalForm, fat: e.target.value})} />
            </div>
            <div className="form-group">
              <label>Water (ml)</label>
              <input type="number" value={goalForm.water} onChange={e => setGoalForm({...goalForm, water: e.target.value})} />
            </div>
          </div>
          <button className="btn-primary" onClick={handleSaveGoal}>Save Goal</button>
        </div>
      )}

      {/* ── Calorie ring + stats + macros (ORIGINAL LAYOUT) ── */}
      {summary && (
        <div className="cal-summary">
          <div className="cal-ring-wrapper">
            <svg viewBox="0 0 120 120" className="cal-ring">
              <circle cx="60" cy="60" r="52" fill="none" stroke="#1e293b" strokeWidth="12" />
              <circle cx="60" cy="60" r="52" fill="none"
                stroke={calPercent > 100 ? '#ef4444' : '#22c55e'}
                strokeWidth="12" strokeLinecap="round"
                strokeDasharray={`${calPercent * 3.267} 326.7`}
                transform="rotate(-90 60 60)" />
            </svg>
            <div className="cal-ring-text">
              <span className="cal-ring-big">{summary.totalCalories}</span>
              <span className="cal-ring-small">/ {summary.calorieGoal} kcal</span>
            </div>
          </div>

          <div className="cal-stats">
            <div className="cal-stat">
              <span className="stat-label">Eaten</span>
              <span className="stat-value">{summary.totalCalories} kcal</span>
            </div>
            <div className="cal-stat">
              <span className="stat-label">Remaining</span>
              <span className="stat-value" style={{ color: remaining < 0 ? '#ef4444' : '#22c55e' }}>{remaining} kcal</span>
            </div>
            <div className="cal-stat">
              <span className="stat-label">Goal</span>
              <span className="stat-value">{summary.calorieGoal} kcal</span>
            </div>
          </div>

          {/* Macro bars (original layout) */}
          <div className="macro-summary">
            <div className="macro-track">
              <div className="macro-track-header">
                <span>Protein</span>
                <span>{summary.totalProtein}g / {summary.proteinGoal}g</span>
              </div>
              <div className="macro-track-bar">
                <div className="macro-track-fill protein" style={{ width: `${Math.min((summary.totalProtein / summary.proteinGoal) * 100, 100)}%` }} />
              </div>
            </div>
            <div className="macro-track">
              <div className="macro-track-header">
                <span>Carbs</span>
                <span>{summary.totalCarbs}g / {summary.carbsGoal}g</span>
              </div>
              <div className="macro-track-bar">
                <div className="macro-track-fill carbs" style={{ width: `${Math.min((summary.totalCarbs / summary.carbsGoal) * 100, 100)}%` }} />
              </div>
            </div>
            <div className="macro-track">
              <div className="macro-track-header">
                <span>Fat</span>
                <span>{summary.totalFat}g / {summary.fatGoal}g</span>
              </div>
              <div className="macro-track-bar">
                <div className="macro-track-fill fat" style={{ width: `${Math.min((summary.totalFat / summary.fatGoal) * 100, 100)}%` }} />
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ── Food search with auto-suggest (ORIGINAL LAYOUT) ── */}
      <div className="food-search">
        <h3>Add Food</h3>
        <div className="search-row" ref={searchBoxRef}>
          <div className="search-input-wrapper">
            <input
              type="text"
              placeholder="Search food (e.g. chicken breast, rice)..."
              value={searchQuery}
              onChange={e => handleInputChange(e.target.value)}
              onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
              autoComplete="off"
            />
            {/* Auto-suggest dropdown */}
            {showSuggestions && suggestions.length > 0 && (
              <div className="suggest-dropdown">
                {suggestions.map((item, i) => {
                  const badge = getDietBadge(item.dietaryCategory);
                  const isDrink = item.dishType === 'drink' || item.category === 'beverage';
                  const servG = item.recommendedServingG || item.serving_size_g || 100;
                  const servLabel = item.servingDescription || (isDrink ? `${Math.round(servG)}ml` : `${Math.round(servG)}g`);
                  const servCal = Math.round((item.calories || 0) * servG / (item.serving_size_g || 100));
                  return (
                    <div key={i} className="suggest-item" onClick={() => handleSelectSuggestion(item)}>
                      <div className="suggest-main">
                        <span className="suggest-name">{item.name}</span>
                        <span className="suggest-cal">{servCal} kcal · {servLabel}</span>
                      </div>
                      <div className="suggest-badges">
                        {badge && <span className={`suggest-badge ${badge.cls}`}>{badge.label}</span>}
                        {item.dishType && item.dishType !== 'ingredient' && (
                          <span className="suggest-badge badge-dish">{item.dishType}</span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
          <select value={mealType} onChange={e => setMealType(e.target.value)}>
            {MEALS.map(m => <option key={m} value={m}>{m.charAt(0).toUpperCase() + m.slice(1)}</option>)}
          </select>
          <button className="btn-primary" onClick={handleSearch} disabled={searching}>
            {searching ? 'Searching...' : 'Search'}
          </button>
        </div>

        {/* Search results (original look) */}
        {searchResults.length > 0 && !selectedFood && (
          <div className="search-results">
            {searchResults.map((food, i) => (
              <div key={i} className="food-result">
                <div className="food-info">
                  <strong>{food.name}</strong>
                  <span>{food.calories} kcal | P: {food.protein_g}g | C: {food.carbohydrates_total_g}g | F: {food.fat_total_g}g</span>
                </div>
                <button className="btn-add" onClick={() => handleAddFood(food)}>+ Add</button>
              </div>
            ))}
          </div>
        )}

        {/* Serving customizer (NEW - appears inline when food is selected) */}
        {selectedFood && (
          <div className="serving-customizer">
            <div className="serving-header">
              <h4>📐 Customize Serving: <em>{selectedFood.name}</em></h4>
              <button className="btn-close" onClick={() => setSelectedFood(null)}>✕</button>
            </div>
            {selectedFood.servingDescription && (
              <div className="serving-hint">
                Recommended: {selectedFood.servingDescription}
                {selectedFood.category && <span className="serving-category"> • {selectedFood.category}</span>}
              </div>
            )}
            <div className="serving-controls">
              <div className="serving-control">
                <label>Unit</label>
                <select value={servingUnit} onChange={e => {
                  const u = SERVING_UNITS.find(u => u.id === e.target.value);
                  setServingUnit(e.target.value);
                  setServingQty(String(u.defaultQty));
                }}>
                  {SERVING_UNITS.map(u => (
                    <option key={u.id} value={u.id}>{u.label}</option>
                  ))}
                </select>
              </div>
              <div className="serving-control">
                <label>Quantity</label>
                <input type="number" min="0.1" step="0.5" value={servingQty}
                  onChange={e => setServingQty(e.target.value)} />
              </div>
              <div className="serving-control">
                <label>Total weight</label>
                <span className="serving-grams">{scaled ? scaled.grams : 0}g</span>
              </div>
            </div>
            {scaled && (
              <div className="serving-preview">
                <div className="preview-grid">
                  <div className="preview-item cal"><span className="pv-val">{scaled.calories}</span><span className="pv-lbl">kcal</span></div>
                  <div className="preview-item"><span className="pv-val">{scaled.protein}g</span><span className="pv-lbl">Protein</span></div>
                  <div className="preview-item"><span className="pv-val">{scaled.carbs}g</span><span className="pv-lbl">Carbs</span></div>
                  <div className="preview-item"><span className="pv-val">{scaled.fat}g</span><span className="pv-lbl">Fat</span></div>
                  <div className="preview-item"><span className="pv-val">{scaled.fiber}g</span><span className="pv-lbl">Fiber</span></div>
                  <div className="preview-item"><span className="pv-val">{scaled.sugar}g</span><span className="pv-lbl">Sugar</span></div>
                </div>
                <button className="btn-primary btn-log" onClick={handleAddCustomServing}>
                  ✅ Log {selectedFood.name} ({scaled.grams}g) to {mealType}
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* ── Today's Meals (ORIGINAL LAYOUT) ── */}
      {summary && summary.meals && (
        <div className="meal-logs">
          <h3>Today's Meals</h3>
          {MEALS.map(meal => {
            const logs = summary.meals[meal] || [];
            if (logs.length === 0) return null;
            const mealCals = logs.reduce((s, l) => s + l.calories, 0);
            return (
              <div key={meal} className="meal-group">
                <div className="meal-header">
                  <h4>{meal.charAt(0).toUpperCase() + meal.slice(1)}</h4>
                  <span>{mealCals.toFixed(0)} kcal</span>
                </div>
                {logs.map(log => (
                  <div key={log.id} className="meal-entry">
                    <span className="meal-food">{log.foodName}</span>
                    <span className="meal-cals">{log.calories} kcal</span>
                    <span className="meal-macros">P:{log.protein}g C:{log.carbs}g F:{log.fat}g</span>
                    <button className="btn-delete" onClick={() => handleDeleteLog(log.id)}>×</button>
                  </div>
                ))}
              </div>
            );
          })}
        </div>
      )}

      {/* ── Water Intake Tracker (NEW - below meals) ── */}
      <div className="water-tracker">
        <h3>💧 Water Intake</h3>
        <div className="water-progress-row">
          <div className="water-ring-wrapper">
            <svg viewBox="0 0 100 100" className="water-ring">
              <circle cx="50" cy="50" r="42" fill="none" stroke="#1e293b" strokeWidth="8" />
              <circle cx="50" cy="50" r="42" fill="none"
                stroke="#38bdf8"
                strokeWidth="8" strokeLinecap="round"
                strokeDasharray={`${waterPercent * 2.639} 263.9`}
                transform="rotate(-90 50 50)" />
            </svg>
            <div className="water-ring-text">
              <span className="water-ring-big">{Math.round(waterData.totalMl)}</span>
              <span className="water-ring-small">/ {waterData.goalMl} ml</span>
            </div>
          </div>
          <div className="water-info">
            <div className="water-glasses">{Math.floor(waterData.totalMl / 250)} glasses consumed</div>
            <div className="water-buttons">
              <button className="water-btn" onClick={() => handleAddWater(150)}>🥤 150ml</button>
              <button className="water-btn" onClick={() => handleAddWater(250)}>🥛 250ml</button>
              <button className="water-btn" onClick={() => handleAddWater(500)}>🍶 500ml</button>
              <button className="water-btn" onClick={() => handleAddWater(750)}>💧 750ml</button>
            </div>
          </div>
        </div>
      </div>

      {dbStatus && <p className="db-status">{dbStatus}</p>}
    </div>
  );
}
