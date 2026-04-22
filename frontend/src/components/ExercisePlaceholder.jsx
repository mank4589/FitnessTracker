export default function ExercisePlaceholder() {
  return (
    <div className="calculator-card exercise-placeholder">
      <div className="placeholder-content">
        <span className="placeholder-icon">🏋️</span>
        <h2>Exercise Logging</h2>
        <p className="subtitle">Track your workouts, sets, reps, and progress</p>
        <div className="coming-soon-badge">Coming Soon</div>
        <p className="placeholder-desc">
          We're building a comprehensive exercise logging system. You'll be able to log workouts, 
          track sets and reps, monitor your progress over time, and get personalized recommendations.
        </p>
        <div className="placeholder-features">
          <div className="placeholder-feature">
            <span>📋</span>
            <span>Workout Logging</span>
          </div>
          <div className="placeholder-feature">
            <span>📊</span>
            <span>Progress Tracking</span>
          </div>
          <div className="placeholder-feature">
            <span>🎯</span>
            <span>Custom Routines</span>
          </div>
          <div className="placeholder-feature">
            <span>📈</span>
            <span>Performance Analytics</span>
          </div>
        </div>
      </div>
    </div>
  );
}
