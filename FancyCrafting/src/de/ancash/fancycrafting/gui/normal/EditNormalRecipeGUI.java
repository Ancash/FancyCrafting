package de.ancash.fancycrafting.gui.normal;

import java.io.IOException;
import java.util.Arrays;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.AbstractEditRecipeGUI;
import de.ancash.fancycrafting.recipe.IRecipe;

public class EditNormalRecipeGUI extends AbstractEditRecipeGUI {

	public EditNormalRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getWorkspaceObjects().getEditRecipeTitle());
	}

	public EditNormalRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(pl, player, recipe);
	}

	@Override
	protected void onRecipeSave() {
		closeAll();
		try {
			plugin.getRecipeManager().saveRecipe(result, ingredients, shaped, recipeName, recipe.getUUID(), 8, 6);
			player.sendMessage(plugin.getResponse().RECIPE_SAVED);
			plugin.getRecipeManager().reloadRecipes();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onRecipeDelete() {
		closeAll();
		try {
			plugin.getRecipeManager().delete(recipe.getUUID().toString());
			player.sendMessage(plugin.getResponse().RECIPE_DELETED);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
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