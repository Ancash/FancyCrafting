package de.ancash.fancycrafting.gui.manage.random;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.fancycrafting.recipe.IRandomShapedRecipe;

public class CreateRandomRecipeGUI extends EditRandomRecipeGUI {

	public CreateRandomRecipeGUI(FancyCrafting pl, Player player, String name) {
		super(pl, player,
				new IRandomShapedRecipe(new ItemStack[1], 1, 1, null, name, UUID.randomUUID(), new HashMap<>(), null),
				pl.getWorkspaceObjects().getCreateRecipeTitle());
	}

	@Override
	protected void onRecipeDelete() {
		closeAll();
	}
}