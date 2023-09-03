package de.ancash.fancycrafting.gui.manage.normal;

import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.exception.InvalidRecipeException;
import de.ancash.fancycrafting.exception.RecipeDeleteException;
import de.ancash.fancycrafting.gui.manage.AbstractRecipeEditGUI;
import de.ancash.fancycrafting.recipe.IRecipe;

public class EditNormalRecipeGUI extends AbstractRecipeEditGUI {

	public EditNormalRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getEditRecipeTitle());
	}

	public EditNormalRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(pl, player, recipe, title);
	}

	@Override
	protected void onRecipeSave() {
		closeAll();
		try {
			plugin.getRecipeManager().saveRecipe(result, ingredients, shaped, recipeName, recipe.getUUID(), 8, 6,
					category);
			player.sendMessage(plugin.getResponse().RECIPE_SAVED);
			plugin.getRecipeManager().reloadRecipes();
		} catch (InvalidRecipeException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("nls")
	@Override
	protected void onRecipeDelete() {
		closeAll();
		try {
			plugin.getRecipeManager().delete(recipe.getUUID().toString());
			player.sendMessage(plugin.getResponse().RECIPE_DELETED);
		} catch (RecipeDeleteException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not delete recipe: " + recipe, e);
		}
	}

	@Override
	protected void onMainMenuOpen() {

	}

	@Override
	protected boolean isRecipeValid() {
		return !plugin.getWorkspaceObjects().getManageRandomInvalidResultItem().isSimilar(result)
				&& Arrays.asList(ingredients).stream().filter(i -> i != null).findAny().isPresent();
	}
}