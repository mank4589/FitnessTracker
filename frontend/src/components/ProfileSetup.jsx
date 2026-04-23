import React, { useState } from 'react';
import { createProfile, updateProfile } from '../services/api';
import './ProfileSetup.css';

const ProfileSetup = ({ onProfileCreated, existingProfile }) => {
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [formData, setFormData] = useState({
    name: existingProfile?.name || '',
    age: existingProfile?.age || '',
    gender: existingProfile?.gender || 'MALE',
    heightCm: existingProfile?.heightCm || '',
    weightKg: existingProfile?.weightKg || '',
    fitnessGoal: 'MAINTAIN',
    activityLevel: 'MODERATE',
    experienceLevel: 'BEGINNER',
    workoutDaysPerWeek: 3,
    workoutDurationMinutes: 45,
    dietaryPreference: 'NONE',
    dietaryRestrictions: [],
    injuries: [],
    targetMuscleGroups: [],
    includeCardio: true,
    mealsPerDay: 3,
    snacksPerDay: 1,
    cuisinePreferences: [],
    medicalConditions: [],
  });

  const fitnessGoals = [
    { value: 'LOSE_FAT', label: 'Lose Fat', icon: '🔥', desc: 'Burn fat and get lean' },
    { value: 'GAIN_MUSCLE', label: 'Build Muscle', icon: '💪', desc: 'Gain strength and size' },
    { value: 'MAINTAIN', label: 'Maintain', icon: '⚖️', desc: 'Keep current fitness' },
    { value: 'RECOMP', label: 'Body Recomp', icon: '🔄', desc: 'Lose fat & gain muscle' },
  ];

  const activityLevels = [
    { value: 'SEDENTARY', label: 'Sedentary', desc: 'Little to no exercise' },
    { value: 'LIGHT', label: 'Lightly Active', desc: 'Exercise 1-3 days/week' },
    { value: 'MODERATE', label: 'Moderately Active', desc: 'Exercise 3-5 days/week' },
    { value: 'VERY_ACTIVE', label: 'Very Active', desc: 'Exercise 6-7 days/week' },
    { value: 'EXTRA_ACTIVE', label: 'Extra Active', desc: 'Very intense exercise daily' },
  ];

  const experienceLevels = [
    { value: 'BEGINNER', label: 'Beginner', desc: 'New to working out (0-1 year)' },
    { value: 'INTERMEDIATE', label: 'Intermediate', desc: 'Some experience (1-3 years)' },
    { value: 'ADVANCED', label: 'Advanced', desc: 'Experienced lifter (3+ years)' },
  ];

  const dietaryPreferences = [
    { value: 'NONE', label: 'No Preference' },
    { value: 'VEGETARIAN', label: 'Vegetarian' },
    { value: 'EGGETARIAN', label: 'Vegetarian + Eggs' },
    { value: 'VEGAN', label: 'Vegan' },
    { value: 'PESCATARIAN', label: 'Pescatarian' },
    { value: 'KETO', label: 'Keto' },
    { value: 'PALEO', label: 'Paleo' },
    { value: 'MEDITERRANEAN', label: 'Mediterranean' },
    { value: 'LOW_CARB', label: 'Low Carb' },
    { value: 'HIGH_PROTEIN', label: 'High Protein' },
  ];

  const cuisineOptions = [
    { value: 'ALL', label: 'All Cuisines', icon: '🌍' },
    { value: 'INDIAN', label: 'Indian', icon: '🇮🇳' },
    { value: 'CHINESE', label: 'Chinese', icon: '🇨🇳' },
    { value: 'JAPANESE', label: 'Japanese', icon: '🇯🇵' },
    { value: 'THAI', label: 'Thai', icon: '🇹🇭' },
    { value: 'ITALIAN', label: 'Italian', icon: '🇮🇹' },
    { value: 'MEXICAN', label: 'Mexican', icon: '🇲🇽' },
    { value: 'AMERICAN', label: 'American', icon: '🇺🇸' },
    { value: 'MEDITERRANEAN', label: 'Mediterranean', icon: '🫒' },
    { value: 'KOREAN', label: 'Korean', icon: '🇰🇷' },
  ];

  const dietaryRestrictions = [
    'GLUTEN_FREE', 'DAIRY_FREE', 'NUT_FREE', 'SOY_FREE', 
    'EGG_FREE', 'SHELLFISH_FREE', 'FISH_FREE', 'BEEF_FREE', 'PORK_FREE',
    'LOW_SODIUM', 'LOW_SUGAR', 'HALAL', 'KOSHER'
  ];

  const medicalConditions = [
    { value: 'DIABETES', label: 'Diabetes', icon: '🩸' },
    { value: 'HYPERTENSION', label: 'High BP', icon: '💓' },
    { value: 'HIGH_CHOLESTEROL', label: 'High Cholesterol', icon: '🫀' },
    { value: 'LOW_BP', label: 'Low BP', icon: '📉' },
    { value: 'KIDNEY_ISSUES', label: 'Kidney Issues', icon: '🫘' },
    { value: 'PCOD', label: 'PCOD / Hormonal', icon: '♀️' },
    { value: 'THYROID', label: 'Thyroid', icon: '🦋' },
    { value: 'HEART_DISEASE', label: 'Heart Disease', icon: '❤️‍🩹' },
    { value: 'OBESITY', label: 'Obesity', icon: '⚖️' },
  ];

  const injuries = [
    { value: 'LOWER_BACK', label: 'Lower Back' },
    { value: 'UPPER_BACK', label: 'Upper Back' },
    { value: 'NECK', label: 'Neck' },
    { value: 'SHOULDER', label: 'Shoulder' },
    { value: 'ELBOW', label: 'Elbow' },
    { value: 'WRIST', label: 'Wrist' },
    { value: 'HIP', label: 'Hip' },
    { value: 'KNEE', label: 'Knee' },
    { value: 'ANKLE', label: 'Ankle' },
    { value: 'ROTATOR_CUFF', label: 'Rotator Cuff' },
  ];

  const muscleGroups = [
    { value: 'CHEST', label: 'Chest' },
    { value: 'BACK', label: 'Back' },
    { value: 'SHOULDERS', label: 'Shoulders' },
    { value: 'BICEPS', label: 'Biceps' },
    { value: 'TRICEPS', label: 'Triceps' },
    { value: 'QUADRICEPS', label: 'Quads' },
    { value: 'HAMSTRINGS', label: 'Hamstrings' },
    { value: 'GLUTES', label: 'Glutes' },
    { value: 'CALVES', label: 'Calves' },
    { value: 'CORE', label: 'Core' },
    { value: 'FULL_BODY', label: 'Full Body' },
  ];

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleMultiSelect = (name, value) => {
    setFormData(prev => {
      const currentValues = prev[name] || [];
      const newValues = currentValues.includes(value)
        ? currentValues.filter(v => v !== value)
        : [...currentValues, value];
      return { ...prev, [name]: newValues };
    });
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError('');
    
    try {
      const profileData = {
        ...formData,
        age: parseInt(formData.age) || null,
        heightCm: parseFloat(formData.heightCm) || null,
        weightKg: parseFloat(formData.weightKg) || null,
        workoutDaysPerWeek: parseInt(formData.workoutDaysPerWeek),
        workoutDurationMinutes: parseInt(formData.workoutDurationMinutes),
        mealsPerDay: parseInt(formData.mealsPerDay),
        snacksPerDay: parseInt(formData.snacksPerDay),
      };
      
      if (existingProfile?.id) {
        // Update existing profile
        const response = await updateProfile(existingProfile.id, profileData);
        onProfileCreated(response.data);
      } else {
        const response = await createProfile(profileData);
        onProfileCreated(response.data);
      }
    } catch (error) {
      console.error('Error creating profile:', error);
      setError('Failed to create profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const canProceed = () => {
    switch (step) {
      case 1: return formData.name.trim() !== '';
      case 2: return formData.heightCm && formData.weightKg;
      case 3: return true;
      case 4: return true;
      case 5: return true;
      default: return true;
    }
  };

  const renderStep = () => {
    switch (step) {
      case 1:
        return (
          <div className="setup-step">
            <h2>Welcome to UFit! 👋</h2>
            <p className="step-description">Let's set up your personalized fitness profile. First, what should we call you?</p>
            
            <div className="form-group large">
              <label>Your Name</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                placeholder="Enter your name"
                autoFocus
              />
            </div>
          </div>
        );

      case 2:
        return (
          <div className="setup-step">
            <h2>Your Body Metrics 📊</h2>
            <p className="step-description">These help us calculate your calorie needs and personalize recommendations.</p>
            
            <div className="form-row">
              <div className="form-group">
                <label>Age</label>
                <input
                  type="number"
                  name="age"
                  value={formData.age}
                  onChange={handleInputChange}
                  placeholder="Years"
                  min="13"
                  max="100"
                />
              </div>
              
              <div className="form-group">
                <label>Gender</label>
                <select name="gender" value={formData.gender} onChange={handleInputChange}>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Height (cm)</label>
                <input
                  type="number"
                  name="heightCm"
                  value={formData.heightCm}
                  onChange={handleInputChange}
                  placeholder="e.g., 175"
                  min="100"
                  max="250"
                />
              </div>
              
              <div className="form-group">
                <label>Weight (kg)</label>
                <input
                  type="number"
                  name="weightKg"
                  value={formData.weightKg}
                  onChange={handleInputChange}
                  placeholder="e.g., 70"
                  min="30"
                  max="300"
                  step="0.1"
                />
              </div>
            </div>
          </div>
        );

      case 3:
        return (
          <div className="setup-step">
            <h2>What's Your Goal? 🎯</h2>
            <p className="step-description">This helps us tailor your workout and nutrition plans.</p>
            
            <div className="goal-cards">
              {fitnessGoals.map(goal => (
                <button
                  key={goal.value}
                  className={`goal-card ${formData.fitnessGoal === goal.value ? 'selected' : ''}`}
                  onClick={() => setFormData(prev => ({ ...prev, fitnessGoal: goal.value }))}
                >
                  <span className="goal-icon">{goal.icon}</span>
                  <span className="goal-label">{goal.label}</span>
                  <span className="goal-desc">{goal.desc}</span>
                </button>
              ))}
            </div>

            <div className="form-group" style={{ marginTop: '24px' }}>
              <label>Current Activity Level</label>
              <div className="activity-options">
                {activityLevels.map(level => (
                  <button
                    key={level.value}
                    className={`activity-btn ${formData.activityLevel === level.value ? 'selected' : ''}`}
                    onClick={() => setFormData(prev => ({ ...prev, activityLevel: level.value }))}
                  >
                    <span className="activity-label">{level.label}</span>
                    <span className="activity-desc">{level.desc}</span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        );

      case 4:
        return (
          <div className="setup-step">
            <h2>Workout Preferences 💪</h2>
            <p className="step-description">Tell us about your training experience and schedule.</p>
            
            <div className="form-group">
              <label>Experience Level</label>
              <div className="experience-options">
                {experienceLevels.map(level => (
                  <button
                    key={level.value}
                    className={`experience-btn ${formData.experienceLevel === level.value ? 'selected' : ''}`}
                    onClick={() => setFormData(prev => ({ ...prev, experienceLevel: level.value }))}
                  >
                    <span className="exp-label">{level.label}</span>
                    <span className="exp-desc">{level.desc}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Workout Days Per Week</label>
                <div className="number-selector">
                  {[2, 3, 4, 5, 6].map(num => (
                    <button
                      key={num}
                      className={`num-btn ${formData.workoutDaysPerWeek === num ? 'selected' : ''}`}
                      onClick={() => setFormData(prev => ({ ...prev, workoutDaysPerWeek: num }))}
                    >
                      {num}
                    </button>
                  ))}
                </div>
              </div>
              
              <div className="form-group">
                <label>Session Duration (min)</label>
                <div className="number-selector">
                  {[30, 45, 60, 75, 90].map(num => (
                    <button
                      key={num}
                      className={`num-btn ${formData.workoutDurationMinutes === num ? 'selected' : ''}`}
                      onClick={() => setFormData(prev => ({ ...prev, workoutDurationMinutes: num }))}
                    >
                      {num}
                    </button>
                  ))}
                </div>
              </div>
            </div>

            <div className="form-group">
              <label>Any Injuries or Limitations? (Select all that apply)</label>
              <div className="chip-grid">
                {injuries.map(injury => (
                  <button
                    key={injury.value}
                    className={`chip ${formData.injuries.includes(injury.value) ? 'selected' : ''}`}
                    onClick={() => handleMultiSelect('injuries', injury.value)}
                  >
                    {injury.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="form-group">
              <label className="checkbox-inline">
                <input
                  type="checkbox"
                  name="includeCardio"
                  checked={formData.includeCardio}
                  onChange={handleInputChange}
                />
                <span>Include cardio in my workout plans</span>
              </label>
            </div>
          </div>
        );

      case 5:
        return (
          <div className="setup-step">
            <h2>Dietary Preferences 🥗</h2>
            <p className="step-description">Help us create meal plans that match your lifestyle.</p>
            
            <div className="form-group">
              <label>Diet Type</label>
              <div className="diet-options">
                {dietaryPreferences.map(pref => (
                  <button
                    key={pref.value}
                    className={`diet-btn ${formData.dietaryPreference === pref.value ? 'selected' : ''}`}
                    onClick={() => setFormData(prev => ({ ...prev, dietaryPreference: pref.value }))}
                  >
                    {pref.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="form-group">
              <label>Cuisine Preferences (Select your favorites)</label>
              <p className="step-description" style={{ fontSize: '0.85rem', marginTop: '-4px', marginBottom: '8px', opacity: 0.7 }}>Your meal plans will prioritize dishes from these cuisines</p>
              <div className="chip-grid">
                {cuisineOptions.map(cuisine => (
                  <button
                    key={cuisine.value}
                    className={`chip ${formData.cuisinePreferences.includes(cuisine.value) ? 'selected' : ''}`}
                    onClick={() => {
                      if (cuisine.value === 'ALL') {
                        setFormData(prev => ({ ...prev, cuisinePreferences: prev.cuisinePreferences.includes('ALL') ? [] : ['ALL'] }));
                      } else {
                        const updated = formData.cuisinePreferences.filter(v => v !== 'ALL');
                        const newValues = updated.includes(cuisine.value)
                          ? updated.filter(v => v !== cuisine.value)
                          : [...updated, cuisine.value];
                        setFormData(prev => ({ ...prev, cuisinePreferences: newValues }));
                      }
                    }}
                  >
                    {cuisine.icon} {cuisine.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="form-group">
              <label>Dietary Restrictions (Select all that apply)</label>
              <div className="chip-grid">
                {dietaryRestrictions.map(restriction => (
                  <button
                    key={restriction}
                    className={`chip ${formData.dietaryRestrictions.includes(restriction) ? 'selected' : ''}`}
                    onClick={() => handleMultiSelect('dietaryRestrictions', restriction)}
                  >
                    {restriction.replace('_', ' ')}
                  </button>
                ))}
              </div>
            </div>

            <div className="form-group">
              <label>Medical Conditions <span style={{fontSize: '0.8rem', opacity: 0.6}}>(optional — affects meal plan filtering)</span></label>
              <p className="step-description" style={{ fontSize: '0.85rem', marginTop: '-4px', marginBottom: '8px', opacity: 0.7 }}>Auto-generated meal plans will exclude foods unsafe for your conditions</p>
              <div className="chip-grid">
                {medicalConditions.map(condition => (
                  <button
                    key={condition.value}
                    className={`chip medical-chip ${formData.medicalConditions.includes(condition.value) ? 'selected medical-selected' : ''}`}
                    onClick={() => handleMultiSelect('medicalConditions', condition.value)}
                  >
                    {condition.icon} {condition.label}
                  </button>
                ))}
              </div>
              {formData.medicalConditions.length > 0 && (
                <p style={{ fontSize: '0.75rem', color: '#fdba74', marginTop: '8px', opacity: 0.8 }}>
                  ⚠️ Based on WHO / AHA / NKF guidelines. Not a substitute for medical advice.
                </p>
              )}
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Meals Per Day</label>
                <div className="number-selector">
                  {[2, 3, 4, 5, 6].map(num => (
                    <button
                      key={num}
                      className={`num-btn ${formData.mealsPerDay === num ? 'selected' : ''}`}
                      onClick={() => setFormData(prev => ({ ...prev, mealsPerDay: num }))}
                    >
                      {num}
                    </button>
                  ))}
                </div>
              </div>
              
              <div className="form-group">
                <label>Snacks Per Day</label>
                <div className="number-selector">
                  {[0, 1, 2, 3].map(num => (
                    <button
                      key={num}
                      className={`num-btn ${formData.snacksPerDay === num ? 'selected' : ''}`}
                      onClick={() => setFormData(prev => ({ ...prev, snacksPerDay: num }))}
                    >
                      {num}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="profile-setup">
      <div className="setup-container">
        <div className="progress-bar">
          <div className="progress-fill" style={{ width: `${(step / 5) * 100}%` }}></div>
        </div>
        <div className="progress-steps">
          {[1, 2, 3, 4, 5].map(s => (
            <div key={s} className={`progress-step ${s <= step ? 'active' : ''} ${s < step ? 'completed' : ''}`}>
              {s < step ? '✓' : s}
            </div>
          ))}
        </div>

        {renderStep()}

        {error && <div className="error-message">{error}</div>}

        <div className="setup-actions">
          {step > 1 && (
            <button className="btn-back" onClick={() => setStep(step - 1)} disabled={loading}>
              ← Back
            </button>
          )}
          
          {step < 5 ? (
            <button 
              className="btn-next" 
              onClick={() => setStep(step + 1)}
              disabled={!canProceed()}
            >
              Continue →
            </button>
          ) : (
            <button 
              className="btn-finish" 
              onClick={handleSubmit}
              disabled={loading}
            >
              {loading ? 'Creating Profile...' : 'Complete Setup ✓'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ProfileSetup;
