import { useState } from 'react';
import { calculateBodyFat } from '../services/api';

export default function BodyFatCalculator() {
  const [form, setForm] = useState({ weight: '', waist: '', neck: '', height: '', gender: 'Male' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await calculateBodyFat({
        weight: parseFloat(form.weight),
        waist: parseFloat(form.waist),
        neck: parseFloat(form.neck),
        height: parseFloat(form.height),
        gender: form.gender,
      });
      setResult(data);
    } catch {
      setError('Calculation failed. Check your inputs.');
    }
  };

  const clear = () => { setForm({ weight: '', waist: '', neck: '', height: '', gender: 'Male' }); setResult(null); };

  const getColor = (bf, gender) => {
    const limit = gender === 'Male' ? [6, 14, 18, 25] : [14, 21, 25, 32];
    if (bf < limit[0]) return '#3b82f6';
    if (bf < limit[1]) return '#22c55e';
    if (bf < limit[2]) return '#84cc16';
    if (bf < limit[3]) return '#f59e0b';
    return '#ef4444';
  };

  return (
    <div className="calculator-card">
      <h2>Body Fat Calculator</h2>
      <p className="subtitle">U.S. Navy method — estimates body fat percentage</p>

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
            <label>Waist (cm)</label>
            <input type="number" step="0.1" value={form.waist} onChange={e => setForm({...form, waist: e.target.value})} required />
          </div>
          <div className="form-group">
            <label>Neck (cm)</label>
            <input type="number" step="0.1" value={form.neck} onChange={e => setForm({...form, neck: e.target.value})} required />
          </div>
        </div>
        <div className="form-row">
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
          <div className="result-hero">
            <span className="result-big" style={{ color: getColor(result.bodyFat, form.gender) }}>{result.bodyFat}%</span>
            <span className="result-category" style={{ color: getColor(result.bodyFat, form.gender) }}>{result.category}</span>
          </div>

          <div className="details-table">
            <div className="detail-row"><span>Body Fat</span><span>{result.bodyFat}%</span></div>
            <div className="detail-row"><span>Category</span><span>{result.category}</span></div>
            <div className="detail-row"><span>Fat Mass</span><span>{result.fatMass} kg</span></div>
            <div className="detail-row"><span>Lean Mass</span><span>{result.leanMass} kg</span></div>
          </div>
        </div>
      )}
    </div>
  );
}
