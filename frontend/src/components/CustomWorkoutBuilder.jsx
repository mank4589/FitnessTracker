import React, { useState, useEffect, useRef } from 'react';
import { 
  createCustomWorkoutPlan, 
  updateWorkoutPlan,
  searchExercises,
} from '../services/api';
import './CustomWorkoutBuilder.css';

const CustomWorkoutBuilder = ({ profileId, onBack, onPlanCreated, editPlan }) => {
  const isEditing = !!editPlan;
  const [planName, setPlanName] = useState(editPlan?.name || 'My Custom Plan');
  const [planDescription, setPlanDescription] = useState(editPlan?.description || '');
  const [daysPerWeek, setDaysPerWeek] = useState(editPlan?.daysPerWeek || 4);
  const [workoutDays, setWorkoutDays] = useState([]);
  const [selectedDayIndex, setSelectedDayIndex] = useState(0);
  const [saving, setSaving] = useState(false);
  const [step, setStep] = useState(editPlan ? 2 : 1); // Skip setup if editing

  // Exercise search state (same pattern as ExerciseLogger)
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [searchError, setSearchError] = useState('');
  const [hasMore, setHasMore] = useState(false);
  const [searchOffset, setSearchOffset] = useState(0);
  const [loadingMore, setLoadingMore] = useState(false);

  const searchTimeoutRef = useRef(null);
  const searchBoxRef = useRef(null);

  const PAGE_SIZE = 20;

  const dayFocusOptions = [
    { value: 'PUSH', label: 'Push Day' },
    { value: 'PULL', label: 'Pull Day' },
    { value: 'LEGS', label: 'Legs Day' },
    { value: 'UPPER', label: 'Upper Body' },
    { value: 'LOWER', label: 'Lower Body' },
    { value: 'FULL_BODY', label: 'Full Body' },
    { value: 'CHEST', label: 'Chest Focus' },
    { value: 'BACK', label: 'Back Focus' },
    { value: 'SHOULDERS', label: 'Shoulders Focus' },
    { value: 'ARMS', label: 'Arms Focus' },
    { value: 'CARDIO', label: 'Cardio' },
    { value: 'REST', label: 'Rest Day' }
  ];

  // Initialize workout days when daysPerWeek changes or from editPlan
  useEffect(() => {
    if (step === 2 && workoutDays.length === 0) {
      if (editPlan && editPlan.workoutDays && editPlan.workoutDays.length > 0) {
        // Pre-populate from existing plan
        const imported = editPlan.workoutDays.map((day, i) => ({
          name: day.name || `Day ${i + 1}`,
          dayNumber: day.dayOrder || i + 1,
          focus: day.focus || 'FULL_BODY',
          description: day.description || '',
          restDay: day.restDay || false,
          exercises: (day.plannedExercises || []).map(pe => ({
            exerciseId: pe.exercise?.id || pe.exerciseId,
            exercise: pe.exercise || { id: pe.exerciseId, name: pe.exerciseName || 'Unknown' },
            sets: pe.sets || 3,
            repRange: pe.repRange || '8-12',
            restSeconds: pe.restSeconds || 90,
            notes: pe.notes || '',
            warmup: pe.warmup || false,
          }))
        }));
        setWorkoutDays(imported);
      } else {
        const days = Array.from({ length: daysPerWeek }, (_, i) => ({
          name: `Day ${i + 1}`,
          dayNumber: i + 1,
          focus: 'FULL_BODY',
          description: '',
          restDay: false,
          exercises: []
        }));
        setWorkoutDays(days);
      }
    }
  }, [step, daysPerWeek]);

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

  // ─── Debounced search (same pattern as ExerciseLogger) ────
  const handleSearchInput = (val) => {
    setSearchQuery(val);
    if (searchTimeoutRef.current) clearTimeout(searchTimeoutRef.current);
    if (val.trim().length < 2) {
      setSearchResults([]);
      setShowSuggestions(false);
      return;
    }
    searchTimeoutRef.current = setTimeout(async () => {
      setSearching(true);
      setSearchOffset(0);
      setSearchError('');
      try {
        const { data } = await searchExercises(val, PAGE_SIZE, 0);
        setSearchResults(data);
        setHasMore(data.length >= PAGE_SIZE);
        setSearchOffset(PAGE_SIZE);
        setShowSuggestions(true);
      } catch (err) {
        setSearchResults([]);
        setSearchError('Could not search exercises. Is the backend running?');
        setShowSuggestions(true);
      }
      setSearching(false);
    }, 250);
  };

  const handleLoadMore = async () => {
    if (loadingMore) return;
    setLoadingMore(true);
    try {
      const { data } = await searchExercises(searchQuery, PAGE_SIZE, searchOffset);
      setSearchResults(prev => [...prev, ...data]);
      setHasMore(data.length >= PAGE_SIZE);
      setSearchOffset(prev => prev + PAGE_SIZE);
    } catch { /* ignore */ }
    setLoadingMore(false);
  };

  // ─── Exercise management ────
  const updateDay = (index, updates) => {
    setWorkoutDays(prev => prev.map((day, i) => 
      i === index ? { ...day, ...updates } : day
    ));
  };

  const addExerciseToCurrentDay = (exercise) => {
    const currentDay = workoutDays[selectedDayIndex];
    // Use exerciseId from the search result (from exercises.json DB)
    // The search API returns { id, name, primaryMuscles, equipment, ... }
    const exerciseId = exercise.exerciseId || exercise.id;
    
    if (currentDay.exercises.some(e => e.exerciseId === exerciseId)) {
      alert('This exercise is already in this day');
      return;
    }
    
    const newPlannedExercise = {
      exerciseId: exerciseId,
      exercise: {
        id: exerciseId,
        name: exercise.name,
        primaryMuscle: exercise.primaryMuscles?.[0] || exercise.primaryMuscle || '',
        secondaryMuscles: exercise.secondaryMuscles || [],
        equipment: exercise.equipment || '',
        imageUrl: exercise.imageUrl || null,
      },
      sets: 3,
      repRange: '8-12',
      restSeconds: 90,
      notes: '',
      warmup: false
    };
    
    updateDay(selectedDayIndex, {
      exercises: [...currentDay.exercises, newPlannedExercise]
    });

    // Clear search
    setSearchQuery('');
    setSearchResults([]);
    setShowSuggestions(false);
  };

  const removeExerciseFromCurrentDay = (exerciseIndex) => {
    const currentDay = workoutDays[selectedDayIndex];
    updateDay(selectedDayIndex, {
      exercises: currentDay.exercises.filter((_, i) => i !== exerciseIndex)
    });
  };

  const updateExerciseInCurrentDay = (exerciseIndex, updates) => {
    const currentDay = workoutDays[selectedDayIndex];
    updateDay(selectedDayIndex, {
      exercises: currentDay.exercises.map((ex, i) => 
        i === exerciseIndex ? { ...ex, ...updates } : ex
      )
    });
  };

  const moveExercise = (exerciseIndex, direction) => {
    const currentDay = workoutDays[selectedDayIndex];
    const exercises = [...currentDay.exercises];
    const newIndex = exerciseIndex + direction;
    
    if (newIndex < 0 || newIndex >= exercises.length) return;
    
    [exercises[exerciseIndex], exercises[newIndex]] = [exercises[newIndex], exercises[exerciseIndex]];
    updateDay(selectedDayIndex, { exercises });
  };

  const handleSavePlan = async () => {
    if (!planName.trim()) {
      alert('Please enter a plan name');
      return;
    }
    
    const hasExercises = workoutDays.some(day => !day.restDay && day.exercises.length > 0);
    if (!hasExercises) {
      alert('Please add at least one exercise to your plan');
      return;
    }
    
    setSaving(true);
    try {
      const planData = {
        name: planName,
        description: planDescription,
        planType: 'CUSTOM',
        daysPerWeek: daysPerWeek,
        custom: true,
        active: true,
        workoutDays: workoutDays.map((day, index) => ({
          name: day.name,
          dayNumber: index + 1,
          dayOfWeek: getDayOfWeek(index),
          focus: day.focus,
          description: day.description,
          restDay: day.restDay,
          plannedExercises: day.exercises.map((ex, exIndex) => ({
            exerciseId: ex.exerciseId,
            orderInDay: exIndex + 1,
            sets: ex.sets,
            repRange: ex.repRange,
            restSeconds: ex.restSeconds,
            notes: ex.notes,
            warmup: ex.warmup
          }))
        }))
      };
      
      if (isEditing && editPlan.id) {
        await updateWorkoutPlan(editPlan.id, planData);
        alert('Workout plan updated successfully!');
      } else {
        await createCustomWorkoutPlan(profileId, planData);
        alert('Custom workout plan created successfully!');
      }
      onPlanCreated();
    } catch (error) {
      console.error('Error saving plan:', error);
      alert('Error saving plan: ' + (error.response?.data?.message || error.message));
    } finally {
      setSaving(false);
    }
  };

  const getDayOfWeek = (index) => {
    const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
    return days[index % 7];
  };

  const formatMuscle = (m) => {
    if (!m) return '';
    return m.split(/[\s_]+/).map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ');
  };

  const muscleEmoji = (m) => {
    const map = { 
      chest: '🫁', back: '🔙', 'middle back': '🔙', 'lower back': '🔙', lats: '🔙',
      shoulders: '💪', biceps: '💪', triceps: '💪', forearms: '🤲',
      quadriceps: '🦵', hamstrings: '🦵', glutes: '🍑', calves: '🦶',
      abdominals: '🎯', traps: '🏋️', neck: '🧣'
    };
    return map[(m || '').toLowerCase()] || '🏋️';
  };

  const levelColor = (level) => {
    const map = { beginner: '#22c55e', intermediate: '#f59e0b', expert: '#ef4444' };
    return map[(level || '').toLowerCase()] || '#6b7280';
  };

  const currentDay = workoutDays[selectedDayIndex];

  if (step === 1) {
    return (
      <div className="cwb-container">
        <div className="cwb-header">
          <button className="cwb-back-btn" onClick={onBack}>← Back</button>
          <h2>Create Custom Plan</h2>
        </div>
        
        <div className="cwb-setup-form">
          <div className="cwb-form-group">
            <label>Plan Name</label>
            <input
              type="text"
              value={planName}
              onChange={(e) => setPlanName(e.target.value)}
              placeholder="e.g., My PPL Split"
              className="cwb-input"
            />
          </div>
          
          <div className="cwb-form-group">
            <label>Description (optional)</label>
            <textarea
              value={planDescription}
              onChange={(e) => setPlanDescription(e.target.value)}
              placeholder="Describe your workout plan..."
              className="cwb-input cwb-textarea"
              rows={3}
            />
          </div>
          
          <div className="cwb-form-group">
            <label>Days Per Week</label>
            <div className="cwb-days-selector">
              {[2, 3, 4, 5, 6, 7].map(num => (
                <button
                  key={num}
                  className={`cwb-day-num ${daysPerWeek === num ? 'active' : ''}`}
                  onClick={() => setDaysPerWeek(num)}
                >
                  {num}
                </button>
              ))}
            </div>
          </div>
          
          <button 
            className="cwb-next-btn"
            onClick={() => setStep(2)}
          >
            Continue to Build Days →
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="cwb-container">
      <div className="cwb-header">
        <button className="cwb-back-btn" onClick={() => setStep(1)}>← Setup</button>
        <h2>{planName}</h2>
        <button 
          className="cwb-save-btn"
          onClick={handleSavePlan}
          disabled={saving}
        >
          {saving ? 'Saving...' : '💾 Save Plan'}
        </button>
      </div>
      
      {/* Day Tabs */}
      <div className="cwb-day-tabs">
        {workoutDays.map((day, index) => (
          <button
            key={index}
            className={`cwb-day-tab ${selectedDayIndex === index ? 'active' : ''} ${day.restDay ? 'rest' : ''}`}
            onClick={() => setSelectedDayIndex(index)}
          >
            <span className="cwb-tab-name">{day.name}</span>
            <span className="cwb-tab-count">
              {day.restDay ? '😴' : `${day.exercises.length} ex`}
            </span>
          </button>
        ))}
      </div>
      
      <div className="cwb-builder-layout">
        {/* Left: Day Config + Exercises */}
        <div className="cwb-day-editor">
          <div className="cwb-day-config">
            <div className="cwb-config-row">
              <div className="cwb-form-group">
                <label>Day Name</label>
                <input
                  type="text"
                  value={currentDay?.name || ''}
                  onChange={(e) => updateDay(selectedDayIndex, { name: e.target.value })}
                  className="cwb-input"
                />
              </div>
              <div className="cwb-form-group">
                <label>Focus</label>
                <select
                  value={currentDay?.focus || 'FULL_BODY'}
                  onChange={(e) => updateDay(selectedDayIndex, { 
                    focus: e.target.value,
                    restDay: e.target.value === 'REST'
                  })}
                  className="cwb-input"
                >
                  {dayFocusOptions.map(opt => (
                    <option key={opt.value} value={opt.value}>{opt.label}</option>
                  ))}
                </select>
              </div>
            </div>
            <div className="cwb-form-group">
              <label>Day Description</label>
              <input
                type="text"
                value={currentDay?.description || ''}
                onChange={(e) => updateDay(selectedDayIndex, { description: e.target.value })}
                placeholder="e.g., Focus on compound movements"
                className="cwb-input"
              />
            </div>
          </div>
          
          {currentDay?.restDay ? (
            <div className="cwb-rest-message">
              <div className="cwb-rest-icon">😴</div>
              <h3>Rest Day</h3>
              <p>Recovery is essential for growth!</p>
            </div>
          ) : (
            <div className="cwb-exercises-section">
              <h3>Exercises ({currentDay?.exercises?.length || 0})</h3>
              {currentDay?.exercises?.length === 0 ? (
                <p className="cwb-no-exercises">
                  Search and add exercises from the panel on the right →
                </p>
              ) : (
                <div className="cwb-exercise-list">
                  {currentDay?.exercises?.map((ex, index) => (
                    <div key={index} className="cwb-planned-exercise">
                      <div className="cwb-order-controls">
                        <button 
                          className="cwb-order-btn"
                          onClick={() => moveExercise(index, -1)}
                          disabled={index === 0}
                        >↑</button>
                        <span className="cwb-order-num">{index + 1}</span>
                        <button 
                          className="cwb-order-btn"
                          onClick={() => moveExercise(index, 1)}
                          disabled={index === currentDay.exercises.length - 1}
                        >↓</button>
                      </div>
                      
                      <div className="cwb-ex-info">
                        <div className="cwb-ex-name-row">
                          <strong>{ex.exercise?.name}</strong>
                          <span className="cwb-ex-equip">{ex.exercise?.equipment?.replace(/_/g, ' ')}</span>
                        </div>
                        
                        <div className="cwb-ex-params">
                          <div className="cwb-param">
                            <label>Sets</label>
                            <input
                              type="number"
                              value={ex.sets}
                              onChange={(e) => updateExerciseInCurrentDay(index, { sets: parseInt(e.target.value) || 1 })}
                              min="1" max="10"
                            />
                          </div>
                          <div className="cwb-param">
                            <label>Reps</label>
                            <input
                              type="text"
                              value={ex.repRange}
                              onChange={(e) => updateExerciseInCurrentDay(index, { repRange: e.target.value })}
                              placeholder="8-12"
                            />
                          </div>
                          <div className="cwb-param">
                            <label>Rest (s)</label>
                            <input
                              type="number"
                              value={ex.restSeconds}
                              onChange={(e) => updateExerciseInCurrentDay(index, { restSeconds: parseInt(e.target.value) || 60 })}
                              min="0" max="300"
                            />
                          </div>
                          <label className="cwb-warmup-check">
                            <input
                              type="checkbox"
                              checked={ex.warmup}
                              onChange={(e) => updateExerciseInCurrentDay(index, { warmup: e.target.checked })}
                            />
                            Warm Up
                          </label>
                        </div>
                        
                        <input
                          type="text"
                          value={ex.notes}
                          onChange={(e) => updateExerciseInCurrentDay(index, { notes: e.target.value })}
                          placeholder="Notes (optional)"
                          className="cwb-notes-input"
                        />
                      </div>
                      
                      <button 
                        className="cwb-remove-btn"
                        onClick={() => removeExerciseFromCurrentDay(index)}
                      >×</button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
        
        {/* Right: Exercise Search Panel */}
        <div className="cwb-search-panel">
          <h3>🔍 Find Exercises</h3>
          <p className="cwb-search-hint">Search 800+ exercises by name, muscle, or equipment</p>
          
          <div className="cwb-search-box" ref={searchBoxRef}>
            <div className="cwb-search-input-wrapper">
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => handleSearchInput(e.target.value)}
                placeholder="Search exercises (e.g. bench press, biceps, dumbbell)..."
                className="cwb-search-input"
                autoComplete="off"
              />
              {searching && <div className="cwb-search-spinner"></div>}
            </div>
            
            {showSuggestions && (
              <div className="cwb-search-dropdown">
                {searchError ? (
                  <div className="cwb-search-msg error">
                    <span>⚠️</span> {searchError}
                  </div>
                ) : searchResults.length === 0 ? (
                  <div className="cwb-search-msg empty">
                    <span>🔍</span> No exercises found for "{searchQuery}"
                  </div>
                ) : (
                  <>
                    {searchResults.map((ex, i) => (
                      <div 
                        key={i} 
                        className="cwb-search-result"
                        onClick={() => !currentDay?.restDay && addExerciseToCurrentDay(ex)}
                      >
                        {ex.imageUrl ? (
                          <img src={ex.imageUrl} alt="" className="cwb-result-thumb" />
                        ) : (
                          <span className="cwb-result-thumb cwb-result-emoji">
                            {muscleEmoji(ex.primaryMuscles?.[0])}
                          </span>
                        )}
                        <div className="cwb-result-info">
                          <span className="cwb-result-name">{ex.name}</span>
                          <div className="cwb-result-meta">
                            {ex.primaryMuscles?.map((m, j) => (
                              <span key={`pm-${j}`} className="cwb-muscle-pill">{formatMuscle(m)}</span>
                            ))}
                            {ex.equipment && (
                              <span className="cwb-equip-pill">{formatMuscle(ex.equipment)}</span>
                            )}
                            {ex.category && (
                              <span className="cwb-type-pill">{formatMuscle(ex.category)}</span>
                            )}
                            {ex.level && (
                              <span className="cwb-level-pill" style={{ backgroundColor: levelColor(ex.level) }}>
                                {formatMuscle(ex.level)}
                              </span>
                            )}
                          </div>
                        </div>
                        <button className="cwb-add-btn" disabled={currentDay?.restDay}>+</button>
                      </div>
                    ))}
                    {hasMore && (
                      <div className="cwb-load-more" onClick={handleLoadMore}>
                        {loadingMore ? 'Loading...' : 'Show more results ▼'}
                      </div>
                    )}
                  </>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CustomWorkoutBuilder;
