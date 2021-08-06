package de.ancash.fancycrafting.recipe;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import de.ancash.datastructures.tuples.Duplet;
import de.ancash.datastructures.tuples.Tuple;
import de.ancash.minecraft.SerializableItemStack;

public abstract class IRecipe {

	final ItemStack result;
	final SerializableItemStack serializableResult;
	final Collection<SerializableItemStack> ingredients;
	final String name;
	
	public IRecipe(ItemStack result, Collection<SerializableItemStack> ings, String name) {
		this.result = result;
		this.serializableResult = new SerializableItemStack(result);
		this.ingredients = Collections.unmodifiableCollection(ings);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public ItemStack getResult() {
		return result.clone();
	}
	
	public SerializableItemStack getSerializedResult() {
		return serializableResult;
	}
	
	public Collection<SerializableItemStack> getIngredients() {
		return ingredients;
	}
	
	public int getIngredientsSize() {
		return ingredients.size();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(!(obj instanceof IRecipe)) return false;
		return serializableResult.equals(((IRecipe) obj).serializableResult) &&
				IRecipe.matchesShapeless(Arrays.asList(ingredients.toArray(new SerializableItemStack[ingredients.size()])), 
						Arrays.asList(((IRecipe) obj).ingredients.toArray(new SerializableItemStack[((IRecipe) obj).ingredients.size()])));
	}
	
	public static boolean matchesShaped(Map<Integer, SerializableItemStack> ingredients, Map<Integer, SerializableItemStack> compareTo) {
		if(ingredients.size() != compareTo.size()) return false;
		if(!ingredients.keySet().equals(compareTo.keySet())) return false;
		if(ingredients.size() == 1 && ingredients.size() == 1) {
			if(ingredients.values().stream().findAny().get().equalsIgnoreAmount(compareTo.values().stream().findAny().get()))  {
				return true;
			} else
				return false;
		}
		int cnt = 0;
		for(Entry<Integer, SerializableItemStack> entry : ingredients.entrySet()) {
			SerializableItemStack ing = entry.getValue();
			SerializableItemStack compare = compareTo.get(entry.getKey());
			if((compare == null) != (ing == null)) return false; 
			if(SerializableItemStack.areSimilar(ing, compare) && compare.getAmount() >= ing.getAmount()) cnt++;
		}
		if(cnt != ingredients.size()) return false;
		return true;
	}
	
	public static boolean matchesShapeless(Collection<SerializableItemStack> ingredients, Collection<SerializableItemStack> compareTo) {
		if(ingredients.size() != compareTo.size()) return false;
		SerializableItemStack[] copyOfIngredients = ingredients.toArray(new SerializableItemStack[ingredients.size()]);
		SerializableItemStack[] copyOfCompare = compareTo.toArray(new SerializableItemStack[compareTo.size()]);
		
		if(copyOfIngredients.length == 1) {
			if(copyOfIngredients[0].equalsIgnoreAmount(copyOfCompare[0])) {
				return true;
			} else
				return false;

		}
		
		for(int i1 = 0; i1<copyOfCompare.length; i1++) {
			SerializableItemStack compare = copyOfCompare[i1];
			boolean matches = false;
			for(int i2 = 0; i2<copyOfIngredients.length ;i2++) {
				SerializableItemStack ing = copyOfIngredients[i2];
				if(ing == null) continue;
				if(SerializableItemStack.areSimilar(ing, compare) && compare.getAmount() >= ing.getAmount()) {
					matches = true;
					copyOfIngredients[i2] = null;
					break;
				}
			}
			if(!matches) return false;
		}
		return true;
	}
	
	public static Duplet<Integer, Integer> optimize(Map<Integer, SerializableItemStack> ingredientsMap) {
		for(Entry<Integer, SerializableItemStack> entry : ingredientsMap.entrySet()) {
			if(entry.getValue() == null) ingredientsMap.remove(entry.getKey(), entry.getValue());
		}
		Duplet<Integer, Integer> moves = Tuple.of(0, 0);
		while(canMoveToLeft(ingredientsMap)) {
			moveToLeft(ingredientsMap);
			moves.setFirst(moves.getFirst() + 1);
		}
		while(canMoveUp(ingredientsMap)) {
			moveUp(ingredientsMap);
			moves.setSecond(moves.getSecond() + 1);
		}
		return moves;
	}
	
	private static void moveUp(Map<Integer, SerializableItemStack> ingredientsMap) {
		for(int i = 4; i<=9; i++) {
			if(!ingredientsMap.containsKey(i)) continue;
			set(i, i - 3, ingredientsMap);
		}
	}
	
	private static void moveToLeft(Map<Integer, SerializableItemStack> ingredientsMap) {
		for(int a = 0; a<3; a++) 
			for(int b = 1; b<=3; b++) 
				set(a * 3 + b, a * 3 + b - 1, ingredientsMap);
	}
	
	public static void set(int a, int b, Map<Integer, SerializableItemStack> ingredientsMap) {
		SerializableItemStack temp = ingredientsMap.get(a);
		if(temp == null) return;
		ingredientsMap.put(b, temp);
		ingredientsMap.remove(a);
	}
	
	private static boolean canMoveUp(Map<Integer, SerializableItemStack> ingredientsMap) {
		if(ingredientsMap.size() == 0) return false;
		for(int i = 1; i<=3; i++) {
			if(ingredientsMap.get(i) != null) return false;
		}
		return true;
	}
	
	private static boolean canMoveToLeft(Map<Integer, SerializableItemStack> ingredientsMap) {
		if(ingredientsMap.size() == 0) return false;
		int moveableSlotsToLeft = 2;
		for(int a = 2; a>=0; a--) {
			for(int b = 2; b>0; b--) {
				if(ingredientsMap.get(a * 3 + b) != null) {
					if(moveableSlotsToLeft >= b) moveableSlotsToLeft = b - 1;
				}
			}
		}
		return moveableSlotsToLeft > 0;
	}
	
	public static Map<Integer, SerializableItemStack> toMap(Map<Character, ItemStack> ingredients, String[] shapes) {
		Map<Integer, SerializableItemStack> ings = new HashMap<Integer, SerializableItemStack>();
		int row = 0;
		for(String shape : shapes) {
			int charCnt = 1;
			for(char c : shape.toCharArray()) {
				if(ingredients.get(c) != null) 
					ings.put(row * 3 + charCnt, new SerializableItemStack(ingredients.get(c)));
				charCnt++;
			}
			row++;
		}
		return ings;
	}
	
	public static IRecipe toIRecipe(Recipe recipe) {
		IRecipe r = null;
		if(recipe.getResult().getType() == null || recipe.getResult().getType() == Material.AIR) return null;
		
		if(recipe instanceof ShapedRecipe) r = new IShapedRecipe(recipe.getResult(), ((ShapedRecipe)recipe).getIngredientMap(), ((ShapedRecipe) recipe).getShape());
		if(recipe instanceof ShapelessRecipe) r = new IShapelessRecipe(recipe.getResult(), ((ShapelessRecipe) recipe).getIngredientList().stream().map(SerializableItemStack::new).collect(Collectors.toList()), null);
		return r;
	}
}
