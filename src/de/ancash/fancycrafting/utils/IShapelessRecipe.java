package de.ancash.fancycrafting.utils;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import de.ancash.ilibrary.minecraft.nbt.NBTItem;

public class IShapelessRecipe extends IRecipe{
	  	  
	private final Collection<ItemStack> ingredients;
	private final ItemStack result;
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(!(obj instanceof IShapelessRecipe)) return false;
		return IRecipe.isSimilar(result, ((IShapelessRecipe)obj).getResult(), false) &&
				IRecipe.matchesShapeless(ingredients, ((IShapelessRecipe) obj).getIngredientsList(), false);
	}
	
	@Override
	public IShapelessRecipe clone() {
		return new IShapelessRecipe(result, ingredients, null);
	}
	
	public IShapelessRecipe(ItemStack result, Collection<ItemStack> ingredients, UUID id) {
		super(result, ingredients);
		this.ingredients = ImmutableList.copyOf(ingredients);;
		if(id != null) {
			NBTItem nbt = new NBTItem(result);
			nbt.setString("fancycrafting.id", id.toString());
			this.result = nbt.getItem();
		} else {
			this.result = result;
		}
	}

	public Collection<ItemStack> getIngredientsList() {
		return ingredients;
	}
	
	@Override
	public ItemStack getResult() {
		return result.clone();
	}

	@Override
	public String toString() {
		return "result=" + result + ", ingredients=" + ingredients;
	}
	
	@Override
	public int getIngredientsSize() {
		return ingredients.size();
	}
	
	
}		