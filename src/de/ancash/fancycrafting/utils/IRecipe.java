package de.ancash.fancycrafting.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.datastructures.tuples.Duplet;
import de.ancash.ilibrary.datastructures.tuples.Tuple;

public abstract class IRecipe {

	private final ItemStack result;
	private final Collection<ItemStack> ings;
	
	public IRecipe(ItemStack result, Collection<ItemStack> ings) {
		this.result = result;
		this.ings = ings;
	}
	
	public ItemStack getResult() {
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(!(obj instanceof IRecipe)) return false;
		return IRecipe.isSimilar(result, ((IRecipe) obj).getResult(), false) &&
				IRecipe.matchesShapeless(Arrays.asList(ings.toArray(new ItemStack[ings.size()])), 
						Arrays.asList(((IRecipe) obj).ings.toArray(new ItemStack[((IRecipe) obj).ings.size()])), false);
	}
	
	@Override
	public String toString() {
		return result.toString();
	}
	
	public abstract int getIngredientsSize();
	
	public static boolean matchesShaped(CompactMap<Integer, ItemStack> a, CompactMap<Integer, ItemStack> b, boolean ignoreData) {
		if(a.size() != b.size()) return false;
		int cnt = 0;
		for(int key : a.keySet()) {
			ItemStack aIs = a.get(key);
			ItemStack bIs = b.get(key);
			if(isSimilar(aIs, bIs, ignoreData)) cnt++;
		}
		if(cnt != a.size()) return false;
		return true;
	}
	
	public static boolean matchesShapeless(List<ItemStack> a, Collection<ItemStack> b, boolean ignoreData) {
		if(a.size() != b.size()) return false;
		List<ItemStack> copyOfB = new ArrayList<ItemStack>();
		b.forEach(is -> {
			copyOfB.add(is.clone());
		});
		List<ItemStack> copyOfA = new ArrayList<ItemStack>();
		a.forEach(is -> {
			copyOfA.add(is.clone());
		});
		
		for(int i1 = 0; i1<a.size(); i1++) {
			ItemStack bIs = copyOfB.get(i1);
			boolean matches = false;
			for(int i2 = 0; i2<copyOfA.size(); i2++) {
				ItemStack aIs = copyOfA.get(i2);
				if(isSimilar(bIs, aIs, ignoreData)) {
					matches = true;
					copyOfA.remove(i2);
				}
			}
			if(!matches) return false;
		}
		return true;
	}
	
	public static Duplet<Integer, Integer> optimize(CompactMap<Integer, ItemStack> ingredientsMap) {
		for(int key : ingredientsMap.keySet()) {
			if(ingredientsMap.get(key) == null) ingredientsMap.remove(key);
		}
		Duplet<Integer, Integer> moves = Tuple.of(0, 0);
		boolean canMoveToLeft = canMoveToLeft(ingredientsMap);
		while(canMoveToLeft) {
			moveToLeft(ingredientsMap);
			moves.setFirst(moves.getFirst() + 1);
			canMoveToLeft = canMoveToLeft(ingredientsMap);
		}
		boolean canMoveUp = canMoveUp(ingredientsMap);
		while(canMoveUp) {
			moveUp(ingredientsMap);
			moves.setSecond(moves.getSecond() + 1);
			canMoveUp = canMoveUp(ingredientsMap);
		}
		return moves;
	}
	
	private static void moveUp(CompactMap<Integer, ItemStack> ingredientsMap) {
		for(int i = 4; i<=9; i++) {
			if(!ingredientsMap.containsKey(i)) continue;
			set(i, i - 3, ingredientsMap);
		}
	}
	
	private static void moveToLeft(CompactMap<Integer, ItemStack> ingredientsMap) {
		for(int a = 0; a<3; a++) {
			for(int b = 1; b<=3; b++) {
				set(a * 3 + b, a * 3 + b - 1, ingredientsMap);
			}
		}
	}
	
	public static void set(int a, int b, CompactMap<Integer, ItemStack> ingredientsMap) {
		ItemStack temp = ingredientsMap.get(a);
		if(temp == null) return;
		ingredientsMap.put(b, temp);
		ingredientsMap.remove(a);
	}
	
	private static boolean canMoveUp(CompactMap<Integer, ItemStack> ingredientsMap) {
		if(ingredientsMap.size() == 0) return false;
		for(int i = 1; i<=3; i++) {
			if(ingredientsMap.get(i) != null) return false;
		}
		return true;
	}
	
	private static boolean canMoveToLeft(CompactMap<Integer, ItemStack> ingredientsMap) {
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
	
	@SuppressWarnings("deprecation")
	public static boolean isSimilar(ItemStack a, ItemStack b, boolean ignoreData) {
		if(a.isSimilar(b)) return true;
		boolean bNull = b == null;
		boolean aNull = a == null;
		if(aNull != bNull) return false;
		if(!a.getType().equals(b.getType())) return false;
		Map<String, Object> bMap = new HashMap<String, Object>();
		b.getItemMeta().serialize().entrySet().forEach(entry ->{
			bMap.put(entry.getKey(), entry.getValue());
		});
		Map<String, Object> aMap = new HashMap<String, Object>();
		a.getItemMeta().serialize().entrySet().forEach(entry ->{
			aMap.put(entry.getKey(), entry.getValue());
		});
		
		String one = a.toString().split("\\{")[1].split(" x")[0];
		String two = b.toString().split("\\{")[1].split(" x")[0];
		if(!ignoreData && !one.equals(two)) {
			return false;
		}
		
		if(bMap.containsKey("Damage") && bMap.get("Damage").equals(32767)) bMap.remove("Damage");
		if(aMap.containsKey("Damage") && aMap.get("Damage").equals(32767)) aMap.remove("Damage");
		if(!aMap.equals(bMap)) {
			return false;
		}
		if(!ignoreData) {
			if(a.getData().getData() != -1 && b.getData().getData() != -1 && a.getData().getData() != b.getData().getData()) {
				return false;
			}
		}
		return true;
	}
	
	public static IRecipe toIRecipe(Recipe recipe) {
		if(recipe instanceof ShapedRecipe) return new IShapedRecipe(recipe.getResult(), ((ShapedRecipe)recipe).getIngredientMap(), ((ShapedRecipe) recipe).getShape());
		if(recipe instanceof ShapelessRecipe) return new IShapelessRecipe(recipe.getResult(), ((ShapelessRecipe) recipe).getIngredientList());
		return null;
	}
}
