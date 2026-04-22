import { useState, useEffect } from 'react';
import HealthDashboard from './components/HealthDashboard';
import HistoryDashboard from './components/HistoryDashboard';
import CalorieTracker from './components/CalorieTracker';
import ExerciseLogger from './components/ExerciseLogger';
import UserProfile from './components/UserProfile';
import ProfileSetup from './components/ProfileSetup';
import MealPlanViewer from './components/MealPlanViewer';
import WorkoutPlans from './components/WorkoutPlans';
import ErrorBoundary from './components/ErrorBoundary';
import { getAllProfiles, getProfileById, deleteProfile } from './services/api';
import './App.css';

const SECTIONS = [
  { id: 'recommendations', label: 'My Plan', icon: '🎯' },
  { id: 'meals', label: 'Nutrition', icon: '🍽️' },
  { id: 'exercises', label: 'Log Workout', icon: '🏋️' },
  { id: 'calculators', label: 'Tools', icon: '🧮' },
];

const CALCULATOR_TABS = [
  { id: 'calculator', label: 'Calculator', icon: '🧮' },
  { id: 'history', label: 'History', icon: '📈' },
];

const RECOMMENDATION_TABS = [
  { id: 'workouts', label: 'Workouts', icon: '💪' },
  { id: 'mealplan', label: 'Meal Plans', icon: '📅' },
  { id: 'profile', label: 'My Profile', icon: '👤' },
];

