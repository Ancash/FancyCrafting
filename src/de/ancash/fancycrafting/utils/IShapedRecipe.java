package de.ancash.fancycrafting.utils;

import java.util.Map;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import de.ancash.ilibrary.datastructures.maps.CompactMap;
import de.ancash.ilibrary.minecraft.nbt.NBTItem;

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
		return new IShapedRecipe(ingredientsMap, result, null);
	}
	
	private final CompactMap<Integer, ItemStack> ingredientsMap;
	private final ItemStack result;
	
	public IShapedRecipe(CompactMap<Integer, ItemStack> ingredientsMap, ItemStack result, UUID id) {
		super(result, ingredientsMap.values());
		this.ingredientsMap = ingredientsMap;
		if(id != null) {
			NBTItem nbt = new NBTItem(result);
			nbt.setString("fancycrafting.id", id.toString());
			this.result = nbt.getItem();
		} else {
			this.result = result;
		}
	}
	
	public IShapedRecipe(ItemStack result, Map<Character, ItemStack> ingredientsMap, String[] shapes) {
		super(result, ingredientsMap.values());
		this.result = result;
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
	public String toString() {
		return "result=" + result + ", ingredients=" + ingredientsMap;
	}

	@Override
	public int getIngredientsSize() {
		return ingredientsMap.size();
	}
}