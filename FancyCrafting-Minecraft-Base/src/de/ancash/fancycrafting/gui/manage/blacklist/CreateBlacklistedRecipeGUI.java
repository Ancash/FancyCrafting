package de.ancash.fancycrafting.gui.manage.blacklist;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.ancash.datastructures.tuples.Tuple;
import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.gui.manage.normal.CreateNormalRecipeGUI;
import de.ancash.fancycrafting.recipe.IShapedRecipe;
import de.ancash.fancycrafting.recipe.IShapelessRecipe;
import de.ancash.minecraft.inventory.input.StringInputGUI;

public class CreateBlacklistedRecipeGUI extends CreateNormalRecipeGUI {

	public static void open(FancyCrafting plugin, Player owner) {
		new StringInputGUI(plugin, owner,
				(text) -> Bukkit.getScheduler().runTaskLater(plugin,
						() -> new CreateBlacklistedRecipeGUI(plugin, owner, text).open(), 1),
				(text) -> Tuple.of(text != null && !text.isEmpty(),
						text == null || text.isEmpty() ? plugin.getResponse().INVALID_RECIPE_NAME : null))
				.setLeft(plugin.getWorkspaceObjects().getInputRecipeNameLeftItem().getOriginal())
				.setRight(plugin.getWorkspaceObjects().getInputRecipeNameRightItem().getOriginal())
				.setTitle(plugin.getWorkspaceObjects().getInputRecipeNameTitle()).setText("").open(); //$NON-NLS-1$
	}

	private final FancyCrafting pl;

	public CreateBlacklistedRecipeGUI(FancyCrafting pl, Player player, String name) {
		super(pl, player, name, pl.getAddRecipeToBlacklistTitle());
		this.pl = pl;
	}

	@Override
	protected void onRecipeDelete() {
		closeAll();
	}

	@SuppressWarnings("nls")
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