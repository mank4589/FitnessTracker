const fs = require('fs');
const filePath = 'd:\\New folder\\ufit\\src\\main\\resources\\food-database.json';
const data = JSON.parse(fs.readFileSync(filePath, 'utf8'));

const newItems = [
  { name: "moong dal cheela", calories: 151, p: 6.2, c: 19.3, f: 5.1, fiber: 3.8, diet: "VEGAN", cat: "indian" },
  { name: "mixed sprouts chaat", calories: 105, p: 7.5, c: 16.2, f: 1.2, fiber: 4.0, diet: "VEGAN", cat: "indian" },
  { name: "besan cheela", calories: 165, p: 7.1, c: 21, f: 5.8, fiber: 4.0, diet: "VEGAN", cat: "indian" },
  { name: "matar paneer", calories: 178, p: 8.5, c: 12, f: 11, fiber: 3.0, diet: "VEGETARIAN", cat: "indian" },
  { name: "malai kofta", calories: 260, p: 6.5, c: 19, f: 18, fiber: 3.0, diet: "VEGETARIAN", cat: "indian" },
  { name: "chilli paneer", calories: 245, p: 10, c: 15, f: 16, fiber: 2.0, diet: "VEGETARIAN", cat: "indian" },
  { name: "paneer bhurji", calories: 290, p: 15, c: 8, f: 22, fiber: 1.0, diet: "VEGETARIAN", cat: "indian" },
  { name: "pakoda", calories: 260, p: 5, c: 22, f: 16, fiber: 3.0, diet: "VEGAN", cat: "indian" },
  { name: "paneer pakoda", calories: 310, p: 12, c: 18, f: 21, fiber: 2.0, diet: "VEGETARIAN", cat: "indian" },
  { name: "paneer paratha", calories: 280, p: 10, c: 32, f: 12, fiber: 4.0, diet: "VEGETARIAN", cat: "indian" },
  { name: "gobhi paratha", calories: 220, p: 5, c: 35, f: 7, fiber: 5.0, diet: "VEGETARIAN", cat: "indian" },
  { name: "pyaaz paratha", calories: 230, p: 5.5, c: 36, f: 7.5, fiber: 4.5, diet: "VEGETARIAN", cat: "indian" },
  { name: "curd", calories: 98, p: 11, c: 3.4, f: 4.3, fiber: 0, diet: "VEGETARIAN", cat: "dairy" },
  { name: "bajra roti", calories: 320, p: 10, c: 65, f: 3.5, fiber: 10, diet: "VEGAN", cat: "indian" },
  { name: "soya chunk curry", calories: 120, p: 12, c: 11, f: 3, fiber: 4.0, diet: "VEGAN", cat: "indian" },
  { name: "arhar dal", calories: 105, p: 6, c: 18, f: 1, fiber: 5.0, diet: "VEGAN", cat: "indian" },
  { name: "masoor dal", calories: 114, p: 7, c: 19, f: 1, fiber: 6.0, diet: "VEGAN", cat: "indian" },
  { name: "moong dal", calories: 104, p: 7, c: 18, f: 1, fiber: 5.0, diet: "VEGAN", cat: "indian" },
  { name: "aloo beans", calories: 85, p: 2, c: 12, f: 3, fiber: 3.0, diet: "VEGAN", cat: "indian" },
  { name: "red sauce pasta", calories: 130, p: 4, c: 22, f: 3, fiber: 2.0, diet: "VEGAN", cat: "italian" },
  { name: "white sauce pasta", calories: 180, p: 5, c: 20, f: 9, fiber: 1.0, diet: "VEGETARIAN", cat: "italian" },
  { name: "jave", calories: 140, p: 3, c: 25, f: 3, fiber: 2.0, diet: "VEGAN", cat: "indian" },
  { name: "buttermilk", calories: 40, p: 2, c: 4, f: 1.5, fiber: 0, diet: "VEGETARIAN", cat: "dairy" },
  { name: "vegetable oats", calories: 75, p: 3, c: 12, f: 1.5, fiber: 2.0, diet: "VEGAN", cat: "breakfast" },
  { name: "methi aloo", calories: 110, p: 3, c: 15, f: 4, fiber: 3.0, diet: "VEGAN", cat: "indian" },
  { name: "methi paratha", calories: 230, p: 6, c: 34, f: 7, fiber: 5.0, diet: "VEGETARIAN", cat: "indian" },
  { name: "lauki", calories: 50, p: 1, c: 6, f: 2, fiber: 2.0, diet: "VEGAN", cat: "indian" },
  { name: "tori", calories: 55, p: 1, c: 7, f: 2, fiber: 2.0, diet: "VEGAN", cat: "indian" },
  { name: "patta gobi aloo", calories: 85, p: 2, c: 11, f: 4, fiber: 3.0, diet: "VEGAN", cat: "indian" }
];

let addedCount = 0;
const existingNames = new Set(data.map(d => d.name.toLowerCase()));
for (const entry of newItems) {
  if (!existingNames.has(entry.name.toLowerCase())) {
    data.push({
      name: entry.name,
      calories: entry.calories,
      serving_size_g: 100,
      fat_total_g: entry.f,
      fat_saturated_g: Math.max(0, parseFloat((entry.f * 0.3).toFixed(1))),
      protein_g: entry.p,
      sodium_mg: 150,
      potassium_mg: 100,
      cholesterol_mg: entry.diet === "VEGAN" ? 0 : 10,
      carbohydrates_total_g: entry.c,
      fiber_g: entry.fiber,
      sugar_g: 2.0,
      category: entry.cat,
      isMeal: false,
      mealType: null,
      dietaryCategory: entry.diet,
      satisfiesRestrictions: []
    });
    existingNames.add(entry.name.toLowerCase());
    addedCount++;
  }
}

fs.writeFileSync(filePath, JSON.stringify(data, null, 2));
console.log(`Successfully added ${addedCount} new dishes.`);
