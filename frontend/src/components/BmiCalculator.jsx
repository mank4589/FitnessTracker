import { useState } from 'react';
import { calculateBmi } from '../services/api';

export default function BmiCalculator() {
  const [form, setForm] = useState({ weight: '', height: '', age: '', gender: 'Male' });
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const { data } = await calculateBmi({
        weight: parseFloat(form.weight),
        height: parseFloat(form.height),
        age: parseInt(form.age),
        gender: form.gender,
      });
      setResult(data);
    } catch (err) {
      setError('Calculation failed. Check your inputs.');
    }
  };

  const clear = () => { setForm({ weight: '', height: '', age: '', gender: 'Male' }); setResult(null); };

  const getBmiColor = (bmi) => {
    if (bmi < 18.5) return '#3b82f6';
    if (bmi < 25) return '#22c55e';
    if (bmi < 30) return '#f59e0b';
    return '#ef4444';
  };

  const getBmiPercent = (bmi) => Math.min((bmi / 40) * 100, 100);

  return (
    <div className="calculator-card">
      <h2>BMI Calculator</h2>
      <p className="subtitle">Body Mass Index — measure of body fat based on height and weight</p>

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
          <div className="result-hero">
            <span className="result-big" style={{ color: getBmiColor(result.bmi) }}>{result.bmi}</span>
            <span className="result-unit">kg/m²</span>
            <span className="result-category" style={{ color: getBmiColor(result.bmi) }}>{result.category}</span>
          </div>

          <div className="gauge-bar">
            <div className="gauge-fill" style={{ width: `${getBmiPercent(result.bmi)}%`, background: getBmiColor(result.bmi) }} />
            <div className="gauge-segments">
              <span style={{left: '0%'}}>0</span>
              <span style={{left: '46.25%'}}>18.5</span>
              <span style={{left: '62.5%'}}>25</span>
              <span style={{left: '75%'}}>30</span>
              <span style={{left: '100%'}}>40</span>
            </div>
          </div>

          <div className="details-table">
            <div className="detail-row"><span>BMI</span><span>{result.bmi} kg/m²</span></div>
            <div className="detail-row"><span>Category</span><span>{result.category}</span></div>
            <div className="detail-row"><span>Healthy Weight Range</span><span>{result.healthyWeightRange[0]} – {result.healthyWeightRange[1]} kg</span></div>
            <div className="detail-row"><span>Your Weight</span><span>{result.weight} kg</span></div>
            <div className="detail-row"><span>Advice</span><span>{result.advice}</span></div>
            <div className="detail-row"><span>BMI Prime</span><span>{result.bmiPrime}</span></div>
          </div>
        </div>
      )}
    </div>
  );
}
