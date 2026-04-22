import React, { useState, useEffect } from 'react';
import {
  getAllProfiles,
  generateMealPlan,
  getLatestMealPlan,
  getAllMealPlans,
  deleteMealPlan,
  addFoodToSlot,
  removeFoodFromSlot,
  suggestFood,
} from '../services/api';
import CustomMealPlanBuilder from './CustomMealPlanBuilder';
import './MealPlanViewer.css';

const DISH_TYPE_LABELS = {
  curry: '🍛 Curry', dal: '🥘 Dal', sabzi: '🥬 Sabzi',
  egg_dish: '🍳 Egg', meat_dish: '🥩 Meat', rice_dish: '🍚 Rice',
  pasta: '🍝 Pasta', complete_meal: '🍽️ Full Meal', staple: '🫓 Staple',
  drink: '🥛 Drink', dessert: '🍰 Dessert', side: '🥗 Side',
  breakfast_item: '🍳 Breakfast', snack: '🍿 Snack', fruit: '🍎 Fruit',
  nut: '🥜 Nuts', condiment: '🧂 Condiment', ingredient: '📦 Ingredient',
};

const formatDishType = (type) => DISH_TYPE_LABELS[type] || type;

const MealPlanViewer = ({ profileId: propProfileId }) => {
  const [profiles, setProfiles] = useState([]);
  const [selectedProfileId, setSelectedProfileId] = useState(propProfileId || '');
  const [mealPlan, setMealPlan] = useState(null);
  const [allPlans, setAllPlans] = useState([]);
  const [loading, setLoading] = useState(false);
  const [view, setView] = useState('viewer'); // 'viewer' | 'builder'
  const [editingSlot, setEditingSlot] = useState(null); // { dayId, slot }
  const [slotSearchQuery, setSlotSearchQuery] = useState('');
  const [slotSearchResults, setSlotSearchResults] = useState([]);
  const [slotSearching, setSlotSearching] = useState(false);
  const [showAllPlans, setShowAllPlans] = useState(false);

  const daysOfWeek = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

  useEffect(() => { loadProfiles(); }, []);
  useEffect(() => { if (propProfileId) setSelectedProfileId(propProfileId); }, [propProfileId]);
  useEffect(() => { if (selectedProfileId) { handleLoadLatest(); loadAllPlans(); } }, [selectedProfileId]);

  const loadProfiles = async () => {
    try {
      const response = await getAllProfiles();
      setProfiles(response.data);
      if (!selectedProfileId && response.data.length > 0) {
        setSelectedProfileId(response.data[0].id);
      }
    } catch (error) { console.error('Error loading profiles:', error); }
  };

  const loadAllPlans = async () => {
    try {
      const response = await getAllMealPlans(selectedProfileId);
      setAllPlans(response.data || []);
    } catch { setAllPlans([]); }
  };

  const handleGeneratePlan = async () => {
    if (!selectedProfileId) { alert('Please select a profile first'); return; }
    setLoading(true);
    try {
      const response = await generateMealPlan(selectedProfileId);
      setMealPlan(response.data);
      loadAllPlans();
    } catch (error) {
      console.error('Error generating meal plan:', error);
      alert('Error generating meal plan. Make sure the profile has health data linked.');
    } finally { setLoading(false); }
  };

  const handleLoadLatest = async () => {
    if (!selectedProfileId) return;
    setLoading(true);
    try {
      const response = await getLatestMealPlan(selectedProfileId);
      setMealPlan(response.data);
    } catch { setMealPlan(null); }
    finally { setLoading(false); }
  };

  const handleDeletePlan = async (planId) => {
    if (!window.confirm('Delete this meal plan?')) return;
    try {
      await deleteMealPlan(planId);
      loadAllPlans();
      if (mealPlan?.id === planId) setMealPlan(null);
    } catch (e) { console.error('Error deleting plan:', e); }
  };

  const handleLoadPlan = (plan) => { setMealPlan(plan); };

  // ─── Slot Editing ────
  const startEditSlot = (dayId, slot) => {
    setEditingSlot({ dayId, slot });
    setSlotSearchQuery('');
    setSlotSearchResults([]);
  };

  const cancelEditSlot = () => {
    setEditingSlot(null);
    setSlotSearchQuery('');
    setSlotSearchResults([]);
  };

  const handleSlotSearch = async (val) => {
    setSlotSearchQuery(val);
    if (val.trim().length < 2) { setSlotSearchResults([]); return; }
    setSlotSearching(true);
    try {
      const { data } = await suggestFood(val);
      setSlotSearchResults(data);
    } catch { setSlotSearchResults([]); }
    setSlotSearching(false);
  };

  const handleAddToSlot = async (food) => {
    if (!mealPlan || !editingSlot) return;
    try {
      const response = await addFoodToSlot(mealPlan.id, editingSlot.dayId, editingSlot.slot, food.id);
      setMealPlan(response.data);
      setSlotSearchQuery('');
      setSlotSearchResults([]);
    } catch (e) { console.error('Error adding food:', e); }
  };

  const handleRemoveFromSlot = async (dayId, slot, foodId) => {
    if (!mealPlan) return;
    try {
      const response = await removeFoodFromSlot(mealPlan.id, dayId, slot, foodId);
      setMealPlan(response.data);
    } catch (e) { console.error('Error removing food:', e); }
  };

  // ─── Helpers ────
  const getDailyPlanByDay = (dayOfWeek) => {
    if (!mealPlan || !mealPlan.dailyPlans) return null;
    return mealPlan.dailyPlans.find(dp => dp.dayOfWeek === dayOfWeek);
  };

  const getDietaryBadge = (dietaryCategory) => {
    switch (dietaryCategory) {
      case 'VEGAN': return { icon: '🌱', label: 'Vegan', className: 'badge-vegan' };
      case 'VEGETARIAN': return { icon: '🥬', label: 'Veg', className: 'badge-veg' };
      case 'EGGETARIAN': return { icon: '🥚', label: 'Egg', className: 'badge-egg' };
      case 'PESCATARIAN': return { icon: '🐟', label: 'Pesc', className: 'badge-pesc' };
      default: return { icon: '🍖', label: 'Non-Veg', className: 'badge-nonveg' };
    }
  };

  const getServingMultiplier = (item) => {
    if (item.recommendedServingG && item.recommendedServingG > 0 && item.servingSize > 0) {
      return item.recommendedServingG / item.servingSize;
    }
    return 1.0;
  };

  // ─── Render Meal Slot ────
  const renderMealSlot = (items, slotName, dailyPlan, slotKey) => {
    const isEditing = editingSlot && editingSlot.dayId === dailyPlan?.id && editingSlot.slot === slotKey;

    if (!items || items.length === 0) {
      return (
        <div className="meal-empty">
          <span>No {slotName} planned</span>
          {dailyPlan && (
            <button className="slot-edit-btn" onClick={() => startEditSlot(dailyPlan.id, slotKey)}>+ Add</button>
          )}
          {isEditing && renderSlotEditor()}
        </div>
      );
    }

    const totals = items.reduce((acc, item) => {
      const mult = getServingMultiplier(item);
      acc.calories += item.calories * mult;
      acc.protein += (item.protein || 0) * mult;
      acc.carbs += (item.carbs || 0) * mult;
      acc.fat += (item.fat || 0) * mult;
      return acc;
    }, { calories: 0, protein: 0, carbs: 0, fat: 0 });

    return (
      <div className="meal-item multi-meal">
        <div className="multi-meal-items">
          {items.map((item, idx) => {
            const badge = getDietaryBadge(item.dietaryCategory);
            const servingG = item.recommendedServingG || item.servingSize || 100;
            const servingLabel = item.servingDescription || `${Math.round(servingG)}g`;
            return (
              <div key={idx} className="multi-meal-entry">
                <div className="meal-header-row">
                  <h5>{item.name}</h5>
                  <div className="meal-header-actions">
                    <span className={`dietary-badge ${badge.className}`}>{badge.icon} {badge.label}</span>
                    {dailyPlan && (
                      <button className="slot-remove-btn"
                        onClick={() => handleRemoveFromSlot(dailyPlan.id, slotKey, item.id)}
                        title="Remove">×</button>
                    )}
                  </div>
                </div>
                <div className="meal-badges-row">
                  <span className="serving-info">{servingLabel}</span>
                  {item.dishType && item.dishType !== 'ingredient' && (
                    <span className="cuisine-tag">{formatDishType(item.dishType)}</span>
                  )}
                  {item.cuisine && item.cuisine !== 'UNIVERSAL' && (
                    <span className="cuisine-tag cuisine-origin">{item.cuisine}</span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
        <div className="meal-nutrition">
          <span>{Math.round(totals.calories)} cal</span>
          <span>P: {totals.protein.toFixed(1)}g</span>
          <span>C: {totals.carbs.toFixed(1)}g</span>
          <span>F: {totals.fat.toFixed(1)}g</span>
        </div>
        {dailyPlan && (
          <button className="slot-edit-btn slot-edit-inline"
            onClick={() => startEditSlot(dailyPlan.id, slotKey)}>+ Add Food</button>
        )}
        {isEditing && renderSlotEditor()}
      </div>
    );
  };

  // ─── Inline Slot Editor (search + add) ────
  const renderSlotEditor = () => (
    <div className="slot-editor">
      <div className="slot-editor-header">
        <input
          type="text" value={slotSearchQuery}
          onChange={(e) => handleSlotSearch(e.target.value)}
          placeholder="Search foods to add..."
          className="slot-editor-input" autoFocus
        />
        <button className="slot-editor-close" onClick={cancelEditSlot}>✕</button>
      </div>
      {slotSearching && <div className="slot-editor-status">Searching...</div>}
      {slotSearchResults.length > 0 && (
        <div className="slot-editor-results">
          {slotSearchResults.slice(0, 8).map((food, i) => {
            const bg = getDietaryBadge(food.dietaryCategory);
            const servG = food.recommendedServingG || food.servingSize || 100;
            return (
              <div key={i} className="slot-editor-item" onClick={() => handleAddToSlot(food)}>
                <div className="slot-editor-item-info">
                  <strong>{food.name}</strong>
                  <span className="slot-editor-item-meta">
                    {Math.round((food.calories || 0) * servG / (food.servingSize || 100))} cal
                    · {food.servingDescription || `${Math.round(servG)}g`}
                  </span>
                </div>
                <span className={`dietary-badge ${bg.className}`} style={{fontSize:'10px',padding:'2px 6px'}}>
                  {bg.icon}
                </span>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );

  // ─── Render Day Card ────
  const renderDayCard = (dayOfWeek) => {
    const dailyPlan = getDailyPlanByDay(dayOfWeek);
    
    if (!dailyPlan) {
      return (
        <div key={dayOfWeek} className="day-card">
          <h3>{dayOfWeek}</h3>
          <p>No plan for this day</p>
        </div>
      );
    }

    return (
      <div key={dayOfWeek} className="day-card">
        <h3>{dayOfWeek}</h3>
        
        <div className="meals-section">
          <div className="meal-slot">
            <h4>🍳 Breakfast <span className="slot-pct">30%</span></h4>
            {renderMealSlot(dailyPlan.breakfastItems || (dailyPlan.breakfast ? [dailyPlan.breakfast] : []), 'breakfast', dailyPlan, 'breakfast')}
          </div>

          <div className="meal-slot">
            <h4>🍱 Lunch <span className="slot-pct">35%</span></h4>
            {renderMealSlot(dailyPlan.lunchItems || (dailyPlan.lunch ? [dailyPlan.lunch] : []), 'lunch', dailyPlan, 'lunch')}
          </div>

          <div className="meal-slot">
            <h4>🍿 Snacks <span className="slot-pct">10%</span></h4>
            {renderMealSlot(dailyPlan.snacks || [], 'snacks', dailyPlan, 'snacks')}
          </div>

          <div className="meal-slot">
            <h4>🍽️ Dinner <span className="slot-pct">25%</span></h4>
            {renderMealSlot(dailyPlan.dinnerItems || (dailyPlan.dinner ? [dailyPlan.dinner] : []), 'dinner', dailyPlan, 'dinner')}
          </div>
        </div>

        <div className="day-summary">
          <h4>Daily Total {mealPlan?.targetCalories ? <span className="target-label">/ {mealPlan.targetCalories} cal target</span> : ''}</h4>
          <div className="summary-stats">
            <div className="stat">
              <span className="stat-value">{Math.round(dailyPlan.totalCalories)}</span>
              <span className="stat-label">Calories</span>
            </div>
            <div className="stat">
              <span className="stat-value">{dailyPlan.totalProtein?.toFixed(1) || 0}g</span>
              <span className="stat-label">Protein</span>
            </div>
            <div className="stat">
              <span className="stat-value">{dailyPlan.totalCarbs?.toFixed(1) || 0}g</span>
              <span className="stat-label">Carbs</span>
            </div>
            <div className="stat">
              <span className="stat-value">{dailyPlan.totalFat?.toFixed(1) || 0}g</span>
              <span className="stat-label">Fat</span>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // ─── All Plans List ────
  const renderAllPlans = () => {
    if (allPlans.length === 0) return null;
    const displayPlans = showAllPlans ? allPlans : allPlans.slice(0, 3);
    return (
      <section className="mp-all-plans">
        <div className="mp-plans-header"><h3>Your Meal Plans ({allPlans.length})</h3></div>
        <div className="mp-plans-list">
          {displayPlans.map(plan => (
            <div key={plan.id} className={`mp-plan-item ${mealPlan?.id === plan.id ? 'active' : ''}`}>
              <div className="mp-plan-info">
                <h4>{plan.targetCalories} cal/day</h4>
                <span className="mp-plan-date">
                  {plan.weekStartDate ? new Date(plan.weekStartDate).toLocaleDateString() : 'Custom'}
                </span>
              </div>
              <div className="mp-plan-actions">
                {mealPlan?.id !== plan.id && (
                  <button onClick={() => handleLoadPlan(plan)} className="mp-btn-load">View</button>
                )}
                <button onClick={() => handleDeletePlan(plan.id)} className="mp-btn-delete">×</button>
              </div>
            </div>
          ))}
        </div>
        {allPlans.length > 3 && (
          <button className="mp-show-more" onClick={() => setShowAllPlans(!showAllPlans)}>
            {showAllPlans ? 'Show Less ▲' : `Show All (${allPlans.length}) ▼`}
          </button>
        )}
      </section>
    );
  };

  // ─── Builder View ────
  if (view === 'builder') {
    return (
      <CustomMealPlanBuilder
        profileId={selectedProfileId}
        onBack={() => { setView('viewer'); handleLoadLatest(); loadAllPlans(); }}
        onPlanCreated={() => { setView('viewer'); handleLoadLatest(); loadAllPlans(); }}
      />
    );
  }

  // ─── Main View ────
  return (
    <div className="meal-plan-viewer">
      <div className="mp-header">
        <div className="mp-header-left">
          <h2>🍽️ Meal Plans</h2>
          <p className="meal-plan-subtitle">Plan your nutrition for the week</p>
        </div>
        <button className="mp-create-btn" onClick={() => setView('builder')} disabled={!selectedProfileId}>
          <span>+</span> Create Custom Plan
        </button>
      </div>

      <div className="controls">
        <div className="control-group">
          <label>Select Profile:</label>
          <select 
            value={selectedProfileId} 
            onChange={(e) => setSelectedProfileId(e.target.value)}
          >
            <option value="">-- Select a Profile --</option>
            {profiles.map(profile => (
              <option key={profile.id} value={profile.id}>
                {profile.name} ({profile.fitnessGoal?.replace('_', ' ')})
              </option>
            ))}
          </select>
        </div>

        <div className="action-buttons">
          <button 
            onClick={handleGeneratePlan} 
            disabled={loading || !selectedProfileId}
            className="btn-primary"
          >
            {loading ? 'Generating...' : '⚡ Generate New Plan'}
          </button>
        </div>
      </div>

      {mealPlan && (
        <div className="meal-plan-content">
          <div className="plan-header">
            <h3>Weekly Meal Plan</h3>
            <div className="plan-targets">
              <p><strong>Profile:</strong> {mealPlan.userProfile?.name}</p>
              <p><strong>Daily Target:</strong> {mealPlan.targetCalories} cal</p>
              <p><strong>Macros:</strong> P: {mealPlan.targetProtein?.toFixed(0)}g | C: {mealPlan.targetCarbs?.toFixed(0)}g | F: {mealPlan.targetFat?.toFixed(0)}g</p>
              <p><strong>Week Starting:</strong> {new Date(mealPlan.weekStartDate).toLocaleDateString()}</p>
            </div>
          </div>

          <div className="days-grid">
            {daysOfWeek.map(day => renderDayCard(day))}
          </div>
        </div>
      )}

      {renderAllPlans()}

      {!mealPlan && !loading && (
        <div className="empty-state">
          <div className="empty-icon">🍽️</div>
          <h3>No Meal Plan Yet</h3>
          <p>Generate a plan based on your profile or create a custom one!</p>
        </div>
      )}
    </div>
  );
};

export default MealPlanViewer;
