package de.ancash.fancycrafting.recipe;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import de.ancash.minecraft.crafting.IContainerWorkbench;
import de.ancash.minecraft.crafting.ICraftingManager;

public class VanillaRecipeMatcher {
	
	private final IContainerWorkbench icw;
	
	public VanillaRecipeMatcher(Player player) {
		icw = ICraftingManager.getSingleton().newInstance(player);
	}
	
	public IRecipe matchVanillaRecipe(ItemStack[] items) {
		if(items.length > 9) {
			IMatrix.optimize(items);
			items = IMatrix.cutMatrix(items, 3);
			if(items.length != 9) return null;
		}
		
		while(items.length < 9) {
			int om = (int) Math.sqrt(items.length);
			int nm = om + 1;
			ItemStack[] temp = new ItemStack[nm * nm];
			for(int c = 0; c<om; c++) 
				for(int r = 0; r<om; r++)
					temp[c * nm + r]  = items[c * om + r];
			items = temp;
		}
		
		for(int i = 0; i<9; i++)
			icw.setItem(i, items[i]);
		Recipe r = icw.getCurrentRecipe();
		for(int i = 0; i<9; i++)
			icw.setItem(i, null);
		return IRecipe.fromVanillaRecipe(r);
	}
}