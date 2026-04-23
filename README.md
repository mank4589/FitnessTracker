# UFit - Fitness Tracker

UFit is a comprehensive fitness and nutrition tracking application designed to help users log workouts, monitor daily caloric intake, and manage personal health goals. 

## 🚀 Live Demo

- **Frontend**: [https://fitness-tracker-rho-ten.vercel.app](https://fitness-tracker-rho-ten.vercel.app)
- **Backend API**: [https://fitnesstracker-production-ce10.up.railway.app](https://fitnesstracker-production-ce10.up.railway.app)

## 🛠️ Technology Stack

### Frontend
- **Framework**: React.js with Vite
- **Language**: JavaScript (JSX)
- **Styling**: Vanilla CSS with CSS Variables for consistent theming
- **Hosting**: Vercel

### Backend
- **Framework**: Spring Boot (Java 21)
- **Database**: PostgreSQL
- **Data Access**: Spring Data JPA / Hibernate
- **Containerization**: Docker (Multi-stage build)
- **Hosting**: Railway

## ✨ Key Features

- **User Authentication**: Secure profile creation and login system with data isolation.
- **Nutrition Tracking**: Log daily meals, calculate caloric intake, and track macronutrients (Protein, Carbs, Fat) using local database.
- **Water Tracking**: Monitor daily water consumption.
- **Exercise Logging**: Record workouts, sets, reps, and track calories burned.
- **Daily Goals**: Set and monitor daily caloric and macronutrient goals.
- **Dark/Minimal UI**: Clean, responsive, and modern user interface focused on usability.

## 🏗️ Local Development Setup

### Prerequisites
- Node.js (v18+)
- Java 21+
- Maven
- PostgreSQL

### 1. Database Setup
1. Ensure PostgreSQL is running locally on port `5432`.
2. Create a database named `ufitdb`.
3. The default credentials in `application.properties` expect username `postgres` and password `4589`. Update these if your local setup differs.

### 2. Backend Setup
```bash
# From the project root directory
mvnw clean compile
mvnw spring-boot:run
```
The backend will start on `http://localhost:8080`.

### 3. Frontend Setup
```bash
# Navigate to the frontend directory
cd frontend

# Install dependencies
npm install

# Start the development server
npm run dev
```
The frontend will be available at `http://localhost:5173`.

## 📦 Deployment

### Frontend (Vercel)
The frontend is continuously deployed via Vercel. 
- Ensure the `VITE_API_URL` environment variable is set to the Railway backend URL (e.g., `https://fitnesstracker-production-ce10.up.railway.app/api`).

### Backend (Railway)
The backend is deployed via Docker on Railway.
- **Environment Variables**:
  - `DATABASE_URL`: Connection string to the Railway PostgreSQL instance.
  - `CORS_ORIGINS`: Comma-separated list of allowed frontend origins (e.g., `https://fitness-tracker-rho-ten.vercel.app`).
  - `DDL_AUTO`: Typically set to `update` to automatically apply schema changes without dropping data.

## 📄 License
This project is licensed under the MIT License.
