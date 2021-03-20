package de.ancash.fancycrafting.utils;

import java.util.List;

import org.bukkit.inventory.ItemStack;

public class IShapelessRecipe extends IRecipe{
	  	  
	private final List<ItemStack> ingredients;
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
		return new IShapelessRecipe(result, ingredients);
	}
	
	public IShapelessRecipe(ItemStack result, List<ItemStack> ingredients) {
		super(result, ingredients);
		this.ingredients = ingredients;
		this.result = result;
	}

	public List<ItemStack> getIngredientsList() {
		return ingredients;
	}
	
	@Override
	public ItemStack getResult() {
		return result;
	}

	@Override
	public String toString() {
		return "result=" + result + ", " + ingredients;
	}
	
	@Override
	public int getIngredientsSize() {
		return ingredients.size();
	}
	
	
}		