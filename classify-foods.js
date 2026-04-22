const fs = require('fs');
const path = require('path');

const DB_PATH = path.join(__dirname, 'src/main/resources/food-database.json');
const data = JSON.parse(fs.readFileSync(DB_PATH, 'utf8'));

// ==========================================================================
// SIMPLIFIED FOOD CLASSIFICATION — 4 dimensions:
//   1. cuisine:  INDIAN, CHINESE, JAPANESE, ITALIAN, etc.
//   2. dishType: curry, dal, sabzi, egg_dish, meat_dish, snack, dessert, 
//                drink, staple, fruit, nut, side, condiment, ingredient,
//                pasta, rice_dish, breakfast_item, complete_meal
//   3. mealTime: BREAKFAST, LUNCH, DINNER, SNACK
//   4. diet:     VEGAN, VEGETARIAN, EGGETARIAN, NON_VEGETARIAN
// ==========================================================================

// ======================== 1. CUISINE ========================

const CUISINE_RULES = [
  { cuisine: 'INDIAN', kw: [
    'dal', 'daal', 'rajma', 'chole', 'chhole', 'chana', 'kadhi', 'sambar', 'rasam',
    'paneer', 'palak', 'aloo', 'gobi', 'sabzi', 'bhindi', 'lauki', 'tori', 'methi',
    'roti', 'chapati', 'naan', 'paratha', 'puri', 'bhatura', 'kulcha',
    'dosa', 'idli', 'uttapam', 'upma', 'poha', 'cheela', 'dhokla', 'thepla',
    'biryani', 'pulao', 'khichdi', 'tehri', 'jeera rice',
    'samosa', 'pakoda', 'pakora', 'vada', 'pav bhaji', 'bhelpuri', 'jhalmuri',
    'chaat', 'kachori', 'dabeli', 'tikki',
    'gulab jamun', 'jalebi', 'barfi', 'ladoo', 'laddu', 'rasgulla', 'modak',
    'halwa', 'kheer', 'payasam',
    'lassi', 'buttermilk', 'chaas', 'masala chai', 'raita', 'papad',
    'tikka', 'tandoori', 'korma', 'vindaloo', 'rogan josh', 'do pyaza',
    'butter chicken', 'malai kofta', 'kofta',
    'rajma chawal', 'chole chawal', 'dal chawal',
    'egg curry', 'fish curry', 'mutton curry', 'lamb curry',
    'bajra', 'jowar', 'makki', 'basmati',
    'curd', 'dahi', 'pickle', 'achaar', 'momos',
  ]},
  { cuisine: 'CHINESE', kw: ['chow mein', 'lo mein', 'kung pao', 'sweet and sour', 'general tso', 'fried rice', 'dim sum', 'wonton', 'hakka', 'manchurian', 'spring roll', 'szechuan', 'schezwan', 'nasi goreng', 'stir fry', 'stir-fry'] },
  { cuisine: 'JAPANESE', kw: ['sushi', 'sashimi', 'ramen', 'miso', 'tempura', 'teriyaki', 'udon', 'soba', 'edamame', 'matcha', 'onigiri'] },
  { cuisine: 'THAI', kw: ['pad thai', 'tom yum', 'thai', 'green curry', 'red curry', 'massaman'] },
  { cuisine: 'ITALIAN', kw: ['pasta', 'spaghetti', 'penne', 'fettuccine', 'linguine', 'carbonara', 'bolognese', 'alfredo', 'marinara', 'primavera', 'lasagna', 'pizza', 'risotto', 'bruschetta', 'tiramisu', 'pesto', 'caprese'] },
  { cuisine: 'MEXICAN', kw: ['taco', 'burrito', 'enchilada', 'quesadilla', 'fajita', 'nachos', 'guacamole', 'salsa', 'churro', 'tortilla chip'] },
  { cuisine: 'KOREAN', kw: ['bibimbap', 'bulgogi', 'kimchi', 'tteokbokki', 'japchae', 'gochujang', 'korean'] },
  { cuisine: 'AMERICAN', kw: ['burger', 'hot dog', 'mac and cheese', 'barbecue', 'bbq', 'club sandwich', 'coleslaw', 'cornbread', 'brownie', 'cookie', 'pancake', 'waffle', 'french toast', 'muffin', 'donut'] },
  { cuisine: 'MEDITERRANEAN', kw: ['hummus', 'falafel', 'shawarma', 'pita', 'tzatziki', 'tabbouleh', 'greek salad', 'baba ganoush'] },
];

