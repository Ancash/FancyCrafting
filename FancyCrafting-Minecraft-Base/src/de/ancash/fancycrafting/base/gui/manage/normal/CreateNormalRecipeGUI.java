package de.ancash.fancycrafting.base.gui.manage.normal;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.ancash.fancycrafting.base.AbstractFancyCrafting;
import de.ancash.fancycrafting.recipe.IShapedRecipe;

public class CreateNormalRecipeGUI extends EditNormalRecipeGUI {

	public CreateNormalRecipeGUI(AbstractFancyCrafting pl, Player player, String name) {
		super(pl, player, new IShapedRecipe(new ItemStack[1], 1, 1, null, name, UUID.randomUUID()),
				pl.getWorkspaceObjects().getCreateRecipeTitle());
	}

	public CreateNormalRecipeGUI(AbstractFancyCrafting pl, Player player, String name, String title) {
		super(pl, player, new IShapedRecipe(new ItemStack[1], 1, 1, null, name, UUID.randomUUID()), title);
	}

	@Override
	protected void onRecipeDelete() {
		closeAll();
	}
}