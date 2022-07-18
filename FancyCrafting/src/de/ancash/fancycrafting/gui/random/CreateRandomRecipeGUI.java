package de.ancash.fancycrafting.gui.random;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRandomShapedRecipe;
import de.ancash.minecraft.inventory.input.StringInputGUI;

public class CreateRandomRecipeGUI extends EditRandomRecipeGUI {

	public CreateRandomRecipeGUI(FancyCrafting pl, Player player, String name) {
		super(pl, player,
				new IRandomShapedRecipe(new ItemStack[1], 1, 1, null, name, UUID.randomUUID(), new HashMap<>()),
				pl.getWorkspaceObjects().getCreateRecipeTitle());
	}

	public static void open(FancyCrafting plugin, Player owner) {
		new StringInputGUI(plugin, owner, str -> {
			if (str != null && !str.isEmpty()) {
				Bukkit.getScheduler().runTaskLater(plugin, () -> new CreateRandomRecipeGUI(plugin, owner, str).open(),
						1);
			} else {
				owner.sendMessage(plugin.getResponse().INVALID_RECIPE_NAME);
			}
		}).setLeft(plugin.getWorkspaceObjects().getInputRecipeNameLeftItem().getOriginal())
				.setRight(plugin.getWorkspaceObjects().getInputRecipeNameRightItem().getOriginal())
				.setTitle(plugin.getWorkspaceObjects().getCreateRecipeTitle()).setText("").open(); //$NON-NLS-1$
	}
}