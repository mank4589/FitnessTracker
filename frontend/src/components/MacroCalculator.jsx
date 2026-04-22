import { useState } from 'react';
import { calculateMacros } from '../services/api';

const ACTIVITIES = ['Sedentary', 'Light', 'Moderate', 'Active', 'Very Active', 'Extra Active'];
const GOALS = [
  'Maintain weight',
  'Mild weight loss of 0.5 lb (0.25 kg) per week',
  'Weight loss of 1 lb (0.5 kg) per week',
  'Extreme weight loss of 2 lb (1 kg) per week',
  'Mild weight gain of 0.5 lb (0.25 kg) per week',
  'Weight gain of 1 lb (0.5 kg) per week',
  'Extreme weight gain of 2 lb (1 kg) per week',
];
const PLANS = ['Balanced', 'Low Fat', 'Low Carb', 'High Protein'];

export default function MacroCalculator() {
  const [form, setForm] = useState({
    weight: '', height: '', age: '', gender: 'Male',
    activityLevel: 'Moderate', goal: 'Maintain weight', dietPlan: 'Balanced',
  });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await calculateMacros({
        weight: parseFloat(form.weight),
        height: parseFloat(form.height),
        age: parseInt(form.age),
        gender: form.gender,
        activityLevel: form.activityLevel,
        goal: form.goal,
        dietPlan: form.dietPlan,
      });
      setResult(data);
    } catch {
      setError('Calculation failed. Check your inputs.');
    }
  };

  const switchPlan = async (plan) => {
    setForm(prev => ({ ...prev, dietPlan: plan }));
    if (form.weight && form.height && form.age) {
      try {
        const { data } = await calculateMacros({
          weight: parseFloat(form.weight),
          height: parseFloat(form.height),
          age: parseInt(form.age),
          gender: form.gender,
          activityLevel: form.activityLevel,
          goal: form.goal,
          dietPlan: plan,
        });
        setResult(data);
      } catch { /* ignore */ }
    }
  };

  const clear = () => {
    setForm({ weight: '', height: '', age: '', gender: 'Male', activityLevel: 'Moderate', goal: 'Maintain weight', dietPlan: 'Balanced' });
    setResult(null);
  };

  const fmt = (v) => Number(v).toLocaleString(undefined, { maximumFractionDigits: 0 });

  return (
    <div className="calculator-card">
      <h2>Macro Calculator</h2>
      <p className="subtitle">Calculate your optimal macronutrient intake based on WHO & AMDR guidelines</p>

      <form onSubmit={handleSubmit} className="calc-form">
        <div className="form-row">
          <div className="form-group">
            <label>Weight (kg)</label>
            <input type="number" step="0.1" value={form.weight} onChange={e => setForm({...form, weight: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Height (m)</label>
            <input type="number" step="0.01" value={form.height} onChange={e => setForm({...form, height: e.target.value})} required />
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label>Age</label>
            <input type="number" value={form.age} onChange={e => setForm({...form, age: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Gender</label>
            <select value={form.gender} onChange={e => setForm({...form, gender: e.target.value})}>
              <option>Male</option>
              <option>Female</option>
            </select>
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label>Activity Level</label>
            <select value={form.activityLevel} onChange={e => setForm({...form, activityLevel: e.target.value})}>
              {ACTIVITIES.map(a => <option key={a}>{a}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Goal</label>
            <select value={form.goal} onChange={e => setForm({...form, goal: e.target.value})}>
              {GOALS.map(g => <option key={g}>{g}</option>)}
            </select>
          </div>
        </div>
        <div className="btn-row">
          <button type="submit" className="btn-primary">Calculate</button>
          <button type="button" className="btn-secondary" onClick={clear}>Clear</button>
        </div>
      </form>

      {error && <p className="error-msg">{error}</p>}

      {result && (
        <div className="result-section macro-result-section">
          <div className="macro-result-intro">
            <p>The results below are the suggested amounts of macronutrients and food energy (Calories) you need to consume daily to <strong>{form.goal.toLowerCase()}</strong>. Each macronutrient amount is represented as a range of values. Please click whichever tab best suits your needs.</p>
          </div>

          <div className="plan-tabs">
            {PLANS.map(p => (
              <button key={p}
                className={`plan-tab ${form.dietPlan === p ? 'active' : ''}`}
                onClick={() => switchPlan(p)}>
                {p}
              </button>
            ))}
          </div>

          {/* ── Macro Results Table ── */}
          <div className="macro-results-table">
            {/* Protein */}
            <div className="macro-row">
              <div className="macro-row-label">
                <span className="macro-row-name">Protein</span>
              </div>
              <div className="macro-row-value">
                <span className="macro-row-amount">{fmt(result.protein)}</span>
                <span className="macro-row-unit">grams/day</span>
                <span className="macro-row-range">Range: {fmt(result.proteinMin)} – {fmt(result.proteinMax)}</span>
              </div>
            </div>

            {/* Carbs */}
            <div className="macro-row">
              <div className="macro-row-label">
                <span className="macro-row-name">Carbs</span>
                <span className="macro-row-sub">Includes Sugar</span>
              </div>
              <div className="macro-row-value">
                <span className="macro-row-amount">{fmt(result.carbs)}</span>
                <span className="macro-row-unit">grams/day</span>
                <span className="macro-row-range">Range: {fmt(result.carbsMin)} – {fmt(result.carbsMax)}</span>
              </div>
            </div>

            {/* Fat */}
            <div className="macro-row">
              <div className="macro-row-label">
                <span className="macro-row-name">Fat</span>
                <span className="macro-row-sub">Includes Saturated Fat</span>
              </div>
              <div className="macro-row-value">
                <span className="macro-row-amount">{fmt(result.fat)}</span>
                <span className="macro-row-unit">grams/day</span>
                <span className="macro-row-range">Range: {fmt(result.fatMin)} – {fmt(result.fatMax)}</span>
              </div>
            </div>

            {/* Sugar */}
            <div className="macro-row macro-row-limit">
              <div className="macro-row-label">
                <span className="macro-row-name">Sugar</span>
              </div>
              <div className="macro-row-value">
                <span className="macro-row-amount">&lt;{fmt(result.sugar)}</span>
                <span className="macro-row-unit">grams/day</span>
              </div>
            </div>

            {/* Saturated Fat */}
            <div className="macro-row macro-row-limit">
              <div className="macro-row-label">
                <span className="macro-row-name">Saturated Fat</span>
              </div>
              <div className="macro-row-value">
                <span className="macro-row-amount">&lt;{fmt(result.saturatedFat)}</span>
                <span className="macro-row-unit">grams/day</span>
              </div>
            </div>

            {/* Food Energy */}
            <div className="macro-row macro-row-energy">
              <div className="macro-row-label">
                <span className="macro-row-name">Food Energy</span>
              </div>
              <div className="macro-row-value">
                <span className="macro-row-amount">{fmt(result.targetCalories)}</span>
                <span className="macro-row-unit">Calories/day</span>
                <span className="macro-row-range">or {fmt(result.targetKj)} kJ/day</span>
              </div>
            </div>
          </div>

          {/* ── Additional details ── */}
          <div className="details-table">
            <div className="detail-row"><span>TDEE</span><span>{result.tdee} kcal</span></div>
            <div className="detail-row"><span>BMR</span><span>{result.bmr} kcal</span></div>
          </div>

          {/* ── Disclaimer ── */}
          <div className="macro-disclaimer">
            <p>The results above are a guideline for more typical situations. Please consult with a doctor for your macronutrient needs if you are an athlete, training for a specific purpose, or on a special diet due to a disease, pregnancy, or other conditions. The protein range is calculated based on the guidelines set by the American Dietetic Association (ADA), the Centers for Disease Control and Prevention (CDC), and the World Health Organization. The carbohydrate range is based on the guidelines and joint recommendations of The Institute of Medicine, The Food and Agriculture Organization, and the World Health Organization.</p>
          </div>
        </div>
      )}
    </div>
  );
}
