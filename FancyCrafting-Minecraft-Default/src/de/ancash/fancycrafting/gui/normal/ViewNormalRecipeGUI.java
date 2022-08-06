package de.ancash.fancycrafting.gui.normal;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.gui.base.AbstractRecipeViewGUI;
import de.ancash.fancycrafting.recipe.IRecipe;

public class ViewNormalRecipeGUI extends AbstractRecipeViewGUI {

	public ViewNormalRecipeGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getViewRecipeTitle());
	}

	public ViewNormalRecipeGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(pl, player, recipe);
	}

	@Override
	protected void onMainMenuOpen() {

	}

	@Override
	public void editRecipe(Player player, IRecipe recipe) {
		new EditNormalRecipeGUI(plugin, player, recipe).open();
	}
}