// ======================== 2. DISH TYPE ========================

const TYPE_RULES = [
  // Dal / Lentil
  { type: 'dal', kw: ['dal', 'daal', 'arhar dal', 'masoor dal', 'moong dal', 'urad dal', 'toor dal', 'chana dal', 'sambar', 'rasam'] },
  // Sabzi (dry/semi-dry vegetable dishes)
  { type: 'sabzi', kw: ['aloo gobi', 'aloo beans', 'aloo matar', 'methi aloo', 'bhindi', 'lauki', 'tori', 'patta gobi', 'gobhi', 'baingan', 'palak', 'sabzi', 'subzi', 'karela', 'aloo', 'gobi'] },
  // Curry (gravy-based dishes)
  { type: 'curry', kw: [
    'curry', 'masala', 'korma', 'vindaloo', 'rogan josh', 'do pyaza',
    'paneer butter masala', 'palak paneer', 'shahi paneer', 'matar paneer',
    'malai kofta', 'chilli paneer', 'paneer bhurji', 'paneer tikka masala',
    'butter chicken', 'chicken tikka masala', 'tandoori chicken',
    'tikka', 'kebab', 'kofta', 'kadhi',
    'rajma', 'chole', 'chhole', 'chana',
    'stew', 'casserole',
    'kung pao', 'sweet and sour', 'general tso', 'orange chicken',
    'teriyaki', 'tempura',
    'soya chunk',
  ]},
  // Egg dishes
  { type: 'egg_dish', kw: ['omelette', 'omelet', 'scrambled egg', 'egg bhurji', 'boiled egg', 'fried egg', 'poached egg', 'egg curry'] },
  // Meat / Fish dishes
  { type: 'meat_dish', kw: [
    'grilled chicken', 'baked chicken', 'roast chicken', 'chicken breast', 'chicken thigh',
    'grilled fish', 'baked fish', 'fish fry', 'salmon', 'tuna steak', 'cod',
    'steak', 'meatloaf', 'meatball',
    'mutton', 'lamb', 'pork chop', 'beef',
    'shrimp', 'prawn',
  ]},
  // Rice dish (standalone)
  { type: 'rice_dish', kw: ['biryani', 'pulao', 'khichdi', 'tehri', 'fried rice', 'rajma chawal', 'chole chawal', 'dal chawal', 'risotto', 'paella', 'jambalaya', 'bibimbap', 'nasi goreng'] },
  // Complete meal
  { type: 'complete_meal', kw: ['thali', 'combo', 'platter', 'burger', 'pizza', 'taco', 'burrito', 'enchilada', 'quesadilla', 'fajita', 'lasagna', 'shepherd pie', 'pot pie', 'pho', 'ramen'] },
  // Pasta / Noodles
  { type: 'pasta', kw: ['pasta', 'spaghetti', 'penne', 'fettuccine', 'linguine', 'carbonara', 'bolognese', 'alfredo', 'marinara', 'primavera', 'macaroni', 'red sauce pasta', 'white sauce pasta', 'noodles', 'chow mein', 'lo mein', 'pad thai', 'hakka noodles', 'mac and cheese'] },
  // Breakfast items
  { type: 'breakfast_item', kw: [
    'oatmeal', 'oats', 'porridge', 'cereal', 'cornflakes', 'granola', 'muesli',
    'pancake', 'waffle', 'french toast',
    'idli', 'dosa', 'masala dosa', 'uttapam', 'upma', 'poha',
    'cheela', 'moong dal cheela', 'besan cheela',
    'dhokla', 'thepla',
    'paratha', 'aloo paratha', 'gobhi paratha', 'pyaaz paratha',
    'methi paratha', 'paneer paratha',
    'toast', 'avocado toast',
  ]},
  // Staple (accompaniment)
  { type: 'staple', kw: ['rice', 'steamed rice', 'basmati', 'brown rice', 'white rice', 'jeera rice', 'jasmine rice', 'roti', 'chapati', 'naan', 'bajra roti', 'jowar roti', 'makki roti', 'tortilla', 'pita'] },
  // Snack
  { type: 'snack', kw: [
    'samosa', 'pakoda', 'pakora', 'paneer pakoda', 'bread pakora',
    'vada', 'vada pav', 'pav bhaji',
    'bhelpuri', 'jhalmuri', 'sev puri', 'pani puri', 'chaat', 'mixed sprouts chaat',
    'kachori', 'dabeli', 'momos', 'cutlet', 'tikki', 'spring roll',
    'sandwich', 'grilled sandwich', 'club sandwich',
    'chips', 'fries', 'popcorn', 'nachos',
    'cookie', 'biscuit', 'cracker',
    'energy bar', 'protein bar', 'granola bar',
  ]},
  // Dessert
  { type: 'dessert', kw: [
    'gulab jamun', 'jalebi', 'barfi', 'ladoo', 'laddu', 'rasgulla', 'modak',
    'halwa', 'kheer', 'payasam', 'pudding', 'custard',
    'ice cream', 'gelato', 'frozen yogurt',
    'cake', 'pastry', 'brownie', 'muffin', 'donut', 'doughnut',
    'chocolate', 'tiramisu', 'churro',
  ]},
  // Drink
  { type: 'drink', kw: [
    'milk', 'skim milk', 'whole milk', 'almond milk', 'soy milk', 'oat milk',
    'buttermilk', 'lassi', 'chaas',
    'tea', 'chai', 'masala chai', 'coffee', 'espresso',
    'juice', 'smoothie', 'shake', 'lemonade',
    'coconut water', 'soda', 'cola',
  ]},
  // Side (salad, raita, soup, curd)
  { type: 'side', kw: ['curd', 'dahi', 'raita', 'pickle', 'achaar', 'papad', 'salad', 'coleslaw', 'yogurt', 'greek yogurt', 'soup', 'broth'] },
  // Fruit
  { type: 'fruit', kw: ['banana', 'apple', 'orange', 'mango', 'papaya', 'guava', 'watermelon', 'pineapple', 'grapes', 'strawberry', 'blueberry', 'kiwi', 'pomegranate', 'lychee', 'cherry', 'peach', 'pear', 'plum', 'avocado', 'dates', 'raisins', 'fig', 'mixed fruit', 'fruit salad', 'fruit'] },
  // Nut
  { type: 'nut', kw: ['almond', 'cashew', 'walnut', 'pistachio', 'pecan', 'peanut', 'groundnut', 'trail mix', 'mixed nuts', 'chia seed', 'flaxseed', 'sunflower seed', 'pumpkin seed'] },
  // Condiment
  { type: 'condiment', kw: ['butter', 'ghee', 'oil', 'olive oil', 'coconut oil', 'sauce', 'soy sauce', 'ketchup', 'mustard', 'mayonnaise', 'chutney', 'dressing', 'honey', 'syrup', 'jam', 'hummus', 'guacamole', 'sugar', 'peanut butter', 'nutella', 'cream cheese', 'sour cream'] },
];

