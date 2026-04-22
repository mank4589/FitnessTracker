/**
 * fetch-foods.js
 * Fetches nutrition data from CalorieNinjas API and populates food-database.json
 * Run with: node fetch-foods.js
 */

const https = require('https');
const fs = require('fs');
const path = require('path');

const API_KEY = 'COW1H+etqXs6qRHUd6I1nA==fl6peHLFPk9IgrM2';
const FOOD_LIST_PATH = path.join(__dirname, 'src/main/resources/chicken biryani.txt');
const DB_PATH = path.join(__dirname, 'src/main/resources/food-database.json');
const BATCH_SIZE = 10;
const DELAY_MS = 650;  // ms between each API call

// ── Category inference ────────────────────────────────────────────────────────
const CATEGORY_MAP = [
  { keywords: ['biryani','tikka','masala','paneer','dal','dosa','idli','vada','samosa','pakora','chana','rajma','chole','roti','naan','paratha','puri','chapati','pulao','upma','poha','sambar','raita','halwa','kheer','ladoo','jalebi','gulab','lassi','chai','dhokla','chutney','palak','bharta','aloo','mutton curry','chicken curry','fish curry','egg curry','prawn','seekh','tandoori','rogan josh','pav bhaji','dal fry','dal makhani','dal tadka','dum aloo','kachori','kadai paneer','kadhi','khichdi','pani puri','paneer tikka','chole bhature','bhatura','bhindi','pongal','rasam','uttapam','payasam','puttu','avial','sambar','rasabali','rasgulla','sandesh','barfi','chikki','mysore pak','shrikhand','modak','basundi','sheer korma','ras malai','rabri','phirni','gajar','sheera','halwa','sohan','boondi','imarti','petha','ghevar','kaju','kalakand','singori','balu shahi','cham cham','ledikeni','lyangcha','malapua','misti doi','pantua','pithe','adhirasam','ariselu','besan','dharwad','double ka meetha','gavvalu','kuzhi','palathalikalu','poornalu','pootharekulu','qubani','unni','kajjikaya','anarsa','dudhi','gatta','ghooghra','handwo','kansar','khandvi','khakhra','laapsi','muthiya','patra','sabudana','sev','sukhdi','thalipeeth','undhiyu','vindaloo','lilva','thepla','khichu','farsi','khaman','churma','litti','makki','misi roti','navrattan','rajma chaval','shahi tukra','vegetable jalfrezi','biryani','pulao','lemon rice'], cat: 'indian' },
  { keywords: ['fried rice','chow mein','lo mein','spring roll','egg roll','wonton','dumpling','hot and sour','mapo tofu','beef and broccoli','mongolian beef','sesame chicken','cashew chicken','stir fry','bok choy','peking duck','char siu','rice noodles','sweet and sour','kung pao','general tso','orange chicken','chilli chicken'], cat: 'chinese' },
  { keywords: ['sushi','sashimi','california roll','nigiri','ramen','miso','udon','soba','tempura','gyoza','tonkatsu','katsu','onigiri','okonomiyaki','yakisoba','matcha','mochi','terry aki','teriyaki','edamame','gyudon','mochi'], cat: 'japanese' },
  { keywords: ['pad thai','green curry','red curry','tom yum','massaman','panang','pad see ew','satay','thai basil','basil chicken','coconut soup','mango sticky rice'], cat: 'thai' },
  { keywords: ['spaghetti','carbonara','fettuccine','alfredo','pizza','lasagna','ravioli','gnocchi','risotto','bruschetta','caprese','chicken parmesan','eggplant parmesan','tiramisu','panna cotta','gelato','minestrone','garlic bread','prosciutto','mozzarella','pasta','penne','macaroni'], cat: 'italian' },
  { keywords: ['taco','burrito','quesadilla','nachos','enchilada','churros','tres leches','refried beans','mexican rice','pico de gallo','salsa','tortilla chips','elote','tamale','guacamole'], cat: 'mexican' },
  { keywords: ['apple','banana','orange','strawberry','blueberry','raspberry','pineapple','watermelon','cantaloupe','honeydew','grapes','cherry','peach','plum','pear','kiwi','pomegranate','coconut','avocado','lemon','lime','grapefruit','tangerine','apricot','fig','dates','cranberry','blackberry','guava','passion fruit','lychee','dragon fruit','jackfruit','persimmon','mango','papaya','starfruit'], cat: 'fruits' },
  { keywords: ['broccoli','cauliflower','carrot','spinach','kale','tomato','cucumber','bell pepper','onion','garlic','potato','sweet potato','green beans','peas','zucchini','eggplant','cabbage','lettuce','celery','mushroom','asparagus','artichoke','brussels sprouts','beet','radish','turnip','parsnip','jalapeno','okra','leek','green onion','ginger','fennel','corn','mixed vegetable','bok choy','poriyal','kootu','keerai','avial'], cat: 'vegetables' },
  { keywords: ['chicken breast','chicken thigh','chicken wing','ground chicken','beef steak','ground beef','beef ribs','prime rib','pork chop','pork tenderloin','bacon','ham','sausage','lamb chop','ground lamb','turkey breast','ground turkey','veal','bison','pepperoni','salami','hot dog','corned beef','roast chicken','grilled chicken','fried chicken','beef burger','meatball','meat loaf','burger','seekh kebab','kofta','chicken tikka','beef jerky'], cat: 'meat' },
  { keywords: ['salmon','tuna','cod','tilapia','shrimp','lobster','crab','scallop','sardine','mackerel','trout','halibut','catfish','clam','mussel','oyster','calamari','octopus','fish and chips','smoked salmon','ceviche','fish stick','crab cake','prawn','grilled salmon','seafood','fish curry','fish taco'], cat: 'seafood' },
  { keywords: ['white rice','brown rice','basmati','jasmine rice','wild rice','quinoa','oats','steel cut oats','whole wheat bread','white bread','sourdough','rye bread','pasta','penne','macaroni','couscous','bulgur','barley','millet','flour tortilla','corn tortilla','pancake','waffle','bagel','croissant','english muffin','granola','cereal','naan','roti','paratha'], cat: 'grains' },
  { keywords: ['milk','whole milk','skim milk','almond milk','soy milk','cheddar','parmesan','cream cheese','swiss cheese','greek yogurt','plain yogurt','yogurt','butter','heavy cream','sour cream','whipped cream','cottage cheese','ricotta','mozzarella'], cat: 'dairy' },
  { keywords: ['scrambled eggs','fried egg','boiled egg','omelette','pancakes','waffles','oatmeal','toast','jam','breakfast sausage','hash browns','home fries','french toast','egg sandwich','biscuits','egg white'], cat: 'breakfast' },
  { keywords: ['potato chips','pretzels','popcorn','chocolate bar','dark chocolate','milk chocolate','granola bar','protein bar','energy bar','trail mix','mixed nuts','peanuts','almonds','cashews','walnuts','cheese crackers','graham crackers','rice cakes','french fries','onion rings','mozzarella sticks','dried fruit','raisins','cookie','brownie','donut','peanut butter','nutella','hummus'], cat: 'snacks' },
  { keywords: ['chocolate cake','vanilla cake','cheesecake','carrot cake','apple pie','pumpkin pie','cherry pie','pecan pie','chocolate chip cookie','sugar cookie','oatmeal cookie','blondie','creme brulee','chocolate mousse','pudding','ice cream','banana split','cinnamon roll','eclair','flan','baklava','tiramisu','gelato','mochi','tres leches','churros','gulab jamun','jalebi','kheer','halwa','ladoo'], cat: 'desserts' },
  { keywords: ['black coffee','latte','cappuccino','espresso','americano','green tea','black tea','chamomile','earl grey','orange juice','apple juice','cranberry juice','grape juice','coca cola','pepsi','sprite','ginger ale','lemonade','iced tea','sweet tea','protein shake','beer','wine','coconut water','sparkling water','energy drink','sports drink','lassi','chai','smoothie','milkshake','hot chocolate'], cat: 'beverages' },
  { keywords: ['black beans','kidney beans','pinto beans','navy beans','chickpeas','lentils','red lentils','split peas','black eyed peas','lima beans','baked beans','bean soup','tofu','tempeh','soy beans'], cat: 'legumes' },
  { keywords: ['ketchup','mustard','mayonnaise','soy sauce','sriracha','hot sauce','barbecue sauce','ranch dressing','italian dressing','caesar dressing','olive oil','vegetable oil','coconut oil','honey','maple syrup','brown sugar','salt','black pepper','vinegar','balsamic vinegar','tomato sauce','pesto sauce','alfredo sauce'], cat: 'condiments' },
  { keywords: ['chicken noodle soup','tomato soup','vegetable soup','clam chowder','broccoli cheddar soup','french onion soup','cream of mushroom','lentil soup','split pea soup','black bean soup','beef stew','chicken tortilla soup','potato soup','corn chowder','pho','minestrone','ramen','miso soup','wonton soup','tom yum','sambar','rasam'], cat: 'soups' },
];

