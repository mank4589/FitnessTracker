const fs = require('fs');
const path = require('path');

const DB_PATH = path.join(__dirname, 'src/main/resources/food-database.json');

const MEAT_KEYWORDS = [
  'chicken', 'beef', 'pork', 'lamb', 'mutton', 'sausage', 'bacon', 'ham', 'turkey', 'veal',
  'pepperoni', 'salami', 'hot dog', 'meatball', 'meat loaf', 'burger', 'steak', 'duck',
  'prosciutto', 'kebab', 'kofta', 'rogan josh', 'tikka masala', 'char siu', 'bolognese',
  'carbonara', 'shawarma', 'taco', 'bulgogi', 'bison', 'jerky'
];

const SEAFOOD_KEYWORDS = [
  'fish', 'salmon', 'tuna', 'cod', 'tilapia', 'shrimp', 'lobster', 'crab', 'scallop',
  'sardine', 'mackerel', 'trout', 'halibut', 'catfish', 'clam', 'mussel', 'oyster',
  'calamari', 'octopus', 'prawn', 'ceviche', 'sushi', 'sashimi', 'nigiri'
];

const DAIRY_EGG_KEYWORDS = [
  'egg', 'cheese', 'milk', 'butter', 'yogurt', 'cream', 'paneer', 'ghee', 'honey',
  'mayonnaise', 'gelato', 'ice cream', 'pudding', 'cheesecake', 'omelette', 'whey',
  'mozzarella', 'parmesan', 'cheddar', 'ricotta', 'cottage', 'swiss', 'cake', 'cookie',
  'brownie', 'donut', 'waffle', 'pancake', 'french toast', 'chocolate', 'latte', 'cappuccino',
  'lassi', 'makhani', 'shahi', 'malai', 'kheer', 'halwa', 'ladoo', 'jalebi', 'gulab jamun',
  'barfi', 'pedha', 'mysore pak', 'payasam'
];

function inferDietaryCategory(name) {
  const lower = name.toLowerCase();
  
  // Exclude false positives like "eggplant" for egg
  const isEggplant = lower.includes('eggplant');
  
  const hasMeat = MEAT_KEYWORDS.some(k => lower.includes(k) && !(k === 'taco' && lower.includes('fish taco')));
  const hasSeafood = SEAFOOD_KEYWORDS.some(k => lower.includes(k));
  const hasDairyEgg = DAIRY_EGG_KEYWORDS.some(k => {
    if (k === 'egg' && isEggplant) return false;
    return lower.includes(k);
  });

  if (hasMeat) {
    return 'NON_VEGETARIAN';
  } else if (hasSeafood) {
    return 'PESCATARIAN';
  } else if (hasDairyEgg) {
    return 'VEGETARIAN';
  } else {
    return 'VEGAN';
  }
}

try {
  const data = fs.readFileSync(DB_PATH, 'utf8');
  let foods = JSON.parse(data);
  let updatedCount = 0;

  foods.forEach(food => {
    // If it's explicitly named a vegan dish (e.g. "Vegan Curry")
    if (food.name.toLowerCase().includes('vegan')) {
      food.dietaryCategory = 'VEGAN';
    } 
    else if (food.name.toLowerCase().includes('vegetarian')) {
      food.dietaryCategory = 'VEGETARIAN';
    }
    else {
      food.dietaryCategory = inferDietaryCategory(food.name);
    }
    updatedCount++;
  });

  fs.writeFileSync(DB_PATH, JSON.stringify(foods, null, 2), 'utf8');
  console.log(`Successfully updated dietaryCategory for ${updatedCount} items.`);
} catch (e) {
  console.error("Error updating database:", e);
}
