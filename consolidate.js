const fs = require('fs');
const path = require('path');
const csv = require('csv-parser');

const resourcesDir = path.join(__dirname, 'src', 'main', 'resources');
const morePlansDir = path.join(resourcesDir, 'more meal plans');
const outputJson = path.join(resourcesDir, 'food-database.json');

const finalItems = new Map(); // key = lowercase name to avoid duplicates

// Helpers for enrichment
function inferRestrictions(ingredientsTxt) {
    const list = ingredientsTxt.toLowerCase();
    const reqs = new Set(['DAIRY_FREE', 'GLUTEN_FREE', 'NUT_FREE', 'SOY_FREE', 'EGG_FREE']);
    
    if (list.includes('milk') || list.includes('cheese') || list.includes('paneer') || list.includes('ghee') || list.includes('butter') || list.includes('cream') || list.includes('yogurt') || list.includes('curd')) reqs.delete('DAIRY_FREE');
    if (list.includes('wheat') || list.includes('flour') || list.includes('maida') || list.includes('bread') || list.includes('pasta') || list.includes('rava') || list.includes('semolina')) reqs.delete('GLUTEN_FREE');
    if (list.includes('peanut') || list.includes('almond') || list.includes('cashew') || list.includes('nut') || list.includes('pistachio')) reqs.delete('NUT_FREE');
    if (list.includes('soy') || list.includes('tofu')) reqs.delete('SOY_FREE');
    if (list.includes('egg') || list.includes('mayo')) reqs.delete('EGG_FREE');
    
    return Array.from(reqs);
}

function inferDietCategory(ingredientsTxt, isVeg, isVegan) {
    const list = ingredientsTxt.toLowerCase();
    if (isVegan || (!list.includes('chicken') && !list.includes('meat') && !list.includes('beef') && !list.includes('pork') && !list.includes('fish') && !list.includes('prawn') && !list.includes('egg') && !list.includes('honey') && !list.includes('milk') && !list.includes('cheese') && !list.includes('ghee'))) return 'VEGAN';
    if (isVeg || (!list.includes('chicken') && !list.includes('meat') && !list.includes('beef') && !list.includes('pork') && !list.includes('fish') && !list.includes('prawn'))) return 'VEGETARIAN';
    if (list.includes('fish') || list.includes('prawn')) return 'PESCATARIAN';
    return 'NONE';
}

function estimateMacros(calories, dietCat, course) {
    let p = 0.2, c = 0.5, f = 0.3;
    if (dietCat === 'VEGAN') { p = 0.15; c = 0.6; f = 0.25; }
    else if (dietCat === 'KETO') { p = 0.25; c = 0.05; f = 0.7; }
    else if (course === 'dessert') { p = 0.05; c = 0.7; f = 0.25; }
    else if (dietCat === 'NONE') { p = 0.3; c = 0.4; f = 0.3; }
    
    return {
        protein_g: parseFloat(((calories * p) / 4).toFixed(1)),
        carbohydrates_total_g: parseFloat(((calories * c) / 4).toFixed(1)),
        fat_total_g: parseFloat(((calories * f) / 9).toFixed(1)),
        fiber_g: parseFloat((calories * 0.015).toFixed(1)),
        sugar_g: course === 'dessert' ? parseFloat(((calories * c * 0.8) / 4).toFixed(1)) : parseFloat((calories * 0.05).toFixed(1))
    };
}

// 1. Load Base food-database.json
function loadBaseFood() {
    console.log("Loading base food items...");
    if (fs.existsSync(outputJson)) {
        const data = JSON.parse(fs.readFileSync(outputJson, 'utf8'));
        for (const item of data) {
            item.isMeal = false;
            if (!item.mealType) item.mealType = null;
            if (!item.dietaryCategory) item.dietaryCategory = 'NONE';
            if (!item.satisfiesRestrictions) item.satisfiesRestrictions = [];
            finalItems.set((item.name || '').toLowerCase(), item);
        }
    }
}

