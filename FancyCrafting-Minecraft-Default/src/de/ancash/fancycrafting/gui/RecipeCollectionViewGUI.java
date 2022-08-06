package de.ancash.fancycrafting.gui;

import java.util.List;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.gui.base.AbstractRecipeCollectionViewGUI;
import de.ancash.fancycrafting.gui.base.AbstractRecipeViewGUI;
import de.ancash.fancycrafting.gui.normal.ViewNormalRecipeGUI;
import de.ancash.fancycrafting.gui.random.ViewRandomRecipeGUI;
import de.ancash.fancycrafting.recipe.IRandomRecipe;
import de.ancash.fancycrafting.recipe.IRecipe;

public class RecipeCollectionViewGUI extends AbstractRecipeCollectionViewGUI{

	public RecipeCollectionViewGUI(AbstractFancyCrafting pl, Player player, List<IRecipe> recipes) {
		super(pl, player, recipes);
	}

	@Override
	public AbstractRecipeViewGUI viewRecipe(IRecipe recipe) {
		return recipe instanceof IRandomRecipe ? new ViewRandomRecipeGUI(pl, player, recipe) : new ViewNormalRecipeGUI(pl, player, recipe);
	}	
}