// ======================== FOOD GROUP (duplicate prevention) ========================
const FOOD_GROUPS = [
  { group: 'milk', keywords: ['milk', 'skim milk', 'whole milk', '2% milk', 'low fat milk', 'almond milk', 'soy milk', 'oat milk'] },
  { group: 'yogurt', keywords: ['yogurt', 'greek yogurt', 'plain yogurt', 'curd', 'dahi'] },
  { group: 'egg', keywords: ['egg', 'boiled egg', 'fried egg', 'poached egg', 'scrambled egg'] },
  { group: 'omelette', keywords: ['omelette', 'omelet', 'egg bhurji'] },
  { group: 'rice', keywords: ['rice', 'steamed rice', 'basmati', 'brown rice', 'white rice', 'jeera rice', 'jasmine rice'] },
  { group: 'roti', keywords: ['roti', 'chapati', 'naan', 'bajra roti', 'jowar roti', 'makki roti', 'pita', 'tortilla'] },
  { group: 'paratha', keywords: ['paratha', 'aloo paratha', 'gobhi paratha', 'pyaaz paratha', 'methi paratha', 'paneer paratha'] },
  { group: 'dal', keywords: ['dal', 'daal', 'arhar dal', 'masoor dal', 'moong dal', 'urad dal', 'toor dal', 'chana dal'] },
  { group: 'paneer', keywords: ['paneer', 'palak paneer', 'shahi paneer', 'matar paneer', 'paneer butter masala', 'chilli paneer', 'paneer bhurji', 'paneer tikka'] },
  { group: 'chicken', keywords: ['chicken', 'butter chicken', 'chicken tikka', 'tandoori chicken', 'chicken curry', 'grilled chicken'] },
  { group: 'pasta', keywords: ['pasta', 'spaghetti', 'penne', 'fettuccine', 'macaroni', 'red sauce pasta', 'white sauce pasta'] },
  { group: 'biryani', keywords: ['biryani', 'chicken biryani', 'vegetable biryani', 'mutton biryani', 'egg biryani'] },
  { group: 'oats', keywords: ['oats', 'oatmeal', 'vegetable oats', 'masala oats'] },
  { group: 'dosa', keywords: ['dosa', 'masala dosa', 'plain dosa', 'rava dosa', 'onion dosa'] },
  { group: 'idli', keywords: ['idli', 'rava idli'] },
  { group: 'tea_coffee', keywords: ['tea', 'chai', 'masala chai', 'green tea', 'coffee', 'espresso', 'latte', 'cappuccino'] },
  { group: 'buttermilk', keywords: ['buttermilk', 'chaas'] },
  { group: 'lassi', keywords: ['lassi', 'mango lassi', 'sweet lassi', 'salt lassi'] },
  { group: 'rajma', keywords: ['rajma', 'rajma masala', 'rajma chawal'] },
  { group: 'chole', keywords: ['chole', 'chhole', 'chana masala', 'chana'] },
  { group: 'samosa', keywords: ['samosa', 'baked samosa'] },
  { group: 'raita', keywords: ['raita'] },
  { group: 'salad', keywords: ['salad'] },
];