function inferCategory(name) {
  const lower = name.toLowerCase();
  for (const { keywords, cat } of CATEGORY_MAP) {
    if (keywords.some(k => lower.includes(k))) return cat;
  }
  return 'other';
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

function fetchNutrition(query) {
  return new Promise((resolve, reject) => {
    const url = `https://api.calorieninjas.com/v1/nutrition?query=${encodeURIComponent(query)}`;
    https.get(url, { headers: { 'X-Api-Key': API_KEY } }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try { resolve(JSON.parse(data)); }
        catch (e) { reject(new Error(`JSON parse error for "${query}": ${data}`)); }
      });
    }).on('error', reject);
  });
}

function toDbEntry(apiItem, queryName) {
  return {
    name: queryName,
    calories: +(apiItem.calories || 0).toFixed(1),
    serving_size_g: +(apiItem.serving_size_g || 100).toFixed(1),
    fat_total_g: +(apiItem.fat_total_g || 0).toFixed(1),
    fat_saturated_g: +(apiItem.fat_saturated_g || 0).toFixed(1),
    protein_g: +(apiItem.protein_g || 0).toFixed(1),
    sodium_mg: +(apiItem.sodium_mg || 0).toFixed(0),
    potassium_mg: +(apiItem.potassium_mg || 0).toFixed(0),
    cholesterol_mg: +(apiItem.cholesterol_mg || 0).toFixed(0),
    carbohydrates_total_g: +(apiItem.carbohydrates_total_g || 0).toFixed(1),
    fiber_g: +(apiItem.fiber_g || 0).toFixed(1),
    sugar_g: +(apiItem.sugar_g || 0).toFixed(1),
    category: inferCategory(queryName),
    isMeal: false,
    mealType: null,
    dietaryCategory: 'NONE',
    satisfiesRestrictions: [],
  };
}

