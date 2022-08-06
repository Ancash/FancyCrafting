package de.ancash.fancycrafting.gui.normal;

import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.exception.InvalidRecipeException;
import de.ancash.fancycrafting.exception.RecipeDeleteException;
import de.ancash.fancycrafting.gui.base.AbstractEditRecipeGUI;
import de.ancash.fancycrafting.recipe.IRecipe;

public class EditNormalRecipeGUI extends AbstractEditRecipeGUI {

	public EditNormalRecipeGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getEditRecipeTitle());
	}

	public EditNormalRecipeGUI(AbstractFancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(pl, player, recipe);
	}

	@Override
	protected void onRecipeSave() {
		closeAll();
		try {
			plugin.getRecipeManager().saveRecipe(result, ingredients, shaped, recipeName, recipe.getUUID(), 8, 6);
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
	public boolean isRecipeValid() {
		return !plugin.getWorkspaceObjects().getManageRandomInvalidResultItem().isSimilar(result) && Arrays.asList(ingredients).stream().filter(i -> i != null).findAny().isPresent();
	}
}