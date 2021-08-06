package de.ancash.fancycrafting.recipe;

import java.util.Collection;

import org.bukkit.inventory.ItemStack;

import de.ancash.minecraft.SerializableItemStack;

public class IShapelessRecipe extends IRecipe{
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(!(obj instanceof IShapelessRecipe)) return false;
		return serializableResult.equals(((IShapelessRecipe)obj).serializableResult) &&
				IRecipe.matchesShapeless(super.ingredients, ((IShapelessRecipe) obj).getIngredients());
	}
	
	public IShapelessRecipe(ItemStack result, Collection<SerializableItemStack> ingredients, String id) {
		super(result, ingredients, id);
	}

	@Override
	public ItemStack getResult() {
		return result.clone();
	}
}		