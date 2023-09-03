package de.ancash.fancycrafting.gui.handler;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.AbstractCraftingWorkspace;
import de.ancash.fancycrafting.recipe.IRecipe;

public class RecipePermissionHandler {

	private final AbstractCraftingWorkspace workspace;

	public RecipePermissionHandler(AbstractCraftingWorkspace workspace) {
		this.workspace = workspace;
	}

	public boolean canCraftRecipe(IRecipe recipe, Player player) {
		return FancyCrafting.canCraftRecipe(recipe, player);
	}

	public void onNoPermission(IRecipe recipe, Player p) {
		workspace.getRecipeMatchCompletionHandler().onNoRecipeMatch();
	}
}