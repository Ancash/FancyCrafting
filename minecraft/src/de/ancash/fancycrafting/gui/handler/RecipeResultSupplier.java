package de.ancash.fancycrafting.gui.handler;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;

public class RecipeResultSupplier {

	public ItemStack getSingleRecipeCraft(IRecipe recipe, Player player) {
		if (recipe instanceof IRandomRecipe) {
			return ((IRandomRecipe) recipe).getRandom().toItem();
		} else if (recipe instanceof IShapedRecipe) {
			return ((IShapedRecipe) recipe).getResult();
		} else if (recipe instanceof IShapelessRecipe) {
			return ((IShapelessRecipe) recipe).getResult();
		}
		throw new IllegalArgumentException("Unknown recipe impl: " + recipe.getClass()); //$NON-NLS-1$
	}
}