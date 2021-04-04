package de.ancash.fancycrafting.utils;

import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.minecraft.nbt.NBTItem;

public class IShapedRecipe extends IRecipe implements Cloneable{

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(!(obj instanceof IShapedRecipe)) return false;
		return IRecipe.isSimilar(result, ((ShapedRecipe)obj).getResult(),false) &&
				IRecipe.matchesShaped(ingredientsMap, ((IShapedRecipe) obj).getIngredientsMap(), false);
	}
	
	@Override
	public IShapedRecipe clone() {
		return new IShapedRecipe(ingredientsMap, result, null);
	}
	
	private final CompactMap<Integer, ItemStack> ingredientsMap;
	private final ItemStack result;
	private final ItemStack resultWithId;
	
	public IShapedRecipe(CompactMap<Integer, ItemStack> ingredientsMap, ItemStack result, String id) {
		super(result, ingredientsMap.values(), id);
		this.ingredientsMap = ingredientsMap;
		this.result = result;
		if(id != null) {
			NBTItem nbt = new NBTItem(result);
			nbt.setString("fancycrafting.id", id);
			this.resultWithId = nbt.getItem();
		} else {
			this.resultWithId = result;
		}
	}
	
	public IShapedRecipe(ItemStack result, Map<Character, ItemStack> ingredientsMap, String[] shapes) {
		super(result, ingredientsMap.values(), null);
		this.result = result;
		this.resultWithId = result;
		this.ingredientsMap = toMap(ingredientsMap, shapes);
		optimize(this.ingredientsMap);
	}

	public CompactMap<Integer, ItemStack> getIngredientsMap() {
		return ingredientsMap;
	}
	
	@Override
	public ItemStack getResult() {
		return result.clone();
	}
	
	@Override
	public ItemStack getResultWithId() {
		return resultWithId;
	}
	
	@Override
	public String toString() {
		return "result=" + result + ", ingredients=" + ingredientsMap;
	}

	@Override
	public int getIngredientsSize() {
		return ingredientsMap.size();
	}
}