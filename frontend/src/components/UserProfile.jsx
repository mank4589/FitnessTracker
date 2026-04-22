import React, { useState, useEffect } from 'react';
import { 
  updateProfile,
  linkHealthSnapshot 
} from '../services/api';
import './UserProfile.css';

const UserProfile = ({ profile, onProfileUpdated }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  
  const [formData, setFormData] = useState({
    name: '',
    age: '',
    gender: 'MALE',
    heightCm: '',
    weightKg: '',
    fitnessGoal: 'MAINTAIN',
    activityLevel: 'MODERATE',
    experienceLevel: 'BEGINNER',
    workoutDaysPerWeek: 3,
    workoutDurationMinutes: 60,
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

  const fitnessGoals = ['LOSE_FAT', 'GAIN_MUSCLE', 'MAINTAIN', 'RECOMP'];
  const activityLevels = ['SEDENTARY', 'LIGHT', 'MODERATE', 'VERY_ACTIVE', 'EXTRA_ACTIVE'];
  const experienceLevels = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
  const dietaryPreferences = ['NONE', 'VEGETARIAN', 'EGGETARIAN', 'VEGAN', 'PESCATARIAN', 'KETO', 'PALEO', 'MEDITERRANEAN', 'LOW_CARB', 'HIGH_PROTEIN'];
  const dietaryRestrictions = ['GLUTEN_FREE', 'DAIRY_FREE', 'NUT_FREE', 'SOY_FREE', 'EGG_FREE', 'SHELLFISH_FREE', 'FISH_FREE', 'BEEF_FREE', 'PORK_FREE', 'LOW_SODIUM', 'LOW_SUGAR', 'HALAL', 'KOSHER'];
  const injuries = ['LOWER_BACK', 'UPPER_BACK', 'NECK', 'SHOULDER', 'ELBOW', 'WRIST', 'HIP', 'KNEE', 'ANKLE', 'ROTATOR_CUFF'];
  const muscleGroups = ['CHEST', 'BACK', 'SHOULDERS', 'BICEPS', 'TRICEPS', 'FOREARMS', 'QUADRICEPS', 'HAMSTRINGS', 'GLUTES', 'CALVES', 'CORE', 'FULL_BODY'];
  const cuisineOptions = ['ALL', 'INDIAN', 'CHINESE', 'JAPANESE', 'THAI', 'ITALIAN', 'MEXICAN', 'AMERICAN', 'MEDITERRANEAN', 'KOREAN'];
  const medicalConditionOptions = [
    { value: 'DIABETES', label: '🩸 Diabetes' },
    { value: 'HYPERTENSION', label: '💓 High BP' },
    { value: 'HIGH_CHOLESTEROL', label: '🫀 Cholesterol' },
    { value: 'LOW_BP', label: '📉 Low BP' },
    { value: 'KIDNEY_ISSUES', label: '🫘 Kidney' },
    { value: 'PCOD', label: '♀️ PCOD' },
    { value: 'THYROID', label: '🦋 Thyroid' },
    { value: 'HEART_DISEASE', label: '❤️‍🩹 Heart' },
    { value: 'OBESITY', label: '⚖️ Obesity' },
  ];

  useEffect(() => {
    if (profile) {
      setFormData({
        name: profile.name || '',
        age: profile.age || '',
        gender: profile.gender || 'MALE',
        heightCm: profile.heightCm || '',
        weightKg: profile.weightKg || '',
        fitnessGoal: profile.fitnessGoal || 'MAINTAIN',
        activityLevel: profile.activityLevel || 'MODERATE',
        experienceLevel: profile.experienceLevel || 'BEGINNER',
        workoutDaysPerWeek: profile.workoutDaysPerWeek || 3,
        workoutDurationMinutes: profile.workoutDurationMinutes || 60,
        dietaryPreference: profile.dietaryPreference || 'NONE',
        dietaryRestrictions: profile.dietaryRestrictions || [],
        injuries: profile.injuries || [],
        targetMuscleGroups: profile.targetMuscleGroups || [],
        includeCardio: profile.includeCardio !== undefined ? profile.includeCardio : true,
        mealsPerDay: profile.mealsPerDay || 3,
        snacksPerDay: profile.snacksPerDay || 1,
        cuisinePreferences: profile.cuisinePreferences || [],
        medicalConditions: profile.medicalConditions || [],
      });
    }
  }, [profile]);

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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const response = await updateProfile(profile.id, formData);
      onProfileUpdated(response.data);
      setIsEditing(false);
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Error updating profile. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleLinkHealth = async () => {
    try {
      await linkHealthSnapshot(profile.id);
      alert('Health snapshot linked successfully!');
    } catch (error) {
      console.error('Error linking health snapshot:', error);
      alert('Error linking health snapshot.');
    }
  };

  const formatLabel = (str) => str?.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase()) || '-';

  if (!profile) {
    return (
      <div className="user-profile-container">
        <div className="empty-profile">
          <div className="empty-icon">👤</div>
          <h3>No Profile Selected</h3>
          <p>Create a profile to get started with personalized recommendations.</p>
        </div>
      </div>
    );
  }

  if (isEditing) {
    return (
      <div className="user-profile-container">
        <h2>Edit Profile</h2>
        <p className="profile-subtitle">Update your fitness profile and preferences</p>

        <div className="profile-form">
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Name *</label>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Age</label>
                <input
                  type="number"
                  name="age"
                  value={formData.age}
                  onChange={handleInputChange}
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
                  min="30"
                  max="300"
                  step="0.1"
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Fitness Goal</label>
                <select name="fitnessGoal" value={formData.fitnessGoal} onChange={handleInputChange}>
                  {fitnessGoals.map(goal => (
                    <option key={goal} value={goal}>{formatLabel(goal)}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Activity Level</label>
                <select name="activityLevel" value={formData.activityLevel} onChange={handleInputChange}>
                  {activityLevels.map(level => (
                    <option key={level} value={level}>{formatLabel(level)}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Experience Level</label>
                <select name="experienceLevel" value={formData.experienceLevel} onChange={handleInputChange}>
                  {experienceLevels.map(level => (
                    <option key={level} value={level}>{formatLabel(level)}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label>Dietary Preference</label>
                <select name="dietaryPreference" value={formData.dietaryPreference} onChange={handleInputChange}>
                  {dietaryPreferences.map(pref => (
                    <option key={pref} value={pref}>{formatLabel(pref)}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Workout Days/Week</label>
                <input
                  type="number"
                  name="workoutDaysPerWeek"
                  value={formData.workoutDaysPerWeek}
                  onChange={handleInputChange}
                  min="1"
                  max="7"
                />
              </div>

              <div className="form-group">
                <label>Workout Duration (min)</label>
                <input
                  type="number"
                  name="workoutDurationMinutes"
                  value={formData.workoutDurationMinutes}
                  onChange={handleInputChange}
                  min="15"
                  max="180"
                />
              </div>
            </div>

            <div className="form-group">
              <label>Dietary Restrictions</label>
              <div className="checkbox-grid">
                {dietaryRestrictions.map(restriction => (
                  <label key={restriction} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={formData.dietaryRestrictions.includes(restriction)}
                      onChange={() => handleMultiSelect('dietaryRestrictions', restriction)}
                    />
                    {formatLabel(restriction)}
                  </label>
                ))}
              </div>
            </div>

            <div className="form-group">
              <label>Injuries / Limitations</label>
              <div className="checkbox-grid">
                {injuries.map(injury => (
                  <label key={injury} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={formData.injuries.includes(injury)}
                      onChange={() => handleMultiSelect('injuries', injury)}
                    />
                    {formatLabel(injury)}
                  </label>
                ))}
              </div>
            </div>

            <div className="form-group">
              <label>Target Muscle Groups</label>
              <div className="checkbox-grid">
                {muscleGroups.map(muscle => (
                  <label key={muscle} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={formData.targetMuscleGroups.includes(muscle)}
                      onChange={() => handleMultiSelect('targetMuscleGroups', muscle)}
                    />
                    {formatLabel(muscle)}
                  </label>
                ))}
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Meals per Day</label>
                <input
                  type="number"
                  name="mealsPerDay"
                  value={formData.mealsPerDay}
                  onChange={handleInputChange}
                  min="1"
                  max="6"
                />
              </div>

              <div className="form-group">
                <label>Snacks per Day</label>
                <input
                  type="number"
                  name="snacksPerDay"
                  value={formData.snacksPerDay}
                  onChange={handleInputChange}
                  min="0"
                  max="5"
                />
              </div>
            </div>

            <div className="form-group">
              <label className="checkbox-label" style={{ display: 'inline-flex', width: 'auto' }}>
                <input
                  type="checkbox"
                  name="includeCardio"
                  checked={formData.includeCardio}
                  onChange={handleInputChange}
                />
                Include Cardio in Recommendations
              </label>
            </div>

            <div className="form-group">
              <label>Cuisine Preferences</label>
              <div className="checkbox-grid">
                {cuisineOptions.map(cuisine => (
                  <label key={cuisine} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={formData.cuisinePreferences.includes(cuisine)}
                      onChange={() => handleMultiSelect('cuisinePreferences', cuisine)}
                    />
                    {formatLabel(cuisine)}
                  </label>
                ))}
              </div>
            </div>

            <div className="form-group">
              <label>Medical Conditions</label>
              <div className="checkbox-grid">
                {medicalConditionOptions.map(condition => (
                  <label key={condition.value} className="checkbox-label" style={formData.medicalConditions.includes(condition.value) ? {background: 'rgba(251,146,60,0.1)', borderColor: 'rgba(251,146,60,0.4)'} : {}}>
                    <input
                      type="checkbox"
                      checked={formData.medicalConditions.includes(condition.value)}
                      onChange={() => handleMultiSelect('medicalConditions', condition.value)}
                    />
                    {condition.label}
                  </label>
                ))}
              </div>
            </div>

            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={loading}>
                {loading ? 'Saving...' : 'Save Changes'}
              </button>
              <button type="button" onClick={() => setIsEditing(false)} className="btn-secondary">
                Cancel
              </button>
            </div>
          </form>
        </div>
      </div>
    );
  }

  return (
    <div className="user-profile-container">
      <h2>My Profile</h2>
      <p className="profile-subtitle">Your personal fitness profile and preferences</p>

      <div className="profile-overview">
        <div className="overview-header">
          <div className="overview-avatar">
            {profile.name?.charAt(0).toUpperCase()}
          </div>
          <div className="overview-info">
            <h3>{profile.name}</h3>
            <span className="goal-badge">{formatLabel(profile.fitnessGoal)}</span>
          </div>
        </div>

        <div className="overview-stats">
          {profile.age && (
            <div className="stat-card">
              <span className="stat-value">{profile.age}</span>
              <span className="stat-label">Age</span>
            </div>
          )}
          {profile.heightCm && (
            <div className="stat-card">
              <span className="stat-value">{profile.heightCm} cm</span>
              <span className="stat-label">Height</span>
            </div>
          )}
          {profile.weightKg && (
            <div className="stat-card">
              <span className="stat-value">{profile.weightKg} kg</span>
              <span className="stat-label">Weight</span>
            </div>
          )}
          <div className="stat-card">
            <span className="stat-value">{profile.workoutDaysPerWeek}</span>
            <span className="stat-label">Days/Week</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">{profile.workoutDurationMinutes} min</span>
            <span className="stat-label">Per Session</span>
          </div>
        </div>
      </div>

      <div className="profile-section">
        <h4><span className="section-icon">🏋️</span> Workout Preferences</h4>
        <div className="info-grid">
          <div className="info-item">
            <span className="info-label">Experience Level</span>
            <span className="info-value">{formatLabel(profile.experienceLevel)}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Activity Level</span>
            <span className="info-value">{formatLabel(profile.activityLevel)}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Include Cardio</span>
            <span className="info-value">{profile.includeCardio ? 'Yes' : 'No'}</span>
          </div>
        </div>

        {profile.injuries && profile.injuries.length > 0 && (
          <div style={{ marginTop: '20px' }}>
            <span className="info-label" style={{ marginBottom: '10px', display: 'block' }}>Injuries / Limitations</span>
            <div className="tags-container">
              {profile.injuries.map(injury => (
                <span key={injury} className="tag injury">{formatLabel(injury)}</span>
              ))}
            </div>
          </div>
        )}

        {profile.targetMuscleGroups && profile.targetMuscleGroups.length > 0 && (
          <div style={{ marginTop: '20px' }}>
            <span className="info-label" style={{ marginBottom: '10px', display: 'block' }}>Target Muscle Groups</span>
            <div className="tags-container">
              {profile.targetMuscleGroups.map(muscle => (
                <span key={muscle} className="tag muscle">{formatLabel(muscle)}</span>
              ))}
            </div>
          </div>
        )}
      </div>

      <div className="profile-section">
        <h4><span className="section-icon">🥗</span> Dietary Preferences</h4>
        <div className="info-grid">
          <div className="info-item">
            <span className="info-label">Diet Type</span>
            <span className="info-value">{formatLabel(profile.dietaryPreference)}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Meals Per Day</span>
            <span className="info-value">{profile.mealsPerDay}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Snacks Per Day</span>
            <span className="info-value">{profile.snacksPerDay}</span>
          </div>
        </div>

        {profile.dietaryRestrictions && profile.dietaryRestrictions.length > 0 && (
          <div style={{ marginTop: '20px' }}>
            <span className="info-label" style={{ marginBottom: '10px', display: 'block' }}>Dietary Restrictions</span>
            <div className="tags-container">
              {profile.dietaryRestrictions.map(restriction => (
                <span key={restriction} className="tag restriction">{formatLabel(restriction)}</span>
              ))}
            </div>
          </div>
        )}

        {profile.cuisinePreferences && profile.cuisinePreferences.length > 0 && (
          <div style={{ marginTop: '20px' }}>
            <span className="info-label" style={{ marginBottom: '10px', display: 'block' }}>Cuisine Preferences</span>
            <div className="tags-container">
              {profile.cuisinePreferences.map(cuisine => (
                <span key={cuisine} className="tag muscle">{formatLabel(cuisine)}</span>
              ))}
            </div>
          </div>
        )}

        {profile.medicalConditions && profile.medicalConditions.length > 0 && (
          <div style={{ marginTop: '20px' }}>
            <span className="info-label" style={{ marginBottom: '10px', display: 'block' }}>⚕️ Medical Conditions</span>
            <div className="tags-container">
              {profile.medicalConditions.map(condition => (
                <span key={condition} className="tag" style={{background: 'rgba(251,146,60,0.12)', color: '#fb923c', border: '1px solid rgba(251,146,60,0.3)'}}>
                  {formatLabel(condition)}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      <button className="btn-edit-profile" onClick={() => setIsEditing(true)}>
        ✏️ Edit Profile
      </button>
    </div>
  );
};

export default UserProfile;
