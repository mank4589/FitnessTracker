import { useState } from 'react';
import { calculateAll } from '../services/api';
import './HealthDashboard.css';

const ACTIVITY_LEVELS = [
  { value: 'Sedentary', label: 'Sedentary', desc: 'Little or no exercise' },
  { value: 'Light', label: 'Light', desc: 'Exercise 1-3 times/week' },
  { value: 'Moderate', label: 'Moderate', desc: 'Exercise 4-5 times/week' },
  { value: 'Active', label: 'Active', desc: 'Daily exercise or intense 3-4x/week' },
  { value: 'Very Active', label: 'Very Active', desc: 'Intense exercise 6-7x/week' },
  { value: 'Extra Active', label: 'Extra Active', desc: 'Very intense daily or physical job' },
];

const GOALS = [
  'Maintain weight',
  'Mild weight loss of 0.5 lb (0.25 kg) per week',
  'Weight loss of 1 lb (0.5 kg) per week',
  'Extreme weight loss of 2 lb (1 kg) per week',
  'Mild weight gain of 0.5 lb (0.25 kg) per week',
  'Weight gain of 1 lb (0.5 kg) per week',
  'Extreme weight gain of 2 lb (1 kg) per week',
];

const DIET_PLANS = ['Balanced', 'Low Fat', 'Low Carb', 'High Protein'];

