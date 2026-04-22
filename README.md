# UFit — Health & Fitness Platform

A full-stack health and fitness web application built with **Spring Boot** and **React**. Track your meals, log workouts, and calculate key health metrics — all backed by WHO/AMDR nutritional guidelines.

---

## ✨ Features

### 🏋️ Health Calculators
| Calculator | Description |
|------------|-------------|
| **BMI** | Body Mass Index with healthy range & weight advice |
| **BMR** | Basal Metabolic Rate (Mifflin-St Jeor & Harris-Benedict) |
| **Body Fat** | Body fat percentage using the U.S. Navy method |
| **TDEE** | Total Daily Energy Expenditure with 6 activity levels |
| **Ideal Weight** | Four formulas — Devine, Robinson, Miller, Hamwi |
| **Macro Calculator** | WHO/AMDR-aligned macronutrient breakdown with 4 diet plans (Balanced, Low Fat, Low Carb, High Protein) |

### 🍽️ Meal Tracker
- Search from a **pre-loaded database of 600+ foods** with full nutrition info
- Log meals with adjustable serving sizes
- Set and track daily calorie & macro goals
- Navigate between days with date picker
- Auto-suggest search with real-time results

### 💪 Exercise Logger
- Search exercises via the **wger API** with auto-suggest
- Log sets, reps, weight, duration, and notes
- View daily exercise history with summary cards
- Date navigation to review past workouts

### 📊 Health History
- Save full health reports to the database
- View history with sortable data table
- Delete individual reports

---

## 🛠️ Technology Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | React 19, Vite 7 |
| **Backend** | Spring Boot 3.2.5, Java 21 |
| **Database** | PostgreSQL |
| **Build** | Maven (backend), npm (frontend) |
| **APIs** | wger REST API (exercises) |

---

## 🚀 Getting Started

### Prerequisites
- **Java 21+**
- **Node.js 18+** and npm
- **PostgreSQL** running on `localhost:5432`

### 1. Set Up the Database

Create a PostgreSQL database named `ufitdb`:

```sql
CREATE DATABASE ufitdb;
```

### 2. Configure the Backend

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ufitdb
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

> **Note:** `ddl-auto=update` automatically creates/updates tables on startup. The food database is seeded from an embedded JSON file on the first run.

### 3. Start the Backend (Spring Boot)

```bash
cd ufit
./mvnw spring-boot:run
```

The backend starts at **http://localhost:8080**.

### 4. Start the Frontend (React + Vite)

```bash
cd ufit/frontend
npm install
npm run dev
```

The frontend starts at **http://localhost:5173**.

---

## 📁 Project Structure

```
ufit/
├── frontend/                          # React Frontend
│   ├── src/
│   │   ├── components/
│   │   │   ├── BmiCalculator.jsx      # BMI calculator
│   │   │   ├── BmrCalculator.jsx      # BMR calculator
│   │   │   ├── BodyFatCalculator.jsx  # Body fat calculator
│   │   │   ├── TdeeCalculator.jsx     # TDEE calculator
│   │   │   ├── IdealWeight.jsx        # Ideal weight calculator
│   │   │   ├── MacroCalculator.jsx    # Macro calculator (WHO/AMDR)
│   │   │   ├── CalorieTracker.jsx     # Meal tracker
│   │   │   ├── ExerciseLogger.jsx     # Exercise logger (wger API)
│   │   │   └── HistoryDashboard.jsx   # Health history dashboard
│   │   ├── services/api.js            # Axios API calls
│   │   ├── App.jsx                    # Main app + routing
│   │   └── App.css                    # Global styles
│   ├── package.json
│   └── vite.config.js
├── src/main/java/com/ufit/
│   ├── UfitApplication.java           # Spring Boot entry point
│   ├── config/WebConfig.java          # CORS + RestTemplate config
│   ├── controller/
│   │   ├── HealthController.java      # Health calculator endpoints
│   │   ├── CalorieController.java     # Meal tracker endpoints
│   │   └── ExerciseController.java    # Exercise logger endpoints
│   ├── logic/HealthCalculator.java    # All calculation algorithms
│   ├── model/
│   │   ├── ufit.java                  # Health report entity
│   │   ├── FoodItem.java              # Food nutrition entity
│   │   ├── MealEntry.java             # Meal log entity
│   │   ├── DailyGoal.java             # Daily goal entity
│   │   └── ExerciseLog.java           # Exercise log entity
│   ├── repository/                    # JPA repositories
│   └── service/
│       ├── ufitService.java           # Health report service
│       ├── CalorieService.java        # Meal tracker service
│       ├── FoodDatabaseSeeder.java    # Auto-seeds food DB on first run
│       └── ExerciseService.java       # Exercise + wger API service
├── src/main/resources/
│   ├── application.properties         # Spring Boot config
│   └── food-database.json             # 600+ food items (seed data)
└── pom.xml
```

---

## 🔌 API Endpoints

### Health Calculators — `/api/health/*`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/health/bmi` | POST | Calculate BMI |
| `/api/health/bmr` | POST | Calculate BMR |
| `/api/health/body-fat` | POST | Calculate body fat % |
| `/api/health/tdee` | POST | Calculate TDEE |
| `/api/health/ideal-weight` | POST | Calculate ideal weight |
| `/api/health/macros` | POST | Calculate macronutrients |
| `/api/health/report` | POST | Save full health report |
| `/api/health/history` | GET | Get all saved reports |
| `/api/health/history/{id}` | DELETE | Delete a report |

### Meal Tracker — `/api/calories/*`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/calories/foods/search` | GET | Search food database |
| `/api/calories/log` | POST | Log a meal entry |
| `/api/calories/log/date/{date}` | GET | Get meals for a date |
| `/api/calories/goal` | POST | Set daily calorie goal |
| `/api/calories/goal/{date}` | GET | Get goal for a date |

### Exercise Logger — `/api/exercises/*`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/exercises/search` | GET | Search exercises (wger API) |
| `/api/exercises/log` | POST | Log an exercise |
| `/api/exercises/log/date/{date}` | GET | Get exercises for a date |
| `/api/exercises/log/{id}` | DELETE | Delete an exercise log |

---

## 🧮 Macro Calculator — WHO/AMDR Guidelines

The macro calculator uses percentages aligned with international health standards:

| Plan | Protein | Carbs | Fat | Source |
|------|---------|-------|-----|--------|
| Balanced | 20% (10–35%) | 55% (45–65%) | 25% (20–35%) | AMDR / WHO |
| Low Fat | 25% (15–30%) | 60% (55–65%) | 15% (10–20%) | WHO low-fat |
| Low Carb | 30% (25–35%) | 20% (10–30%) | 50% (40–55%) | Low-carb lit. |
| High Protein | 35% (30–40%) | 35% (25–45%) | 30% (25–35%) | ADA |

- **Sugar limit**: <10% of total energy (WHO)
- **Saturated Fat limit**: <10% of total energy (WHO)

---

## 🔧 Troubleshooting

| Problem | Solution |
|---------|----------|
| Port 8080 in use | Stop the existing process or change port in `application.properties` |
| Port 5173 in use | Stop the existing Vite process or run `npx vite --port 3000` |
| DB connection failed | Ensure PostgreSQL is running and credentials in `application.properties` are correct |
| Food search empty | Wait for the first-run seed to complete (check console for `[FoodDB]` logs) |
| Exercise search fails | Ensure the backend is running (wger API calls are proxied through the backend) |

---

## 📄 License

MIT License — See LICENSE file
