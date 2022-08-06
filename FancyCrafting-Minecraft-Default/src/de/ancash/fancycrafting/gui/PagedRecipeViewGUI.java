package de.ancash.fancycrafting.gui;

import java.util.List;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.gui.base.AbstractPagedRecipeViewGUI;
import de.ancash.fancycrafting.recipe.IRecipe;

public class PagedRecipeViewGUI extends AbstractPagedRecipeViewGUI {

	public PagedRecipeViewGUI(AbstractFancyCrafting pl, Player player, List<IRecipe> recipes) {
		super(pl, player, recipes);
	}

	@Override
	public void onRecipeClick(IRecipe recipe) {
		pl.viewRecipe(player, recipe);
	}
}