// ======================== SERVING SIZES ========================
const SERVING_RULES = [
  // Multi-word specifics first
  { keywords: ['butter chicken', 'butter paneer', 'butter naan', 'butter roti', 'butter masala'], serving: 200 },
  { keywords: ['peanut butter sandwich', 'almond butter toast'], serving: 200 },
  { keywords: ['cream of mushroom', 'cream of chicken', 'cream of tomato'], serving: 245 },
  { keywords: ['ice cream'], serving: 132 },
  { keywords: ['sour cream'], serving: 30 },
  { keywords: ['cream cheese'], serving: 28 },
  { keywords: ['peanut butter', 'almond butter', 'nutella', 'tahini'], serving: 32 },
  { keywords: ['butter', 'ghee', 'margarine'], serving: 14 },
  { keywords: ['oil', 'olive oil', 'coconut oil', 'sesame oil'], serving: 14 },
  { keywords: ['sauce', 'soy sauce', 'fish sauce', 'hot sauce'], serving: 15 },
  { keywords: ['ketchup', 'mustard', 'mayonnaise', 'salsa', 'chutney'], serving: 30 },
  { keywords: ['honey', 'syrup', 'jam', 'jelly'], serving: 21 },
  { keywords: ['sugar', 'brown sugar'], serving: 12 },
  // Drinks — all in ml, capped to 1 glass (250ml)
  { keywords: ['milk', 'almond milk', 'soy milk', 'oat milk'], serving: 250 },
  { keywords: ['buttermilk', 'lassi', 'chaas'], serving: 250 },
  { keywords: ['masala chai', 'tea', 'coffee', 'espresso'], serving: 200 },
  { keywords: ['juice', 'smoothie', 'shake', 'lemonade', 'coconut water'], serving: 250 },
  // Dairy/sides
  { keywords: ['yogurt', 'curd', 'dahi'], serving: 150 },
  { keywords: ['raita'], serving: 100 },
  { keywords: ['cheese', 'cheddar', 'mozzarella', 'parmesan', 'feta'], serving: 28 },
  // Fruits
  { keywords: ['banana', 'apple', 'orange', 'pear', 'peach', 'plum', 'mango', 'papaya', 'guava'], serving: 150 },
  { keywords: ['strawberr', 'blueberr', 'raspberr', 'grape', 'cherry'], serving: 140 },
  { keywords: ['watermelon', 'melon'], serving: 200 },
  { keywords: ['pineapple', 'kiwi'], serving: 165 },
  { keywords: ['avocado'], serving: 75 },
  { keywords: ['dates', 'raisins', 'dried'], serving: 40 },
  { keywords: ['mixed fruit'], serving: 200 },
  // Nuts/seeds
  { keywords: ['almond', 'cashew', 'walnut', 'pistachio', 'pecan', 'macadamia'], serving: 28 },
  { keywords: ['peanut', 'groundnut', 'trail mix', 'mixed nuts'], serving: 28 },
  { keywords: ['chia seed', 'flaxseed', 'sunflower seed', 'pumpkin seed'], serving: 28 },
  // Eggs
  { keywords: ['egg', 'boiled egg', 'fried egg'], serving: 50 },
  { keywords: ['omelette', 'omelet', 'egg bhurji', 'scrambled egg'], serving: 150 },
  // Staples
  { keywords: ['rice', 'steamed rice', 'basmati', 'brown rice', 'jeera rice'], serving: 200 },
  { keywords: ['roti', 'chapati', 'naan', 'tortilla', 'pita'], serving: 80 },
  { keywords: ['paratha', 'thepla', 'kulcha', 'bhatura'], serving: 80 },
  { keywords: ['oats', 'oatmeal', 'porridge', 'muesli', 'granola'], serving: 40 },
  { keywords: ['cereal', 'cornflakes'], serving: 30 },
  // Indian vegetables/curries
  { keywords: ['lauki', 'tori', 'patta gobi', 'bhindi', 'gobhi', 'palak', 'methi aloo', 'aloo beans', 'aloo gobi', 'baingan', 'karela'], serving: 200 },
  { keywords: ['dal', 'daal', 'lentil', 'rajma', 'chole', 'chhole', 'chana', 'sambar', 'rasam', 'kadhi'], serving: 200 },
  { keywords: ['curry', 'masala', 'korma', 'vindaloo', 'kofta', 'soya chunk'], serving: 200 },
  { keywords: ['paneer', 'matar paneer', 'palak paneer', 'shahi paneer', 'chilli paneer', 'paneer bhurji'], serving: 200 },
  // Rice dishes
  { keywords: ['biryani', 'pulao', 'khichdi', 'tehri', 'fried rice', 'rajma chawal'], serving: 250 },
  // Snacks
  { keywords: ['samosa', 'kachori', 'pakoda', 'pakora', 'vada'], serving: 100 },
  { keywords: ['dosa', 'uttapam', 'cheela', 'dhokla', 'idli'], serving: 120 },
  { keywords: ['chaat', 'bhelpuri', 'sevpuri', 'pani puri'], serving: 150 },
  { keywords: ['chips', 'fries', 'french fries'], serving: 85 },
  { keywords: ['popcorn'], serving: 28 },
  { keywords: ['cookie', 'biscuit', 'cracker'], serving: 30 },
  // Pasta/noodles
  { keywords: ['pasta', 'spaghetti', 'penne', 'fettuccine', 'macaroni', 'lasagna'], serving: 250 },
  { keywords: ['noodles', 'chow mein', 'lo mein', 'ramen', 'pad thai'], serving: 250 },
  // Western
  { keywords: ['pizza'], serving: 107 },
  { keywords: ['burger', 'sandwich', 'wrap', 'sub', 'club'], serving: 200 },
  { keywords: ['taco', 'burrito', 'quesadilla', 'enchilada'], serving: 200 },
  { keywords: ['soup', 'stew', 'broth'], serving: 245 },
  // Desserts
  { keywords: ['gulab jamun', 'rasgulla', 'jalebi', 'barfi', 'ladoo', 'laddu'], serving: 50 },
  { keywords: ['halwa', 'kheer', 'payasam', 'pudding', 'custard'], serving: 120 },
  { keywords: ['cake', 'pastry', 'brownie', 'muffin', 'donut'], serving: 75 },
  { keywords: ['chocolate'], serving: 40 },
  // Meats
  { keywords: ['chicken', 'turkey'], serving: 140 },
  { keywords: ['beef', 'steak', 'lamb', 'mutton', 'pork'], serving: 140 },
  { keywords: ['fish', 'salmon', 'tuna', 'cod', 'tilapia'], serving: 140 },
  { keywords: ['shrimp', 'prawn', 'crab', 'lobster'], serving: 140 },
  { keywords: ['sausage', 'hot dog', 'bacon', 'ham'], serving: 56 },
  { keywords: ['tofu', 'tempeh', 'seitan'], serving: 120 },
  // Misc
  { keywords: ['salad'], serving: 200 },
  { keywords: ['bean', 'kidney bean', 'black bean', 'chickpea'], serving: 130 },
  { keywords: ['pav bhaji'], serving: 300 },
  { keywords: ['bajra roti', 'jowar roti', 'makki roti'], serving: 60 },
];