export default function HealthDashboard() {
  const [form, setForm] = useState({
    weight: '', height: '', age: '', gender: 'Male',
    activityLevel: 'Sedentary', goal: 'Maintain weight', dietPlan: 'Balanced',
    waist: '', neck: ''
  });
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await calculateAll({
        weight: parseFloat(form.weight),
        height: parseFloat(form.height),
        age: parseInt(form.age),
        gender: form.gender,
        activityLevel: form.activityLevel,
        goal: form.goal,
        dietPlan: form.dietPlan,
        waist: form.waist ? parseFloat(form.waist) : 0,
        neck: form.neck ? parseFloat(form.neck) : 0,
      });
      setResult(data);
    } catch (err) {
      setError('Calculation failed. Please check your inputs.');
    } finally {
      setLoading(false);
    }
  };

  const clear = () => {
    setForm({ weight: '', height: '', age: '', gender: 'Male', activityLevel: 'Sedentary', goal: 'Maintain weight', dietPlan: 'Balanced', waist: '', neck: '' });
    setResult(null);
  };

  const update = (field, value) => setForm(prev => ({ ...prev, [field]: value }));

  const getBmiColor = (bmi) => {
    if (bmi < 18.5) return '#3b82f6';
    if (bmi < 25) return '#22c55e';
    if (bmi < 30) return '#f59e0b';
    return '#ef4444';
  };

  return (
    <div className="health-dashboard">
      <div className="dashboard-header">
        <h2>🏥 Health Calculator</h2>
        <p className="subtitle">Enter your details once — get BMI, BMR, TDEE, Body Fat, Ideal Weight & Macros</p>
      </div>

      <form onSubmit={handleSubmit} className="dashboard-form">
        {/* Basic Info */}
        <div className="form-section">
          <h3>📋 Basic Information</h3>
          <div className="form-grid">
            <div className="form-group">
              <label>Weight (kg)</label>
              <input type="number" step="0.1" placeholder="e.g. 70" value={form.weight} onChange={e => update('weight', e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Height (m)</label>
              <input type="number" step="0.01" placeholder="e.g. 1.75" value={form.height} onChange={e => update('height', e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Age</label>
              <input type="number" placeholder="e.g. 25" value={form.age} onChange={e => update('age', e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Gender</label>
              <select value={form.gender} onChange={e => update('gender', e.target.value)}>
                <option>Male</option>
                <option>Female</option>
              </select>
            </div>
          </div>
        </div>

        {/* Body Measurements (optional) */}
        <div className="form-section">
          <h3>📐 Body Measurements <span className="optional-tag">Optional</span></h3>
          <div className="form-grid">
            <div className="form-group">
              <label>Waist (cm)</label>
              <input type="number" step="0.1" placeholder="For body fat %" value={form.waist} onChange={e => update('waist', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Neck (cm)</label>
              <input type="number" step="0.1" placeholder="For body fat %" value={form.neck} onChange={e => update('neck', e.target.value)} />
            </div>
          </div>
        </div>

        {/* Activity & Goals */}
        <div className="form-section">
          <h3>🏃 Activity & Goals</h3>
          <div className="form-group full-width">
            <label>Activity Level</label>
            <div className="radio-grid">
              {ACTIVITY_LEVELS.map(al => (
                <label key={al.value} className={`radio-card ${form.activityLevel === al.value ? 'selected' : ''}`}>
                  <input type="radio" name="activity" value={al.value} checked={form.activityLevel === al.value} onChange={e => update('activityLevel', e.target.value)} />
                  <span className="radio-label">{al.label}</span>
                  <span className="radio-desc">{al.desc}</span>
                </label>
              ))}
            </div>
          </div>
          <div className="form-grid">
            <div className="form-group">
              <label>Goal</label>
              <select value={form.goal} onChange={e => update('goal', e.target.value)}>
                {GOALS.map(g => <option key={g} value={g}>{g}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Diet Plan</label>
              <select value={form.dietPlan} onChange={e => update('dietPlan', e.target.value)}>
                {DIET_PLANS.map(d => <option key={d} value={d}>{d}</option>)}
              </select>
            </div>
          </div>
        </div>

        <div className="btn-row">
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? '⏳ Calculating...' : '🧮 Calculate Everything'}
          </button>
          <button type="button" className="btn-secondary" onClick={clear}>Clear</button>
        </div>
      </form>

      {error && <p className="error-msg">{error}</p>}

      {result && (
        <div className="results-dashboard">

          {/* BMI Card */}
          <div className="result-card bmi-card">
            <h3>⚖️ BMI</h3>
            <div className="result-hero">
              <span className="result-big" style={{ color: getBmiColor(result.bmi) }}>{result.bmi}</span>
              <span className="result-unit">kg/m²</span>
              <span className="result-category" style={{ color: getBmiColor(result.bmi) }}>{result.bmiCategory}</span>
            </div>
            <div className="gauge-bar">
              <div className="gauge-fill" style={{ width: `${Math.min((result.bmi / 40) * 100, 100)}%`, background: getBmiColor(result.bmi) }} />
              <div className="gauge-segments">
                <span style={{left: '0%'}}>0</span>
                <span style={{left: '46%'}}>18.5</span>
                <span style={{left: '62%'}}>25</span>
                <span style={{left: '75%'}}>30</span>
                <span style={{left: '100%'}}>40</span>
              </div>
            </div>
            <p className="result-advice">{result.bmiAdvice}</p>
            <div className="detail-row"><span>BMI Prime</span><span>{result.bmiPrime}</span></div>
            <div className="detail-row"><span>Healthy Range</span><span>{result.healthyWeightRange[0]} – {result.healthyWeightRange[1]} kg</span></div>
          </div>

          {/* BMR & TDEE Card */}
          <div className="result-card energy-card">
            <h3>🔥 Energy Expenditure</h3>
            <div className="energy-stats">
              <div className="energy-stat">
                <span className="stat-label">BMR (Mifflin)</span>
                <span className="stat-value">{result.bmrMifflin}</span>
                <span className="stat-unit">cal/day</span>
              </div>
              <div className="energy-stat">
                <span className="stat-label">BMR (Harris)</span>
                <span className="stat-value">{result.bmrHarris}</span>
                <span className="stat-unit">cal/day</span>
              </div>
              <div className="energy-stat highlight">
                <span className="stat-label">TDEE</span>
                <span className="stat-value">{result.tdee}</span>
                <span className="stat-unit">cal/day</span>
              </div>
              <div className="energy-stat target">
                <span className="stat-label">Target Calories</span>
                <span className="stat-value">{result.targetCalories}</span>
                <span className="stat-unit">cal/day</span>
              </div>
            </div>
          </div>

          {/* Body Fat Card (conditional) */}
          {result.bodyFat && (
            <div className="result-card bodyfat-card">
              <h3>📊 Body Composition</h3>
              <div className="body-comp">
                <div className="comp-circle">
                  <svg viewBox="0 0 36 36" className="donut-chart">
                    <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      fill="none" stroke="rgba(255,255,255,0.1)" strokeWidth="3" />
                    <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                      fill="none" stroke="#f59e0b" strokeWidth="3"
                      strokeDasharray={`${result.bodyFat}, 100`} />
                  </svg>
                  <div className="comp-center">
                    <span className="comp-value">{result.bodyFat}%</span>
                    <span className="comp-label">Body Fat</span>
                  </div>
                </div>
                <div className="comp-details">
                  <div className="detail-row"><span>Category</span><span>{result.bodyFatCategory}</span></div>
                  <div className="detail-row"><span>Fat Mass</span><span>{result.fatMass} kg</span></div>
                  <div className="detail-row"><span>Lean Mass</span><span>{result.leanMass} kg</span></div>
                </div>
              </div>
            </div>
          )}

          {/* Ideal Weight Card */}
          <div className="result-card ideal-card">
            <h3>🎯 Ideal Weight</h3>
            <div className="ideal-formulas">
              <div className="formula-row"><span>Devine Formula</span><span>{result.idealWeight.devine} kg</span></div>
              <div className="formula-row"><span>Robinson Formula</span><span>{result.idealWeight.robinson} kg</span></div>
              <div className="formula-row"><span>Miller Formula</span><span>{result.idealWeight.miller} kg</span></div>
              <div className="formula-row"><span>Hamwi Formula</span><span>{result.idealWeight.hamwi} kg</span></div>
              <div className="formula-row highlight"><span>Healthy BMI Range</span><span>{result.idealWeight.healthyRange[0]} – {result.idealWeight.healthyRange[1]} kg</span></div>
            </div>
          </div>

          {/* Macros Card */}
          <div className="result-card macros-card">
            <h3>🥗 Daily Macros</h3>
            <div className="macros-grid">
              <div className="macro-item protein">
                <div className="macro-icon">🥩</div>
                <div className="macro-value">{result.protein}g</div>
                <div className="macro-label">Protein</div>
                <div className="macro-range">{result.proteinMin}–{result.proteinMax}g</div>
              </div>
              <div className="macro-item carbs">
                <div className="macro-icon">🌾</div>
                <div className="macro-value">{result.carbs}g</div>
                <div className="macro-label">Carbs</div>
                <div className="macro-range">{result.carbsMin}–{result.carbsMax}g</div>
              </div>
              <div className="macro-item fats">
                <div className="macro-icon">🥑</div>
                <div className="macro-value">{result.fat}g</div>
                <div className="macro-label">Fat</div>
                <div className="macro-range">{result.fatMin}–{result.fatMax}g</div>
              </div>
            </div>
            <div className="macro-extras">
              <div className="detail-row"><span>Max Sugar</span><span>{result.sugar}g / day</span></div>
              <div className="detail-row"><span>Max Saturated Fat</span><span>{result.saturatedFat}g / day</span></div>
            </div>
          </div>

        </div>
      )}
    </div>
  );
}