// 2. Load meal_dataset.csv
function loadMealDataset() {
    return new Promise((resolve) => {
        console.log("Loading meal dataset...");
        fs.createReadStream(path.join(resourcesDir, 'meal_dataset.csv'))
            .pipe(csv())
            .on('data', (row) => {
                const name = row.meal_name || row.name;
                if (!name) return;
                const cal = parseInt(row.calories) || 0;
                finalItems.set(name.toLowerCase(), {
                    name: name,
                    isMeal: true,
                    calories: cal,
                    serving_size_g: 400, // standard meal size
                    protein_g: parseFloat(row.protein) || 0,
                    carbohydrates_total_g: parseFloat(row.carbs) || 0,
                    fat_total_g: parseFloat(row.fat) || 0,
                    fiber_g: parseFloat(row.fiber) || 0,
                    sugar_g: Math.max(0, parseFloat(row.carbs)*0.1),
                    sodium_mg: 500,
                    potassium_mg: 300,
                    cholesterol_mg: 0,
                    fat_saturated_g: Math.max(0, parseFloat(row.fat)*0.3),
                    category: "Meal",
                    mealType: (row.mealType || 'LUNCH').toUpperCase(),
                    dietaryCategory: (row.dietaryCategory || 'NONE').toUpperCase(),
                    tags: [],
                    satisfiesRestrictions: (row.restrictions || '').split(';').filter(x => x.trim() !== ''),
                    prepTimeMinutes: parseInt(row.prepTime || row.prepTimeMinutes || 30),
                    description: row.description || `Delicious ${name}`,
                    ingredients: row.ingredients || row.description || name
                });
            })
            .on('end', resolve);
    });
}

// 3. Load healthy_meal_plans.csv (has scaled macros, we will rescale assuming max calories ~800, max protein ~50g)
function loadHealthyMeals() {
    return new Promise((resolve) => {
        console.log("Loading healthy meals...");
        fs.createReadStream(path.join(morePlansDir, 'healthy_meal_plans.csv'))
            .pipe(csv())
            .on('data', (row) => {
                const name = row.meal_name;
                if (!name || finalItems.has(name.toLowerCase())) return;
                const cal = Math.round(parseFloat(row.calories) * 800) || 400; 
                let dietCat = 'NONE';
                if (row.vegan === '1') dietCat = 'VEGAN';
                else if (row.vegetarian === '1') dietCat = 'VEGETARIAN';
                else if (row.keto === '1') dietCat = 'KETO';
                else if (row.paleo === '1') dietCat = 'PALEO';
                
                const macros = estimateMacros(cal, dietCat, 'main course');
                const reqs = [];
                if (row.gluten_free === '1') reqs.push('GLUTEN_FREE');
                
                finalItems.set(name.toLowerCase(), {
                    name, isMeal: true, calories: cal, serving_size_g: 350,
                    protein_g: macros.protein_g,
                    carbohydrates_total_g: macros.carbohydrates_total_g,
                    fat_total_g: macros.fat_total_g,
                    fiber_g: macros.fiber_g,
                    sugar_g: macros.sugar_g,
                    sodium_mg: 400, potassium_mg: 200, cholesterol_mg: 0, fat_saturated_g: macros.fat_total_g * 0.2,
                    category: "Healthy",
                    mealType: 'LUNCH',
                    dietaryCategory: dietCat,
                    tags: [],
                    satisfiesRestrictions: reqs,
                    prepTimeMinutes: 30,
                    description: `A healthy ${dietCat !== 'NONE' ? dietCat.toLowerCase() + ' ' : ''}meal.`,
                    ingredients: name
                });
            })
            .on('end', resolve);
    });
}

