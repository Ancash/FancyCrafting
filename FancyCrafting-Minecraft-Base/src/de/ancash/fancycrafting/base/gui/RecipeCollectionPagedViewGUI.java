package de.ancash.fancycrafting.base.gui;

import java.util.List;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;

public class RecipeCollectionPagedViewGUI extends AbstractRecipeCollectionPagedViewGUI {

	public RecipeCollectionPagedViewGUI(AbstractFancyCrafting pl, Player player, List<IRecipe> recipes) {
		super(pl, player, recipes);
	}

	@Override
	protected void onRecipeClick(IRecipe recipe) {
		pl.viewRecipe(player, recipe);
	}

}