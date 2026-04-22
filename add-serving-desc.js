const fs = require('fs');
const path = require('path');

const DB_PATH = path.join(__dirname, 'src/main/resources/food-database.json');
const data = JSON.parse(fs.readFileSync(DB_PATH, 'utf8'));

// Rules for serving descriptions based on food name/category
const DESC_RULES = [
  // Rotis, naan, chapati (1 roti ~40g, paratha ~80g)
  { match: (n) => /\broti\b/i.test(n) && !/paratha/i.test(n), desc: (s) => `${Math.round(s/40)} roti${Math.round(s/40) > 1 ? 's' : ''}` },
  { match: (n) => /\bchapati\b/i.test(n), desc: (s) => `${Math.round(s/40)} chapati${Math.round(s/40) > 1 ? 's' : ''}` },
  { match: (n) => /\bnaan\b/i.test(n), desc: (s) => `${Math.round(s/80)} naan` },
  { match: (n) => /paratha|thepla|kulcha/i.test(n), desc: (s) => `${Math.round(s/80)} paratha${Math.round(s/80) > 1 ? 's' : ''}` },
  { match: (n) => /bhatura|bhature/i.test(n), desc: (s) => `${Math.round(s/80)} bhatura${Math.round(s/80) > 1 ? 's' : ''}` },
  
  // Eggs (~50g each)
  { match: (n) => /\begg\b/i.test(n) && !/omelette|omelet|egg curry|egg bhurji|egg fried|egg roll/i.test(n), desc: (s) => `${Math.round(s/50)} egg${Math.round(s/50) > 1 ? 's' : ''}` },
  { match: (n) => /boiled egg/i.test(n), desc: (s) => `${Math.round(s/50)} boiled egg${Math.round(s/50) > 1 ? 's' : ''}` },
  
  // Idli (~30g each)
  { match: (n) => /\bidli\b/i.test(n), desc: (s) => `${Math.round(s/30)} idli${Math.round(s/30) > 1 ? 's' : ''}` },
  
  // Dosa (~120g each)
  { match: (n) => /\bdosa\b/i.test(n), desc: () => `1 dosa` },
  
  // Samosa (~50g each)
  { match: (n) => /samosa/i.test(n), desc: (s) => `${Math.round(s/50)} samosa${Math.round(s/50) > 1 ? 's' : ''}` },
  
  // Sweets (1 piece ~25-50g)
  { match: (n) => /gulab jamun/i.test(n), desc: (s) => `${Math.round(s/25)} piece${Math.round(s/25) > 1 ? 's' : ''}` },
  { match: (n) => /rasgulla/i.test(n), desc: (s) => `${Math.round(s/40)} piece${Math.round(s/40) > 1 ? 's' : ''}` },
  { match: (n) => /jalebi/i.test(n), desc: (s) => `${Math.round(s/25)} piece${Math.round(s/25) > 1 ? 's' : ''}` },
  { match: (n) => /ladoo|laddu/i.test(n), desc: (s) => `${Math.round(s/25)} ladoo${Math.round(s/25) > 1 ? 's' : ''}` },
  { match: (n) => /barfi/i.test(n), desc: (s) => `${Math.round(s/25)} piece${Math.round(s/25) > 1 ? 's' : ''}` },
  
  // Pakoda/pakora (~20g each)
  { match: (n) => /pakod|pakora/i.test(n), desc: (s) => `${Math.round(s/20)} piece${Math.round(s/20) > 1 ? 's' : ''}` },
  
  // Vada (~40g each)
  { match: (n) => /\bvada\b/i.test(n), desc: (s) => `${Math.round(s/40)} vada${Math.round(s/40) > 1 ? 's' : ''}` },
  
  // Dhokla (~30g each)
  { match: (n) => /dhokla/i.test(n), desc: (s) => `${Math.round(s/30)} piece${Math.round(s/30) > 1 ? 's' : ''}` },
  
  // Cheela (1 cheela ~60g) 
  { match: (n) => /cheela/i.test(n), desc: (s) => `${Math.round(s/60)} cheela${Math.round(s/60) > 1 ? 's' : ''}` },
  
  // Beverages  
  { match: (n) => /milk|buttermilk|lassi|chai|tea|coffee|juice|smoothie|shake|lemonade/i.test(n), desc: () => '1 glass' },
  { match: (n) => /coconut water/i.test(n), desc: () => '1 glass' },
  
  // Yogurt / Curd
  { match: (n) => /yogurt|curd|dahi/i.test(n), desc: () => '1 bowl' },
  
  // Dal / curry / sabzi (1 bowl)
  { match: (n) => /\bdal\b|\bdaal\b|sambar|rasam|curry|masala|korma|kofta|kadhi|sabzi|subzi/i.test(n), desc: () => '1 bowl' },
  { match: (n) => /rajma|chole|chhole|chana\b/i.test(n), desc: () => '1 bowl' },
  { match: (n) => /paneer\b/i.test(n) && /bhurji|matar|palak|shahi|chilli|butter/i.test(n), desc: () => '1 bowl' },
  { match: (n) => /lauki|tori|patta gobi|bhindi|gobi|palak|methi aloo|aloo beans|aloo gobi|baingan|karela|soya chunk/i.test(n), desc: () => '1 bowl' },
  
  // Rice dishes (1 plate)
  { match: (n) => /biryani|pulao|khichdi|tehri|fried rice|rajma chawal/i.test(n), desc: () => '1 plate' },
  
  // Pasta / Noodles (1 plate)
  { match: (n) => /pasta|spaghetti|penne|noodles|chow mein|lo mein|ramen|pad thai/i.test(n), desc: () => '1 plate' },
  
  // Pizza (1 slice)
  { match: (n) => /pizza/i.test(n), desc: () => '1 slice' },
  
  // Burger / Sandwich / Wrap
  { match: (n) => /burger|sandwich|wrap|sub\b|club/i.test(n), desc: () => '1 piece' },
  
  // Soup (1 bowl)
  { match: (n) => /soup|stew|broth/i.test(n), desc: () => '1 bowl' },
  
  // Rice (1 bowl cooked)
  { match: (n) => /\brice\b/i.test(n) && !/biryani|pulao|fried rice/i.test(n), desc: () => '1 bowl' },
  
  // Bread / Toast (1-2 slices)
  { match: (n) => /bread|toast/i.test(n), desc: (s) => `${Math.round(s/30)} slice${Math.round(s/30) > 1 ? 's' : ''}` },
  
  // Oats / Oatmeal (1 bowl)
  { match: (n) => /oats|oatmeal|porridge/i.test(n), desc: () => '1 bowl' },
  
  // Fruits (1 medium / 1 cup)
  { match: (n) => /banana|apple|orange|pear|peach|plum|mango|papaya|guava/i.test(n), desc: () => '1 medium' },
  { match: (n) => /strawberr|blueberr|raspberr|grape|cherry/i.test(n), desc: () => '1 cup' },
  { match: (n) => /watermelon|melon/i.test(n), desc: () => '2 cups' },
  { match: (n) => /pineapple|kiwi/i.test(n), desc: () => '1 cup' },
  { match: (n) => /avocado/i.test(n), desc: () => '½ avocado' },
  { match: (n) => /coconut\b/i.test(n) && !/oil|water|milk|cream/i.test(n), desc: () => '¼ cup' },
  { match: (n) => /dates|raisins/i.test(n), desc: () => '¼ cup' },
  
  // Nuts / Seeds (1 handful)
  { match: (n) => /almond|cashew|walnut|pistachio|pecan|peanut|groundnut/i.test(n) && !/butter|milk/i.test(n), desc: () => '1 handful' },
  { match: (n) => /chia|flax|sunflower seed|pumpkin seed/i.test(n), desc: () => '1 tbsp' },
  
  // Cheese (1 slice)
  { match: (n) => /cheese|cheddar|mozzarella|parmesan|feta|swiss|gouda|brie/i.test(n) && !/cream cheese/i.test(n), desc: () => '1 slice' },
  
  // Butter / Spreads
  { match: (n) => /peanut butter|almond butter|nutella|tahini/i.test(n), desc: () => '2 tbsp' },
  { match: (n) => /\bbutter\b|ghee/i.test(n) && !/chicken|paneer|naan|roti|masala/i.test(n), desc: () => '1 tbsp' },
  
  // Oil
  { match: (n) => /\boil\b/i.test(n), desc: () => '1 tbsp' },
  
  // Honey / Syrup / Jam
  { match: (n) => /honey|syrup|jam|jelly/i.test(n), desc: () => '1 tbsp' },
  
  // Halwa / Kheer (1 bowl)
  { match: (n) => /halwa|kheer|payasam|pudding|custard/i.test(n), desc: () => '1 bowl' },
  
  // Ice cream
  { match: (n) => /ice cream|gelato/i.test(n), desc: () => '1 scoop' },
  
  // Meat portions
  { match: (n) => /chicken|turkey/i.test(n) && !/biryani|curry|masala|tikka|butter/i.test(n), desc: () => '1 portion' },
  { match: (n) => /fish|salmon|tuna|cod|tilapia/i.test(n) && !/curry|masala|fry/i.test(n), desc: () => '1 fillet' },
  { match: (n) => /steak|beef|lamb|mutton|pork/i.test(n) && !/curry|masala|stew/i.test(n), desc: () => '1 portion' },
  
  // Sausage / Bacon
  { match: (n) => /sausage/i.test(n), desc: (s) => `${Math.round(s/28)} piece${Math.round(s/28) > 1 ? 's' : ''}` },
  { match: (n) => /bacon/i.test(n), desc: (s) => `${Math.round(s/14)} strip${Math.round(s/14) > 1 ? 's' : ''}` },
  
  // Omelette
  { match: (n) => /omelette|omelet/i.test(n), desc: () => '1 omelette' },
  
  // Salad
  { match: (n) => /salad/i.test(n), desc: () => '1 bowl' },
  
  // Cereal / Granola
  { match: (n) => /cereal|granola|muesli|cornflakes/i.test(n), desc: () => '1 bowl' },
  
  // Jave
  { match: (n) => /\bjave\b/i.test(n), desc: () => '1 bowl' },
  
  // Chaat
  { match: (n) => /chaat/i.test(n), desc: () => '1 plate' },
  
  // Sushi
  { match: (n) => /sushi|maki/i.test(n), desc: () => '6 pieces' },
  
  // Pancake / Waffle
  { match: (n) => /pancake/i.test(n), desc: () => '2 pancakes' },
  { match: (n) => /waffle/i.test(n), desc: () => '1 waffle' },
  
  // Chips / Fries
  { match: (n) => /chips|fries/i.test(n), desc: () => '1 serving' },
  
  // Kachori
  { match: (n) => /kachori/i.test(n), desc: (s) => `${Math.round(s/50)} piece${Math.round(s/50) > 1 ? 's' : ''}` },
  
  // Popcorn
  { match: (n) => /popcorn/i.test(n), desc: () => '1 cup' },
];

let addedCount = 0;

for (const item of data) {
  const name = item.name;
  const serving = item.recommendedServingG || 100;
  
  let matched = false;
  for (const rule of DESC_RULES) {
    if (rule.match(name)) {
      item.servingDescription = rule.desc(serving);
      matched = true;
      addedCount++;
      break;
    }
  }
  
  if (!matched) {
    // Generic fallback based on serving size
    if (serving <= 30) item.servingDescription = '1 tbsp';
    else if (serving <= 60) item.servingDescription = '1 small serving';
    else if (serving <= 150) item.servingDescription = '1 serving';
    else if (serving <= 250) item.servingDescription = '1 bowl';
    else item.servingDescription = '1 plate';
  }
}

fs.writeFileSync(DB_PATH, JSON.stringify(data, null, 2));

console.log(`\n=== Serving Descriptions Added ===`);
console.log(`Matched by rules: ${addedCount}`);
console.log(`Total items: ${data.length}`);

// Show samples
console.log('\nSamples:');
data.filter(i => i.isMeal).slice(0, 15).forEach(i => 
  console.log(`  ${i.name}: ${i.servingDescription} (${i.recommendedServingG}g, ~${Math.round(i.calories * (i.recommendedServingG||100)/100)} cal)`)
);