// ── Main ──────────────────────────────────────────────────────────────────────
async function main() {
  // 1. Load & deduplicate food list
  const rawList = fs.readFileSync(FOOD_LIST_PATH, 'utf8');
  const allItems = [...new Set(
    rawList.split(/\r?\n/).map(l => l.trim()).filter(l => l.length > 0)
  )];
  console.log(`Total unique items in list: ${allItems.length}`);

  // 2. Load existing DB
  let existing = [];
  try {
    const raw = fs.readFileSync(DB_PATH, 'utf8').trim();
    existing = raw && raw !== '[]' ? JSON.parse(raw) : [];
  } catch { existing = []; }
  const existingNames = new Set(existing.map(e => e.name.toLowerCase().trim()));
  console.log(`Existing DB entries: ${existing.length}`);

  // 3. Filter new items
  const toFetch = allItems.filter(item => !existingNames.has(item.toLowerCase().trim()));
  console.log(`Items to fetch: ${toFetch.length}\n`);

  if (toFetch.length === 0) {
    console.log('Nothing new to fetch. DB is already up to date!');
    return;
  }

  // 4. Fetch in batches
  const newEntries = [];
  const noResult = [];
  const failed = [];
  const totalBatches = Math.ceil(toFetch.length / BATCH_SIZE);

  for (let i = 0; i < toFetch.length; i += BATCH_SIZE) {
    const batch = toFetch.slice(i, i + BATCH_SIZE);
    const batchNum = Math.floor(i / BATCH_SIZE) + 1;
    process.stdout.write(`[${batchNum}/${totalBatches}] Fetching: ${batch.map(b => `"${b}"`).join(', ')} ... `);

    for (const foodName of batch) {
      try {
        const result = await fetchNutrition(foodName);
        if (result.items && result.items.length > 0) {
          newEntries.push(toDbEntry(result.items[0], foodName));
          process.stdout.write('✓');
        } else {
          noResult.push(foodName);
          process.stdout.write('?');
        }
      } catch (err) {
        failed.push(foodName);
        process.stdout.write('✗');
      }
      await sleep(DELAY_MS);
    }
    console.log(` (${newEntries.length} total so far)`);

    // Save progress after each batch (crash-safe)
    const snapshot = [...existing, ...newEntries];
    fs.writeFileSync(DB_PATH, JSON.stringify(snapshot, null, 2), 'utf8');
  }

  // 5. Final save
  const finalDb = [...existing, ...newEntries];
  fs.writeFileSync(DB_PATH, JSON.stringify(finalDb, null, 2), 'utf8');

  console.log(`\n✅ Done!`);
  console.log(`   Added:        ${newEntries.length} new entries`);
  console.log(`   Total in DB:  ${finalDb.length}`);
  if (noResult.length) console.log(`   No API data:  ${noResult.length} items → ${noResult.join(', ')}`);
  if (failed.length)   console.log(`   Errors:       ${failed.length} items → ${failed.join(', ')}`);
}

main().catch(err => { console.error('Fatal:', err); process.exit(1); });
