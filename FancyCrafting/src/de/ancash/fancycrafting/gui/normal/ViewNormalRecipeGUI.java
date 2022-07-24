package de.ancash.fancycrafting.gui.normal;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.AbstractViewRecipeGUI;
import de.ancash.fancycrafting.recipe.IRecipe;

public class ViewNormalRecipeGUI extends AbstractViewRecipeGUI {

	public ViewNormalRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getViewRecipeTitle());
	}

	public ViewNormalRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(pl, player, recipe);
	}

	@Override
	protected void onMainMenuOpen() {

	}
}