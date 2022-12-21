package de.ancash.fancycrafting.base.gui.handler;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.base.gui.AbstractCraftingWorkspace;
import de.ancash.fancycrafting.recipe.IRecipe;

public class RecipePermissionHandler {

	private final AbstractCraftingWorkspace workspace;

	public RecipePermissionHandler(AbstractCraftingWorkspace workspace) {
		this.workspace = workspace;
	}

	public boolean canCraftRecipe(IRecipe recipe, Player player) {
		return AbstractFancyCrafting.canCraftRecipe(recipe, player);
	}

	public void onNoPermission(IRecipe recipe, Player p) {
		workspace.getRecipeMatchCompletionHandler().onNoRecipeMatch();
	}
}