function App() {
  const [currentProfile, setCurrentProfile] = useState(null);
  const [allProfiles, setAllProfiles] = useState([]);
  const [showProfileSetup, setShowProfileSetup] = useState(false);
  const [showProfileSwitcher, setShowProfileSwitcher] = useState(false);
  const [loading, setLoading] = useState(true);
  
  const [activeSection, setActiveSection] = useState('recommendations');
  const [activeCalcTab, setActiveCalcTab] = useState('calculator');
  const [activeRecommendTab, setActiveRecommendTab] = useState('workouts');

  // Load saved profile on mount
  useEffect(() => {
    loadInitialProfile();
  }, []);

  const loadInitialProfile = async () => {
    setLoading(true);
    try {
      const savedProfileId = localStorage.getItem('ufit_current_profile_id');
      const response = await getAllProfiles();
      const profiles = response.data;
      setAllProfiles(profiles);

      if (savedProfileId) {
        const savedProfile = profiles.find(p => p.id === savedProfileId);
        if (savedProfile) {
          setCurrentProfile(savedProfile);
        } else if (profiles.length > 0) {
          setCurrentProfile(profiles[0]);
          localStorage.setItem('ufit_current_profile_id', profiles[0].id);
        } else {
          setShowProfileSetup(true);
        }
      } else if (profiles.length > 0) {
        // DEV DEFAULT: prefer "Mayank" profile if available
        const defaultProfile = profiles.find(p => p.name?.toLowerCase().includes('mayank')) || profiles[0];
        setCurrentProfile(defaultProfile);
        localStorage.setItem('ufit_current_profile_id', defaultProfile.id);
      } else {
        setShowProfileSetup(true);
      }
    } catch (error) {
      console.error('Error loading profiles:', error);
      setShowProfileSetup(true);
    } finally {
      setLoading(false);
    }
  };

  const handleProfileCreated = (profile) => {
    setCurrentProfile(profile);
    setAllProfiles(prev => [...prev, profile]);
    localStorage.setItem('ufit_current_profile_id', profile.id);
    setShowProfileSetup(false);
  };

  const handleSwitchProfile = (profile) => {
    setCurrentProfile(profile);
    localStorage.setItem('ufit_current_profile_id', profile.id);
    setShowProfileSwitcher(false);
  };

  const handleProfileUpdated = (updatedProfile) => {
    setCurrentProfile(updatedProfile);
    setAllProfiles(prev => prev.map(p => p.id === updatedProfile.id ? updatedProfile : p));
  };

  const handleDeleteProfile = async (profileId) => {
    if (!window.confirm('Are you sure you want to delete this profile? This cannot be undone.')) return;
    try {
      await deleteProfile(profileId);
      const remaining = allProfiles.filter(p => p.id !== profileId);
      setAllProfiles(remaining);
      if (currentProfile?.id === profileId) {
        if (remaining.length > 0) {
          setCurrentProfile(remaining[0]);
          localStorage.setItem('ufit_current_profile_id', remaining[0].id);
        } else {
          setCurrentProfile(null);
          localStorage.removeItem('ufit_current_profile_id');
          setShowProfileSetup(true);
        }
      }
      setShowProfileSwitcher(false);
    } catch (err) {
      alert('Failed to delete profile.');
    }
  };

  const renderCalculator = () => {
    switch (activeCalcTab) {
      case 'calculator': return <HealthDashboard />;
      case 'history': return <HistoryDashboard />;
      default: return <HealthDashboard />;
    }
  };

  const renderRecommendations = () => {
    switch (activeRecommendTab) {
      case 'profile': return <UserProfile profile={currentProfile} onProfileUpdated={handleProfileUpdated} />;
      case 'mealplan': return <MealPlanViewer profileId={currentProfile?.id} />;
      case 'workouts': return <WorkoutPlans profileId={currentProfile?.id} />;
      default: return <WorkoutPlans profileId={currentProfile?.id} />;
    }
  };

  const renderSection = () => {
    switch (activeSection) {
      case 'calculators': return renderCalculator();
      case 'meals': return <CalorieTracker />;
      case 'exercises': return <ExerciseLogger />;
      case 'recommendations': return renderRecommendations();
      default: return renderRecommendations();
    }
  };

  if (loading) {
    return (
      <div className="app loading-screen">
        <div className="loading-content">
          <h1 className="logo">UFit</h1>
          <div className="loading-spinner"></div>
          <p>Loading your profile...</p>
        </div>
      </div>
    );
  }

  if (showProfileSetup) {
    return <ProfileSetup onProfileCreated={handleProfileCreated} />;
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="header-left">
          <h1 className="logo">UFit</h1>
        </div>
        <div className="header-right">
          <button className="profile-badge" onClick={() => setShowProfileSwitcher(!showProfileSwitcher)}>
            <span className="profile-avatar">{currentProfile?.name?.charAt(0).toUpperCase()}</span>
            <span className="profile-name">{currentProfile?.name}</span>
            <span className="dropdown-arrow">▼</span>
          </button>
          
          {showProfileSwitcher && (
            <div className="profile-dropdown">
              <div className="dropdown-header">Switch Profile</div>
              {allProfiles.map(profile => (
                <div key={profile.id} className={`dropdown-item ${profile.id === currentProfile?.id ? 'active' : ''}`}>
                  <button className="dropdown-item-main" onClick={() => handleSwitchProfile(profile)}>
                    <span className="item-avatar">{profile.name?.charAt(0).toUpperCase()}</span>
                    <div className="item-info">
                      <span className="item-name">{profile.name}</span>
                      <span className="item-goal">{profile.fitnessGoal?.replace('_', ' ')}</span>
                    </div>
                    {profile.id === currentProfile?.id && <span className="check-mark">✓</span>}
                  </button>
                  <button
                    className="delete-profile-btn"
                    title="Delete profile"
                    onClick={(e) => { e.stopPropagation(); handleDeleteProfile(profile.id); }}
                  >🗑️</button>
                </div>
              ))}
              <div className="dropdown-divider"></div>
              <button className="dropdown-item add-new" onClick={() => setShowProfileSetup(true)}>
                <span className="item-avatar">+</span>
                <span className="item-name">Create New Profile</span>
              </button>
            </div>
          )}
        </div>
      </header>

      {/* Top-level section navigation */}
      <nav className="section-nav">
        {SECTIONS.map(section => (
          <button
            key={section.id}
            className={`section-btn ${activeSection === section.id ? 'active' : ''}`}
            onClick={() => setActiveSection(section.id)}
          >
            <span className="section-icon">{section.icon}</span>
            <span className="section-label">{section.label}</span>
          </button>
        ))}
      </nav>

      {/* Sub-tab navigation (for calculators and recommendations) */}
      {activeSection === 'calculators' && (
        <nav className="tab-nav">
          {CALCULATOR_TABS.map(tab => (
            <button
              key={tab.id}
              className={`tab-btn ${activeCalcTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveCalcTab(tab.id)}
            >
              <span className="tab-icon">{tab.icon}</span>
              <span className="tab-label">{tab.label}</span>
            </button>
          ))}
        </nav>
      )}

      {activeSection === 'recommendations' && (
        <nav className="tab-nav">
          {RECOMMENDATION_TABS.map(tab => (
            <button
              key={tab.id}
              className={`tab-btn ${activeRecommendTab === tab.id ? 'active' : ''}`}
              onClick={() => setActiveRecommendTab(tab.id)}
            >
              <span className="tab-icon">{tab.icon}</span>
              <span className="tab-label">{tab.label}</span>
            </button>
          ))}
        </nav>
      )}

      <main className="app-main">
        <ErrorBoundary>
          {renderSection()}
        </ErrorBoundary>
      </main>
    </div>
  );
}

export default App;
