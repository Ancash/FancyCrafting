package de.ancash.fancycrafting.gui;

import java.util.List;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRecipe;

public class RecipeCollectionPagedViewGUI extends AbstractRecipeCollectionPagedViewGUI {

	public RecipeCollectionPagedViewGUI(FancyCrafting pl, Player player, List<IRecipe> recipes) {
		super(pl, player, recipes);
	}

	@Override
	protected void onRecipeClick(IRecipe recipe) {
		pl.viewRecipe(player, recipe);
	}

}