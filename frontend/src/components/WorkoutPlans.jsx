import React, { useState, useEffect } from 'react';
import {
  getAllProfiles,
  generateWorkoutPlan,
  getAllWorkoutPlans,
  getActiveWorkoutPlan,
  activateWorkoutPlan,
  deleteWorkoutPlan,
} from '../services/api';
import CustomWorkoutBuilder from './CustomWorkoutBuilder';
import './WorkoutPlans.css';

const WORKOUT_SPLITS = [
  {
    type: 'FULL_BODY',
    name: 'Full Body',
    icon: '🏋️',
    level: 'Beginner',
    levelColor: '#22c55e',
    gradient: 'linear-gradient(135deg, #059669 0%, #10b981 50%, #34d399 100%)',
    borderGlow: 'rgba(16, 185, 129, 0.4)',
    description: 'Train every major muscle group each session. Perfect for beginners who want maximum results with fewer gym days. Builds a strong foundation with compound movements.',
    bestFor: 'Building a solid base',
    frequency: '6 days/week',
    schedule: 'Lower • Push • Pull • Rest • Lower • Upper • Rest',
    routine: [
      {
        day: 'Monday', name: 'Lower Body A (Posterior Chain)',
        desc: 'Focusing on the glutes and hamstrings to build a strong foundation.',
        exercises: [
          { name: 'Romanian Deadlifts (RDLs)', sets: 3, reps: '8-10' },
          { name: 'Lying Hamstring Curls', sets: 3, reps: '10-12' },
          { name: 'Bulgarian Split Squats', sets: 3, reps: '8-10 per leg' },
          { name: 'Leg Press (High & Wide Stance)', sets: 3, reps: '10-12' },
          { name: 'Standing Calf Raises', sets: 4, reps: '12-15' },
        ]
      },
      {
        day: 'Tuesday', name: 'Push (Shoulders & Triceps)',
        desc: 'Prioritizing the deltoids when you are fresh to ensure they don\'t lag behind.',
        exercises: [
          { name: 'Seated Overhead Dumbbell Press', sets: 3, reps: '8-10' },
          { name: 'Cable Lateral Raises', sets: 4, reps: '12-15', note: 'focus on the stretch and controlled eccentric' },
          { name: 'Machine Chest Press', sets: 3, reps: '8-10' },
          { name: 'Overhead Cable Triceps Extensions', sets: 3, reps: '10-12' },
          { name: 'Triceps Pushdowns', sets: 3, reps: '12-15' },
        ]
      },
      {
        day: 'Wednesday', name: 'Pull (Back & Biceps)',
        desc: 'Building back width and thickness.',
        exercises: [
          { name: 'Weighted Pull-ups or Lat Pulldowns', sets: 3, reps: '8-10' },
          { name: 'Barbell Rows or Heavy T-Bar Rows', sets: 3, reps: '8-10' },
          { name: 'Single-Arm Dumbbell Rows', sets: 3, reps: '10-12 per arm' },
          { name: 'Face Pulls', sets: 3, reps: '15', note: 'great for rear delts and rotator cuff health' },
          { name: 'Incline Dumbbell Bicep Curls', sets: 3, reps: '10-12' },
        ]
      },
      { day: 'Thursday', name: 'Rest', rest: true },
      {
        day: 'Friday', name: 'Lower Body B (Quads)',
        desc: 'Shifting the mechanical tension to the anterior chain.',
        exercises: [
          { name: 'Hack Squats', sets: 3, reps: '8-10', note: 'control the descent, drive hard out of the hole' },
          { name: 'Leg Extensions', sets: 3, reps: '12-15', note: 'hold the squeeze at the top for 1 second' },
          { name: 'Walking Lunges', sets: 3, reps: '10-12 steps per leg' },
          { name: 'Seated Calf Raises', sets: 4, reps: '15' },
        ]
      },
      {
        day: 'Saturday', name: 'Upper Body (Chest & Arms)',
        desc: 'Rounding out the week with upper body volume, prioritizing chest.',
        exercises: [
          { name: 'Incline Dumbbell Flies', sets: 3, reps: '10-12', note: 'focus on the deep stretch at the bottom' },
          { name: 'Flat Barbell or Dumbbell Bench Press', sets: 3, reps: '8-10' },
          { name: 'Pec Deck / Cable Crossovers', sets: 3, reps: '12-15' },
          { name: 'Hammer Curls', sets: 3, reps: '10-12' },
          { name: 'Skull Crushers', sets: 3, reps: '10-12' },
        ]
      },
      { day: 'Sunday', name: 'Rest', rest: true },
    ]
  },
  {
    type: 'PUSH_PULL_LEGS',
    name: 'Push, Pull, Legs',
    icon: '💪',
    level: 'Intermediate',
    levelColor: '#f59e0b',
    gradient: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%)',
    borderGlow: 'rgba(99, 102, 241, 0.4)',
    description: 'Divide workouts by movement pattern — push (chest, shoulders, triceps), pull (back, biceps), and legs. Allows higher volume per muscle with great recovery.',
    bestFor: 'Well-rounded development',
    frequency: '6 days/week',
    schedule: 'Pull • Push • Legs • Pull • Push • Legs • Rest',
    routine: [
      {
        day: 'Monday', name: 'Pull (Back Thickness & Biceps)',
        exercises: [
          { name: 'Barbell Rows', sets: 3, reps: '8-10' },
          { name: 'T-Bar Rows (Chest Supported)', sets: 3, reps: '8-12' },
          { name: 'Lat Pulldowns (Neutral Grip)', sets: 3, reps: '10-12' },
          { name: 'Face Pulls', sets: 3, reps: '12-15' },
          { name: 'Barbell Bicep Curls', sets: 3, reps: '8-10' },
          { name: 'Hammer Curls', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Tuesday', name: 'Push (Shoulder Focus & Triceps)',
        exercises: [
          { name: 'Seated Dumbbell Overhead Press', sets: 3, reps: '8-10' },
          { name: 'Cable Lateral Raises', sets: 4, reps: '12-15', note: 'focus on the eccentric' },
          { name: 'Incline Dumbbell Bench Press', sets: 3, reps: '8-10' },
          { name: 'Overhead Cable Triceps Extensions', sets: 3, reps: '10-12' },
          { name: 'Triceps Rope Pushdowns', sets: 3, reps: '12-15' },
        ]
      },
      {
        day: 'Wednesday', name: 'Legs (Posterior Chain Focus)',
        exercises: [
          { name: 'Romanian Deadlifts (RDLs)', sets: 3, reps: '8-10' },
          { name: 'Lying Hamstring Curls', sets: 4, reps: '10-12' },
          { name: 'Leg Press (High/Wide Stance)', sets: 3, reps: '10-12' },
          { name: 'Standing Calf Raises', sets: 4, reps: '12-15' },
        ]
      },
      {
        day: 'Thursday', name: 'Pull (Back Width & Biceps)',
        exercises: [
          { name: 'Weighted Pull-ups (or Wide Grip Lat Pulldowns)', sets: 3, reps: '8-10' },
          { name: 'Single-Arm Dumbbell Rows', sets: 3, reps: '8-12 per arm' },
          { name: 'Cable Pullovers (Straight Arm)', sets: 3, reps: '12-15' },
          { name: 'Reverse Pec Deck (Rear Delts)', sets: 3, reps: '12-15' },
          { name: 'Incline Dumbbell Bicep Curls', sets: 3, reps: '10-12' },
          { name: 'Preacher Curls', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Friday', name: 'Push (Chest Focus & Triceps)',
        exercises: [
          { name: 'Flat Barbell Bench Press', sets: 3, reps: '8-10' },
          { name: 'Incline Dumbbell Flies', sets: 3, reps: '10-12', note: 'focus on the deep stretch at the bottom' },
          { name: 'Machine Shoulder Press', sets: 3, reps: '10-12' },
          { name: 'Dumbbell Lateral Raises', sets: 3, reps: '12-15' },
          { name: 'Skull Crushers', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Saturday', name: 'Legs (Quad Focus)',
        exercises: [
          { name: 'Hack Squats', sets: 3, reps: '8-10', note: 'control the descent, drive hard out of the hole' },
          { name: 'Bulgarian Split Squats', sets: 3, reps: '8-10 per leg' },
          { name: 'Leg Extensions', sets: 4, reps: '12-15', note: 'hold the squeeze at the top for 1 second' },
          { name: 'Seated Calf Raises', sets: 4, reps: '15' },
        ]
      },
      { day: 'Sunday', name: 'Rest & Recovery', rest: true },
    ]
  },
  {
    type: 'UPPER_LOWER',
    name: 'Upper / Lower',
    icon: '⚡',
    level: 'Intermediate',
    levelColor: '#f59e0b',
    gradient: 'linear-gradient(135deg, #0ea5e9 0%, #38bdf8 50%, #7dd3fc 100%)',
    borderGlow: 'rgba(14, 165, 233, 0.4)',
    description: 'Alternate between upper and lower body sessions. Each muscle gets trained multiple times per week with optimal recovery. Excellent for building raw strength.',
    bestFor: 'Strength gains',
    frequency: '6 days/week',
    schedule: 'Lower • Upper • Lower • Upper • Lower • Upper • Rest',
    routine: [
      {
        day: 'Monday', name: 'Lower A (Posterior Chain Focus)',
        exercises: [
          { name: 'Romanian Deadlifts (RDLs)', sets: 4, reps: '8-10' },
          { name: 'Lying Hamstring Curls', sets: 3, reps: '10-12' },
          { name: 'Leg Press (High & Wide Stance)', sets: 3, reps: '10-12' },
          { name: 'Walking Lunges', sets: 3, reps: '10-12 steps per leg' },
          { name: 'Standing Calf Raises', sets: 4, reps: '12-15' },
        ]
      },
      {
        day: 'Tuesday', name: 'Upper A (Shoulder Focus)',
        exercises: [
          { name: 'Seated Dumbbell Overhead Press', sets: 4, reps: '8-10' },
          { name: 'Cable Lateral Raises', sets: 4, reps: '12-15', note: 'focus on a slow, controlled eccentric' },
          { name: 'Machine Chest Press', sets: 3, reps: '8-10' },
          { name: 'Lat Pulldowns (Neutral Grip)', sets: 3, reps: '10-12' },
          { name: 'Overhead Cable Triceps Extensions', sets: 3, reps: '10-12' },
          { name: 'Hammer Curls', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Wednesday', name: 'Lower B (Quad Focus)',
        exercises: [
          { name: 'Hack Squats', sets: 4, reps: '8-10', note: 'control the descent, drive hard out of the hole' },
          { name: 'Bulgarian Split Squats', sets: 3, reps: '8-10 per leg' },
          { name: 'Leg Extensions', sets: 3, reps: '12-15', note: 'hold the squeeze at the top for 1 second' },
          { name: 'Seated Calf Raises', sets: 4, reps: '15' },
        ]
      },
      {
        day: 'Thursday', name: 'Upper B (Chest Focus)',
        exercises: [
          { name: 'Incline Dumbbell Flies', sets: 4, reps: '10-12', note: 'focus on the deep stretch at the bottom' },
          { name: 'Flat Barbell Bench Press', sets: 3, reps: '8-10' },
          { name: 'Barbell Rows', sets: 3, reps: '8-10' },
          { name: 'Face Pulls', sets: 3, reps: '12-15' },
          { name: 'Skull Crushers', sets: 3, reps: '10-12' },
          { name: 'Incline Dumbbell Bicep Curls', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Friday', name: 'Lower C (Glute & Hamstring Volume)',
        exercises: [
          { name: 'Good Mornings or 45-Degree Back Extensions', sets: 3, reps: '10-12' },
          { name: 'Seated Leg Curls', sets: 3, reps: '12-15' },
          { name: 'Goblet Squats (Heels Elevated)', sets: 3, reps: '10-12' },
          { name: 'Calf Press on Leg Press Machine', sets: 4, reps: '12-15' },
        ]
      },
      {
        day: 'Saturday', name: 'Upper C (Back & Arms Focus)',
        exercises: [
          { name: 'Weighted Pull-ups (or Wide Grip Lat Pulldowns)', sets: 3, reps: '8-10' },
          { name: 'T-Bar Rows (Chest Supported)', sets: 3, reps: '8-12' },
          { name: 'Dumbbell Lateral Raises', sets: 3, reps: '12-15' },
          { name: 'Triceps Rope Pushdowns', sets: 3, reps: '12-15' },
          { name: 'Preacher Curls', sets: 3, reps: '10-12' },
        ]
      },
      { day: 'Sunday', name: 'Rest & Recovery', rest: true },
    ]
  },
  {
    type: 'BRO_SPLIT',
    name: 'Body Part Split',
    icon: '🔥',
    level: 'Advanced',
    levelColor: '#ef4444',
    gradient: 'linear-gradient(135deg, #dc2626 0%, #ef4444 50%, #f87171 100%)',
    borderGlow: 'rgba(239, 68, 68, 0.4)',
    description: 'Dedicate each day to a single muscle group for maximum volume and isolation. The classic bodybuilding approach for chasing muscle growth and hypertrophy.',
    bestFor: 'Muscle gain (Hypertrophy)',
    frequency: '6 days/week',
    schedule: 'Chest • Shoulders • Back • Legs A • Arms • Legs B • Rest',
    routine: [
      {
        day: 'Monday', name: 'Chest',
        exercises: [
          { name: 'Incline Dumbbell Flies', sets: 4, reps: '10-12', note: 'focus on the deep stretch at the bottom' },
          { name: 'Flat Barbell Bench Press', sets: 4, reps: '8-10' },
          { name: 'Machine Chest Press', sets: 3, reps: '10-12' },
          { name: 'Cable Crossovers (Low to High)', sets: 3, reps: '12-15' },
          { name: 'Pec Deck', sets: 3, reps: '12-15' },
        ]
      },
      {
        day: 'Tuesday', name: 'Shoulders',
        exercises: [
          { name: 'Seated Dumbbell Overhead Press', sets: 4, reps: '8-10' },
          { name: 'Cable Lateral Raises', sets: 4, reps: '12-15', note: 'focus on a slow, controlled eccentric' },
          { name: 'Dumbbell Front Raises', sets: 3, reps: '10-12' },
          { name: 'Reverse Pec Deck (Rear Delts)', sets: 4, reps: '12-15' },
          { name: 'Dumbbell Shrugs', sets: 4, reps: '12-15' },
        ]
      },
      {
        day: 'Wednesday', name: 'Back',
        exercises: [
          { name: 'Barbell Rows', sets: 4, reps: '8-10' },
          { name: 'Weighted Pull-ups (or Lat Pulldowns)', sets: 4, reps: '8-10' },
          { name: 'T-Bar Rows (Chest Supported)', sets: 3, reps: '8-12' },
          { name: 'Single-Arm Dumbbell Rows', sets: 3, reps: '10-12 per arm' },
          { name: 'Straight Arm Cable Pullovers', sets: 3, reps: '12-15' },
        ]
      },
      {
        day: 'Thursday', name: 'Legs A (Quad Focus)',
        exercises: [
          { name: 'Hack Squats', sets: 4, reps: '8-10', note: 'control the descent, drive hard out of the hole' },
          { name: 'Leg Press (Narrow Stance)', sets: 3, reps: '10-12' },
          { name: 'Leg Extensions', sets: 4, reps: '12-15', note: 'hold the squeeze at the top for 1 second' },
          { name: 'Walking Lunges', sets: 3, reps: '10-12 steps per leg' },
          { name: 'Standing Calf Raises', sets: 4, reps: '12-15' },
        ]
      },
      {
        day: 'Friday', name: 'Arms (Biceps & Triceps)',
        exercises: [
          { name: 'Skull Crushers', sets: 4, reps: '10-12' },
          { name: 'Barbell Bicep Curls', sets: 4, reps: '8-10' },
          { name: 'Overhead Cable Triceps Extensions', sets: 3, reps: '10-12' },
          { name: 'Incline Dumbbell Bicep Curls', sets: 3, reps: '10-12' },
          { name: 'Triceps Rope Pushdowns', sets: 3, reps: '12-15' },
          { name: 'Hammer Curls', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Saturday', name: 'Legs B (Posterior Chain Focus)',
        exercises: [
          { name: 'Romanian Deadlifts (RDLs)', sets: 4, reps: '8-10' },
          { name: 'Lying Hamstring Curls', sets: 4, reps: '10-12' },
          { name: 'Bulgarian Split Squats', sets: 3, reps: '8-10 per leg' },
          { name: 'Seated Leg Curls', sets: 3, reps: '12-15' },
          { name: 'Seated Calf Raises', sets: 4, reps: '15' },
        ]
      },
      { day: 'Sunday', name: 'Rest & Recovery', rest: true },
    ]
  },
  {
    type: 'UPPER_LOWER_PPL',
    name: 'PPL + Upper/Lower',
    icon: '⚔️',
    level: 'Advanced',
    levelColor: '#ef4444',
    gradient: 'linear-gradient(135deg, #7c3aed 0%, #a855f7 50%, #c084fc 100%)',
    borderGlow: 'rgba(168, 85, 247, 0.4)',
    description: 'The ultimate hybrid — combine push/pull/legs for targeted volume with upper/lower for balanced strength. Best of both worlds for experienced lifters seeking maximum gains.',
    bestFor: 'Strength + Hypertrophy',
    frequency: '6 days/week',
    schedule: 'Pull • Push • Lower • Upper • Lower • Arms • Rest',
    routine: [
      {
        day: 'Monday', name: 'Pull (Back Thickness & Biceps)',
        exercises: [
          { name: 'Barbell Rows', sets: 3, reps: '8-10' },
          { name: 'T-Bar Rows (Chest Supported)', sets: 3, reps: '8-12' },
          { name: 'Lat Pulldowns (Neutral Grip)', sets: 3, reps: '10-12' },
          { name: 'Face Pulls', sets: 3, reps: '12-15' },
          { name: 'Barbell Bicep Curls', sets: 3, reps: '8-10' },
          { name: 'Hammer Curls', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Tuesday', name: 'Push (Shoulder Focus & Triceps)',
        exercises: [
          { name: 'Seated Dumbbell Overhead Press', sets: 4, reps: '8-10' },
          { name: 'Cable Lateral Raises', sets: 4, reps: '12-15', note: 'focus on a slow, controlled eccentric' },
          { name: 'Machine Chest Press', sets: 3, reps: '8-10' },
          { name: 'Overhead Cable Triceps Extensions', sets: 3, reps: '10-12' },
          { name: 'Triceps Rope Pushdowns', sets: 3, reps: '12-15' },
        ]
      },
      {
        day: 'Wednesday', name: 'Lower A (Posterior Chain Focus)',
        exercises: [
          { name: 'Romanian Deadlifts (RDLs)', sets: 4, reps: '8-10' },
          { name: 'Lying Hamstring Curls', sets: 4, reps: '10-12' },
          { name: 'Leg Press (High/Wide Stance)', sets: 3, reps: '10-12' },
          { name: 'Standing Calf Raises', sets: 4, reps: '12-15' },
        ]
      },
      {
        day: 'Thursday', name: 'Upper (Chest & Back Width)',
        exercises: [
          { name: 'Incline Dumbbell Flies', sets: 4, reps: '10-12', note: 'focus on the deep stretch at the bottom' },
          { name: 'Flat Barbell Bench Press', sets: 3, reps: '8-10' },
          { name: 'Weighted Pull-ups (or Wide Grip Lat Pulldowns)', sets: 3, reps: '8-10' },
          { name: 'Single-Arm Dumbbell Rows', sets: 3, reps: '8-12 per arm' },
          { name: 'Reverse Pec Deck (Rear Delts)', sets: 3, reps: '12-15' },
        ]
      },
      {
        day: 'Friday', name: 'Lower B (Quad Focus)',
        exercises: [
          { name: 'Hack Squats', sets: 4, reps: '8-10', note: 'control the descent, drive hard out of the hole' },
          { name: 'Bulgarian Split Squats', sets: 3, reps: '8-10 per leg' },
          { name: 'Leg Extensions', sets: 3, reps: '12-15', note: 'hold the squeeze at the top for 1 second' },
          { name: 'Seated Calf Raises', sets: 4, reps: '15' },
        ]
      },
      {
        day: 'Saturday', name: 'Arms & Weak Points',
        exercises: [
          { name: 'Skull Crushers', sets: 3, reps: '10-12' },
          { name: 'Incline Dumbbell Bicep Curls', sets: 3, reps: '10-12' },
          { name: 'Dumbbell Lateral Raises', sets: 4, reps: '12-15', note: 'extra volume for the medial delts' },
          { name: 'Cable Crunches', sets: 3, reps: '12-15' },
          { name: 'Hanging Leg Raises', sets: 3, reps: 'to failure' },
        ]
      },
      { day: 'Sunday', name: 'Rest & Recovery', rest: true },
    ]
  },
  {
    type: 'FULL_BODY_DAILY',
    name: 'Full Body (Daily)',
    icon: '🌟',
    level: 'Beginner',
    levelColor: '#22c55e',
    gradient: 'linear-gradient(135deg, #0d9488 0%, #14b8a6 50%, #5eead4 100%)',
    borderGlow: 'rgba(20, 184, 166, 0.4)',
    description: 'Hit every muscle group 6 times per week with varied emphasis each day. A high-frequency approach ideal for beginners or those who prefer shorter, daily sessions.',
    bestFor: 'High frequency training',
    frequency: '6 days/week',
    schedule: 'Full A • Full B • Full C • Full D • Full E • Full F • Rest',
    routine: [
      {
        day: 'Monday', name: 'Full Body A (Posterior Chain & Chest)',
        exercises: [
          { name: 'Romanian Deadlifts (RDLs)', sets: 3, reps: '8-10' },
          { name: 'Flat Barbell Bench Press', sets: 3, reps: '8-10' },
          { name: 'Barbell Rows', sets: 3, reps: '8-10' },
          { name: 'Incline Dumbbell Flies', sets: 3, reps: '10-12', note: 'focus on the deep stretch' },
          { name: 'Lying Hamstring Curls', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Tuesday', name: 'Full Body B (Shoulder Focus & Quads)',
        exercises: [
          { name: 'Seated Dumbbell Overhead Press', sets: 4, reps: '8-10' },
          { name: 'Cable Lateral Raises', sets: 4, reps: '12-15', note: 'focus on a slow, controlled eccentric' },
          { name: 'Hack Squats', sets: 3, reps: '8-10', note: 'control the descent, drive hard out of the hole' },
          { name: 'Lat Pulldowns (Neutral Grip)', sets: 3, reps: '10-12' },
          { name: 'Overhead Cable Triceps Extensions', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Wednesday', name: 'Full Body C (Back Width & Biceps)',
        exercises: [
          { name: 'Weighted Pull-ups (or Wide Grip Lat Pulldowns)', sets: 3, reps: '8-10' },
          { name: 'Bulgarian Split Squats', sets: 3, reps: '8-10 per leg' },
          { name: 'Machine Chest Press', sets: 3, reps: '10-12' },
          { name: 'Face Pulls', sets: 3, reps: '12-15' },
          { name: 'Barbell Bicep Curls', sets: 3, reps: '8-10' },
        ]
      },
      {
        day: 'Thursday', name: 'Full Body D (Posterior Chain & Upper Volume)',
        exercises: [
          { name: 'Leg Press (High & Wide Stance)', sets: 3, reps: '10-12' },
          { name: 'Incline Dumbbell Bench Press', sets: 3, reps: '8-10' },
          { name: 'T-Bar Rows (Chest Supported)', sets: 3, reps: '8-12' },
          { name: 'Dumbbell Lateral Raises', sets: 3, reps: '12-15' },
          { name: 'Triceps Rope Pushdowns', sets: 3, reps: '12-15' },
        ]
      },
      {
        day: 'Friday', name: 'Full Body E (Quad Focus & Chest)',
        exercises: [
          { name: 'Walking Lunges', sets: 3, reps: '10-12 steps per leg' },
          { name: 'Leg Extensions', sets: 3, reps: '12-15', note: 'hold the squeeze at the top for 1 second' },
          { name: 'Incline Dumbbell Flies', sets: 3, reps: '10-12' },
          { name: 'Single-Arm Dumbbell Rows', sets: 3, reps: '10-12 per arm' },
          { name: 'Preacher Curls', sets: 3, reps: '10-12' },
        ]
      },
      {
        day: 'Saturday', name: 'Full Body F (Shoulder Volume & Arms)',
        exercises: [
          { name: 'Machine Shoulder Press', sets: 3, reps: '10-12' },
          { name: 'Reverse Pec Deck (Rear Delts)', sets: 3, reps: '12-15' },
          { name: 'Seated Leg Curls', sets: 3, reps: '12-15' },
          { name: 'Skull Crushers', sets: 3, reps: '10-12' },
          { name: 'Hammer Curls', sets: 3, reps: '10-12' },
          { name: 'Standing Calf Raises', sets: 4, reps: '12-15' },
        ]
      },
      { day: 'Sunday', name: 'Rest & Recovery', rest: true },
    ]
  }
];

const WorkoutPlans = ({ profileId: propProfileId }) => {
  const [profiles, setProfiles] = useState([]);
  const [selectedProfileId, setSelectedProfileId] = useState(propProfileId || '');
  const [activePlan, setActivePlan] = useState(null);
  const [allPlans, setAllPlans] = useState([]);
  const [selectedDayIndex, setSelectedDayIndex] = useState(0);
  const [loading, setLoading] = useState(false);
  const [generatingType, setGeneratingType] = useState(null);
  const [view, setView] = useState('plans');
  const [showAllPlans, setShowAllPlans] = useState(false);
  const [expandedSplit, setExpandedSplit] = useState(null);
  const [expandedSplitDay, setExpandedSplitDay] = useState(0);
  const [editPlan, setEditPlan] = useState(null);

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    if (propProfileId) {
      setSelectedProfileId(propProfileId);
    }
  }, [propProfileId]);

  useEffect(() => {
    if (selectedProfileId) {
      loadPlansForProfile();
    }
  }, [selectedProfileId]);

  const loadInitialData = async () => {
    try {
      const [profilesRes] = await Promise.all([getAllProfiles()]);
      setProfiles(profilesRes.data);
      if (!selectedProfileId && profilesRes.data.length > 0) {
        setSelectedProfileId(profilesRes.data[0].id);
      }
    } catch (error) {
      console.error('Error loading initial data:', error);
    }
  };

  const loadPlansForProfile = async () => {
    setLoading(true);
    try {
      const [activeRes, allRes] = await Promise.all([
        getActiveWorkoutPlan(selectedProfileId).catch(() => ({ data: null })),
        getAllWorkoutPlans(selectedProfileId)
      ]);
      setActivePlan(activeRes.data);
      setAllPlans(allRes.data);
      setSelectedDayIndex(0);
    } catch (error) {
      console.error('Error loading plans:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleGeneratePlan = async (planType) => {
    if (!selectedProfileId) {
      alert('Please select a profile first');
      return;
    }
    setGeneratingType(planType);
    try {
      const response = await generateWorkoutPlan(selectedProfileId, planType);
      setActivePlan(response.data);
      await loadPlansForProfile();
    } catch (error) {
      console.error('Error generating plan:', error);
      alert('Error generating workout plan');
    } finally {
      setGeneratingType(null);
    }
  };

  const handleActivatePlan = async (planId) => {
    try {
      const response = await activateWorkoutPlan(planId, selectedProfileId);
      setActivePlan(response.data);
      await loadPlansForProfile();
    } catch (error) {
      console.error('Error activating plan:', error);
    }
  };

  const handleDeletePlan = async (planId) => {
    if (!window.confirm('Are you sure you want to delete this plan?')) return;
    try {
      await deleteWorkoutPlan(planId);
      await loadPlansForProfile();
    } catch (error) {
      console.error('Error deleting plan:', error);
    }
  };

  const toggleSplitExpand = (type) => {
    if (expandedSplit === type) {
      setExpandedSplit(null);
    } else {
      setExpandedSplit(type);
      setExpandedSplitDay(0);
    }
  };

  // ─── Render Active Plan ───
  const renderActivePlan = () => {
    if (!activePlan || !activePlan.workoutDays) return null;
    const day = activePlan.workoutDays[selectedDayIndex];

    return (
      <section className="wp-active-plan">
        <div className="wp-active-header">
          <div className="wp-active-title-row">
            <div>
              <h3 className="wp-active-name">{activePlan.name}</h3>
              <p className="wp-active-desc">{activePlan.description}</p>
            </div>
            <div className="wp-active-actions">
              <button className="wp-edit-btn" onClick={() => { setEditPlan(activePlan); setView('builder'); }}>
                ✏️ Edit Plan
              </button>
              <span className="wp-active-badge">Active</span>
            </div>
          </div>
        </div>

        <div className="wp-day-selector">
          {activePlan.workoutDays.map((d, index) => (
            <button
              key={d.id || index}
              className={`wp-day-pill ${selectedDayIndex === index ? 'active' : ''} ${d.restDay ? 'rest' : ''}`}
              onClick={() => setSelectedDayIndex(index)}
            >
              <span className="wp-day-pill-name">{d.name}</span>
              <span className="wp-day-pill-dow">{d.dayOfWeek?.slice(0, 3)}</span>
            </button>
          ))}
        </div>

        {day && (
          day.restDay ? (
            <div className="wp-rest-day">
              <div className="wp-rest-icon">😴</div>
              <h3>Rest Day</h3>
              <p>Take time to recover. Your muscles grow during rest!</p>
            </div>
          ) : (
            <div className="wp-day-content">
              <div className="wp-day-info-row">
                <div className="wp-day-title-area">
                  <h4>{day.name}</h4>
                  {day.focus && <span className="wp-focus-badge">{day.focus?.replace('_', ' ')}</span>}
                </div>
                {day.description && <p className="wp-day-desc">{day.description}</p>}
              </div>

              <div className="wp-day-stats">
                <div className="wp-stat">
                  <span className="wp-stat-val">{day.plannedExercises?.length || 0}</span>
                  <span className="wp-stat-lbl">exercises</span>
                </div>
                <div className="wp-stat">
                  <span className="wp-stat-val">{day.plannedExercises?.reduce((s, e) => s + e.sets, 0) || 0}</span>
                  <span className="wp-stat-lbl">total sets</span>
                </div>
                <div className="wp-stat">
                  <span className="wp-stat-val">~{day.estimatedDurationMinutes || 45}</span>
                  <span className="wp-stat-lbl">min</span>
                </div>
              </div>

              <div className="wp-exercises-list">
                {day.plannedExercises?.map((pe, index) => {
                  const exercise = pe.exercise;
                  if (!exercise) return null;
                  return (
                    <div key={pe.id || index} className={`wp-exercise-card ${pe.warmup ? 'warmup' : ''}`}>
                      <div className="wp-ex-order">{index + 1}</div>
                      <div className="wp-ex-body">
                        <div className="wp-ex-top">
                          <h5 className="wp-ex-name">
                            {pe.warmup && <span className="wp-warmup-tag">Warm Up</span>}
                            {exercise.name}
                          </h5>
                          <span className="wp-ex-equip">{exercise.equipment?.replace(/_/g, ' ')}</span>
                        </div>
                        <div className="wp-ex-metrics">
                          <div className="wp-metric"><span className="wp-metric-val">{pe.sets}</span><span className="wp-metric-lbl">sets</span></div>
                          <span className="wp-metric-x">×</span>
                          <div className="wp-metric"><span className="wp-metric-val">{pe.repRange}</span><span className="wp-metric-lbl">reps</span></div>
                          <div className="wp-metric rest"><span className="wp-metric-val">{pe.restSeconds}s</span><span className="wp-metric-lbl">rest</span></div>
                        </div>
                        {pe.notes && <p className="wp-ex-notes">💡 {pe.notes}</p>}
                        <div className="wp-ex-muscles">
                          <span className="wp-primary-muscle">{exercise.primaryMuscle}</span>
                          {exercise.secondaryMuscles?.length > 0 && (
                            <span className="wp-secondary-muscles">Also: {exercise.secondaryMuscles.slice(0, 2).join(', ')}</span>
                          )}
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )
        )}
      </section>
    );
  };

  // ─── Render Routine Preview (expanded split card) ───
  const renderRoutinePreview = (split) => {
    if (!split.routine) return null;
    const dayData = split.routine[expandedSplitDay];
    if (!dayData) return null;

    return (
      <div className="wp-routine-preview">
        <div className="wp-routine-days-nav">
          {split.routine.map((rd, i) => (
            <button
              key={i}
              className={`wp-routine-day-btn ${expandedSplitDay === i ? 'active' : ''} ${rd.rest ? 'rest' : ''}`}
              onClick={() => setExpandedSplitDay(i)}
            >
              <span className="wp-rday-label">{rd.day}</span>
            </button>
          ))}
        </div>

        <div className="wp-routine-content">
          <div className="wp-routine-day-header">
            <h4>{dayData.name}</h4>
            {dayData.desc && <p>{dayData.desc}</p>}
          </div>

          {dayData.rest ? (
            <div className="wp-routine-rest">
              <span>😴</span>
              <p>Rest & Recovery — let your muscles grow!</p>
            </div>
          ) : (
            <div className="wp-routine-exercises">
              {dayData.exercises.map((ex, i) => (
                <div key={i} className="wp-routine-ex">
                  <span className="wp-routine-ex-num">{i + 1}</span>
                  <div className="wp-routine-ex-info">
                    <span className="wp-routine-ex-name">{ex.name}</span>
                    {ex.note && <span className="wp-routine-ex-note">💡 {ex.note}</span>}
                  </div>
                  <div className="wp-routine-ex-sets">
                    <span>{ex.sets} × {ex.reps}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    );
  };

  // ─── Render Split Cards ───
  const renderSplitCards = () => (
    <section className="wp-splits-section">
      <div className="wp-splits-header">
        <h3 className="wp-splits-title">Workout Splits</h3>
        <p className="wp-splits-subtitle">Select a training split based on your experience level and goals</p>
      </div>
      <div className="wp-splits-grid">
        {WORKOUT_SPLITS.map(split => (
          <div
            key={split.type}
            className={`wp-split-card ${expandedSplit === split.type ? 'expanded' : ''}`}
            style={{ '--split-gradient': split.gradient, '--split-glow': split.borderGlow }}
          >
            <div className="wp-split-header">
              <span className="wp-split-icon">{split.icon}</span>
              <div className="wp-split-title-area">
                <h4 className="wp-split-name">{split.name}</h4>
                <div className="wp-split-badges">
                  <span className="wp-level-badge" style={{ backgroundColor: split.levelColor }}>{split.level}</span>
                  <span className="wp-days-badge">{split.frequency}</span>
                </div>
              </div>
            </div>
            <p className="wp-split-desc">{split.description}</p>
            <div className="wp-split-footer">
              <div className="wp-split-meta">
                <div className="wp-split-meta-item">
                  <span className="wp-meta-label">Schedule</span>
                  <span className="wp-meta-value">{split.schedule}</span>
                </div>
                <div className="wp-split-meta-item">
                  <span className="wp-meta-label">Best for</span>
                  <span className="wp-meta-value">{split.bestFor}</span>
                </div>
              </div>
              <div className="wp-split-actions">
                <button
                  className="wp-view-routine-btn"
                  onClick={() => toggleSplitExpand(split.type)}
                >
                  {expandedSplit === split.type ? '▲ Hide Routine' : '👁️ View Routine'}
                </button>
                <button
                  className="wp-generate-btn"
                  onClick={() => handleGeneratePlan(split.type)}
                  disabled={generatingType !== null || !selectedProfileId}
                  style={{ background: split.gradient }}
                >
                  {generatingType === split.type ? (
                    <><span className="wp-spinner"></span> Generating...</>
                  ) : (
                    <>⚡ Generate Plan</>
                  )}
                </button>
              </div>
            </div>

            {expandedSplit === split.type && renderRoutinePreview(split)}
          </div>
        ))}
      </div>
    </section>
  );

  // ─── Render All Plans ───
  const renderAllPlans = () => {
    if (allPlans.length === 0) return null;
    const displayPlans = showAllPlans ? allPlans : allPlans.slice(0, 3);

    return (
      <section className="wp-all-plans">
        <div className="wp-plans-header"><h3>Your Plans ({allPlans.length})</h3></div>
        <div className="wp-plans-list">
          {displayPlans.map(plan => (
            <div key={plan.id} className={`wp-plan-item ${plan.active ? 'active' : ''}`}>
              <div className="wp-plan-info">
                <div className="wp-plan-name-row">
                  <h4>{plan.name}</h4>
                  {plan.active && <span className="wp-plan-active-dot"></span>}
                </div>
                <div className="wp-plan-meta-row">
                  <span className="wp-plan-type">{plan.planType?.replace(/_/g, ' ')}</span>
                  <span className="wp-plan-days">{plan.daysPerWeek} days/wk</span>
                  {plan.custom && <span className="wp-custom-tag">Custom</span>}
                </div>
              </div>
              <div className="wp-plan-actions">
                {!plan.active && (
                  <button onClick={() => handleActivatePlan(plan.id)} className="wp-btn-activate">Activate</button>
                )}
                <button onClick={() => handleDeletePlan(plan.id)} className="wp-btn-delete">×</button>
              </div>
            </div>
          ))}
        </div>
        {allPlans.length > 3 && (
          <button className="wp-show-more" onClick={() => setShowAllPlans(!showAllPlans)}>
            {showAllPlans ? 'Show Less ▲' : `Show All (${allPlans.length}) ▼`}
          </button>
        )}
      </section>
    );
  };

  if (view === 'builder') {
    return (
      <CustomWorkoutBuilder
        profileId={selectedProfileId}
        editPlan={editPlan}
        onBack={() => { setView('plans'); setEditPlan(null); loadPlansForProfile(); }}
        onPlanCreated={() => { setView('plans'); setEditPlan(null); loadPlansForProfile(); }}
      />
    );
  }

  return (
    <div className="wp-container">
      <div className="wp-header">
        <div className="wp-header-left">
          <h2 className="wp-page-title">💪 Workout Plans</h2>
          <p className="wp-page-subtitle">Build muscle, gain strength, transform your body</p>
        </div>
        <button className="wp-create-btn" onClick={() => setView('builder')} disabled={!selectedProfileId}>
          <span>+</span> Create Custom Plan
        </button>
      </div>

      {loading && (
        <div className="wp-loading">
          <div className="wp-loading-spinner"></div>
          <p>Loading your workout plans...</p>
        </div>
      )}

      {!loading && (
        <>
          {activePlan ? renderActivePlan() : (
            <div className="wp-empty-active">
              <div className="wp-empty-icon">🏋️</div>
              <h3>No Active Workout Plan</h3>
              <p>Choose a workout split below or create a custom plan to get started!</p>
            </div>
          )}
          {renderSplitCards()}
          {renderAllPlans()}
        </>
      )}
    </div>
  );
};

export default WorkoutPlans;
