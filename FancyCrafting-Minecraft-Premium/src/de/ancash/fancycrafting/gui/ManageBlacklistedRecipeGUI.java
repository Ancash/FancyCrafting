package de.ancash.fancycrafting.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.base.gui.manage.normal.EditNormalRecipeGUI;
import de.ancash.fancycrafting.recipe.IRecipe;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;

@SuppressWarnings("nls")
public class ManageBlacklistedRecipeGUI extends EditNormalRecipeGUI {

	private final FancyCrafting pl;

	public ManageBlacklistedRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe) {
		this(pl, player, recipe, pl.getManageBlacklistTitle());
	}

	public ManageBlacklistedRecipeGUI(FancyCrafting pl, Player player, IRecipe recipe, String title) {
		super(pl, player, recipe, title);
		this.pl = pl;
	}

	@Override
	protected void onRecipeDelete() {
		closeAll();
		try {
			pl.removeBlacklistedRecipe(recipe);
			player.sendMessage("§aDeleted blacklisted recipe!");
		} catch (IOException e) {
			player.sendMessage("§cCould not delete blacklisted recipe!");
			pl.getLogger().log(Level.SEVERE, "Could not delete blacklisted recipe: " + recipe, e);
		}
	}

	@Override
	protected void onRecipeSave() {
		closeAll();
		try {
			pl.addRecipeToBlacklist(shaped ? new IShapedRecipe(ingredients, 8, 6, result, recipeName, recipe.getUUID())
					: new IShapelessRecipe(Arrays.asList(ingredients), result, recipeName, recipe.getUUID()));
			player.sendMessage("§aSaved blacklisted recipe!");
		} catch (IOException e) {
			player.sendMessage("§cCould not save blacklisted recipe!");
			pl.getLogger().log(Level.SEVERE, "Could not save blacklisted recipe: " + recipe, e);
		}
	}
}