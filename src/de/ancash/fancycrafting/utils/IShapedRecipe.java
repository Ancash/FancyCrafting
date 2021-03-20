package de.ancash.fancycrafting.utils;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import de.ancash.ilibrary.datastructures.maps.CompactMap;

public class IShapedRecipe extends IRecipe implements Cloneable{

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(!(obj instanceof IShapedRecipe)) return false;
		return IRecipe.isSimilar(result, ((ShapedRecipe)obj).getResult(), false) &&
				IRecipe.matchesShaped(ingredientsMap, ((IShapedRecipe) obj).getIngredientsMap(), false);
	}
	
	@Override
	public IShapedRecipe clone() {
		return new IShapedRecipe(ingredientsMap, result);
	}
	
	private final CompactMap<Integer, ItemStack> ingredientsMap;
	private final ItemStack result;
	
	private IShapedRecipe(CompactMap<Integer, ItemStack> ingredientsMap, ItemStack result) {
		super(result, ingredientsMap.values());
		this.ingredientsMap = ingredientsMap;
		this.result = result;
	}
	
	public IShapedRecipe(ItemStack result, Map<Character, ItemStack> ingredientsMap, String[] shapes) {
		super(result, ingredientsMap.values());
		this.result = result;
		CompactMap<Integer, ItemStack> ings = new CompactMap<Integer, ItemStack>();
		int row = 0;
		for(String shape : shapes) {
			int charCnt = 1;
			for(char c : shape.toCharArray()) {
				ings.put(row * 3 + charCnt, ingredientsMap.get(c));
				charCnt++;
			}
			row++;
		}
		this.ingredientsMap = ings;
		optimize(this.ingredientsMap);
	}

	public CompactMap<Integer, ItemStack> getIngredientsMap() {
		return ingredientsMap;
	}
	
	@Override
	public ItemStack getResult() {
		return result;
	}
	
	@Override
	public String toString() {
		return "result=" + result + ", " + ingredientsMap;
	}

	@Override
	public int getIngredientsSize() {
		return ingredientsMap.size();
	}
}