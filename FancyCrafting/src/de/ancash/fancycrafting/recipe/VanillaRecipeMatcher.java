package de.ancash.fancycrafting.recipe;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.ancash.fancycrafting.FancyCrafting;
import de.ancash.minecraft.crafting.IContainerWorkbench;
import de.ancash.minecraft.crafting.ICraftingManager;

public class VanillaRecipeMatcher {

	private final IContainerWorkbench icw;
	private final FancyCrafting pl;

	public VanillaRecipeMatcher(FancyCrafting pl, Player player) {
		icw = ICraftingManager.getSingleton().newInstance(player);
		this.pl = pl;
	}

	public IContainerWorkbench getContainerWorkbench() {
		return icw;
	}

	public IRecipe matchVanillaRecipe(IMatrix<ItemStack> matrix) {
		matrix = matrix.clone();
		if (matrix.getArray().length != 9) {
			matrix.cut(3, 3);
			if (matrix.getArray().length != 9)
				return null;
		}

		for (int i = 0; i < 9; i++)
			icw.setItem(i, matrix.getArray()[i]);
		Recipe r = icw.getCurrentRecipe();
		for (int i = 0; i < 9; i++)
			icw.setItem(i, null);
		return IRecipe.fromVanillaRecipe(pl, r);
	}
}