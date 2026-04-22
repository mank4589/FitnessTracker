import { useState } from 'react';
import { calculateIdealWeight } from '../services/api';

export default function IdealWeight() {
  const [form, setForm] = useState({ height: '', gender: 'Male' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await calculateIdealWeight({
        height: parseFloat(form.height),
        gender: form.gender,
      });
      setResult(data);
    } catch {
      setError('Calculation failed. Check your inputs.');
    }
  };

  const clear = () => { setForm({ height: '', gender: 'Male' }); setResult(null); };

  return (
    <div className="calculator-card">
      <h2>Ideal Weight Calculator</h2>
      <p className="subtitle">Based on multiple scientific formulas</p>

      <form onSubmit={handleSubmit} className="calc-form">
        <div className="form-row">
          <div className="form-group">
            <label>Height (m)</label>
            <input type="number" step="0.01" value={form.height} onChange={e => setForm({...form, height: e.target.value})} required />
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
          <div className="result-cards four-col">
            <div className="metric-card">
              <h3>Devine</h3>
              <span className="metric-value">{result.devine}</span>
              <span className="metric-unit">kg</span>
            </div>
            <div className="metric-card">
              <h3>Robinson</h3>
              <span className="metric-value">{result.robinson}</span>
              <span className="metric-unit">kg</span>
            </div>
            <div className="metric-card">
              <h3>Miller</h3>
              <span className="metric-value">{result.miller}</span>
              <span className="metric-unit">kg</span>
            </div>
            <div className="metric-card">
              <h3>Hamwi</h3>
              <span className="metric-value">{result.hamwi}</span>
              <span className="metric-unit">kg</span>
            </div>
          </div>

          <div className="details-table">
            <div className="detail-row"><span>Devine (1974)</span><span>{result.devine} kg</span></div>
            <div className="detail-row"><span>Robinson (1983)</span><span>{result.robinson} kg</span></div>
            <div className="detail-row"><span>Miller (1983)</span><span>{result.miller} kg</span></div>
            <div className="detail-row"><span>Hamwi (1964)</span><span>{result.hamwi} kg</span></div>
            <div className="detail-row highlight"><span>Healthy BMI Range</span><span>{result.healthyRange[0]} – {result.healthyRange[1]} kg</span></div>
          </div>
        </div>
      )}
    </div>
  );
}
