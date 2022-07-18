package de.ancash.fancycrafting.gui.handler;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.recipe.IRecipe;

public interface IRecipePermissionHandler {

	public boolean canCraftRecipe(IRecipe recipe, Player player);

	public void onNoPermission(IRecipe recipe, Player player);
}