// 4. Load indian_food.csv
function loadIndianFood() {
    return new Promise((resolve) => {
        console.log("Loading indian foods...");
        fs.createReadStream(path.join(morePlansDir, 'indian_food.csv'))
            .pipe(csv())
            .on('data', (row) => {
                const name = row.name;
                if (!name || finalItems.has(name.toLowerCase())) return;
                const dietCat = inferDietCategory(row.ingredients, row.diet === 'vegetarian', false);
                const isDessert = row.course === 'dessert';
                const cal = isDessert ? 350 : 450;
                const macros = estimateMacros(cal, dietCat, row.course);
                const reqs = inferRestrictions(row.ingredients);
                
                finalItems.set(name.toLowerCase(), {
                    name, isMeal: true, calories: cal, serving_size_g: 300,
                    protein_g: macros.protein_g, carbohydrates_total_g: macros.carbohydrates_total_g,
                    fat_total_g: macros.fat_total_g, fiber_g: macros.fiber_g, sugar_g: macros.sugar_g,
                    sodium_mg: 600, potassium_mg: 150, cholesterol_mg: 0, fat_saturated_g: macros.fat_total_g * 0.4,
                    category: "Indian",
                    mealType: isDessert ? 'SNACK' : 'DINNER',
                    dietaryCategory: dietCat,
                    tags: [row.flavor_profile, row.region].filter(x => x && x !== '-1'),
                    satisfiesRestrictions: reqs,
                    prepTimeMinutes: parseInt(row.prep_time) > 0 ? parseInt(row.prep_time) : 30,
                    description: `Classic Indian ${row.course || 'dish'}.`,
                    ingredients: row.ingredients || ''
                });
            })
            .on('end', resolve);
    });
}

// 5. Load diet_plans.json
function loadDietPlans() {
    console.log("Loading diet plans...");
    if (fs.existsSync(path.join(morePlansDir, 'diet_plans.json'))) {
        const plans = JSON.parse(fs.readFileSync(path.join(morePlansDir, 'diet_plans.json'), 'utf8'));
        for (const plan of plans.diet_plans) {
            const dietCat = plan.id.toUpperCase();
            for (const day of plan.sample_days) {
                for (const mealKey in day.meals) {
                    if (mealKey === 'snacks') {
                        for (const snack of day.meals.snacks) {
                            addDietPlanMeal(snack.name, snack.calories, 'SNACK', dietCat);
                        }
                    } else {
                        addDietPlanMeal(day.meals[mealKey].name, day.meals[mealKey].calories, mealKey.toUpperCase(), dietCat);
                    }
                }
            }
        }
    }
}

function addDietPlanMeal(name, cal, type, dietCat) {
    if (!name || finalItems.has(name.toLowerCase())) return;
    const macros = estimateMacros(cal, dietCat, 'main course');
    const actualDiet = dietCat === 'VEGAN' || dietCat === 'VEGETARIAN' || dietCat === 'KETOGENIC' || dietCat === 'PALEO' ? dietCat : 'NONE';
    
    finalItems.set(name.toLowerCase(), {
        name, isMeal: true, calories: cal, serving_size_g: 350,
        protein_g: macros.protein_g, carbohydrates_total_g: macros.carbohydrates_total_g,
        fat_total_g: macros.fat_total_g, fiber_g: macros.fiber_g, sugar_g: macros.sugar_g,
        sodium_mg: 400, potassium_mg: 200, cholesterol_mg: 0, fat_saturated_g: macros.fat_total_g * 0.2,
        category: "Diet Plan", mealType: type, dietaryCategory: actualDiet, tags: [], satisfiesRestrictions: ['DAIRY_FREE', 'GLUTEN_FREE'], // optimistic healthy default
        prepTimeMinutes: 20, description: `${dietCat} friendly ${type.toLowerCase()}.`, ingredients: name
    });
}

// Run everything
async function run() {
    loadBaseFood();
    await loadMealDataset();
    await loadHealthyMeals();
    await loadIndianFood();
    loadDietPlans();
    
    const output = Array.from(finalItems.values());
    console.log(`Writing ${output.length} total items to food-database.json...`);
    fs.writeFileSync(outputJson, JSON.stringify(output, null, 2));
    console.log("Done!");
}

run();