// ======================== HELPERS ========================

function matchesAny(name, keywords) {
  return keywords.some(kw => {
    const regex = new RegExp('(^|\\s|-)' + kw.toLowerCase().replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '($|\\s|-|,)', 'i');
    return regex.test(name);
  });
}

function getCuisine(name) {
  for (const rule of CUISINE_RULES) {
    if (matchesAny(name, rule.kw)) return rule.cuisine;
  }
  return 'UNIVERSAL';
}

function getDishType(name, category) {
  for (const rule of TYPE_RULES) {
    if (matchesAny(name, rule.kw)) return rule.type;
  }
  // Category-based fallback
  const cat = (category || '').toLowerCase();
  if (cat === 'indian') return 'curry';
  if (cat === 'breakfast') return 'breakfast_item';
  if (cat === 'snacks') return 'snack';
  if (cat === 'desserts') return 'dessert';
  if (cat === 'beverages') return 'drink';
  if (cat === 'fruits') return 'fruit';
  if (cat === 'soups') return 'side';
  return 'ingredient';
}

function getMealTime(dishType) {
  switch (dishType) {
    case 'breakfast_item': return 'BREAKFAST';
    case 'snack': case 'fruit': case 'nut': case 'dessert': return 'SNACK';
    case 'curry': case 'dal': case 'sabzi': case 'rice_dish': case 'pasta':
    case 'complete_meal': case 'staple': case 'egg_dish': case 'meat_dish':
      return 'LUNCH';
    case 'drink': case 'side': return 'SNACK';
    default: return null;
  }
}

