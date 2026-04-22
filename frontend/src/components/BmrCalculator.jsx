import { useState } from 'react';
import { calculateBmr } from '../services/api';

export default function BmrCalculator() {
  const [form, setForm] = useState({ weight: '', height: '', age: '', gender: 'Male' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await calculateBmr({
        weight: parseFloat(form.weight),
        height: parseFloat(form.height),
        age: parseInt(form.age),
        gender: form.gender,
      });
      setResult(data);
    } catch {
      setError('Calculation failed. Check your inputs.');
    }
  };

  const clear = () => { setForm({ weight: '', height: '', age: '', gender: 'Male' }); setResult(null); };

  return (
    <div className="calculator-card">
      <h2>BMR Calculator</h2>
      <p className="subtitle">Basal Metabolic Rate — calories your body burns at rest</p>

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
        <div className="btn-row">
          <button type="submit" className="btn-primary">Calculate</button>
          <button type="button" className="btn-secondary" onClick={clear}>Clear</button>
        </div>
      </form>

      {error && <p className="error-msg">{error}</p>}

      {result && (
        <div className="result-section">
          <div className="result-cards">
            <div className="metric-card">
              <h3>Mifflin-St Jeor</h3>
              <span className="metric-value">{result.mifflinStJeor}</span>
              <span className="metric-unit">kcal/day</span>
            </div>
            <div className="metric-card">
              <h3>Harris-Benedict</h3>
              <span className="metric-value">{result.harrisBenedict}</span>
              <span className="metric-unit">kcal/day</span>
            </div>
          </div>

          <div className="details-table">
            <div className="detail-row"><span>Mifflin-St Jeor BMR</span><span>{result.mifflinStJeor} kcal/day</span></div>
            <div className="detail-row"><span>Harris-Benedict BMR</span><span>{result.harrisBenedict} kcal/day</span></div>
            <div className="detail-row"><span>Difference</span><span>{Math.abs(result.mifflinStJeor - result.harrisBenedict).toFixed(1)} kcal</span></div>
          </div>

          <div className="info-box">
            <p><strong>Mifflin-St Jeor</strong> is considered the most accurate modern formula for estimating BMR.</p>
            <p><strong>Harris-Benedict</strong> (revised 1984) tends to overestimate by ~5% but is still widely used.</p>
          </div>
        </div>
      )}
    </div>
  );
}
