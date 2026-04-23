import { useState, useEffect } from 'react';
import HealthDashboard from './components/HealthDashboard';
import HistoryDashboard from './components/HistoryDashboard';
import CalorieTracker from './components/CalorieTracker';
import ExerciseLogger from './components/ExerciseLogger';
import UserProfile from './components/UserProfile';
import ProfileSetup from './components/ProfileSetup';
import LoginScreen from './components/LoginScreen';
import MealPlanViewer from './components/MealPlanViewer';
import WorkoutPlans from './components/WorkoutPlans';
import ErrorBoundary from './components/ErrorBoundary';
import { getAllProfiles, getProfileById, deleteProfile, loginUser } from './services/api';
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
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [showProfileSetup, setShowProfileSetup] = useState(false);
  const [showProfileSwitcher, setShowProfileSwitcher] = useState(false);
  const [loading, setLoading] = useState(true);

  const [activeSection, setActiveSection] = useState('recommendations');
  const [activeCalcTab, setActiveCalcTab] = useState('calculator');
  const [activeRecommendTab, setActiveRecommendTab] = useState('workouts');

  // Check for saved login on mount
  useEffect(() => {
    checkSavedLogin();
  }, []);

  const checkSavedLogin = async () => {
    setLoading(true);
    try {
      const savedProfileId = localStorage.getItem('ufit_current_profile_id');
      const savedUsername = localStorage.getItem('ufit_username');

      if (savedProfileId && savedUsername) {
        // We have a saved session — try to load the profile
        const res = await getProfileById(savedProfileId);
        if (res.data) {
          setCurrentProfile(res.data);
          setIsLoggedIn(true);
        } else {
          // Profile no longer exists, clear storage
          handleLogout();
        }
      }
      // If nothing saved, user needs to login
    } catch (error) {
      console.error('Error restoring session:', error);
      // Clear bad session data
      localStorage.removeItem('ufit_current_profile_id');
      localStorage.removeItem('ufit_username');
    } finally {
      setLoading(false);
    }
  };

  const handleLoginSuccess = (profile) => {
    setCurrentProfile(profile);
    setIsLoggedIn(true);
    localStorage.setItem('ufit_current_profile_id', profile.id);
    localStorage.setItem('ufit_username', profile.username);

    // If profile has no name or essential setup, show setup
    if (!profile.name || !profile.heightCm || !profile.weightKg) {
      setShowProfileSetup(true);
    }
  };

  const handleLogout = () => {
    setCurrentProfile(null);
    setIsLoggedIn(false);
    setShowProfileSwitcher(false);
    localStorage.removeItem('ufit_current_profile_id');
    localStorage.removeItem('ufit_username');
  };

  const handleProfileCreated = (profile) => {
    setCurrentProfile(profile);
    localStorage.setItem('ufit_current_profile_id', profile.id);
    setShowProfileSetup(false);
  };

  const handleProfileUpdated = (updatedProfile) => {
    setCurrentProfile(updatedProfile);
  };

  const handleDeleteProfile = async (profileId) => {
    if (!window.confirm('Are you sure you want to delete this profile? This cannot be undone.')) return;
    try {
      await deleteProfile(profileId);
      if (currentProfile?.id === profileId) {
        handleLogout();
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
      case 'meals': return <CalorieTracker profileId={currentProfile?.id} />;
      case 'exercises': return <ExerciseLogger profileId={currentProfile?.id} />;
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

  // Not logged in — show login screen
  if (!isLoggedIn) {
    return <LoginScreen onLoginSuccess={handleLoginSuccess} />;
  }

  // Logged in but profile needs setup
  if (showProfileSetup) {
    return <ProfileSetup
      existingProfile={currentProfile}
      onProfileCreated={handleProfileCreated}
    />;
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
              <div className="dropdown-header">Account</div>
              <div className="dropdown-item active">
                <button className="dropdown-item-main">
                  <span className="item-avatar">{currentProfile?.name?.charAt(0).toUpperCase()}</span>
                  <div className="item-info">
                    <span className="item-name">{currentProfile?.name}</span>
                    <span className="item-goal">@{currentProfile?.username}</span>
                  </div>
                  <span className="check-mark">✓</span>
                </button>
              </div>
              <div className="dropdown-divider"></div>
              <button className="dropdown-item add-new" onClick={() => { setShowProfileSetup(true); setShowProfileSwitcher(false); }}>
                <span className="item-avatar">⚙️</span>
                <span className="item-name">Edit Profile</span>
              </button>
              <button className="dropdown-item add-new logout-btn" onClick={handleLogout}>
                <span className="item-avatar">🚪</span>
                <span className="item-name">Log Out</span>
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
