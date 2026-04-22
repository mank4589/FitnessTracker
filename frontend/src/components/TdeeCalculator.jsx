import { useState } from 'react';
import { calculateTdee } from '../services/api';

const ACTIVITIES = ['Sedentary', 'Light', 'Moderate', 'Active', 'Extra Active'];

export default function TdeeCalculator() {
  const [form, setForm] = useState({ weight: '', height: '', age: '', gender: 'Male', activityLevel: 'Moderate' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await calculateTdee({
        weight: parseFloat(form.weight),
        height: parseFloat(form.height),
        age: parseInt(form.age),
        gender: form.gender,
        activityLevel: form.activityLevel,
      });
      setResult(data);
    } catch {
      setError('Calculation failed. Check your inputs.');
    }
  };

  const clear = () => { setForm({ weight: '', height: '', age: '', gender: 'Male', activityLevel: 'Moderate' }); setResult(null); };

  return (
    <div className="calculator-card">
      <h2>TDEE Calculator</h2>
      <p className="subtitle">Total Daily Energy Expenditure — total calories you burn per day</p>

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
          <div className="form-group full-width">
            <label>Activity Level</label>
            <select value={form.activityLevel} onChange={e => setForm({...form, activityLevel: e.target.value})}>
              {ACTIVITIES.map(a => <option key={a}>{a}</option>)}
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
        <div className="result-section">
          <div className="result-cards">
            <div className="metric-card accent">
              <h3>TDEE</h3>
              <span className="metric-value">{result.tdee}</span>
              <span className="metric-unit">kcal/day</span>
            </div>
            <div className="metric-card">
              <h3>BMR</h3>
              <span className="metric-value">{result.bmr}</span>
              <span className="metric-unit">kcal/day</span>
            </div>
          </div>

          <div className="details-table">
            <div className="detail-row"><span>TDEE</span><span>{result.tdee} kcal/day</span></div>
            <div className="detail-row"><span>BMR (base)</span><span>{result.bmr} kcal/day</span></div>
            <div className="detail-row"><span>Activity Multiplier</span><span>×{result.activityMultiplier}</span></div>
            <div className="detail-row"><span>Mild Weight Loss</span><span>{(result.tdee - 250).toFixed(0)} kcal/day</span></div>
            <div className="detail-row"><span>Weight Loss</span><span>{(result.tdee - 500).toFixed(0)} kcal/day</span></div>
            <div className="detail-row"><span>Mild Weight Gain</span><span>{(result.tdee + 250).toFixed(0)} kcal/day</span></div>
            <div className="detail-row"><span>Weight Gain</span><span>{(result.tdee + 500).toFixed(0)} kcal/day</span></div>
          </div>
        </div>
      )}
    </div>
  );
}