function getFoodGroup(name) {
  const nameLower = name.toLowerCase();
  for (const fg of FOOD_GROUPS) {
    if (fg.keywords.some(kw => nameLower === kw.toLowerCase())) return fg.group;
  }
  for (const fg of FOOD_GROUPS) {
    if (fg.keywords.some(kw => nameLower.includes(kw.toLowerCase()))) return fg.group;
  }
  return name.toLowerCase().replace(/\s+/g, '_');
}

function getServingSize(name) {
  const nameLower = name.toLowerCase();
  for (const rule of SERVING_RULES) {
    if (rule.keywords.some(kw => nameLower.includes(kw.toLowerCase()))) return rule.serving;
  }
  return null; // will use category fallback
}

// ======================== PROCESS EACH ITEM ========================

for (const item of data) {
  const nameLower = item.name.toLowerCase();
  
  // 1. Cuisine
  item.cuisine = getCuisine(nameLower);
  
  // 2. Dish Type
  item.dishType = getDishType(nameLower, item.category);
  
  // 3. Food Group
  item.foodGroup = getFoodGroup(item.name);
  
  // 4. isMeal
  item.isMeal = !['ingredient', 'condiment', 'drink', 'side', 'fruit', 'nut', 'staple'].includes(item.dishType);
  
  // 5. Meal Time
  item.mealType = getMealTime(item.dishType);
  
  // 6. Diet (EGGETARIAN tagging)
  const EGG_KW = ['egg', 'omelette', 'omelet', 'scrambled', 'bhurji', 'boiled egg'];
  if (item.dietaryCategory === 'NON_VEGETARIAN' || item.dietaryCategory === 'VEGETARIAN') {
    const isEgg = EGG_KW.some(kw => {
      const re = new RegExp('(^|\\s|-)' + kw + '($|\\s|-|,)', 'i');
      return re.test(nameLower);
    });
    if (isEgg && !nameLower.includes('chicken') && !nameLower.includes('mutton') 
        && !nameLower.includes('fish') && !nameLower.includes('turkey')
        && !nameLower.includes('beef') && !nameLower.includes('pork')
        && !nameLower.includes('eggplant')) {
      item.dietaryCategory = 'EGGETARIAN';
    }
  }
  
  // 7. Serving size
  let serving = getServingSize(item.name);
  if (!serving) {
    const cat = (item.category || '').toLowerCase();
    if (cat === 'condiments') serving = 15;
    else if (cat === 'dairy') serving = 150;
    else if (cat === 'fruits') serving = 150;
    else if (cat === 'beverages') serving = 250;
    else if (cat === 'snacks' || cat === 'desserts') serving = 100;
    else if (['indian', 'chinese', 'japanese', 'thai', 'italian', 'mexican', 'korean'].includes(cat)) serving = 200;
    else if (cat === 'meat' || cat === 'seafood') serving = 140;
    else if (cat === 'vegetables') serving = 150;
    else if (cat === 'grains') serving = 100;
    else if (cat === 'legumes') serving = 200;
    else if (cat === 'soups') serving = 245;
    else if (cat === 'breakfast') serving = 200;
    else serving = 100;
  }
  item.recommendedServingG = serving;
  
  // 8. Drink cap: max 250ml (1 glass)
  if (item.dishType === 'drink') {
    item.recommendedServingG = Math.min(250, Math.max(150, item.recommendedServingG));
  }
}

