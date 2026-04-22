import { useState, useEffect } from 'react';
import { getHealthHistory, saveHealthReport, deleteHealthReport } from '../services/api';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const ACTIVITIES = ['Sedentary', 'Light', 'Moderate', 'Active', 'Extra Active'];

export default function HistoryDashboard() {
  const [form, setForm] = useState({
    weight: '', height: '', age: '', gender: 'Male',
    waist: '', neck: '', activityLevel: 'Moderate',
  });
  const [history, setHistory] = useState([]);
  const [error, setError] = useState('');

  const loadHistory = async () => {
    try {
      const { data } = await getHealthHistory();
      setHistory(data);
    } catch { /* ignore */ }
  };

  useEffect(() => { loadHistory(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await saveHealthReport({
        weight: parseFloat(form.weight),
        height: parseFloat(form.height),
        age: parseInt(form.age),
        gender: form.gender,
        waist: parseFloat(form.waist),
        neck: parseFloat(form.neck),
        activityLevel: form.activityLevel,
      });
      setForm({ weight: '', height: '', age: '', gender: 'Male', waist: '', neck: '', activityLevel: 'Moderate' });
      loadHistory();
    } catch {
      setError('Failed to save report. Check your inputs.');
    }
  };

  const handleDelete = async (id) => {
    try {
      await deleteHealthReport(id);
      loadHistory();
    } catch { /* ignore */ }
  };

  const chartData = history.map(h => ({
    date: h.entryDate ? new Date(h.entryDate).toLocaleDateString() : '',
    BMI: h.bmi ? parseFloat(h.bmi.toFixed(1)) : 0,
    BMR: h.bmr ? Math.round(h.bmr) : 0,
    'Body Fat %': h.bodyFat ? parseFloat(h.bodyFat.toFixed(1)) : 0,
    TDEE: h.tdee ? Math.round(h.tdee) : 0,
  }));

  return (
    <div className="calculator-card">
      <h2>Health History</h2>
      <p className="subtitle">Save a full health report and track your progress over time</p>

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
          <div className="form-group">
            <label>Age</label>
            <input type="number" value={form.age} onChange={e => setForm({...form, age: e.target.value})} required />
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
          <div className="form-group full-width">
            <label>Activity Level</label>
            <select value={form.activityLevel} onChange={e => setForm({...form, activityLevel: e.target.value})}>
              {ACTIVITIES.map(a => <option key={a}>{a}</option>)}
            </select>
          </div>
        </div>
        <div className="btn-row">
          <button type="submit" className="btn-primary">Save Health Report</button>
        </div>
      </form>

      {error && <p className="error-msg">{error}</p>}

      {chartData.length > 0 && (
        <div className="chart-section">
          <h3>Progress Chart</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} />
              <YAxis yAxisId="left" stroke="#94a3b8" fontSize={12} />
              <YAxis yAxisId="right" orientation="right" stroke="#94a3b8" fontSize={12} />
              <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: 8 }} />
              <Legend />
              <Line yAxisId="left" type="monotone" dataKey="BMI" stroke="#3b82f6" strokeWidth={2} dot={{ r: 4 }} />
              <Line yAxisId="left" type="monotone" dataKey="Body Fat %" stroke="#f59e0b" strokeWidth={2} dot={{ r: 4 }} />
              <Line yAxisId="right" type="monotone" dataKey="BMR" stroke="#22c55e" strokeWidth={2} dot={{ r: 4 }} />
              <Line yAxisId="right" type="monotone" dataKey="TDEE" stroke="#a855f7" strokeWidth={2} dot={{ r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      )}

      {history.length > 0 && (
        <div className="history-table-wrapper">
          <h3>History Records</h3>
          <div className="table-scroll">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Date</th>
                  <th>BMI</th>
                  <th>BMR</th>
                  <th>Body Fat %</th>
                  <th>TDEE</th>
                  <th>Ideal Weight</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {history.map(h => (
                  <tr key={h.id}>
                    <td>{h.entryDate ? new Date(h.entryDate).toLocaleString() : '—'}</td>
                    <td>{h.bmi?.toFixed(1)}</td>
                    <td>{h.bmr?.toFixed(0)}</td>
                    <td>{h.bodyFat?.toFixed(1)}%</td>
                    <td>{h.tdee?.toFixed(0)}</td>
                    <td>{h.idealWeight?.toFixed(1)} kg</td>
                    <td><button className="btn-delete" onClick={() => handleDelete(h.id)}>×</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {history.length === 0 && <p className="empty-state">No history yet. Save your first health report above!</p>}
    </div>
  );
}
