package de.ancash.fancycrafting.gui.handler;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.recipe.IRecipe;

public interface IRecipeMatchHandler {

	public void onRecipeMatch();

	public void onNoRecipeMatch();

	public ItemStack getSingleRecipeCraft(IRecipe recipe, Player player);
}