// ======================== WRITE ========================
fs.writeFileSync(DB_PATH, JSON.stringify(data, null, 2));

// ======================== STATS ========================
const types = {};
data.forEach(d => { types[d.dishType] = (types[d.dishType] || 0) + 1; });
const cuisines = {};
data.forEach(d => { cuisines[d.cuisine] = (cuisines[d.cuisine] || 0) + 1; });

console.log(`\n=== Classification Complete ===`);
console.log(`Total items: ${data.length}`);

console.log(`\n── Cuisine Distribution ──`);
Object.entries(cuisines).sort((a,b) => b[1]-a[1]).forEach(([c, n]) => console.log(`  ${c}: ${n}`));

console.log(`\n── Dish Type Distribution ──`);
Object.entries(types).sort((a,b) => b[1]-a[1]).forEach(([t, n]) => {
  const examples = data.filter(d => d.dishType === t).slice(0,3).map(d => d.name).join(', ');
  console.log(`  ${t}: ${n}  (${examples})`);
});

console.log(`\n── Sample Indian Curries ──`);
data.filter(d => d.cuisine === 'INDIAN' && d.dishType === 'curry').slice(0,5).forEach(d => console.log(`  🍛 ${d.name}`));
console.log(`\n── Sample Indian Dal ──`);
data.filter(d => d.dishType === 'dal').slice(0,5).forEach(d => console.log(`  🥘 ${d.name}`));
console.log(`\n── Sample Indian Sabzi ──`);
data.filter(d => d.dishType === 'sabzi').slice(0,5).forEach(d => console.log(`  🥗 ${d.name}`));
console.log(`\n── Sample Drinks (ml) ──`);
data.filter(d => d.dishType === 'drink').slice(0,8).forEach(d => console.log(`  🥛 ${d.name}: ${d.recommendedServingG}ml`));
