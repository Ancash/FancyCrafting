package de.ancash.fancycrafting.gui.manage.normal;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.AbstractRecipeViewGUI;
import de.ancash.fancycrafting.recipe.IRecipe;

public class ViewNormalRecipeGUI extends AbstractRecipeViewGUI {

	public ViewNormalRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getViewRecipeTitle());
	}

	public ViewNormalRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(pl, player, recipe);
	}

	@Override
	protected void onMainMenuOpen() {

	}

	@Override
	protected void editRecipe(Player player, IRecipe recipe) {
		new EditNormalRecipeGUI(plugin, player, recipe).open();
	}
}