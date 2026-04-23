import { useState, useEffect, useRef } from 'react';
import { searchExercises, getExerciseDetail, logExercise, getExerciseSummary, deleteExerciseLog } from '../services/api';

export default function ExerciseLogger({ profileId }) {
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [searchTerm, setSearchTerm] = useState('');
  const [results, setResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [searchError, setSearchError] = useState('');
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(false);
  const [searchOffset, setSearchOffset] = useState(0);
  const [selectedExercise, setSelectedExercise] = useState(null);
  const [exerciseDetail, setExerciseDetail] = useState(null);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [showSuggestions, setShowSuggestions] = useState(false);

  // Log form
  const [logForm, setLogForm] = useState({ sets: '3', reps: '10', weightKg: '0', durationMinutes: '0', notes: '' });

  // Summary
  const [summary, setSummary] = useState(null);

  const suggestTimeoutRef = useRef(null);
  const searchBoxRef = useRef(null);

  // ─── Data loading ──────────────────────
  const loadSummary = async () => {
    try {
      const { data } = await getExerciseSummary(profileId, date);
      setSummary(data);
    } catch { /* ignore */ }
  };

  useEffect(() => { if (profileId) loadSummary(); }, [date, profileId]);

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

  // ─── Search (debounced) ────────────────
  const PAGE_SIZE = 20;

  const handleInputChange = (val) => {
    setSearchTerm(val);
    if (suggestTimeoutRef.current) clearTimeout(suggestTimeoutRef.current);
    if (val.trim().length < 2) {
      setResults([]);
      setShowSuggestions(false);
      return;
    }
    suggestTimeoutRef.current = setTimeout(async () => {
      setSearching(true);
      setSearchOffset(0);
      setSearchError('');
      try {
        const { data } = await searchExercises(val, PAGE_SIZE, 0);
        setResults(data);
        setHasMore(data.length >= PAGE_SIZE);
        setSearchOffset(PAGE_SIZE);
        setShowSuggestions(true);
      } catch (err) {
        setResults([]);
        setSearchError('Could not connect to the server. Make sure the backend is running.');
        setShowSuggestions(true);
      }
      setSearching(false);
    }, 200);
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) return;
    setSearching(true);
    setShowSuggestions(false);
    setSearchOffset(0);
    setSearchError('');
    try {
      const { data } = await searchExercises(searchTerm, PAGE_SIZE, 0);
      setResults(data);
      setHasMore(data.length >= PAGE_SIZE);
      setSearchOffset(PAGE_SIZE);
      setShowSuggestions(true);
    } catch (err) {
      setResults([]);
      setSearchError('Could not connect to the server. Make sure the backend is running.');
      setShowSuggestions(true);
    }
    setSearching(false);
  };

  const handleLoadMore = async () => {
    if (loadingMore) return;
    setLoadingMore(true);
    try {
      const { data } = await searchExercises(searchTerm, PAGE_SIZE, searchOffset);
      setResults(prev => [...prev, ...data]);
      setHasMore(data.length >= PAGE_SIZE);
      setSearchOffset(prev => prev + PAGE_SIZE);
    } catch { /* ignore */ }
    setLoadingMore(false);
  };

  // ─── Select exercise & load detail ─────
  const handleSelect = async (exercise) => {
    setSelectedExercise(exercise);
    setSearchTerm(exercise.name);
    setShowSuggestions(false);
    setResults([]);
    setSearchError('');

    // Set smart defaults based on category
    const cat = (exercise.category || '').toLowerCase();
    if (cat.includes('cardio')) {
      setLogForm({ sets: '0', reps: '0', weightKg: '0', durationMinutes: '30', notes: '' });
    } else if (cat.includes('stretch') || cat === 'stretching') {
      setLogForm({ sets: '0', reps: '0', weightKg: '0', durationMinutes: '10', notes: '' });
    } else if (cat === 'plyometrics') {
      setLogForm({ sets: '3', reps: '10', weightKg: '0', durationMinutes: '0', notes: '' });
    } else {
      setLogForm({ sets: '3', reps: '10', weightKg: '0', durationMinutes: '0', notes: '' });
    }

    // Load full detail
    if (exercise.exerciseId) {
      setLoadingDetail(true);
      try {
        const { data } = await getExerciseDetail(exercise.exerciseId);
        setExerciseDetail(data);
      } catch {
        setExerciseDetail(null);
      }
      setLoadingDetail(false);
    }
  };

  const handleClose = () => {
    setSelectedExercise(null);
    setExerciseDetail(null);
    setSearchTerm('');
    setSearchError('');
  };

  // ─── Log exercise ─────────────────────
  const handleLog = async () => {
    if (!selectedExercise) return;
    const primaryMuscle = selectedExercise.primaryMuscles?.[0] || exerciseDetail?.primaryMuscles?.[0] || '';
    await logExercise(profileId, {
      exerciseName: selectedExercise.name,
      category: primaryMuscle,
      sets: parseInt(logForm.sets) || 0,
      reps: parseInt(logForm.reps) || 0,
      weightKg: parseFloat(logForm.weightKg) || 0,
      durationMinutes: parseInt(logForm.durationMinutes) || 0,
      notes: logForm.notes,
      logDate: date,
    });
    handleClose();
    setLogForm({ sets: '3', reps: '10', weightKg: '0', durationMinutes: '0', notes: '' });
    loadSummary();
  };

  const handleDelete = async (id) => {
    await deleteExerciseLog(id);
    loadSummary();
  };

  // ─── Date helpers ─────────────────────
  const shiftDate = (days) => {
    const d = new Date(date);
    d.setDate(d.getDate() + days);
    setDate(d.toISOString().slice(0, 10));
  };

  const isToday = date === new Date().toISOString().slice(0, 10);

  const formatMuscle = (m) => m.split(' ').map(w => w.charAt(0).toUpperCase() + w.slice(1).toLowerCase()).join(' ');

  const muscleEmoji = (m) => {
    const map = { 
      chest: '🫁', back: '🔙', 'middle back': '🔙', 'lower back': '🔙', lats: '🔙',
      shoulders: '💪', biceps: '💪', triceps: '💪', forearms: '🤲',
      quadriceps: '🦵', hamstrings: '🦵', glutes: '🍑', calves: '🦶', adductors: '🦵', abductors: '🦵',
      abdominals: '🎯', traps: '🏋️', neck: '🧣'
    };
    return map[(m || '').toLowerCase()] || '🏋️';
  };

  const levelColor = (level) => {
    const map = { beginner: '#22c55e', intermediate: '#f59e0b', expert: '#ef4444' };
    return map[(level || '').toLowerCase()] || '#6b7280';
  };

  // ─── Category-aware log form ─────
  const getCategory = () => {
    const raw = exerciseDetail?.category || selectedExercise?.category || '';
    return raw.toLowerCase();
  };

  const renderLogFormFields = () => {
    const cat = getCategory();
    const isCardio = cat.includes('cardio');
    const isStretching = cat.includes('stretch') || cat === 'stretching';
    const isStrength = cat.includes('strength');
    const isPlyometrics = cat === 'plyometrics';

    // Cardio: only duration
    if (isCardio) {
      return (
        <div className="exercise-form-grid exercise-form-grid--narrow">
          <div className="form-group">
            <label>Duration (min)</label>
            <input type="number" min="1" value={logForm.durationMinutes}
              onChange={e => setLogForm({...logForm, durationMinutes: e.target.value})} />
          </div>
        </div>
      );
    }

    // Stretching: only duration
    if (isStretching) {
      return (
        <div className="exercise-form-grid exercise-form-grid--narrow">
          <div className="form-group">
            <label>Duration (min)</label>
            <input type="number" min="1" value={logForm.durationMinutes}
              onChange={e => setLogForm({...logForm, durationMinutes: e.target.value})} />
          </div>
        </div>
      );
    }

    // Plyometrics: sets and reps
    if (isPlyometrics) {
      return (
        <div className="exercise-form-grid exercise-form-grid--narrow">
          <div className="form-group">
            <label>Sets</label>
            <input type="number" min="1" value={logForm.sets}
              onChange={e => setLogForm({...logForm, sets: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Reps</label>
            <input type="number" min="1" value={logForm.reps}
              onChange={e => setLogForm({...logForm, reps: e.target.value})} />
          </div>
        </div>
      );
    }

    // Strength: sets, reps, weight
    if (isStrength) {
      return (
        <div className="exercise-form-grid">
          <div className="form-group">
            <label>Sets</label>
            <input type="number" min="1" value={logForm.sets}
              onChange={e => setLogForm({...logForm, sets: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Reps</label>
            <input type="number" min="1" value={logForm.reps}
              onChange={e => setLogForm({...logForm, reps: e.target.value})} />
          </div>
          <div className="form-group">
            <label>Weight (kg)</label>
            <input type="number" min="0" step="0.5" value={logForm.weightKg}
              onChange={e => setLogForm({...logForm, weightKg: e.target.value})} />
          </div>
        </div>
      );
    }

    // Default / unknown: show all fields
    return (
      <div className="exercise-form-grid">
        <div className="form-group">
          <label>Sets</label>
          <input type="number" min="1" value={logForm.sets}
            onChange={e => setLogForm({...logForm, sets: e.target.value})} />
        </div>
        <div className="form-group">
          <label>Reps</label>
          <input type="number" min="1" value={logForm.reps}
            onChange={e => setLogForm({...logForm, reps: e.target.value})} />
        </div>
        <div className="form-group">
          <label>Weight (kg)</label>
          <input type="number" min="0" step="0.5" value={logForm.weightKg}
            onChange={e => setLogForm({...logForm, weightKg: e.target.value})} />
        </div>
        <div className="form-group">
          <label>Duration (min)</label>
          <input type="number" min="0" value={logForm.durationMinutes}
            onChange={e => setLogForm({...logForm, durationMinutes: e.target.value})} />
        </div>
      </div>
    );
  };

  return (
    <div className="calculator-card exercise-logger">
      <h2>🏋️ Exercise Logger</h2>
      <p className="subtitle">Search 800+ exercises with images and instructions</p>

      {/* ── Date navigation ── */}
      <div className="tracker-header">
        <div className="date-picker-group">
          <button className="date-nav-btn" onClick={() => shiftDate(-1)}>◀</button>
          <input type="date" value={date} onChange={e => setDate(e.target.value)} className="date-picker" />
          <button className="date-nav-btn" onClick={() => shiftDate(1)}>▶</button>
        </div>
        {!isToday && (
          <button className="btn-secondary" onClick={() => setDate(new Date().toISOString().slice(0, 10))}>
            Today
          </button>
        )}
      </div>

      {/* ── Daily summary cards ── */}
      {summary && (
        <div className="exercise-summary-row">
          <div className="exercise-stat-card">
            <span className="exercise-stat-icon">🎯</span>
            <span className="exercise-stat-value">{summary.exerciseCount}</span>
            <span className="exercise-stat-label">Exercises</span>
          </div>
          <div className="exercise-stat-card">
            <span className="exercise-stat-icon">📋</span>
            <span className="exercise-stat-value">{summary.totalSets}</span>
            <span className="exercise-stat-label">Total Sets</span>
          </div>
          <div className="exercise-stat-card">
            <span className="exercise-stat-icon">🔄</span>
            <span className="exercise-stat-value">{summary.totalReps}</span>
            <span className="exercise-stat-label">Total Reps</span>
          </div>
          <div className="exercise-stat-card">
            <span className="exercise-stat-icon">⏱️</span>
            <span className="exercise-stat-value">{summary.totalDuration}</span>
            <span className="exercise-stat-label">Minutes</span>
          </div>
        </div>
      )}

      {/* ── Exercise search ── */}
      <div className="food-search exercise-search-section">
        <h3>Search Exercises</h3>
        <div className="search-row" ref={searchBoxRef}>
          <div className="search-input-wrapper">
            <input
              type="text"
              placeholder="Search by name, muscle, equipment (e.g. biceps, chest, dumbbell)..."
              value={searchTerm}
              onChange={e => handleInputChange(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
              autoComplete="off"
            />
            {showSuggestions && (
              <div className="suggest-dropdown exercise-dropdown">
                {searchError ? (
                  <div className="search-message search-error">
                    <span className="search-message-icon">⚠️</span>
                    <span>{searchError}</span>
                  </div>
                ) : results.length === 0 ? (
                  <div className="search-message search-empty">
                    <span className="search-message-icon">🔍</span>
                    <span>No exercises found for "{searchTerm}". Try a different search term.</span>
                  </div>
                ) : (
                  <>
                    {results.map((ex, i) => (
                      <div key={i} className="suggest-item exercise-suggest-item" onClick={() => handleSelect(ex)}>
                        {ex.imageUrl ? (
                          <img src={ex.imageUrl} alt="" className="exercise-thumb" />
                        ) : (
                          <span className="exercise-thumb exercise-thumb-emoji">{muscleEmoji(ex.primaryMuscles?.[0])}</span>
                        )}
                        <div className="exercise-suggest-info">
                          <span className="suggest-name">{ex.name}</span>
                          <div className="exercise-suggest-meta">
                            {ex.primaryMuscles?.map((m, j) => (
                              <span key={`pm-${j}`} className="muscle-pill target" style={{fontSize:'10px',padding:'2px 8px'}}>{formatMuscle(m)}</span>
                            ))}
                            {ex.equipment && (
                              <span className="exercise-equip-badge">{formatMuscle(ex.equipment)}</span>
                            )}
                            {ex.category && (
                              <span className="exercise-type-badge">{formatMuscle(ex.category)}</span>
                            )}
                            {ex.level && (
                              <span className="exercise-level-badge" style={{backgroundColor: levelColor(ex.level)}}>{formatMuscle(ex.level)}</span>
                            )}
                          </div>
                        </div>
                      </div>
                    ))}
                    {hasMore && (
                      <div className="load-more-row" onClick={handleLoadMore}>
                        {loadingMore ? 'Loading...' : `Show more results ▼`}
                      </div>
                    )}
                  </>
                )}
              </div>
            )}
          </div>
          <button className="btn-primary" onClick={handleSearch} disabled={searching}>
            {searching ? 'Searching...' : '🔍 Search'}
          </button>
        </div>
      </div>

      {/* ── Exercise detail panel ── */}
      {selectedExercise && (
        <div className="exercise-detail-panel">
          <div className="exercise-detail-header">
            <div>
              <h3 className="exercise-detail-name">{selectedExercise.name}</h3>
              <div className="exercise-detail-badges">
                {(exerciseDetail?.primaryMuscles || selectedExercise.primaryMuscles || []).map((m, i) => (
                  <span key={i} className="muscle-pill target">{formatMuscle(m)}</span>
                ))}
                {(exerciseDetail?.equipment || selectedExercise.equipment) && (
                  <span className="exercise-equip-badge">{formatMuscle(exerciseDetail?.equipment || selectedExercise.equipment)}</span>
                )}
                {(exerciseDetail?.category || selectedExercise.category) && (
                  <span className="exercise-type-badge">{formatMuscle(exerciseDetail?.category || selectedExercise.category)}</span>
                )}
                {(exerciseDetail?.level || selectedExercise.level) && (
                  <span className="exercise-level-badge" style={{backgroundColor: levelColor(exerciseDetail?.level || selectedExercise.level)}}>
                    {formatMuscle(exerciseDetail?.level || selectedExercise.level)}
                  </span>
                )}
              </div>
            </div>
            <button className="btn-close" onClick={handleClose}>✕</button>
          </div>

          {/* Log form — at the top */}
          <div className="exercise-log-form">
            <h4>📝 Log This Exercise</h4>
            {renderLogFormFields()}

            <div className="form-group" style={{ marginTop: '12px' }}>
              <label>Notes (optional)</label>
              <input type="text" placeholder="e.g. felt strong, increased weight..."
                value={logForm.notes}
                onChange={e => setLogForm({...logForm, notes: e.target.value})} />
            </div>

            <button className="btn-primary btn-log exercise-log-btn" onClick={handleLog}>
              ✅ Log {selectedExercise.name}
            </button>
          </div>

          {loadingDetail && (
            <div className="exercise-detail-loading">
              <div className="loading-spinner"></div>
              <p>Loading exercise details...</p>
            </div>
          )}

          {exerciseDetail && !loadingDetail && (
            <div className="exercise-detail-body">
              {/* Images row */}
              <div className="exercise-media-row">
                {exerciseDetail.imageUrl && (
                  <div className="exercise-images-container">
                    <img src={exerciseDetail.imageUrl} alt={exerciseDetail.name} className="exercise-detail-img" />
                    {exerciseDetail.secondImageUrl && (
                      <img src={exerciseDetail.secondImageUrl} alt={`${exerciseDetail.name} step 2`} className="exercise-detail-img" />
                    )}
                  </div>
                )}
              </div>

              {/* Exercise Info */}
              <div className="exercise-info-grid">
                {exerciseDetail.force && (
                  <div className="exercise-info-item">
                    <span className="exercise-info-label">Force</span>
                    <span className="exercise-info-value">{formatMuscle(exerciseDetail.force)}</span>
                  </div>
                )}
                {exerciseDetail.mechanic && (
                  <div className="exercise-info-item">
                    <span className="exercise-info-label">Mechanic</span>
                    <span className="exercise-info-value">{formatMuscle(exerciseDetail.mechanic)}</span>
                  </div>
                )}
              </div>

              {/* Muscles */}
              <div className="exercise-muscles-section">
                {exerciseDetail.primaryMuscles?.length > 0 && (
                  <div className="exercise-muscle-group">
                    <h4>🎯 Primary Muscles</h4>
                    <div className="exercise-muscle-pills">
                      {exerciseDetail.primaryMuscles.map((m, i) => (
                        <span key={i} className="muscle-pill target">{formatMuscle(m)}</span>
                      ))}
                    </div>
                  </div>
                )}
                {exerciseDetail.secondaryMuscles?.length > 0 && (
                  <div className="exercise-muscle-group">
                    <h4>🔗 Secondary Muscles</h4>
                    <div className="exercise-muscle-pills">
                      {exerciseDetail.secondaryMuscles.map((m, i) => (
                        <span key={i} className="muscle-pill secondary">{formatMuscle(m)}</span>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              {/* Instructions */}
              {exerciseDetail.instructions?.length > 0 && (
                <div className="exercise-instructions">
                  <h4>📋 Instructions</h4>
                  <ol className="instruction-list">
                    {exerciseDetail.instructions.map((step, i) => (
                      <li key={i}>{step}</li>
                    ))}
                  </ol>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* ── Today's Workout ── */}
      {summary && summary.logs && summary.logs.length > 0 && (
        <div className="exercise-log-list">
          <h3>Today's Workout</h3>
          {summary.logs.map(log => (
            <div key={log.id} className="exercise-log-entry">
              <div className="exercise-log-main">
                <div className="exercise-log-name-row">
                  <strong>{log.exerciseName}</strong>
                  {log.category && <span className="exercise-category-badge small">{log.category}</span>}
                </div>
                <div className="exercise-log-details">
                  {log.sets > 0 && <span className="exercise-detail-chip">📋 {log.sets} sets</span>}
                  {log.reps > 0 && <span className="exercise-detail-chip">🔄 {log.reps} reps</span>}
                  {log.weightKg > 0 && <span className="exercise-detail-chip">🏋️ {log.weightKg} kg</span>}
                  {log.durationMinutes > 0 && <span className="exercise-detail-chip">⏱️ {log.durationMinutes} min</span>}
                </div>
                {log.notes && <div className="exercise-log-notes">📝 {log.notes}</div>}
              </div>
              <button className="btn-delete" onClick={() => handleDelete(log.id)}>×</button>
            </div>
          ))}
        </div>
      )}

      {/* ── Empty state ── */}
      {summary && summary.logs && summary.logs.length === 0 && (
        <div className="empty-state exercise-empty">
          <span style={{ fontSize: '48px', display: 'block', marginBottom: '16px' }}>🏋️</span>
          <p>No exercises logged for this day</p>
          <p style={{ fontSize: '14px', color: 'var(--text-muted)' }}>Search and add exercises above to start tracking your workout</p>
        </div>
      )}
    </div>
  );
}
