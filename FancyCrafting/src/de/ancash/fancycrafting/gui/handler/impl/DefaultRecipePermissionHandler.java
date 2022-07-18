package de.ancash.fancycrafting.gui.handler.impl;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.AbstractCraftingWorkspace;
import de.ancash.fancycrafting.gui.handler.IRecipePermissionHandler;
import de.ancash.fancycrafting.recipe.IRecipe;

public class DefaultRecipePermissionHandler implements IRecipePermissionHandler {

	private final AbstractCraftingWorkspace workspace;
	
	public DefaultRecipePermissionHandler(AbstractCraftingWorkspace workspace) {
		this.workspace = workspace;
	}

	@Override
	public boolean canCraftRecipe(IRecipe recipe, Player player) {
		return FancyCrafting.canCraftRecipe(recipe, player);
	}

	@Override
	public void onNoPermission(IRecipe recipe, Player p) {
		workspace.getRecipeMatchCompletionHandler().onNoRecipeMatch();
	}

}